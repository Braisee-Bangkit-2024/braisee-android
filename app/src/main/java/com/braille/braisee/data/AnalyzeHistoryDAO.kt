package com.braille.braisee.data

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface AnalyzeHistoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(history: AnalyzeHistory)

    @Update
    suspend fun updateHistory(history: AnalyzeHistory)

    @Delete
    suspend fun deleteHistory(history: AnalyzeHistory)

    @Query("SELECT * FROM analyze_history ORDER BY id DESC")
    fun getAllHistory(): LiveData<List<AnalyzeHistory>>

    @Query("SELECT * FROM analyze_history WHERE favorite = 1")
    fun getFavoriteHistory(): LiveData<List<AnalyzeHistory>>

    @Query("DELETE FROM analyze_history WHERE favorite = 1")
    suspend fun deleteFavorites()


    @Query("DELETE FROM analyze_history")
    suspend fun deleteAllHistory()

    @Query("SELECT EXISTS(SELECT * FROM analyze_history WHERE id = :id)")
    fun checkIsFavorite(id: String): LiveData<Boolean>

}
