package com.braille.braisee.api

import com.braille.braisee.response.ApiResponse
import okhttp3.MultipartBody
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface ApiService {
    @Multipart
    @POST("predict")
    suspend fun postImage(
        @Part file: MultipartBody.Part,
    ): ApiResponse
}