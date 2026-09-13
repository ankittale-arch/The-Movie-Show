package com.ankitt.themovieshow.core.data.mapper

import com.ankitt.themovieshow.core.data.model.*
import com.ankitt.themovieshow.core.database.movie.*
import com.ankitt.themovieshow.core.network.model.*

fun MovieDto.toEntity(): MovieEntity = MovieEntity(
    movieId = id,
    title = title,
    overview = overview,
    posterPath = posterPath,
    backdropPath = backdropPath,
    releaseDate = releaseDate,
    voteAverage = voteAverage,
    voteCount = voteCount,
    popularity = popularity,
)

fun MovieEntity.toDomain(): Movie = Movie(
    id = movieId,
    title = title,
    overview = overview,
    posterPath = posterPath,
    backdropPath = backdropPath,
    releaseDate = releaseDate,
    voteAverage = voteAverage,
    voteCount = voteCount,
    popularity = popularity,
)

fun GenreDto.toEntity(): GenreEntity = GenreEntity(genreId = id, name = name)

fun GenreEntity.toDomain(): Genre = Genre(id = genreId, name = name)

fun MovieDetailDto.toMovieEntity(): MovieEntity = MovieEntity(
    movieId = id,
    title = title,
    overview = overview,
    posterPath = posterPath,
    backdropPath = backdropPath,
    releaseDate = releaseDate,
    voteAverage = voteAverage,
    voteCount = voteCount,
    popularity = popularity,
)

fun MovieDetailDto.toDetailEntity(): MovieDetailEntity = MovieDetailEntity(
    movieId = id,
    runtime = runtime,
    tagline = tagline,
    originalLanguage = originalLanguage,
    trailerYoutubeKey = videos.results.youtubeTrailerKey(),
)

/** Prefers an official trailer, then any trailer, then any YouTube video at all. */
private fun List<VideoDto>.youtubeTrailerKey(): String? {
    val youtubeVideos = filter { it.site == "YouTube" && it.key.isNotBlank() }
    return youtubeVideos.firstOrNull { it.type == "Trailer" && it.official }?.key
        ?: youtubeVideos.firstOrNull { it.type == "Trailer" }?.key
        ?: youtubeVideos.firstOrNull()?.key
}

fun CastMemberDto.toEntity(movieId: Int): MovieCastEntity = MovieCastEntity(
    movieId = movieId,
    castId = id,
    name = name,
    character = character,
    profilePath = profilePath,
    order = order,
)

fun MovieCastEntity.toDomain(): CastMember = CastMember(
    id = castId,
    name = name,
    character = character,
    profilePath = profilePath,
)

fun PersonDetailDto.toDomain(): PersonDetail = PersonDetail(
    id = id,
    name = name,
    biography = biography,
    birthday = birthday,
    deathday = deathday,
    placeOfBirth = placeOfBirth,
    profilePath = profilePath,
    knownForDepartment = knownForDepartment,
    knownFor = movieCredits.cast
        .sortedByDescending { it.popularity }
        .take(10)
        .map { it.toDomain() },
)

fun PersonKnownForMovieDto.toDomain(): KnownForMovie = KnownForMovie(
    id = id,
    title = title,
    posterPath = posterPath,
    releaseDate = releaseDate,
)
