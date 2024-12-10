package com.braille.braisee.factory

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.braille.braisee.data.AnalyzeRepo
import com.braille.braisee.data.AppDatabase
import com.braille.braisee.data.Injection
import com.braille.braisee.ui.analyze.AnalyzeViewModel
import com.braille.braisee.ui.favorite.FavoriteViewModel
import com.braille.braisee.ui.home.HomeViewModel

class ViewModelFactory(private val repository: AnalyzeRepo) : ViewModelProvider.Factory {


    companion object {
        @Volatile
        private var instance: ViewModelFactory? = null

        fun getInstance(context: Context): ViewModelFactory {
            return instance ?: synchronized(this) {
                instance ?: ViewModelFactory(
                    AnalyzeRepo(AppDatabase.getDatabase(context).analyzeHistoryDao())
                )
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {

            modelClass.isAssignableFrom(HomeViewModel::class.java) -> {
                HomeViewModel(repository) as T
            }

            modelClass.isAssignableFrom(FavoriteViewModel::class.java) -> {
                FavoriteViewModel(repository) as T
            }

            modelClass.isAssignableFrom(AnalyzeViewModel::class.java) -> {
                AnalyzeViewModel(repository) as T
            }

            else -> throw IllegalArgumentException("Unknown ViewModel class" + modelClass.name)
        }
    }
}
