package com.ankitt.themovieshow.core.network.api

import com.ankitt.themovieshow.core.network.model.*
import retrofit2.http.*

/**
 * Thin Retrofit interface: one method per TMDB endpoint, returning DTOs only. This interface is
 * intentionally the *only* thing in the app that depends on Retrofit annotations. Search/credits
 * endpoints are added in later phases as their DTOs and mappers land alongside them — nothing
 * here is UI- or Room-shaped, and nothing outside `core:network` (repositories included) is
 * allowed to call this service directly; repositories go through it, never the UI or ViewModel
 * layer.
 */
interface TmdbApiService {

    @GET("configuration")
    suspend fun getApiConfiguration(): ConfigurationDto

    @GET("trending/movie/day")
    suspend fun getTrendingMovies(@Query("page") page: Int = 1): MoviePageDto

    @GET("movie/now_playing")
    suspend fun getNowPlayingMovies(@Query("page") page: Int = 1): MoviePageDto

    @GET("movie/popular")
    suspend fun getPopularMovies(@Query("page") page: Int = 1): MoviePageDto

    @GET("discover/movie")
    suspend fun discoverMovies(
        @Query("page") page: Int = 1,
        @Query("sort_by") sortBy: String = "popularity.desc",
        @Query("with_genres") withGenres: Int? = null,
    ): MoviePageDto

    @GET("movie/upcoming")
    suspend fun getUpcomingMovies(@Query("page") page: Int = 1): MoviePageDto

    @GET("genre/movie/list")
    suspend fun getMovieGenres(): GenreListDto

    @GET("search/movie")
    suspend fun searchMovies(
        @Query("query") query: String,
        @Query("page") page: Int = 1,
        @Query("include_adult") includeAdult: Boolean = false,
    ): MoviePageDto

    @GET("movie/{movie_id}")
    suspend fun getMovieDetail(
        @Path("movie_id") movieId: Int,
        @Query("append_to_response") appendToResponse: String = "credits,videos",
    ): MovieDetailDto
}
