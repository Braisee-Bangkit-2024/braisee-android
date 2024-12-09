package com.braille.braisee.response

import com.google.gson.annotations.SerializedName

data class ApiResponse(

    @field:SerializedName("predicted_text")
    val character: List<String>
)