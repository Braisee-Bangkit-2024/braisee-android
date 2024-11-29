package com.braille.braisee.data.learn

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Module(
    val title: String,
    val description: String,
    val ytLink: String
):Parcelable
