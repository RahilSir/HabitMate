package com.example.habittracker.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClientQuotes {
    private const val BASE_URL = "https://zenquotes.io/api/"

    val instance: QuoteApi by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        retrofit.create(QuoteApi::class.java)
    }
}
