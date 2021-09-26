package com.example.dailywordpractice

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.dailywordpractice.Data.WordItem
import com.example.dailywordpractice.Data.WordItemRepository
import kotlinx.coroutines.launch

/**
 * View Model to keep a reference to the word itemRepository and
 * an up-to-date list of all words.
 */

class WordViewModel(private val itemRepository: WordItemRepository) : ViewModel() {

    // Using LiveData and caching what allWords returns has several benefits:
    // - We can put an observer on the data (instead of polling for changes) and only update the
    //   the UI when the data actually changes.
    // - Repository is completely separated from the UI through the ViewModel.
    val allWords: LiveData<List<WordItem>> = itemRepository.allWords.asLiveData()

    /**
     * Launching a new coroutine to insert the data in a non-blocking way
     */
    fun insert(wordItem: WordItem) = viewModelScope.launch {
        itemRepository.insert(wordItem)
    }

    fun delete(wordItem: WordItem) = viewModelScope.launch {
        itemRepository.delete(wordItem)
    }
}

class WordViewModelFactory(private val itemRepository: WordItemRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WordViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return WordViewModel(itemRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
