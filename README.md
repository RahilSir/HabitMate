HabitMate Habit Tracker App
📱 Overview
The Habit Tracker App is a mobile Android application designed to help users build and maintain positive daily habits. Users can create, track, and manage habits, set daily reminders, and monitor their progress over time. The app provides an easy-to-use interface for adding habits, selecting active days, setting reminder times, and saving all habit data securely in Firebase Realtime Database.
⚙️ Key Features
• Add New Habits – Users can input a habit name, duration, active days, and reminder time.
• Reminders – Built-in time picker lets users schedule daily reminders for their habits.
• Firebase Integration – All habit data is stored in Firebase Realtime Database for persistence and real-time syncing.
• User-Friendly Interface – Clean and simple UI built with XML layouts and Kotlin logic.
• Local Validation – Prevents empty or invalid habit entries before submission.
🧩 Tech Stack
Component	Description
Language	Kotlin
IDE	Android Studio
Database	Firebase Realtime Database
UI	XML Layouts
Architecture	Activity-based (AddHabitActivity for creation)
Version Control	GitHub
🗂️ Project Structure
HabitTracker/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/example/habittracker/
│   │   │   │   ├── AddHabitActivity.kt        # Main activity for adding habits
│   │   │   │   └── models/
│   │   │   │       └── Habit.kt               # Data model class for a habit
│   │   │   ├── res/layout/
│   │   │   │   └── activity_add_habit.xml     # XML layout for habit creation UI
│   │   │   ├── AndroidManifest.xml
│   │   └── ...
└── build.gradle
🧠 Core Classes
AddHabitActivity.kt – Handles user interactions for adding new habits and saving them to Firebase.
Habit.kt – Represents the structure of a habit object stored in Firebase.
Example Habit data model:

data class Habit(
    val id: String = "",
    val habitName: String = "",
    val duration: Int = 0,
    val days: List<String> = emptyList(),
    val reminderTime: String = ""
)
🔥 Firebase Setup
• Go to Firebase Console and create a new project.
• Add your Android app package name (e.g., com.example.habittracker).
• Download the google-services.json file and place it in the app/ folder.
• Add Firebase dependencies to your build.gradle files.
• Sync the project with Gradle files.
🧑‍💻 How to Run
• Clone the repository from GitHub.
• Open the project in Android Studio.
• Connect a device or start an Android emulator.
• Click Run ▶️ in Android Studio.
🧾 Example Use Case
1. Open the app and go to the Add Habit screen.
2. Enter your habit name (e.g., 'Drink Water').
3. Set the duration (e.g., 21 days).
4. Select the days you want reminders (e.g., Monday–Friday).
5. Choose a reminder time using the time picker.
6. Tap Save Habit.
7. The habit is now stored in Firebase and ready to be tracked!
🛠️ Troubleshooting
• Unresolved Firebase references – Ensure google-services.json is added and Gradle synced.
• Kotlin errors – Use File → Invalidate Caches / Restart.
• App crashes on save – Verify Firebase read/write permissions.
✨ Future Enhancements
• View and edit existing habits.
• Daily progress tracking dashboard.
• Push notifications for reminders.
• Dark mode support.
🧑‍🎓 Developer
Name: Rahil Sirkissoon
Project: Habit Tracker App
Platform: Android Studio (Kotlin + Firebase)
Year: 2025
