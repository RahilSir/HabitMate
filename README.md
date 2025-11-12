# 🧠 HabitMate - Habit Tracker App

A comprehensive Android habit tracking application built with **Kotlin** that helps users build and maintain positive habits with offline support, multi-language capabilities, and seamless cloud synchronization.

---

## 📱 Overview

**HabitMate** is a feature-rich habit tracking app that combines modern Android development practices with a focus on user experience. The app works seamlessly both online and offline, ensuring users never lose their progress regardless of connectivity.

### Core Capabilities
- ✅ Create and track daily habits with customizable schedules
- ✅ Set personalized reminders for each habit
- ✅ Mark habits as complete and track progress over time
- ✅ **Offline-first architecture** - works without internet connection
- ✅ Automatic cloud synchronization when online
- ✅ Multi-language support (English & Afrikaans)
- ✅ Google Sign-In integration
- ✅ Daily motivational quotes
- ✅ Visual progress tracking with charts

---

## ✨ Key Features

### 🎯 Habit Management
- Add habits with name, duration, and specific weekdays
- Set custom reminder times using TimePicker
- Mark habits as complete with checkbox tracking
- View all habits in an organized list view
- Edit and delete habits (soft delete with sync)

### 📊 Progress Tracking
- Circular progress indicator showing completion percentage
- View completed habits in a dedicated section
- Track habit streaks and consistency
- Visual feedback on daily progress

### 🌐 Offline & Sync
- **Works completely offline** - no internet required
- Local database storage using **Room**
- Automatic background synchronization via **WorkManager**
- Sync queue system tracks pending changes
- Conflict resolution with last-write-wins strategy
- Network status detection and user notifications

### 👤 Authentication
- Email/Password authentication via Firebase
- Google Sign-In integration with custom branded button
- **Offline Mode** - use app without creating an account
- Secure user data isolation per account

### 🌍 Localization
- Multi-language support (English & Afrikaans)
- In-app language switcher in Settings
- Persistent language preference
- Easy to add more languages

### 🔔 Notifications
- Habit reminder notifications at set times
- Runtime permission handling for Android 13+
- Notification channels for organized alerts
- Customizable notification settings

### 💡 Additional Features
- Daily motivational quotes with offline caching
- Clean Material Design UI
- Dark mode ready
- Responsive layouts for different screen sizes

---

## 🏗️ Architecture

### Design Patterns
- **Repository Pattern** - Single source of truth for data
- **MVVM Architecture** - Separation of concerns
- **LiveData** - Reactive UI updates
- **Offline-First** - Local database as primary data source

### Project Structure
```
app/
├── database/
│   ├── HabitEntity.kt          # Room entity for habits
│   ├── HabitDao.kt             # Database access object
│   ├── SyncQueueEntity.kt      # Tracks pending syncs
│   ├── SyncQueueDao.kt         # Sync queue operations
│   └── AppDatabase.kt          # Room database instance
├── models/
│   └── Habit.kt                # Data model for habits
├── network/
│   ├── RetrofitClient.kt       # API client setup
│   ├── ApiService.kt           # API endpoints
│   └── RetrofitClientQuotes.kt # Quotes API client
├── repository/
│   └── HabitRepository.kt      # Mediates between DB and API
├── utils/
│   ├── NetworkUtils.kt         # Network connectivity checks
│   ├── NotificationHelper.kt   # Notification management
│   ├── SyncScheduler.kt        # Background sync scheduler
│   ├── LanguageManager.kt      # Language switching
│   └── Constants.kt            # App-wide constants
├── workers/
│   └── SyncWorker.kt           # Background sync worker
└── activities/
    ├── MainActivity.kt          # Login screen
    ├── RegisterActivity.kt      # Registration
    ├── HomeActivity.kt          # Main habit list
    ├── AddHabitActivity.kt      # Add new habits
    ├── ProgressActivity.kt      # Progress tracking
    └── SettingsActivity.kt      # App settings
```

---

## 🔧 Technologies & Libraries

| Component | Technology | Version |
|-----------|-----------|---------|
| **Language** | Kotlin | Latest |
| **IDE** | Android Studio | Ladybug+ |
| **Min SDK** | Android 7.0 (API 24) | - |
| **Target SDK** | Android 14 (API 34) | - |

### Core Dependencies
```gradle
// AndroidX Core
implementation 'androidx.core:core-ktx:1.12.0'
implementation 'androidx.appcompat:appcompat:1.6.1'
implementation 'com.google.android.material:material:1.11.0'
implementation 'androidx.constraintlayout:constraintlayout:2.1.4'

// Firebase
implementation platform('com.google.firebase:firebase-bom:33.1.2')
implementation 'com.google.firebase:firebase-auth-ktx'
implementation 'com.google.firebase:firebase-database-ktx'

// Google Sign-In
implementation 'com.google.android.gms:play-services-auth:20.7.0'

// Room Database (Offline Storage)
implementation 'androidx.room:room-runtime:2.6.1'
implementation 'androidx.room:room-ktx:2.6.1'
kapt 'androidx.room:room-compiler:2.6.1'

// Coroutines
implementation 'org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3'
implementation 'org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3'

// WorkManager (Background Sync)
implementation 'androidx.work:work-runtime-ktx:2.9.0'

// Retrofit (REST API)
implementation 'com.squareup.retrofit2:retrofit:2.9.0'
implementation 'com.squareup.retrofit2:converter-gson:2.9.0'

// Lifecycle Components
implementation 'androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0'
implementation 'androidx.lifecycle:lifecycle-livedata-ktx:2.7.0'

// Gson
implementation 'com.google.code.gson:gson:2.10.1'
```

---

## 🚀 Getting Started

### Prerequisites
- Android Studio Ladybug or newer
- Android SDK 24+
- Firebase account
- Google API Console project (for Google Sign-In)

### Installation

1. **Clone the repository:**
```bash
git clone https://github.com/RahilSir/HabitMate.git
cd HabitMate
```

2. **Firebase Setup:**
   - Go to [Firebase Console](https://console.firebase.google.com/)
   - Create a new project
   - Add an Android app with your package name
   - Download `google-services.json`
   - Place it in `app/` directory
   - Enable Authentication (Email/Password & Google)
   - Enable Realtime Database

3. **Google Sign-In Setup:**
   - In Firebase Console, go to Authentication → Sign-in method
   - Enable Google Sign-In
   - Copy the Web client ID
   - Add it to `res/values/strings.xml`:
```xml
   <string name="default_web_client_id">YOUR_WEB_CLIENT_ID_HERE</string>
```

4. **Get SHA-1 Fingerprint:**
```bash
# For Windows
keytool -list -v -keystore "%USERPROFILE%\.android\debug.keystore" -alias androiddebugkey -storepass android -keypass android

# For Mac/Linux
keytool -list -v -keystore ~/.android/debug.keystore -alias androiddebugkey -storepass android -keypass android
```
   - Add SHA-1 to Firebase Console → Project Settings → Your Android App

5. **Build and Run:**
   - Open project in Android Studio
   - Sync Gradle files
   - Connect device or start emulator
   - Click Run ▶️

---

## 📖 How to Use

### First Time Setup
1. Launch the app
2. Choose one of three options:
   - **Login** with email/password
   - **Register** for a new account
   - **Continue in Offline Mode** (no account needed)

### Adding Habits
1. Tap the **"Add Habit"** button
2. Enter habit name and duration
3. Select days of the week
4. Set reminder time
5. Tap **"Save"**

### Tracking Progress
1. Check off habits as you complete them
2. View progress percentage in the **Progress** tab
3. See completed habits list
4. Track your streak

### Offline Mode
- All features work without internet
- Changes sync automatically when reconnected
- Pending sync count shown in status bar

### Settings
- Switch between English and Afrikaans
- Enable/disable notifications
- Manage notification permissions
- Exit offline mode and login

---

## 🔥 API Integration

### Backend Endpoints
The app uses MockAPI.io for demonstration. Replace with your own backend:
```kotlin
// Base URL
https://your-api.mockapi.io/api/v1/

// Endpoints
GET    /habits?userId={userId}     // Fetch user's habits
POST   /habits                     // Create new habit
PUT    /habits/{id}                // Update habit
PATCH  /habits/{id}                // Update habit status
DELETE /habits/{id}                // Delete habit
```

### Quotes API
```kotlin
GET https://zenquotes.io/api/random  // Random motivational quote
```

---

## 🎨 UI/UX Features

- **Material Design 3** components
- Custom Google Sign-In button with branding
- Circular progress indicators
- Smooth animations and transitions
- Toast notifications for user feedback
- Pull-to-refresh support
- Empty state illustrations
- Loading indicators

---

## 🔐 Security & Privacy

- ✅ User authentication via Firebase Auth
- ✅ Secure password handling (never stored locally)
- ✅ User data isolation (each user sees only their data)
- ✅ Offline data encrypted at rest
- ✅ Network traffic over HTTPS
- ✅ No third-party tracking
- ✅ GDPR compliant data handling

---

## 🌟 Future Enhancements

- [ ] Habit categories and tags
- [ ] Streak tracking and badges
- [ ] Data export (CSV, PDF)
- [ ] Social sharing features
- [ ] Habit templates library
- [ ] Advanced statistics and insights
- [ ] Widget support
- [ ] Wear OS companion app
- [ ] Dark mode toggle
- [ ] Habit notes and journaling

---

## 🐛 Known Issues

- Notification icon needs custom design (currently using default)
- Progress percentage resets daily (needs persistent streak tracking)
- Google Sign-In requires internet (by design)

---

## 🤝 Contributing

Contributions are welcome! Please follow these steps:

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

---

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

## 👨‍💻 Author

**Rahil Sir**
- GitHub: [@RahilSir](https://github.com/RahilSir)
- Email: rahilsirkissoon@gmail.com
---

## 🙏 Acknowledgments

- Firebase for backend services
- Material Design for UI guidelines
- ZenQuotes API for motivational quotes
- Android community for amazing libraries

---

## 📸 Screenshots

![Login Screen]([screenshots/login.png](https://github.com/RahilSir/HabitMate/blob/main/Screenshot_20251112_153347.png))
![Home Screen](screenshots/home.png)
![Add Habit](screenshots/add_habit.png)
![Progress](screenshots/progress.png)

---

## 📊 Project Status

**Current Version:** 1.0.0  
**Status:** Active Development  
**Last Updated:** November 2025

---

**Built with ❤️ using Kotlin and Android Studio**
