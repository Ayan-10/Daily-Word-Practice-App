package com.example.dailywordpractice

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.speech.tts.TextToSpeech
import android.util.Log
import io.paperdb.Paper
import java.util.*

class TTSBroadcastReceiver: BroadcastReceiver() {
    lateinit var textToSpeech: TextToSpeech
    var BROADCAST_ACTION = "com.unitedcoders.android.broadcasttest.SHOWTOAST"

    override fun onReceive(context: Context?, intent: Intent?) {
        Paper.init(context)

        val word = intent!!.getStringExtra("goat")
        val definition = intent.getStringExtra("messi")
        val id = intent.getIntExtra("ID",0)

        Log.d("htht", "onReceive: ")
        textToSpeech = TextToSpeech(context, TextToSpeech.OnInitListener {

            // if No error is found then only it will run
            if (it != TextToSpeech.ERROR) {
                // To Choose language of speech
                textToSpeech.setLanguage(Locale.UK)
                var pitch = (Paper.book().read(Constants.PITCH, 50) / 50).toFloat()
//                    var index = Paper.book().read(WORD_INDEX, 0)
                if (pitch < 0.01f) {
                    pitch = 0.01f
                }
                var volume = (Paper.book().read(Constants.VOLUME, 50) / 50).toFloat()
                if (volume < 0.01f) {
                    volume = 0.01f
                }
                textToSpeech.setPitch(pitch)
                textToSpeech.setSpeechRate(volume)

                Log.d("hoho", "recive: " + word)
                textToSpeech.speak(
                    "Word",
                    TextToSpeech.QUEUE_ADD,
                    null
                )
                textToSpeech.speak(
                    word,
                    TextToSpeech.QUEUE_ADD,
                    null
                )
                textToSpeech.speak(
                    "Definition",
                    TextToSpeech.QUEUE_ADD,
                    null
                )
                textToSpeech.speak(
                    definition,
                    TextToSpeech.QUEUE_ADD,
                    null
                )
                }

            })

    }
}