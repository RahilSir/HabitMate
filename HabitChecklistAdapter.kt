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
import com.example.habittracker.repository.HabitRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HabitChecklistAdapter(
    context: Context,
    private val habits: MutableList<Habit>  // ✅ Keep reference to mutable list
) : ArrayAdapter<Habit>(context, 0, habits) {

    private val repository = HabitRepository(context)

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.list_item_habit, parent, false)

        val habit = getItem(position) ?: return view

        val habitNameTextView = view.findViewById<TextView>(R.id.habitNameTextView)
        val habitCheckBox = view.findViewById<CheckBox>(R.id.habitCheckBox)

        val durationStr = habit.duration?.toString() ?: "N/A"
        val daysStr = habit.days?.joinToString(", ") ?: "No days set"
        val habitText = "${habit.habitName} ($durationStr days) - ${habit.reminderTime}"
        val streakTextView = view.findViewById<TextView>(R.id.streakTextView)

        habitNameTextView.text = habitText


        if (habit.currentStreak > 0) {
            streakTextView.text = "🔥 ${habit.currentStreak} day streak!"
            streakTextView.visibility = View.VISIBLE
        } else {
            streakTextView.visibility = View.GONE
        }



        // ✅ CRITICAL: Remove listener before setting checked state
        habitCheckBox.setOnCheckedChangeListener(null)
        habitCheckBox.isChecked = habit.isChecked

        // ✅ Set listener after initial state is set
        habitCheckBox.setOnCheckedChangeListener { _, isChecked ->
            // Update in-memory immediately for smooth UI
            habit.isChecked = isChecked

            // Save to database in background
            val habitId = habit.id
            if (!habitId.isNullOrEmpty()) {
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        repository.updateHabitCheckedStatus(habitId, isChecked)

                        // Optional: Show success message on main thread
                        withContext(Dispatchers.Main) {
                            val status = if (isChecked) "completed" else "unchecked"
                            Toast.makeText(
                                context,
                                "'${habit.habitName}' $status",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } catch (e: Exception) {
                        withContext(Dispatchers.Main) {
                            // Revert checkbox on error
                            habit.isChecked = !isChecked
                            habitCheckBox.isChecked = !isChecked

                            Toast.makeText(
                                context,
                                "Error: ${e.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            }
        }

        return view
    }
}