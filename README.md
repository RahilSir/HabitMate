# 🧠 Habit Tracker App

A simple and effective Android app built in **Kotlin** using **Android Studio** that helps users build and maintain positive habits. The app allows users to add, track, and set reminders for their daily habits, storing the data securely in **Firebase Realtime Database**.

---

## 📱 Overview

The **Habit Tracker App** enables users to:
- Add new habits with a name, duration, and specific days of the week.
- Set reminder times using a **TimePicker**.
- Save all habits to **Firebase** for persistence.
- Display confirmation messages upon successful or failed habit creation.

The project follows a clean and modular structure, ensuring readability and scalability for future enhancements.

---

## ✨ Key Features

✅ Add a habit with:
- Habit name  
- Duration (number of days)  
- Selected weekdays (Monday–Sunday)  
- Reminder time  

✅ Firebase integration for data storage  
✅ Validation for input fields  
✅ Real-time confirmation using **Toast messages**  
✅ Modern Android UI with **XML layouts**

---

How to Run the App

Clone the repository:

git clone https://github.com/<RahilSir>/HabitMate.git


Open the project in Android Studio.

Connect an Android device or start an emulator.

Click Run ▶️ to build and launch the app.



- `AddHabitActivity.kt`: Handles the logic for creating and saving habits.  
- `Habit.kt`: Data model defining a habit’s attributes.  
- `activity_add_habit.xml`: The layout used for adding new habits.  

---

## 🔧 Technologies Used

| Component | Description |
|------------|-------------|
| **Language** | Kotlin |
| **IDE** | Android Studio |
| **Database** | Firebase Realtime Database |
| **UI Design** | XML |
| **Build Tool** | Gradle |

---

## 🔥 Firebase Setup

1. Go to [Firebase Console](https://console.firebase.google.com/).  
2. Create a new project and connect it to your Android app.  
3. Add the `google-services.json` file to:  

4. In your app’s `build.gradle`, make sure these dependencies are added:
```gradle
implementation 'com.google.firebase:firebase-database:20.3.0'
implementation 'com.google.firebase:firebase-analytics:21.5.0'




