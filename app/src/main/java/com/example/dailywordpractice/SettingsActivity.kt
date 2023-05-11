package com.example.dailywordpractice

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.ListPreference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import androidx.preference.SeekBarPreference
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import com.example.dailywordpractice.Constants.FREQUENCY
import com.example.dailywordpractice.Constants.PITCH
import com.example.dailywordpractice.Constants.VOLUME
import io.paperdb.Paper
import java.util.concurrent.TimeUnit

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)
        if (savedInstanceState == null) {
            supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.settings, SettingsFragment())
                    .commit()
        }
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    class SettingsFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)

            PreferenceManager.getDefaultSharedPreferences(requireContext())

            Paper.init(requireContext())
            val listPreference = findPreference<ListPreference>("frequency")
            listPreference?.setOnPreferenceChangeListener { preference, newValue ->
                val item: String = newValue as String

                if (preference.key.equals("frequency")){

                    when (item) {
                        "15" -> Paper.book().write(FREQUENCY, 15L)
                        "30" -> Paper.book().write(FREQUENCY, 30L)
                        "1" -> Paper.book().write(FREQUENCY, 1L)
                        "2" -> Paper.book().write(FREQUENCY, 2L)
                        "3" -> Paper.book().write(FREQUENCY, 3L)
                        "4" -> Paper.book().write(FREQUENCY, 4L)
                        "5" -> Paper.book().write(FREQUENCY, 5L)
                        "6" -> Paper.book().write(FREQUENCY, 6L)
                    }
                    Paper.book().write(Constants.FIRST_APPEARANCE_SWITCH, true)
                    WorkManager.getInstance().cancelAllWorkByTag("work")

                    val constraints = Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()

                    val frequency = Paper.book().read(FREQUENCY, 1L)
                    showDialog()
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
                true
            }

            val volumeSeekBarPreference = findPreference<SeekBarPreference>("volume")
            volumeSeekBarPreference?.setOnPreferenceChangeListener { _, newValue ->

                val progress = Integer.valueOf(newValue.toString())
                Paper.book().write(VOLUME, progress)

                true
            }


            val pitchSeekBarPreference = findPreference<SeekBarPreference>("pitch")
            pitchSeekBarPreference?.setOnPreferenceChangeListener { _, newValue ->

                val progress = Integer.valueOf(newValue.toString())
                Paper.book().write(PITCH, progress)
                true
            }

        }

        private fun showDialog(){
            val mDialog = AlertDialog.Builder(requireContext())
                .setTitle("Information")
                .setMessage("You will be getting voice notes in " +
                        "every ${Paper.book().read(FREQUENCY, 1L)} hours " +
                        "in purpose of practicing new words and definitions")
                .setCancelable(false)
                .setPositiveButton("OK") { dialog, which -> dialog.dismiss() }.show()
        }
    }
}