package com.example.habittracker.network

import retrofit2.Call
import retrofit2.http.GET

// Model matches JSON from ZenQuotes
data class Quote(
    val q: String,  // quote text
    val a: String   // author
)

interface QuoteApi {
    @GET("random")
    fun getRandomQuote(): Call<List<Quote>>
}
