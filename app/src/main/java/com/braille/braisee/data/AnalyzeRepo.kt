package com.braille.braisee.data

import androidx.lifecycle.LiveData

class AnalyzeRepo (private val analyzeHistoryDao: AnalyzeHistoryDao) {

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

}