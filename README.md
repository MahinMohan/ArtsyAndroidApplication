## Artist Search Mobile Application

# Demonstration

This app showcases advanced **native Android application development**, focusing on modern UI/UX, efficient networking, robust authentication, and full integration with cloud-based backend services to provide seamless artist exploration on the go. You can watch the demo here:

<br>
<a href="https://github.com/user-attachments/assets/3497ad2a-b230-48ae-8c30-2629b64dc86a">  <img src="https://github.com/user-attachments/assets/14de167f-7295-476e-a555-6c7051545329" width="508" height="846" alt="Watch Demo Video"/></a>






## Overview

The Artsy Mobile Search Application Insight is a rich **Android mobile application** that extends the artist exploration platform to mobile users with a smooth, native experience. Developed using **Kotlin** and **Jetpack Compose**, this app leverages modern Android architecture components and libraries such as Retrofit and Coil to deliver dynamic artist search, detailed views, user authentication, and favorites management—all integrated with the Artsy API.

## Skill Highlights

- **Android Development:** Built with Kotlin and Jetpack Compose following Material Design 3 guidelines for a clean, intuitive, and responsive UI optimized for Pixel 8 Pro (API 34).
- **Networking:** Uses **Retrofit** with Kotlin Coroutines and OkHttp client for efficient, asynchronous API calls to the backend and Artsy services.
- **Image Loading:** Implements **Coil** Compose for asynchronous image loading with caching and error handling.
- **User Authentication & Session Management:** Handles persistent login via cookies with PersistentCookieJar and Secure Storage, ensuring seamless user sessions across app restarts.
- **State Management & UI:** Uses Jetpack Compose's declarative paradigm with composables like LazyColumn, Snackbar, Dialogs, and Material icons to build dynamic, reactive screens.
- **Multi-Screen Navigation:** Implements clear navigation flow across Home, Login, Register, Search Results, Artist Details, and Favorites screens with back stacking and action bar management.
- **Error Handling & User Feedback:** Comprehensive UI error states for invalid input, network failures, empty results, and loading indicators maintain robustness and user trust.
- **Third-party Libraries:** Integrates essential Android libraries like Retrofit, Coil, OkHttp, and PersistentCookieJar to enhance development velocity and app performance.

## Architecture

- **Frontend:** Kotlin Android app using Jetpack Compose for UI, Retrofit for networking, and SharedPreferences for persistent cookie storage.
- **Backend:** Relies on the Node.js Express backend implemented in the previous project for secure API interactions and user data management.
- **APIs:** Artsy REST API endpoints for authentication, search, artist details, artworks, and categories.
- **Deployment Environment:** Targeted for Android emulator with Pixel 8 Pro setup, API 34.

## Features

- Splash screen and adaptive app icon for professional branding.
- Home screen with user's favorites section and dynamic date display.
- Dynamic search bar with auto-fetch for artist results starting from 3 characters.
- Detailed artist info screen with tab layout supporting info, artworks, and similar artists.
- User registration and login screens with robust input validation.
- Persistent login management with automated cookie handling and session verification.
- Favorites toggle supporting add/remove with real-time feedback snackbars.
- Responsive UI components including cards, dialogs, progress bars, and menus.
- Ability to open Artsy homepage externally from the app for extended exploration.

## Setup Instructions

### Prerequisites

- Android Studio with API 34 SDK
- Kotlin 1.8+ and Jetpack Compose 1.4+
- Backend server (Node.js Express) deployed and accessible
- Artsy API credentials configured in backend

### Running Locally

1. Clone the repository.
2. Open in Android Studio and set up the emulator (Pixel 8 Pro, API 34).
3. Configure backend URL and API keys in app settings or build configs.
4. Build and run the app on the emulator.
5. Navigate through login, search, and artist exploration flows.

## Project Structure

- `/app/` — Main Kotlin source code with Jetpack Compose UI components and network services
- `/res/` — Android resources including icons, layouts, and themes
- `/build.gradle` — Project build configuration
- `/README.md` — Project overview and setup instructions

## Tech Stack

- **Languages:** Kotlin, Java (Android SDK)
- **UI Framework:** Jetpack Compose (Material 3)
- **Networking:** Retrofit, OkHttp, Kotlin Coroutines
- **Image Loading:** Coil Compose
- **Session Management:** PersistentCookieJar, SharedPreferences
- **APIs:** Artsy REST API
- **Tools:** Android Studio, Pixel Emulator API 34
