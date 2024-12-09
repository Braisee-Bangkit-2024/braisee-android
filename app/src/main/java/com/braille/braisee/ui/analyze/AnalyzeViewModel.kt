package com.braille.braisee.ui.analyze

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.braille.braisee.data.AnalyzeHistory
import com.braille.braisee.data.AnalyzeRepo
import kotlinx.coroutines.launch
import androidx.lifecycle.asLiveData


class AnalyzeViewModel(private val repository: AnalyzeRepo) : ViewModel() {

    private val _analyzeHistory = MutableLiveData<AnalyzeHistory>()
    val analyzeHistory: LiveData<AnalyzeHistory> = _analyzeHistory

    fun getHistoryById(id: Int) = repository.getHistoryById(id)

    fun saveAnalyze(analyze: AnalyzeHistory) {
        viewModelScope.launch {
            repository.insertHistory(analyze)
        }
    }


}