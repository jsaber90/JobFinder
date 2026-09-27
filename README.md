# JobFinder

JobFinder is an Android app for finding relevant job opportunities by keyword, with a focus on jobs in Egypt.

## Current features

- Jetpack Compose UI
- MVVM architecture
- Kotlin coroutines and `Flow`
- Room database for saved jobs
- Keyword filtering by title, company, location, and description
- JobFinder launcher icon
- Android project configured for Gradle, Retrofit, Room, and WorkManager

## Tech stack

- Kotlin
- Jetpack Compose and Material 3
- AndroidX ViewModel
- Room
- Retrofit
- WorkManager
- Kotlin coroutines

## Build and run

Set the Android SDK and Java paths, then run:

```powershell
./gradlew.bat assembleDebug
```

Install on a connected Android device:

```powershell
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb shell monkey -p com.ai.jobfinder 1
```

## Job API

The planned job source is the Jooble Egypt REST API:

```text
https://eg.jooble.org/api/YOUR_API_KEY
```

Keep the Jooble API key on a protected backend. Do not commit it to the Android project or place it directly in the APK.

## Roadmap

- Add Jooble API synchronization
- Add WorkManager periodic background sync
- Add push and email notifications for new jobs
- Add saved searches and job details
- Add direct application links
