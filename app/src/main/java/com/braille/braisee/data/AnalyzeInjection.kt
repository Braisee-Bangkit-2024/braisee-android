package com.braille.braisee.data

import android.content.Context

class AnalyzeInjection {

}
object Injection {
    fun provideRepository(context: Context): AnalyzeRepo {
        val database: AppDatabase = AppDatabase.getDatabase(context)
        val dao = database.analyzeHistoryDao()
        return AnalyzeRepo.getInstance(dao)
    }
}
