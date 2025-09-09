🎨 Artsy Android Application

An elegant Android app built with Kotlin and Jetpack Compose that allows users to discover artists, explore artworks, and manage favourites. With a modern UI and secure authentication, Artsy delivers a smooth and engaging experience for art enthusiasts.

🚀 Features

🔐 User Authentication – Register, Login, Logout, and Delete Account

🎭 Artist Search – Find artists by name and view detailed info

🖼️ Artworks Gallery – Explore categories and collections of artworks

⭐ Favourites – Add/remove favourites with real-time sync

🎨 Modern UI – Built entirely with Jetpack Compose & Material3

🌐 Networking Layer – Secure API calls with OkHttp, Retrofit, and custom cookie management

📱 Responsive Layout – Optimized for multiple screen sizes

🛠️ Tech Stack

Language: Kotlin

UI: Jetpack Compose (Material 3, Theming, Animations)

Networking: Retrofit + OkHttp + Manual CookieJar

State Management: Compose State + ViewModels

Build System: Gradle (KTS)

IDE: Android Studio

📂 Project Structure
ArtsyAndroidApplication-main/
│── app/
│   ├── src/main/
│   │   ├── java/com/example/artsyapplication/
│   │   │   ├── MainActivity.kt
│   │   │   ├── network/       # API clients and networking logic
│   │   │   ├── screenviews/   # UI screens (Login, Register, Home, Artist Details, etc.)
│   │   │   └── ui/theme/      # App theming (Colors, Typography, Theme)
│   │   ├── res/               # Drawable assets, XML configs, Strings
│   │   └── AndroidManifest.xml
│   ├── build.gradle.kts
│   └── proguard-rules.pro
├── build.gradle.kts
├── gradle.properties
├── settings.gradle.kts
└── gradlew / gradlew.bat

⚙️ Setup & Installation
1️⃣ Clone the Repository
git clone https://github.com/your-username/ArtsyAndroidApplication.git
cd ArtsyAndroidApplication-main

2️⃣ Open in Android Studio

Open Android Studio.

Click File → Open and select the ArtsyAndroidApplication-main folder.

Let Gradle sync automatically (first time may take a few minutes).

Make sure you have the latest Android SDK and Emulator/physical device set up.

3️⃣ Run the App in Android Studio

Select a device from the Device Manager (Emulator or USB-connected phone).

Press Shift + F10 or click ▶️ Run in the top menu.

Android Studio will build and install the app automatically.

4️⃣ Run via Command Line (Optional)

If you prefer running outside Android Studio, you can use Gradle commands:

# Clean previous builds
./gradlew clean

# Build the debug APK
./gradlew assembleDebug

# Install on connected device/emulator
./gradlew installDebug

# Run tests
./gradlew test


Once installed, find Artsy App on your device and launch it.

🔮 Future Improvements

Dark Mode support

Offline caching for artworks

Push notifications for new artists

Unit + UI tests (JUnit, Espresso)

🤝 Contributing

Contributions are welcome! Fork the repo, create a feature branch, and submit a pull request.
