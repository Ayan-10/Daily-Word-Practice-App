package com.example.dailywordpractice.Data

import androidx.annotation.WorkerThread
import com.example.dailywordpractice.Data.WordItem
import com.example.dailywordpractice.Data.WordItemDao
import kotlinx.coroutines.flow.Flow

class WordItemRepository(private val wordItemDao: WordItemDao) {

    // Room executes all queries on a separate thread.
    // Observed Flow will notify the observer when the data has changed.
    val allWords: Flow<List<WordItem>> = wordItemDao.getAlphabetizedWords()

    // By default Room runs suspend queries off the main thread, therefore, we don't need to
    // implement anything else to ensure we're not doing long running database work
    // off the main thread.
    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun insert(wordItem: WordItem) {
        wordItemDao.insert(wordItem)
    }

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun insertAll(wordItems: List<WordItem>) {
        wordItemDao.insertAll(wordItems)
    }

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun delete(wordItem: WordItem) {
        wordItemDao.delete(wordItem)
    }
}
