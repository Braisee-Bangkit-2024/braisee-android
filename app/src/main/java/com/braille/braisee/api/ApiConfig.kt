package com.braille.braisee.api

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {
    private const val BASE_URL = "https://api-machine-learning-609610394608.asia-southeast2.run.app/"

    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS) // Waktu tunggu untuk koneksi
            .readTimeout(30, TimeUnit.SECONDS)    // Waktu tunggu untuk membaca respon
            .writeTimeout(30, TimeUnit.SECONDS)   // Waktu tunggu untuk menulis permintaan
            .build()
    }

    val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient) // Gunakan OkHttpClient yang sudah dikonfigurasi
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}
