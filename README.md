# FocusWay - A Focus Timer App

FocusWay is a productivity app that helps users track and improve their focus sessions through a Cyberpunk-themed flip timer.

## Features

- **Flip Timer**: Start and stop focus sessions by flipping your phone
- **Task Management**: Add, delete, and track tasks
- **Statistics**: View detailed statistics about your focus sessions
- **Authentication**: User accounts with Firebase Authentication
- **Dark Theme**: Sleek cyberpunk-inspired dark UI with neon accents

## Setup Instructions

### Prerequisites

- Android Studio Arctic Fox or newer
- JDK 17
- An Android device or emulator running Android 7.0 (API 24) or higher
- A Firebase account (for authentication)

### Clone and Run

1. Clone the repository:
   ```
   git clone https://github.com/yourusername/focusway.git
   ```

2. Open the project in Android Studio.

3. Sync Gradle and build the project.

4. Run the app on your device or emulator.

### Firebase Setup

The app uses Firebase for authentication. To set up Firebase:

1. Create a new Firebase project at [firebase.google.com](https://firebase.google.com/)
2. Add an Android app to your Firebase project:
   - Package name: `com.mansi.focusway`
   - App nickname: `FocusWay`
3. Download the `google-services.json` file and place it in the `app/` directory
4. Enable Email/Password authentication in the Firebase Console:
   - Go to Authentication > Sign-in method
   - Enable Email/Password provider

## Architecture

The app follows MVVM (Model-View-ViewModel) architecture:

- **Model**: Room database entities (Task, FocusSession, DailyStats)
- **View**: Composable UI screens
- **ViewModel**: Manages UI-related data and business logic

## Libraries Used

- Jetpack Compose for UI
- Room for local database
- Firebase Authentication
- Material 3 Components
- Kotlin Coroutines
- Navigation Compose

## Contributing

1. Fork the repository
2. Create your feature branch: `git checkout -b feature/amazing-feature`
3. Commit your changes: `git commit -m 'Add some amazing feature'`
4. Push to the branch: `git push origin feature/amazing-feature`
5. Open a Pull Request 