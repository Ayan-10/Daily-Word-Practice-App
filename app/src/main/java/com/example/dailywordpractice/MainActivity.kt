package com.example.dailywordpractice

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
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
import java.util.concurrent.TimeUnit


class MainActivity : AppCompatActivity(), WordItemAdapter {

    private val newWordActivityRequestCode = 1
    private val wordViewModel: WordViewModel by viewModels {
        WordViewModelFactory((application as WordItemApplication).repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.activity_main)

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
        Paper.book().read(FREQUENCY, 3L)
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

            val frequency = Paper.book().read(FREQUENCY, 3L)

            val mDialog = AlertDialog.Builder(this)
                .setTitle("Information")
                .setMessage("You will be getting voice notes in " +
                        "every ${Paper.book().read(FREQUENCY, 3L)} hours " +
                        "in purpose of practicing new words and definitions. Please add words by clicking the + icon")
                .setCancelable(false)
                .setPositiveButton("OK") { dialog, which -> dialog.dismiss() }.show()

            val request =
                PeriodicWorkRequest.Builder(TextToSpeech::class.java, frequency, TimeUnit.HOURS)
                    .addTag("work")
                    .build()

            WorkManager.getInstance().enqueue(request)
        }

        switch.isChecked = Paper.book().read(IS_SWITCH, true)

        val fab = findViewById<FloatingActionButton>(R.id.fab)
        fab.setOnClickListener {
            val intent = Intent(this@MainActivity, AddWordActivity::class.java)
            startActivityForResult(intent, newWordActivityRequestCode)
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

                val frequency = Paper.book().read(FREQUENCY, 3L)
                showDialog()
                val request =
                    PeriodicWorkRequest.Builder(TextToSpeech::class.java, frequency, TimeUnit.HOURS)
                        .addTag("work")
                        .build()

                WorkManager.getInstance().enqueue(request)

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

    override fun onActivityResult(requestCode: Int, resultCode: Int, intentData: Intent?) {
        super.onActivityResult(requestCode, resultCode, intentData)

        if (requestCode == newWordActivityRequestCode && resultCode == RESULT_OK) {
            val word = intentData?.getStringExtra(AddWordActivity.EXTRA_REPLY)
            val definition = intentData?.getStringExtra(AddWordActivity.EXTRA_REPLY1)

            val wordItem = word?.let { definition?.let { it1 -> WordItem(it, it1) } }
            wordItem?.let { wordViewModel.insert(it) }

        } else {
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
                    "every ${Paper.book().read(FREQUENCY, 3L)} hours " +
                    "in purpose of practicing new words and definitions")
            .setCancelable(false)
            .setPositiveButton("OK") { dialog, which -> dialog.dismiss() }.show()
    }
}
