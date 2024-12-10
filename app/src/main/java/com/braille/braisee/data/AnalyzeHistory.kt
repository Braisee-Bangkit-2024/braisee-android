@file:Suppress("DEPRECATED_ANNOTATION")

package com.braille.braisee.data

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize

@Entity(tableName = "analyze_history")
@Parcelize
data class AnalyzeHistory(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val imageUri: String,
    val result: String,
    var favorite: Boolean
): Parcelable
