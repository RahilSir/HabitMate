package com.example.habittracker.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    // Placeholder URL since your primary persistence is Firebase, not a REST API
    private const val BASE_URL = "https://68d973cb90a75154f0da715c.mockapi.io/"

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    // This is the instance that other classes (like MainActivity) will access
    val api: ApiService by lazy {
        retrofit.create(ApiService::class.java)
    }
}