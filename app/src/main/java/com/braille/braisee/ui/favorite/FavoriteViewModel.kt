package com.braille.braisee.ui.favorite

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.braille.braisee.data.AnalyzeHistory
import com.braille.braisee.data.AnalyzeRepo
import kotlinx.coroutines.launch

class FavoriteViewModel(private val repository: AnalyzeRepo) : ViewModel() {

    fun getFavoriteHistory(): LiveData<List<AnalyzeHistory>> = repository.getFavoriteHistory()

    fun toggleBookmark(history: AnalyzeHistory){
        history.favorite = !history.favorite
        viewModelScope.launch {
            repository.updateHistory(history)
        }
    }

}