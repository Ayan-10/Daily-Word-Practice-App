package com.example.dailywordpractice.Data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "word_table")
data class WordItem(@PrimaryKey @ColumnInfo(name = "word") val word: String, val definition: String)
