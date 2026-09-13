package com.ankitt.themovieshow.core.database.movie

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface MovieDetailDao {

    @Upsert
    suspend fun upsertMovie(movie: MovieEntity)

    @Upsert
    suspend fun upsertMovieDetail(detail: MovieDetailEntity)

    @Upsert
    suspend fun upsertGenres(genres: List<GenreEntity>)

    @Query("DELETE FROM movie_genre_cross_ref WHERE movieId = :movieId")
    suspend fun clearGenreCrossRefs(movieId: Int)

    @Upsert
    suspend fun upsertGenreCrossRefs(refs: List<MovieGenreCrossRef>)

    @Query("DELETE FROM movie_cast WHERE movieId = :movieId")
    suspend fun clearCast(movieId: Int)

    @Upsert
    suspend fun upsertCast(cast: List<MovieCastEntity>)

    /** Same atomic clear-then-insert pattern as [MovieDao.replaceListMembership]. */
    @Transaction
    suspend fun replaceMovieDetail(
        movie: MovieEntity,
        detail: MovieDetailEntity,
        genres: List<GenreEntity>,
        genreCrossRefs: List<MovieGenreCrossRef>,
        cast: List<MovieCastEntity>,
    ) {
        upsertMovie(movie)
        upsertMovieDetail(detail)
        upsertGenres(genres)
        clearGenreCrossRefs(movie.movieId)
        upsertGenreCrossRefs(genreCrossRefs)
        clearCast(movie.movieId)
        upsertCast(cast)
    }

    @Query("SELECT * FROM movie WHERE movieId = :movieId")
    fun observeMovie(movieId: Int): Flow<MovieEntity?>

    @Query("SELECT * FROM movie_detail WHERE movieId = :movieId")
    fun observeMovieDetail(movieId: Int): Flow<MovieDetailEntity?>

    @Query(
        """SELECT genre.* FROM genre
           INNER JOIN movie_genre_cross_ref ON genre.genreId = movie_genre_cross_ref.genreId
           WHERE movie_genre_cross_ref.movieId = :movieId
           ORDER BY genre.name ASC""",
    )
    fun observeGenresForMovie(movieId: Int): Flow<List<GenreEntity>>

    @Query("SELECT * FROM movie_cast WHERE movieId = :movieId ORDER BY `order` ASC")
    fun observeCastForMovie(movieId: Int): Flow<List<MovieCastEntity>>
}
