package com.example.habittracker

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.CheckBox
import android.widget.TextView
import android.widget.Toast
import com.example.habittracker.models.Habit
import com.example.habittracker.network.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HabitChecklistAdapter(context: Context, habits: List<Habit>) :
    ArrayAdapter<Habit>(context, 0, habits) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.list_item_habit, parent, false)

        val habit = getItem(position)
        val habitNameTextView = view.findViewById<TextView>(R.id.habitNameTextView)
        val habitCheckBox = view.findViewById<CheckBox>(R.id.habitCheckBox)

        if (habit != null) {
            val durationStr = habit.duration?.toString() ?: "N/A"
            val daysStr = habit.days?.joinToString(", ") ?: "No days set"
            val habitText = "${habit.habitName} ($durationStr days) - ${habit.reminderTime}"

            habitNameTextView.text = habitText

            // Prevent recycling issues
            habitCheckBox.setOnCheckedChangeListener(null)
            habitCheckBox.isChecked = habit.isChecked

            habitCheckBox.setOnCheckedChangeListener { _, isChecked ->
                habit.isChecked = isChecked

                // Show toast immediately
                val status = if (isChecked) "completed" else "not completed"
                Toast.makeText(
                    context,
                    "'${habit.habitName}' marked as $status for today.",
                    Toast.LENGTH_SHORT
                ).show()

                // --- API call to save isChecked ---
                val habitId = habit.id
                if (!habitId.isNullOrEmpty()) {
                    val statusUpdate = mapOf("isChecked" to isChecked)
                    RetrofitClient.api.updateHabitCheckStatus(habitId, statusUpdate)
                        .enqueue(object : Callback<Habit> {
                            override fun onResponse(call: Call<Habit>, response: Response<Habit>) {
                                if (!response.isSuccessful) {
                                    Toast.makeText(
                                        context,
                                        "Failed to update '${habit.habitName}' on server",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }

                            override fun onFailure(call: Call<Habit>, t: Throwable) {
                                Toast.makeText(
                                    context,
                                    "Network error: ${t.message}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        })
                }
            }
        }

        return view
    }
}
