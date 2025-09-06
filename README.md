# PairUp

PairUp is a Tinder-like Android application designed to help users connect and pair up with others based on shared interests. The app features a swipe-based interface, real-time updates, and seamless integration with Firebase for authentication and data storage.

## Features

- **Swipe Cards:** Users can swipe left or right to like or pass on other users.
- **Real-Time Updates:** User profiles and matches update in real-time using Firebase Realtime Database.
- **Authentication:** Secure sign-in and sign-up with Firebase Authentication.
- **Profile Management:** Users can create and edit their profiles, including uploading photos.
- **Chat:** Matched users can chat with each other in real-time.
- **Consistent UI:** The app uses consistent modal components and CSS classes for a unified user experience.
- **Live Refresh:** Project lists and matches update dynamically without requiring a full page reload.

## Firebase Integration

PairUp uses Firebase for:

- **Authentication:** Email/password and social login support.
- **Realtime Database:** Storing user profiles, matches, and chat messages.
- **Storage:** Uploading and retrieving user profile images.

### Firebase Setup

1. **Create a Firebase Project:**
   - Go to [Firebase Console](https://console.firebase.google.com/).
   - Click "Add project" and follow the setup steps.

2. **Register your Android app:**
   - Add your app’s package name.
   - Download the `google-services.json` file and place it in your app’s `/app` directory.

3. **Add Firebase SDK to your project:**
   - In your `build.gradle` files, add the required Firebase dependencies:
     ```gradle
     // Project-level build.gradle
     classpath 'com.google.gms:google-services:4.3.15'
     ```
     ```gradle
     // App-level build.gradle
     apply plugin: 'com.google.gms.google-services'

     implementation 'com.google.firebase:firebase-auth:22.3.0'
     implementation 'com.google.firebase:firebase-database:20.3.0'
     implementation 'com.google.firebase:firebase-storage:20.3.0'
     ```

4. **Sync your project** with Gradle.

5. **Initialize Firebase** in your app’s `Application` or `MainActivity`:
   ```java
   import com.google.firebase.FirebaseApp;

   @Override
   public void onCreate() {
       super.onCreate();
       FirebaseApp.initializeApp(this);
   }
   ```

## Getting Started

1. **Clone the repository:**
   ```
   git clone https://github.com/janpul/pairup-tinder-app.git
   ```

2. **Open in Android Studio** and let Gradle sync.

3. **Add your `google-services.json`** to the `/app` directory.

4. **Build and run** the app on your device or emulator.

## Project Structure

- `/app/src/main/java/` — Main source code
- `/app/src/main/res/` — Resources (layouts, drawables, etc.)
- `/app/build.gradle` — App-level Gradle config
- `/google-services.json` — Firebase config (not included in repo)

## License

This project is licensed under the MIT License.

---

**Note:**  
Do not commit your `google-services.json` or any sensitive keys to the repository.
