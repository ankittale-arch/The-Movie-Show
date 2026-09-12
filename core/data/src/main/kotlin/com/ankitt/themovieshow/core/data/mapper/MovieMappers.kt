package com.ankitt.themovieshow.core.data.mapper

import com.ankitt.themovieshow.core.data.model.Genre
import com.ankitt.themovieshow.core.data.model.Movie
import com.ankitt.themovieshow.core.database.movie.GenreEntity
import com.ankitt.themovieshow.core.database.movie.MovieEntity
import com.ankitt.themovieshow.core.network.model.GenreDto
import com.ankitt.themovieshow.core.network.model.MovieDto

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
