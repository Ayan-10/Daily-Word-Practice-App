package com.example.dailywordpractice

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.TextToSpeech.OnInitListener
import android.util.Log
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
                        textToSpeech.speak(
                            "Your Daily Word Practice is starting",
                            TextToSpeech.QUEUE_FLUSH,
                            null
                        )
                        for (item in list) {
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
                        }
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
}