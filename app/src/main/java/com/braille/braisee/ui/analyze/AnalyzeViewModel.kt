package com.braille.braisee.ui.analyze

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.braille.braisee.data.AnalyzeHistory
import com.braille.braisee.data.AnalyzeRepo
import kotlinx.coroutines.launch

class AnalyzeViewModel(private val repository: AnalyzeRepo) : ViewModel() {
    fun saveAnalyze(analyze: AnalyzeHistory) {
        viewModelScope.launch {
            repository.insertHistory(analyze)
        }
    }
}