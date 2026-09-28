# JobFinder

JobFinder is an Android app for finding relevant job opportunities by keyword, with a focus on jobs in Egypt.

## Current features

- Jetpack Compose UI
- MVVM architecture
- Kotlin coroutines and `Flow`
- Room database for saved jobs
- Keyword filtering by title, company, location, and description
- In-app notifications for new jobs that open the job link when tapped
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

## Job API

The planned job source is the Jooble Egypt REST API:

```text
https://eg.jooble.org/api/YOUR_API_KEY
```

For local development, keep the key in the ignored `local.properties` file:

```properties
jooble.api.key=YOUR_API_KEY
```

The current Android prototype reads this local value to test Jooble integration. Do not commit `local.properties`, `.env`, or any API key. For production, keep the Jooble key on a protected backend because keys embedded in APKs can be extracted.

## Roadmap

- Add Jooble API synchronization
- Add WorkManager periodic background sync
- Improve in-app notifications and background sync controls
- Add saved searches and job details
- Add richer job details and application tracking
