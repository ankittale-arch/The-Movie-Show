package com.ankitt.themovieshow.core.data

import com.ankitt.themovieshow.core.common.di.IoDispatcher
import com.ankitt.themovieshow.core.data.mapper.toDetailEntity
import com.ankitt.themovieshow.core.data.mapper.toDomain
import com.ankitt.themovieshow.core.data.mapper.toEntity
import com.ankitt.themovieshow.core.data.mapper.toMovieEntity
import com.ankitt.themovieshow.core.data.model.Genre
import com.ankitt.themovieshow.core.data.model.Movie
import com.ankitt.themovieshow.core.data.model.MovieDetail
import com.ankitt.themovieshow.core.database.movie.MovieDao
import com.ankitt.themovieshow.core.database.movie.MovieDetailDao
import com.ankitt.themovieshow.core.database.movie.MovieGenreCrossRef
import com.ankitt.themovieshow.core.database.movie.MovieListEntity
import com.ankitt.themovieshow.core.database.sync.SyncStateDao
import com.ankitt.themovieshow.core.database.sync.SyncStateEntity
import com.ankitt.themovieshow.core.network.api.TmdbApiService
import com.ankitt.themovieshow.core.network.model.MovieDto
import com.ankitt.themovieshow.core.network.model.MoviePageDto
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

class MovieRepositoryImpl @Inject constructor(
    private val tmdbApiService: TmdbApiService,
    private val movieDao: MovieDao,
    private val movieDetailDao: MovieDetailDao,
    private val syncStateDao: SyncStateDao,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : MovieRepository {

    override fun observeMoviesForList(listKey: String): Flow<List<Movie>> =
        movieDao.observeMoviesForList(listKey)
            .map { entities -> entities.map { it.toDomain() } }
            .flowOn(ioDispatcher)

    override fun observeGenres(): Flow<List<Genre>> =
        movieDao.observeGenres()
            .map { entities -> entities.map { it.toDomain() } }
            .flowOn(ioDispatcher)

    override suspend fun refreshHome(): Result<Unit> = withContext(ioDispatcher) {
        val results = coroutineScope {
            listOf(
                async { refreshMoviesForListInternal(HomeListKeys.TRENDING) },
                async { refreshMoviesForListInternal(HomeListKeys.NOW_PLAYING) },
                async { refreshMoviesForListInternal(HomeListKeys.POPULAR) },
                async { refreshMoviesForListInternal(HomeListKeys.DISCOVER) },
                async { refreshMoviesForListInternal(HomeListKeys.UPCOMING) },
                async { refreshGenres() },
            ).awaitAll()
        }

        val firstFailure = results.firstOrNull { it.isFailure }
        if (firstFailure != null && results.all { it.isFailure }) {
            Result.failure(firstFailure.exceptionOrNull() ?: IllegalStateException("Home refresh failed"))
        } else {
            Result.success(Unit)
        }
    }

    override suspend fun searchMovies(query: String): Result<Unit> = withContext(ioDispatcher) {
        refreshList(HomeListKeys.SEARCH) { tmdbApiService.searchMovies(query = query).results }
    }

    override suspend fun clearSearchResults() = withContext(ioDispatcher) {
        movieDao.clearListMembership(HomeListKeys.SEARCH)
    }

    override suspend fun refreshMoviesForList(listKey: String): Result<Unit> = withContext(ioDispatcher) {
        refreshMoviesForListInternal(listKey)
    }

    override suspend fun loadMoreMoviesForList(listKey: String, nextPage: Int): Result<Boolean> =
        withContext(ioDispatcher) {
            runCatching {
                val page = fetchMoviePage(listKey, nextPage)
                val existingCount = movieDao.countListMembership(listKey)
                val entities = page.results.map { it.toEntity() }
                val memberships = page.results.mapIndexed { index, dto ->
                    MovieListEntity(listKey, dto.id, existingCount + index)
                }
                movieDao.appendListMembership(entities, memberships)
                page.page < page.totalPages
            }
        }

    /**
     * Maps a [HomeListKeys] value to its TMDB call. A `genre(id)`-shaped key is parsed back into
     * its id rather than modeled as a separate parameter, so every fixed row and every genre row
     * share this one dispatch — [refreshHome]'s concurrent refresh, [refreshMoviesForList], and
     * [loadMoreMoviesForList] all go through it.
     */
    private suspend fun fetchMoviePage(listKey: String, page: Int): MoviePageDto = when (listKey) {
        HomeListKeys.TRENDING -> tmdbApiService.getTrendingMovies(page = page)
        HomeListKeys.NOW_PLAYING -> tmdbApiService.getNowPlayingMovies(page = page)
        HomeListKeys.POPULAR -> tmdbApiService.getPopularMovies(page = page)
        HomeListKeys.DISCOVER -> tmdbApiService.discoverMovies(page = page)
        HomeListKeys.UPCOMING -> tmdbApiService.getUpcomingMovies(page = page)
        else -> {
            val genreId = HomeListKeys.genreIdOrNull(listKey)
                ?: throw IllegalArgumentException("Unknown list key: $listKey")
            tmdbApiService.discoverMovies(page = page, withGenres = genreId)
        }
    }

    private suspend fun refreshMoviesForListInternal(listKey: String): Result<Unit> =
        refreshList(listKey) { fetchMoviePage(listKey, page = 1).results }

    private suspend fun refreshList(listKey: String, fetch: suspend () -> List<MovieDto>): Result<Unit> {
        val result = runCatching {
            val movies = fetch()
            val entities = movies.map { it.toEntity() }
            val memberships = movies.mapIndexed { index, dto -> MovieListEntity(listKey, dto.id, index) }
            movieDao.replaceListMembership(listKey, entities, memberships)
        }
        syncStateDao.upsert(
            SyncStateEntity(
                resource = listKey,
                lastSyncedAtEpochMillis = System.currentTimeMillis(),
                isSyncing = false,
                lastErrorMessage = result.exceptionOrNull()?.message,
            ),
        )
        return result
    }

    override fun observeMovieDetail(movieId: Int): Flow<MovieDetail?> = combine(
        movieDetailDao.observeMovie(movieId),
        movieDetailDao.observeMovieDetail(movieId),
        movieDetailDao.observeGenresForMovie(movieId),
        movieDetailDao.observeCastForMovie(movieId),
    ) { movie, detail, genres, cast ->
        if (movie == null) {
            null
        } else {
            MovieDetail(
                id = movie.movieId,
                title = movie.title,
                overview = movie.overview,
                posterPath = movie.posterPath,
                backdropPath = movie.backdropPath,
                releaseDate = movie.releaseDate,
                runtime = detail?.runtime,
                voteAverage = movie.voteAverage,
                voteCount = movie.voteCount,
                tagline = detail?.tagline,
                originalLanguage = detail?.originalLanguage,
                genres = genres.map { it.toDomain() },
                cast = cast.map { it.toDomain() },
                trailerYoutubeKey = detail?.trailerYoutubeKey,
            )
        }
    }.flowOn(ioDispatcher)

    override suspend fun refreshMovieDetail(movieId: Int): Result<Unit> = withContext(ioDispatcher) {
        runCatching {
            val dto = tmdbApiService.getMovieDetail(movieId)
            movieDetailDao.replaceMovieDetail(
                movie = dto.toMovieEntity(),
                detail = dto.toDetailEntity(),
                genres = dto.genres.map { it.toEntity() },
                genreCrossRefs = dto.genres.map { MovieGenreCrossRef(movieId = dto.id, genreId = it.id) },
                cast = dto.credits.cast.map { it.toEntity(movieId = dto.id) },
            )
        }
    }

    private suspend fun refreshGenres(): Result<Unit> {
        val result = runCatching {
            val genres = tmdbApiService.getMovieGenres().genres
            movieDao.upsertGenres(genres.map { it.toEntity() })
        }
        syncStateDao.upsert(
            SyncStateEntity(
                resource = HomeListKeys.GENRES,
                lastSyncedAtEpochMillis = System.currentTimeMillis(),
                isSyncing = false,
                lastErrorMessage = result.exceptionOrNull()?.message,
            ),
        )
        return result
    }
}
