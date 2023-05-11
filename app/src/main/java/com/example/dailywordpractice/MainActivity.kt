package com.example.dailywordpractice

import android.Manifest
import android.content.ContentResolver
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.util.Log
import android.view.View
import android.webkit.MimeTypeMap
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.observe
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import com.example.dailywordpractice.Constants.FIRST_APPEARANCE
import com.example.dailywordpractice.Constants.FIRST_APPEARANCE_SWITCH
import com.example.dailywordpractice.Constants.FREQUENCY
import com.example.dailywordpractice.Constants.IS_SWITCH
import com.example.dailywordpractice.Constants.PITCH
import com.example.dailywordpractice.Constants.VOLUME
import com.example.dailywordpractice.Constants.WORD_SPEAK_DATABASE
import com.example.dailywordpractice.Data.WordItem
import com.example.dailywordpractice.Data.WordItemApplication
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.switchmaterial.SwitchMaterial
import io.paperdb.Paper
import kotlinx.coroutines.launch
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.poifs.crypt.Decryptor
import org.apache.poi.poifs.crypt.EncryptionInfo
import org.apache.poi.poifs.filesystem.POIFSFileSystem
import org.apache.poi.ss.usermodel.*
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit


class MainActivity : AppCompatActivity(), WordItemAdapter {
    private lateinit var fab: FloatingActionButton
    private lateinit var fabAdd: FloatingActionButton
    private lateinit var fabXl: FloatingActionButton
    private val tag: String = "main"
    private val requestCode = 101
    private val PICK_FILE = 100
    private var clicked = false
    private var fileUri: Uri? = null

    private val newWordActivityRequestCode = 1
    private val wordViewModel: WordViewModel by viewModels {
        WordViewModelFactory((application as WordItemApplication).repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.activity_main)
        System.setProperty(
            "org.apache.poi.javax.xml.stream.XMLInputFactory",
            "com.fasterxml.aalto.stax.InputFactoryImpl"
        );
        System.setProperty(
            "org.apache.poi.javax.xml.stream.XMLOutputFactory",
            "com.fasterxml.aalto.stax.OutputFactoryImpl"
        );
        System.setProperty(
            "org.apache.poi.javax.xml.stream.XMLEventFactory",
            "com.fasterxml.aalto.stax.EventFactoryImpl"
        );

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerview)
        val adapter = WordItemListAdapter(this)
        recyclerView.adapter = adapter
        val linearLayoutManager = LinearLayoutManager(this)
        linearLayoutManager.reverseLayout = true
        linearLayoutManager.stackFromEnd = true
        recyclerView.layoutManager = linearLayoutManager
        val switch = findViewById<SwitchCompat>(R.id.switch1)
        val textView = findViewById<TextView>(R.id.textView)
        val noWordsTxt = findViewById<TextView>(R.id.no_words)
        Paper.init(this)
        Paper.book().read(FREQUENCY, 1L)
        Paper.book().read(VOLUME, 50)
        Paper.book().read(PITCH, 50)

        if (Paper.book().read(FIRST_APPEARANCE, true)) {
            switch.isChecked = true
            Paper.book().write(IS_SWITCH, true)
            Paper.book().read(FIRST_APPEARANCE_SWITCH, true)
            Paper.book().write(FIRST_APPEARANCE_SWITCH, true)
            WorkManager.getInstance().cancelAllWorkByTag("work")

            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val mDialog = AlertDialog.Builder(this)
                .setTitle("Information")
                .setMessage("You will be getting voice notes in " +
                        "every ${Paper.book().read(FREQUENCY, 1L)} hours " +
                        "in purpose of practicing new words and definitions. Please add words by clicking the + icon")
                .setCancelable(false)
                .setPositiveButton("OK") { dialog, which -> dialog.dismiss() }.show()

            val frequency = Paper.book().read(FREQUENCY, 1L)
            var request: PeriodicWorkRequest? = null
            if( frequency > 6L){
                request =
                    PeriodicWorkRequest.Builder(
                        TextToSpeech::class.java,
                        frequency,
                        TimeUnit.MINUTES
                    )
                        .addTag("work")
                        .build()
            }else {
                request =
                    PeriodicWorkRequest.Builder(
                        TextToSpeech::class.java,
                        frequency,
                        TimeUnit.HOURS
                    )
                        .addTag("work")
                        .build()
            }

            WorkManager.getInstance().enqueue(request)
        }

        switch.isChecked = Paper.book().read(IS_SWITCH, true)

        fab = findViewById<FloatingActionButton>(R.id.fab)
        fabAdd = findViewById<FloatingActionButton>(R.id.fab_add)
        fabXl = findViewById<FloatingActionButton>(R.id.fab_xl)

        fab.setOnClickListener {
            addButtonClicked()
        }

        fabAdd.setOnClickListener {
            val intent = Intent(this@MainActivity, AddWordActivity::class.java)
            startActivityForResult(intent, newWordActivityRequestCode)
        }

        fabXl.setOnClickListener {
            checkForStoragePermission()
        }

        val settings = findViewById<ImageView>(R.id.settings_icon)
        settings.setOnClickListener {
            val intent = Intent(this@MainActivity, SettingsActivity::class.java)
            startActivityForResult(intent, 2)
        }

        switch.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                Paper.book().write(IS_SWITCH, true)
                Paper.book().write(FIRST_APPEARANCE_SWITCH, true)
                WorkManager.getInstance().cancelAllWorkByTag("work")

                val constraints = Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()

                val frequency = Paper.book().read(FREQUENCY, 1L)
                var request: PeriodicWorkRequest? = null
                if( frequency > 6L){
                    request =
                        PeriodicWorkRequest.Builder(
                            TextToSpeech::class.java,
                            frequency,
                            TimeUnit.MINUTES
                        )
                            .addTag("work")
                            .build()
                }else {
                    request =
                        PeriodicWorkRequest.Builder(
                            TextToSpeech::class.java,
                            frequency,
                            TimeUnit.HOURS
                        )
                            .addTag("work")
                            .build()
                }

                WorkManager.getInstance().enqueue(request!!)

                textView.text = "Don't forget to turn it off before sleeping"
            } else {
                // The toggle is disabled
                Paper.book().write(IS_SWITCH, false)
                Paper.book().write(FIRST_APPEARANCE_SWITCH, false)
                textView.text = "You must turn it on to get voice notes"
            }
        }

        // Add an observer on the LiveData returned by getAlphabetizedWords.
        // The onChanged() method fires when the observed data changes and the activity is
        // in the foreground.
        wordViewModel.allWords.observe(owner = this) { words ->
            // Update the cached copy of the words in the adapter.
            if(words.isNotEmpty()) {
                recyclerView.visibility = View.VISIBLE
                noWordsTxt.visibility = View.GONE
                adapter.submitList(words)
                Paper.book().write(WORD_SPEAK_DATABASE, words.reversed())
                val list: List<WordItem> = Paper.book().read(WORD_SPEAK_DATABASE)
                for (item in list) {
                    Log.d("hehe", item.word)
                }
            } else {
                recyclerView.visibility = View.GONE
                noWordsTxt.visibility = View.VISIBLE
            }
        }
    }

    private fun addButtonClicked() {
        if(!clicked){
            fabAdd.visibility = View.VISIBLE
            fabXl.visibility = View.VISIBLE
            fab.setImageDrawable(getResources().getDrawable(android.R.drawable.ic_menu_close_clear_cancel))
        } else {
            fabAdd.visibility = View.GONE
            fabXl.visibility = View.GONE
            fab.setImageDrawable(getResources().getDrawable(R.drawable.ic_add_black_24dp))
        }
        clicked = !clicked
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intentData: Intent?) {
        super.onActivityResult(requestCode, resultCode, intentData)

        if (requestCode == newWordActivityRequestCode && resultCode == RESULT_OK) {
            val word = intentData?.getStringExtra(AddWordActivity.EXTRA_REPLY)
            val definition = intentData?.getStringExtra(AddWordActivity.EXTRA_REPLY1)

            val wordItem = word?.let { definition?.let { it1 -> WordItem(it, it1) } }
            wordItem?.let { wordViewModel.insert(it) }
        } else if (requestCode == PICK_FILE && resultCode == RESULT_OK){
            var mimeTypeExtension: String? = ""
            intentData?.data?.also { uri ->
                Log.e(tag, "ApachPOI Selected file Uri : " + uri)
                mimeTypeExtension = uri.getExtention(this)
                Log.e(tag, "ApachPOI Selected file mimeTypeExtension : " + mimeTypeExtension)
                if (mimeTypeExtension != null && mimeTypeExtension?.isNotEmpty() == true) {

                    if (mimeTypeExtension?.contentEquals("xlsx") == true
                        || mimeTypeExtension?.contentEquals("xls") == true
                    ) {
                        Log.e(
                            tag,
                            "ApachPOI Selected file mimeTypeExtension valid : " + mimeTypeExtension
                        )
                    } else {
                        Toast.makeText(this, "invalid file selected", Toast.LENGTH_SHORT).show()
                        return
                    }
                }
                copyFileAndExtract(uri, mimeTypeExtension.orEmpty())
            }
        }
        else {
            Toast.makeText(
                applicationContext,
                R.string.empty_not_saved,
                Toast.LENGTH_LONG
            ).show()
        }
    }

    override fun onDeleteClicked(wordItem: WordItem) {
        val mDialog = AlertDialog.Builder(this)
            .setTitle("Delete")
            .setMessage("Are you sure! You want to delete this word?")
            .setCancelable(false)
            .setPositiveButton("Delete") { dialog: DialogInterface, which: Int ->
                wordViewModel.delete(wordItem)
                dialog.dismiss()
            }
            .setNegativeButton(
                "Cancel"
            ) { dialog, which -> dialog.dismiss() }.show()
    }

    private fun showDialog(){
        val mDialog = AlertDialog.Builder(this)
            .setTitle("Information")
            .setMessage("You will be getting voice notes in " +
                    "every ${Paper.book().read(FREQUENCY, 1L)} hours " +
                    "in purpose of practicing new words and definitions")
            .setCancelable(false)
            .setPositiveButton("OK") { dialog, which -> dialog.dismiss() }.show()
    }

    private fun copyFileAndExtract(uri: Uri, extension: String) {
        val dir = File(this.filesDir, "doc")
        dir.mkdirs()
        val fileName = getFileName(uri)
        var file = File(dir, fileName)
        file?.createNewFile()
        val fout = FileOutputStream(file)
        try {
            contentResolver.openInputStream(uri)?.use { inputStream ->
                fout.use { output ->
                    inputStream.copyTo(output)
                    output.flush()
                }
            }
            fileUri = FileProvider.getUriForFile(this, packageName + ".provider", file!!)
        } catch (e: Exception) {
            fileUri = uri
            e.printStackTrace()
        }
        fileUri?.apply {
            file.apply {
                Log.e(tag, "Document not encrypted")
                readExcelFileFromAssets(this.absolutePath)

            }
        }
    }

    fun readExcelFileFromAssets(filePath: String, password: String = "") {
        var file = File(filePath)
        try {
                val myInput = FileInputStream(file)
                val firstRow: MutableList<String> = arrayListOf()

            var workbook: Workbook? = null

                if (password.isNotEmpty()) {
                    workbook = WorkbookFactory.create(file, password)
                    val posFile = POIFSFileSystem(file, true)
                    if (file.name.endsWith("xlsx")) {
                        val info = EncryptionInfo(posFile)
                        val d = Decryptor.getInstance((info))

                        workbook = XSSFWorkbook(d.getDataStream(posFile))
                    } else {
                        org.apache.poi.hssf.record.crypto.Biff8EncryptionKey.setCurrentUserPassword(
                            password
                        )
                        workbook = HSSFWorkbook(posFile.root, true)
                    }
                } else {
                    if (file.name.endsWith("xlsx")) {
                        workbook = XSSFWorkbook(myInput)
                    } else {
                        workbook = HSSFWorkbook(myInput)
                    }
                }
                workbook = addColumnIfNotAdded(workbook)
                val mySheet = workbook.getSheetAt(0)
                val rowIter: Iterator<Row> = mySheet.iterator()
                while (rowIter.hasNext()) {
                    val row: Row = rowIter.next()
                    val cellIter1: Iterator<Cell> = row.cellIterator()
                    if (row.rowNum == 0) {
                        while (cellIter1.hasNext()) {
                            val firstCell: Cell = cellIter1.next()
                            firstRow.add(firstCell.toString())
                        }
                    }
                    val cellIter: Iterator<Cell> = row.cellIterator()
//                    val singleRowList: MutableList<SingleRow> = arrayListOf()
                    if (row.rowNum >= 0) {
                        while (cellIter.hasNext()) {
                            for (i in firstRow) {
                                if (cellIter.hasNext()) {
                                    val cell1: Cell = cellIter.next()
                                    val cell2: Cell = cellIter.next()
                                    Log.d(tag, "readExcelFileFromAssets:  $cell1 $cell2")
                                    val wordItemExcel = WordItem(cell1.toString(),cell2.toString())
                                    wordItemExcel.let { wordViewModel.insert(it) }
//                                    singleRowList.add(SingleRow(i.toString(), cell.toString()))
                                }
                            }
                        }
//                        if (singleRowList.isEmpty() == false) {
//                            try {
//                                if (singleRowList.get(singleRowList.size - 1).value?.isNotEmpty() == true) {
//                                    if (singleRowList.get(singleRowList.size - 1).value?.equals(
//                                            AppConstant.Completed
//                                        ) == false
//                                    ) {
//                                        singleRowList.get(singleRowList.size - 1).value =
//                                            AppConstant.Pending
//                                    }
//                                }
//                            } catch (e: Exception) {
//                                e.printStackTrace()
//                            }
//                        }
                    }
                }


        } catch (e: Exception) {
            e.printStackTrace()
//            excelExceptionListData.postValue(e.message.orEmpty())
        }
    }


    private fun checkForStoragePermission() {
        if (ContextCompat.checkSelfPermission(
                this@MainActivity,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_DENIED
        ) {
            ActivityCompat.requestPermissions(
                this@MainActivity,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                requestCode
            )
            openDocument()
        } else {
            openDocument()
        }
    }

    private fun openDocument() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            type = "*/*"
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
            addCategory(Intent.CATEGORY_OPENABLE)
        }
        startActivityForResult(intent, PICK_FILE)
    }

    fun Uri.getExtention(context: Context): String? {
        var extension: String? = ""
        extension = if (this.scheme == ContentResolver.SCHEME_CONTENT) {
            val mime = MimeTypeMap.getSingleton()
            mime.getExtensionFromMimeType(context.getContentResolver().getType(this))
        } else {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                MimeTypeMap.getFileExtensionFromUrl(
                    FileProvider.getUriForFile(
                        context,
                        context.packageName + ".provider",
                        File(this.path)
                    )
                        .toString()
                )
            } else {
                MimeTypeMap.getFileExtensionFromUrl(Uri.fromFile(File(this.path)).toString())
            }
        }
        return extension
    }

    fun getFileName(uri: Uri): String? = when (uri.scheme) {
        ContentResolver.SCHEME_CONTENT -> getContentFileName(uri)
        else -> uri.path?.let(::File)?.name
    }

    private fun getContentFileName(uri: Uri): String? = runCatching {
        contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            cursor.moveToFirst()
            return@use cursor.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME)
                .let(cursor::getString)
        }
    }.getOrNull()

    private fun addColumnIfNotAdded(workBook: Workbook): Workbook {
        val sheet = workBook.getSheetAt(0)
        val rowIterator: Iterator<Row> = sheet.iterator()
        while (rowIterator.hasNext()) {
            val row: Row = rowIterator.next()
            Log.e("ApachPOI row count : ", row.count().toString())
            val cellIterator: Iterator<Cell> = row.cellIterator()
            while (cellIterator.hasNext()) {
                val column: Cell = cellIterator.next()
                if (!cellIterator.hasNext()) {
                    if (column.cellType == Cell.CELL_TYPE_STRING) {
                        if (column.stringCellValue.equals(AppConstant.Status) ||
                            column.stringCellValue.equals(AppConstant.Completed)
                        ) {
                            if (column.stringCellValue.equals(AppConstant.Completed)) {
                                val style = workBook.createCellStyle()
                                style.fillBackgroundColor = IndexedColors.YELLOW.index
                                style.setFillPattern(FillPatternType.SOLID_FOREGROUND)
                                row.rowStyle = style
                                Log.e("Already completed", "so setting green")
                            }
                        } else {
//                            val cell = row.createCell(row.lastCellNum + 1)
//                            cell.setCellValue(AppConstant.Status)
//                            Log.e("column.stringCellValue ", "else case")
                        }
                    } else {
//                        val cell = row.createCell(row.lastCellNum + 1)
//                        cell.setCellValue(AppConstant.Status)
//                        Log.e("column.cellType ", "else case")
                    }
                }
            }
        }
        return workBook
    }

}
