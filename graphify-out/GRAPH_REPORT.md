# Graph Report - TheMovieShow  (2026-09-14)

## Corpus Check
- Corpus is ~39,568 words - fits in a single context window. You may not need a graph.

## Summary
- 615 nodes · 1032 edges · 66 communities (19 shown, 30 thin omitted)
- Extraction: 96% EXTRACTED · 4% INFERRED · 0% AMBIGUOUS · INFERRED: 40 edges (avg confidence: 0.85)
- Token cost: 1,011,373 input · 0 output

## Community Hubs (Navigation)
- Home Screen UI
- TMDB API Service
- Data Layer DI + Mappers
- Movie Domain Models
- Movie Repository Impl
- Bookmark/Watchlist DAO
- CI Build Pipeline
- App Navigation Shell
- Recently Viewed UI
- Entity Mappers
- Movie Detail Screen
- App-wide DI Modules
- TMDB Image URLs + Bookmarks UI
- Person Bio Sheet UI
- Movie List Screen
- Application Setup + Image Loading
- Coroutine Dispatcher DI
- WorkManager Sync Initializer
- Background Sync Scheduler
- Image Loader DI
- Cache Staleness Policy
- Gradle Wrapper Script
- Play Store Icon Asset
- App Icon Asset (hdpi fg)
- App Icon Asset (hdpi)
- App Round Icon Asset (hdpi)
- Launcher Icon Asset (hdpi)
- Launcher Round Icon Asset (hdpi)
- App Icon Asset (mdpi fg)
- App Icon Asset (mdpi)
- App Round Icon Asset (mdpi)
- Launcher Icon Asset (mdpi)
- Launcher Round Icon Asset (mdpi)
- App Icon Asset (xhdpi fg)
- App Icon Asset (xhdpi)
- App Round Icon Asset (xhdpi)
- Launcher Icon Asset (xhdpi)
- Launcher Round Icon Asset (xhdpi)
- App Icon Asset (xxhdpi fg)
- App Icon Asset (xxhdpi)
- App Round Icon Asset (xxhdpi)
- Launcher Icon Asset (xxhdpi)
- Launcher Round Icon Asset (xxhdpi)
- App Icon Asset (xxxhdpi fg)
- App Icon Asset (xxxhdpi)
- App Round Icon Asset (xxxhdpi)
- Launcher Icon Asset (xxxhdpi)
- Launcher Round Icon Asset (xxxhdpi)
- Search Empty State Icon

## God Nodes (most connected - your core abstractions)
1. `MovieRepository` - 36 edges
2. `MovieRepositoryImpl` - 36 edges
3. `MovieEntity` - 18 edges
4. `TheMovieShowDatabase` - 17 edges
5. `TheMovieShowTheme()` - 17 edges
6. `app module` - 17 edges
7. `MovieDetailDao` - 16 edges
8. `TmdbApiService` - 16 edges
9. `Movie` - 15 edges
10. `RatingBadge()` - 15 edges

## Surprising Connections (you probably didn't know these)
- `MovieDetailBody()` --calls--> `Chip()`  [INFERRED]
  feature/moviedetail/src/main/kotlin/com/ankitt/themovieshow/feature/moviedetail/MovieDetailScreen.kt → core/designsystem/src/main/kotlin/com/ankitt/themovieshow/core/designsystem/components/Chip.kt
- `MovieDetailBody()` --calls--> `RatingBadge()`  [INFERRED]
  feature/moviedetail/src/main/kotlin/com/ankitt/themovieshow/feature/moviedetail/MovieDetailScreen.kt → core/designsystem/src/main/kotlin/com/ankitt/themovieshow/core/designsystem/components/RatingBadge.kt
- `MovieDetailBody()` --calls--> `SyncStatusBanner()`  [INFERRED]
  feature/moviedetail/src/main/kotlin/com/ankitt/themovieshow/feature/moviedetail/MovieDetailScreen.kt → core/designsystem/src/main/kotlin/com/ankitt/themovieshow/core/designsystem/components/SyncStatusBanner.kt
- `MovieDetailViewModel` --calls--> `SyncMetadata`  [EXTRACTED]
  feature/moviedetail/src/main/kotlin/com/ankitt/themovieshow/feature/moviedetail/MovieDetailViewModel.kt → core/data/src/main/kotlin/com/ankitt/themovieshow/core/data/model/SyncMetadata.kt
- `BookmarkCard()` --calls--> `RatingBadge()`  [EXTRACTED]
  feature/bookmarks/src/main/kotlin/com/ankitt/themovieshow/feature/bookmarks/BookmarksScreen.kt → core/designsystem/src/main/kotlin/com/ankitt/themovieshow/core/designsystem/components/RatingBadge.kt

## Import Cycles
- None detected.

## Hyperedges (group relationships)
- **Core Modules Forming Architecture Base** — readme_core_common, readme_core_network, readme_core_database, readme_core_data, readme_core_sync, readme_core_designsystem [EXTRACTED 1.00]
- **Feature Modules Wired Into App** — readme_feature_home, readme_feature_search, readme_feature_movielist, readme_feature_moviedetail, readme_feature_bookmarks, readme_feature_recentlyviewed, readme_app_module [EXTRACTED 1.00]
- **CI Build and Test Pipeline** — github_workflows_ci_gradlew_lintdebug, github_workflows_ci_gradlew_testdebugunittest, github_workflows_ci_gradlew_assembledebug, github_workflows_ci_build_job [EXTRACTED 1.00]

## Communities (66 total, 30 thin omitted)

### Community 0 - "Home Screen UI"
Cohesion: 0.06
Nodes (39): androidx, AnimatedVisibilityScope, Color, Composable, HomeListKeys, Modifier, sharedMovieElement(), Chip() (+31 more)

### Community 1 - "TMDB API Service"
Cohesion: 0.06
Nodes (25): TmdbApiService, AuthInterceptor, NetworkModule, ConfigurationDto, ImagesConfigurationDto, GenreDto, GenreListDto, CastMemberDto (+17 more)

### Community 2 - "Data Layer DI + Mappers"
Cohesion: 0.06
Nodes (15): DataModule, toDomain(), Genre, CastMember, KnownForMovie, MovieDetail, PersonDetail, SyncMetadata (+7 more)

### Community 3 - "Movie Domain Models"
Cohesion: 0.09
Nodes (9): Movie, PendingOperation, PendingOperationType, Flow, Result, MovieRepositoryImpl, CoroutineWorker, Result (+1 more)

### Community 4 - "Movie Repository Impl"
Cohesion: 0.08
Nodes (12): DatabaseModule, Context, Flow, PendingOperationDao, PendingOperationEntity, RecentlyViewedDao, RecentlyViewedEntity, Flow (+4 more)

### Community 5 - "Bookmark/Watchlist DAO"
Cohesion: 0.09
Nodes (9): BookmarkDao, Flow, FavoriteMovieEntity, WatchlistMovieEntity, Flow, MovieDao, MovieEntity, MovieListEntity (+1 more)

### Community 6 - "CI Build Pipeline"
Cohesion: 0.07
Nodes (37): build Job, CI Workflow, GitHub Release, ./gradlew assembleDebug, ./gradlew assembleRelease bundleRelease, ./gradlew lintDebug, ./gradlew testDebugUnitTest, KEYSTORE_BASE64 secret (+29 more)

### Community 7 - "App Navigation Shell"
Cohesion: 0.11
Nodes (25): MainActivity, AppNavHost(), Bundle, ComponentActivity, BookmarksScreen(), bookmarksEntry(), BookmarksRoute, NavKey (+17 more)

### Community 8 - "Recently Viewed UI"
Cohesion: 0.11
Nodes (21): TheMovieShowTheme(), Modifier, RecentlyViewedCard(), RecentlyViewedContent(), RecentlyViewedContentPreview(), RecentlyViewedGrid(), RecentlyViewedItem, RecentlyViewedUiState (+13 more)

### Community 9 - "Entity Mappers"
Cohesion: 0.11
Nodes (10): toDetailEntity(), toEntity(), toMovieEntity(), youtubeTrailerKey(), GenreEntity, MovieCastEntity, Flow, MovieDetailDao (+2 more)

### Community 10 - "Movie Detail Screen"
Cohesion: 0.14
Nodes (17): CastItem(), DetailIconButton(), InfoColumn(), Modifier, MovieDetailBody(), MovieDetailContent(), MovieDetailContentPreview(), TrailerSection() (+9 more)

### Community 11 - "App-wide DI Modules"
Cohesion: 0.12
Nodes (13): ApplicationScopeModule, ConnectivityModule, ConnectivityObserver, Flow, StateFlow, ConnectivityObserverImpl, NetworkCallback, Flow (+5 more)

### Community 12 - "TMDB Image URLs + Bookmarks UI"
Cohesion: 0.14
Nodes (11): TmdbImageUrl, BookmarkCard(), BookmarksContent(), BookmarksContentPreview(), BookmarksGrid(), Modifier, BookmarkItem, BookmarksUiState (+3 more)

### Community 13 - "Person Bio Sheet UI"
Cohesion: 0.23
Nodes (13): CloseButton(), KnownForItem(), Modifier, PersonBioBody(), PersonBioSheet(), PersonBioSheetContent(), PersonBioSheetContentPreview(), KnownForMovieUi (+5 more)

### Community 14 - "Movie List Screen"
Cohesion: 0.20
Nodes (11): Modifier, MovieListCard(), MovieListContent(), MovieListContentPreview(), MovieListGrid(), MovieListScreen(), MovieListItem, MovieListUiState (+3 more)

### Community 15 - "Application Setup + Image Loading"
Cohesion: 0.23
Nodes (9): ImageLoader, TheMovieShowApplication, WorkerFactoryEntryPoint, Application, Configuration, Factory, HiltWorkerFactory, PlatformContext (+1 more)

### Community 17 - "WorkManager Sync Initializer"
Cohesion: 0.53
Nodes (3): Context, SyncInitializer, Initializer

### Community 19 - "Image Loader DI"
Cohesion: 0.60
Nodes (3): ImageLoaderModule, Context, ImageLoader

### Community 21 - "Gradle Wrapper Script"
Cohesion: 0.83
Nodes (3): gradlew script, die(), warn()

## Knowledge Gaps
- **59 isolated node(s):** `PendingOperationType`, `ImagesConfigurationDto`, `GenreDto`, `CreditsDto`, `CastMemberDto` (+54 more)
  These have ≤1 connection - possible missing edges or undocumented components. (Counts symbols only; 151 node(s) total have ≤1 connection when file, concept and rationale nodes are included.)
- **30 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `MovieRepositoryImpl` connect `Movie Domain Models` to `Data Layer DI + Mappers`, `Movie Repository Impl`?**
  _High betweenness centrality (0.280) - this node is a cross-community bridge._
- **Why does `MovieRepository` connect `Data Layer DI + Mappers` to `Movie Domain Models`, `Recently Viewed UI`, `TMDB Image URLs + Bookmarks UI`, `Person Bio Sheet UI`, `Movie List Screen`?**
  _High betweenness centrality (0.228) - this node is a cross-community bridge._
- **Why does `TmdbApiService` connect `TMDB API Service` to `Movie Repository Impl`?**
  _High betweenness centrality (0.113) - this node is a cross-community bridge._
- **What connects `PendingOperationType`, `ImagesConfigurationDto`, `GenreDto` to the rest of the system?**
  _59 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `Home Screen UI` be split into smaller, more focused modules?**
  _Cohesion score 0.058469945355191254 - nodes in this community are weakly interconnected._
- **Should `TMDB API Service` be split into smaller, more focused modules?**
  _Cohesion score 0.05520614954577219 - nodes in this community are weakly interconnected._
- **Should `Data Layer DI + Mappers` be split into smaller, more focused modules?**
  _Cohesion score 0.06207482993197279 - nodes in this community are weakly interconnected._