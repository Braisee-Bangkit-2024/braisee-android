package com.braille.braisee.data

import androidx.lifecycle.LiveData

class AnalyzeRepo(private val analyzeHistoryDao: AnalyzeHistoryDao) {

    suspend fun insertHistory(history: AnalyzeHistory) {
        analyzeHistoryDao.insertHistory(history)
    }

    suspend fun deleteHistory(history: AnalyzeHistory) {
        analyzeHistoryDao.deleteHistory(history)
    }

    suspend fun updateHistory(history: AnalyzeHistory) {
        analyzeHistoryDao.updateHistory(history)
    }

    fun getAllHistory(): LiveData<List<AnalyzeHistory>> {
        return analyzeHistoryDao.getAllHistory()
    }

    fun getFavoriteHistory(): LiveData<List<AnalyzeHistory>>{
        return analyzeHistoryDao.getFavoriteHistory()
    }

    fun getHistoryById(id: Int) = analyzeHistoryDao.getHistoryById(id)




    fun checkIsFavorite(id: String){
        analyzeHistoryDao.checkIsFavorite(id)
    }

    companion object {
        @Volatile
        private var instance: AnalyzeRepo? = null

        fun getInstance(
            analyzeDao: AnalyzeHistoryDao
        ): AnalyzeRepo =
            instance ?: synchronized(this) {
                instance ?: AnalyzeRepo(analyzeDao)
            }.also { instance = it }
    }
}