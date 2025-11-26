# ActiveBreak 🏃

**ActiveBreak** is an Android application designed to help users maintain a healthy lifestyle by reminding them to take breaks, perform physical exercises, and keep track of important tasks.

The app is perfect for those who spend a lot of time at the computer or lead a sedentary lifestyle.

## ✨ Key Features

*   **🧘 Break Reminders:**
    *   Get notifications with activity suggestions (exercise, eye rest, drinking water, etc.).
    *   Customizable reminder interval (from 15 minutes to 2 hours).
    *   Activities are divided by time of day (morning, afternoon, evening).
*   **✅ TODO List:**
    *   Task list management with deadline tracking capability.
    *   Recurring tasks (daily, weekly, by days of the week).
    *   Task reminders at selected time.
    *   Ability to "pause" a task.
    *   Compact and convenient task list interface.
*   **📊 Statistics:**
    *   Track completed activities and tasks.
    *   (In development) View statistics for day and week.
*   **⚙️ Flexible Settings:**
    *   Set active working hours (start and end of working day).
    *   Enable/disable notifications for breaks and tasks separately.
    *   **🌐 Language Support:** Choose between Russian, English, or system language.
*   **💬 Messenger Integration:**
    *   Send break notifications to **Telegram** (via bot).

## 🛠️ Technology Stack

The application is built using modern Android development technologies:

*   **Language:** [Kotlin](https://kotlinlang.org/)
*   **UI:** [Jetpack Compose](https://developer.android.com/jetpack/compose) (Material Design 3)
*   **Architecture:** MVVM (Model-View-ViewModel)
*   **Asynchronous:** Kotlin Coroutines + Flow
*   **Database:** [Room](https://developer.android.com/training/data-storage/room) (SQLite)
*   **Background Tasks:** [WorkManager](https://developer.android.com/topic/libraries/architecture/workmanager)
*   **Settings:** [DataStore Preferences](https://developer.android.com/topic/libraries/architecture/datastore)
*   **Navigation:** Navigation Compose
*   **Dependency Injection:** (Manual injection via `Application` class)

## 📲 Installation and Launch

1.  Clone the repository:
    ```bash
    git clone https://github.com/your-username/ActiveBreak.git
    ```
2.  Open the project in **Android Studio**.
3.  Wait for Gradle synchronization.
4.  Run the application on an emulator or real device (Android 8.0+).

## 🚀 How to Build Release Version

To create a signed APK or AAB file:

1.  In Android Studio go to **Build** -> **Generate Signed Bundle / APK...**
2.  Choose **Android App Bundle** (for Google Play) or **APK** (for manual installation).
3.  Create a new keystore or use existing one.
    *   *Important:* Save the `.jks` file and passwords in a secure place!
4.  Choose **release** build variant.
5.  The finished file will be located in the `app/release/` folder.

## 🆕 Latest Updates

### November 25, 2025 - Persistent Notification Feature

**Added persistent notification in notification panel (Notification Shade):**

- ✅ **Persistent Status Notification**: Permanent notification with current app status
- ✅ **Quick Controls**: Quick control buttons (pause/resume) directly from notification panel
- ✅ **Smart Status Display**: Dynamic status display ("Active" / "Paused")
- ✅ **WorkManager Integration**: Automatic background task management when switching status
- ✅ **Improved UX**: Simplified notification texts without excessive app name repetition
- ✅ **BroadcastReceiver**: Full integration with Android notification system

**Technical Improvements:**
- Added separate notification channel for status (`STATUS_CHANNEL_ID`)
- Implemented `BroadcastReceiver` for handling notification actions
- Integration with `MainActivity` for app state synchronization
- Support for Android M+ for checking active notifications

**Recent commits:**
- `53ca065` - add to notification bar
- `fd8bebc` - fixes  
- `5a4db90` - fixes

### November 26, 2024 - Multi-language Support

**Added support for multiple languages:**

- ✅ **Language Selection**: Choose between Russian, English, or system language in settings
- ✅ **Complete Localization**: All UI elements are properly localized
- ✅ **Persistent Settings**: Language preference is saved and applied on app restart
- ✅ **Easy Extension**: Simple framework for adding more languages in the future

## 📝 License

This project is distributed under the MIT License. See the LICENSE file for details (if available).

---
*Developed with care for your health!* 💪
