package com.braille.braisee.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.braille.braisee.data.AnalyzeHistory
import com.braille.braisee.data.AnalyzeRepo
import kotlinx.coroutines.launch

class HomeViewModel(private val repository: AnalyzeRepo) : ViewModel() {

    private val _isBookmarked = MutableLiveData<Boolean>()
    val isBookmarked: LiveData<Boolean> = _isBookmarked

    val allHistory: LiveData<List<AnalyzeHistory>> = repository.getAllHistory()

    fun updateHistory(history: AnalyzeHistory) {
        viewModelScope.launch {
            repository.updateHistory(history)
        }
    }

}