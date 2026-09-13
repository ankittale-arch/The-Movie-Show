# The Movie Show

An Android app for browsing movies, powered by [The Movie Database (TMDB)](https://www.themoviedb.org/) API.

## Features

- **Home** — curated movie listings (e.g. trending/popular) on launch.
- **Search** — look up movies by title.
- **Movie List** — browse movies by category/list type.
- **Movie Detail** — full details for a selected movie (overview, poster, metadata).
- **Bookmarks** — save movies to a personal list for later.
- **Recently Viewed** — automatic history of movies you've opened.
- **Offline cache** — movie data is cached locally (Room) so recently loaded content is available without a network connection.
- **Background sync** — a periodic outbox sync and a daily data refresh keep cached data up to date, scheduled automatically at process start via WorkManager.

## Architecture

Multi-module Android project using Kotlin, Jetpack Compose, and Hilt for dependency injection.

**Core modules**
- `core:common` — shared utilities.
- `core:network` — TMDB API client (Retrofit/OkHttp), DTOs, auth interceptor.
- `core:database` — Room database, local caching.
- `core:data` — repositories bridging network + database.
- `core:sync` — WorkManager-based periodic sync (outbox sync, data refresh) via an `androidx.startup` initializer.
- `core:designsystem` — shared Compose UI components/theme.

**Feature modules**
- `feature:home`
- `feature:search`
- `feature:movielist`
- `feature:moviedetail`
- `feature:bookmarks`
- `feature:recentlyviewed`

**App module** (`app`) wires everything together via Hilt (`TheMovieShowApplication`) and hosts `MainActivity`.

## Tech Stack

- Kotlin, Jetpack Compose
- Hilt (dependency injection, including `@HiltWorker` for background work)
- Room (local persistence/cache)
- Retrofit/OkHttp (TMDB API)
- WorkManager (periodic background sync)
- Coil3 (image loading)
- androidx.startup (lazy component initialization)

## Requirements

- minSdk 24, targetSdk 37
- A TMDB API key (configured for `core:network`'s auth interceptor)

## Building

```bash
./gradlew :app:assembleDebug
```



<img width="1080" height="2424" alt="Screenshot_20260913_164739" src="https://github.com/user-attachments/assets/79d14c9d-f489-4e23-aebf-e1bff2c35aeb" />
<img width="1080" height="2424" alt="Screenshot_20260913_164722" src="https://github.com/user-attachments/assets/0108434b-5ea0-4cbc-a66f-809a3fe404d1" />

