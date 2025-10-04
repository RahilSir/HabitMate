package com.example.habittracker.network

import com.example.habittracker.models.Habit
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {

    // 1. READ (GET) - Fetches all habits for a specific user.
    // The path is just "habits" because the base URL in RetrofitClient already contains the domain.
    // @Query("userId") adds "?userId={value}" to the URL for filtering.
    @GET("habits")
    fun getHabits(@Query("userId") userId: String): Call<List<Habit>>

    // 2. CREATE (POST) - Saves a new habit.
    // The Habit object in the @Body MUST now contain the 'userId' field.
    @POST("habits")
    fun addHabit(
        @Body habit: Habit
    ): Call<Habit>

    // 3. DELETE - Removes a habit by its ID.
    @DELETE("habits/{id}")
    fun deleteHabit(@Path("id") habitId: String): Call<Unit>


    // NEW: Update habit (just the isChecked field)
    @PATCH("habits/{id}")
    fun updateHabitCheckStatus(
        @Path("id") habitId: String,
        @Body status: Map<String, Boolean>
    ): Call<Habit>


}
