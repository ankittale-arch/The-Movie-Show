package com.ankitt.themovieshow.feature.moviedetail

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.ui.LocalNavAnimatedContentScope
import coil3.compose.AsyncImage
import com.ankitt.themovieshow.core.designsystem.animation.sharedMovieElement
import com.ankitt.themovieshow.core.designsystem.components.*
import com.ankitt.themovieshow.core.designsystem.theme.TheMovieShowTheme
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView

@Composable
fun MovieDetailScreen(
    movieId: Int,
    onBackClick: () -> Unit,
    viewModel: MovieDetailViewModel = hiltViewModel(),
) {
    LaunchedEffect(movieId) {
        viewModel.load(movieId)
    }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    MovieDetailContent(
        uiState = uiState,
        onBackClick = onBackClick,
        onFavoriteClick = viewModel::toggleFavorite,
        onWatchlistClick = viewModel::toggleWatchlist,
    )
}

@Composable
private fun MovieDetailContent(
    uiState: MovieDetailUiState,
    onBackClick: () -> Unit,
    onFavoriteClick: () -> Unit = {},
    onWatchlistClick: () -> Unit = {},
) {
    Scaffold(contentWindowInsets = WindowInsets.safeDrawing) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            val movie = uiState.movie
            val errorMessage = uiState.errorMessage
            when {
                uiState.isLoading && movie == null ->
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))

                errorMessage != null && movie == null -> Text(
                    text = errorMessage,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(24.dp),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.error,
                )

                movie == null -> Text(
                    text = "Movie not found",
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(24.dp),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                else -> {
                    var selectedCastId by rememberSaveable { mutableStateOf<Int?>(null) }
                    MovieDetailBody(
                        movie = movie,
                        isOffline = uiState.isOffline,
                        isStale = uiState.isStale,
                        lastSyncedAtEpochMillis = uiState.lastSyncedAtEpochMillis,
                        onBackClick = onBackClick,
                        onCastMemberClick = { selectedCastId = it },
                    )
                    val castId = selectedCastId
                    if (castId != null) {
                        PersonBioSheet(personId = castId, onDismissRequest = { selectedCastId = null })
                    }
                }
            }

            // Floats over the backdrop regardless of which branch above is showing, so back
            // navigation still works while the detail is loading or failed.
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                DetailIconButton(onClick = onBackClick) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
                if (movie != null) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        DetailIconButton(onClick = onFavoriteClick) {
                            Icon(
                                imageVector = if (uiState.isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                                contentDescription = "Favorite",
                            )
                        }
                        DetailIconButton(onClick = onWatchlistClick) {
                            Icon(
                                imageVector = if (uiState.isInWatchlist) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder,
                                contentDescription = "Watchlist",
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailIconButton(onClick: () -> Unit, content: @Composable () -> Unit) {
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(Color.Black.copy(alpha = 0.35f)),
    ) {
        CompositionLocalProvider(
            LocalContentColor provides Color.White,
            content = content,
        )
    }
}

@Composable
private fun MovieDetailBody(
    movie: MovieDetailUi,
    isOffline: Boolean,
    isStale: Boolean,
    lastSyncedAtEpochMillis: Long?,
    onBackClick: () -> Unit,
    onCastMemberClick: (Int) -> Unit = {},
) {
    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
        AsyncImage(
            model = movie.backdropUrl,
            contentDescription = movie.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f),
        )

        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.TopCenter) {
            AsyncImage(
                model = movie.posterUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .offset(y = (-56).dp)
                    .width(140.dp)
                    .aspectRatio(2f / 3f)
                    .clip(RoundedCornerShape(12.dp))
                    .sharedMovieElement(
                        key = "movie-poster-${movie.id}",
                        animatedVisibilityScope = LocalNavAnimatedContentScope.current,
                    ),
            )
        }

        SyncStatusBanner(
            isOffline = isOffline,
            isStale = isStale,
            lastSyncedAtEpochMillis = lastSyncedAtEpochMillis,
            modifier = Modifier.fillMaxWidth(),
        )

        Text(
            text = movie.title,
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(top = 8.dp),
        )

        if (movie.genres.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
            ) {
                movie.genres.forEach { genre -> Chip(text = genre) }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(top = 20.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            InfoColumn(label = "Release Date", value = movie.releaseDate ?: "—")
            InfoColumn(label = "Duration", value = movie.durationText ?: "—")
            InfoColumn(label = "Rating") {
                RatingBadge(rating = movie.rating)
            }
            InfoColumn(label = "Language", value = movie.language ?: "—")
        }

        val tagline = movie.tagline
        if (tagline != null) {
            Text(
                text = tagline,
                style = MaterialTheme.typography.bodyLarge.copy(fontStyle = FontStyle.Italic),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(top = 20.dp),
            )
        }

        val trailerYoutubeKey = movie.trailerYoutubeKey
        if (trailerYoutubeKey != null) {
            TrailerSection(
                youtubeKey = trailerYoutubeKey,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(top = 20.dp),
            )
        }

        Text(
            text = movie.overview,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(top = 12.dp),
        )

        if (movie.cast.isNotEmpty()) {
            Text(
                text = "Cast",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(start = 24.dp, top = 24.dp, bottom = 8.dp),
            )
            LazyRow(
                contentPadding = PaddingValues(horizontal = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                items(movie.cast, key = { it.id }) { member ->
                    CastItem(member = member, onClick = { onCastMemberClick(member.id) })
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun InfoColumn(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = 4.dp),
        )
    }
}

@Composable
private fun InfoColumn(label: String, content: @Composable () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Box(modifier = Modifier.padding(top = 4.dp)) {
            content()
        }
    }
}

@Composable
private fun CastItem(member: CastMemberUi, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .width(72.dp)
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        AsyncImage(
            model = member.profileUrl,
            contentDescription = member.name,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape),
        )
        Text(
            text = member.name,
            style = MaterialTheme.typography.labelLarge,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 6.dp),
        )
    }
}

/**
 * A trailer thumbnail with a play button; tapping it swaps in a [YoutubeEmbeddedPlayer] in place,
 * so the WebView (and the network request it makes) is only created once the user actually asks
 * to watch it.
 */
@Composable
private fun TrailerSection(youtubeKey: String, modifier: Modifier = Modifier) {
    var isPlaying by rememberSaveable(youtubeKey) { mutableStateOf(false) }

    Box(
        modifier = modifier
            .aspectRatio(16f / 9f)
            .clip(RoundedCornerShape(12.dp))
            .background(Color.Black),
    ) {
        if (isPlaying) {
            YoutubeEmbeddedPlayer(youtubeKey = youtubeKey, modifier = Modifier.fillMaxSize())
        } else {
            AsyncImage(
                model = "https://img.youtube.com/vi/$youtubeKey/hqdefault.jpg",
                contentDescription = "Play trailer",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .clickable { isPlaying = true },
            )
            Icon(
                imageVector = Icons.Filled.PlayArrow,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.45f))
                    .padding(8.dp),
            )
        }
    }
}

/**
 * Wraps the YouTube IFrame Player API via android-youtube-player rather than a hand-rolled
 * WebView + `<iframe>`: it manages the JS bridge, lifecycle (pause on background, release on
 * dispose) and play/pause state itself, so playback starts reliably on the first tap instead of
 * needing a second tap on YouTube's own paused thumbnail.
 */
@Composable
private fun YoutubeEmbeddedPlayer(youtubeKey: String, modifier: Modifier = Modifier) {
    val lifecycleOwner = LocalLifecycleOwner.current
    AndroidView(
        modifier = modifier,
        factory = { context ->
            YouTubePlayerView(context).apply {
                lifecycleOwner.lifecycle.addObserver(this)
                addYouTubePlayerListener(object : AbstractYouTubePlayerListener() {
                    override fun onReady(youTubePlayer: YouTubePlayer) {
                        youTubePlayer.loadVideo(youtubeKey, 0f)
                    }
                })
            }
        },
        onRelease = { it.release() },
    )
}

@Preview(showBackground = true)
@Composable
private fun MovieDetailContentPreview() {
    TheMovieShowTheme {
        MovieDetailContent(uiState = MovieDetailUiState(), onBackClick = {})
    }
}
