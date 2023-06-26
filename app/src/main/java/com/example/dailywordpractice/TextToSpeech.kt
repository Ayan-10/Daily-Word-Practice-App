package com.example.dailywordpractice

//import com.example.dailywordpractice.Constants.WORD_INDEX
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.speech.tts.TextToSpeech
import android.speech.tts.TextToSpeech.OnInitListener
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.EXTRA_NOTIFICATION_ID
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.dailywordpractice.Constants.FIRST_APPEARANCE
import com.example.dailywordpractice.Constants.FIRST_APPEARANCE_SWITCH
import com.example.dailywordpractice.Constants.IS_SWITCH
import com.example.dailywordpractice.Constants.PITCH
import com.example.dailywordpractice.Constants.VOLUME
import com.example.dailywordpractice.Data.WordItem
import io.paperdb.Paper
import java.util.*

class TextToSpeech(context: Context, workerParams: WorkerParameters) :
    Worker(context, workerParams) {

    lateinit var textToSpeech: TextToSpeech
    var context: Context = context

    override fun doWork(): Result {
        Paper.init(context)

        if (Paper.book().read(IS_SWITCH, true) && !(Paper.book().read(FIRST_APPEARANCE_SWITCH, true))) {
            Log.d("hehe", "item.word")
            val list: List<WordItem> = Paper.book().read(Constants.WORD_SPEAK_DATABASE, ArrayList())
            for (item in list) {
                Log.d("hehe", item.word)
            }
            textToSpeech = TextToSpeech(applicationContext, OnInitListener {

                // if No error is found then only it will run
                if (it != TextToSpeech.ERROR) {
                    // To Choose language of speech
                    textToSpeech.setLanguage(Locale.UK)
                    var pitch = (Paper.book().read(PITCH, 50) / 50).toFloat()
//                    var index = Paper.book().read(WORD_INDEX, 0)
                    if (pitch < 0.01f) {
                        pitch = 0.01f
                    }
                    var volume = (Paper.book().read(VOLUME, 50)/50).toFloat()
                    if (volume < 0.01f) {
                        volume = 0.01f
                    }
                    textToSpeech.setPitch(pitch)
                    textToSpeech.setSpeechRate(volume)
                    if (list.isNotEmpty()) {
//                        list.random()
//                        Log.d("hoho", "doWork: "+index)
//                        index++
//                        Paper.book().write(WORD_INDEX, index%list.size)
                        textToSpeech.speak(
                            "Your Daily Word Practice is starting",
                            TextToSpeech.QUEUE_FLUSH,
                            null
                        )
//                        for (item in list) {
                        val item = list.random()
                        Log.d("hoho", "doWork: "+item.word)
                            textToSpeech.speak(
                                "Word",
                                TextToSpeech.QUEUE_ADD,
                                null
                            )
                            textToSpeech.speak(
                                item.word,
                                TextToSpeech.QUEUE_ADD,
                                null
                            )
                            textToSpeech.speak(
                                "Definition",
                                TextToSpeech.QUEUE_ADD,
                                null
                            )
                            textToSpeech.speak(
                                item.definition,
                                TextToSpeech.QUEUE_ADD,
                                null
                            )
                        displayNotification(item)
                    }
                } else {
                    Log.d("hehe", "Error")
                }
            })
        } else {
            Paper.book().write(FIRST_APPEARANCE, false)
            Log.d("hehe", "Boom")
            Paper.book().write(FIRST_APPEARANCE_SWITCH, false)
        }
        return Result.success()
    }

    private fun displayNotification(item: WordItem) {
        val r = Random()
        val reqCode = r.nextInt()

        val snoozeIntent = Intent(context, TTSBroadcastReceiver::class.java).apply {
            putExtra("ID", reqCode)
            putExtra(EXTRA_NOTIFICATION_ID, 0)
            action = "com.unitedcoders.android.broadcasttest.SHOWTOAST"
            putExtra("goat", item.word)
            putExtra("messi", item.definition)
        }
        val snoozePendingIntent: PendingIntent =
            PendingIntent.getBroadcast(context, 0, snoozeIntent, Intent.FILL_IN_DATA)

        val CHANNEL_ID = "channel_name" // The id of the channel.

        val notificationBuilder: NotificationCompat.Builder =
            NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.baseline_settings)
                .setContentTitle(item.word)
                .setContentText(item.definition)
                .setAutoCancel(true)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .addAction(
                    R.drawable.baseline_settings, "Speak out loud",
                    snoozePendingIntent
                )


        val notificationManager: NotificationManager = context.getSystemService(
            NOTIFICATION_SERVICE
        ) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name: CharSequence = "Channel Name" // The user-visible name of the channel.
            val importance = NotificationManager.IMPORTANCE_HIGH
            val mChannel = NotificationChannel(CHANNEL_ID, name, importance)
            notificationManager.createNotificationChannel(mChannel)
        }
        notificationManager.notify(
            reqCode,
            notificationBuilder.build()
        ) // 0 is the request code, it should be unique id

        //                        }
    }
}