package com.example.dailywordpractice.Data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface WordItemDao {

    // The flow always holds/caches latest version of data. Notifies its observers when the
    // data has changed.
    @Query("SELECT * FROM word_table")
    fun getAlphabetizedWords(): Flow<List<WordItem>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(wordItem: WordItem)

    @Delete
    suspend fun delete(wordItem: WordItem)

    @Query("DELETE FROM word_table")
    suspend fun deleteAll()
}
