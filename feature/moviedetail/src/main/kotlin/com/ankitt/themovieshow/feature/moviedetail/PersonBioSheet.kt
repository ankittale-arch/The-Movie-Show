package com.ankitt.themovieshow.feature.moviedetail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp as lerpColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.ankitt.themovieshow.core.designsystem.theme.TheMovieShowTheme

private val ExpandedBannerHeight = 220.dp
private val CollapsedBannerHeight = 56.dp
private val CollapsedAvatarSize = 36.dp
private val ExpandedAvatarSize = 152.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonBioSheet(
    personId: Int,
    onDismissRequest: () -> Unit,
    viewModel: PersonDetailViewModel = hiltViewModel(),
) {
    LaunchedEffect(personId) { viewModel.load(personId) }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        contentWindowInsets = { WindowInsets(0) },
    ) {
        PersonBioSheetContent(uiState = uiState, onCloseClick = onDismissRequest)
    }
}

@Composable
private fun PersonBioSheetContent(uiState: PersonDetailUiState, onCloseClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 420.dp, max = 640.dp),
    ) {
        val person = uiState.person
        val errorMessage = uiState.errorMessage
        when {
            uiState.isLoading && person == null ->
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))

            errorMessage != null && person == null -> Text(
                text = errorMessage,
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(24.dp),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.error,
            )

            person != null -> PersonBioBody(person = person, onCloseClick = onCloseClick)
        }

        // Floats over the banner regardless of which branch above is showing (loading/error too),
        // same convention as MovieDetailScreen's back button, so closing always works.
        if (person == null) {
            CloseButton(onClick = onCloseClick, tint = Color.White, modifier = Modifier.align(Alignment.TopStart))
        }
    }
}

/**
 * The banner photo and the sticky header's avatar are the same [person] image at two sizes —
 * scrolling interpolates the banner down to [CollapsedBannerHeight] and fades the header avatar
 * in over it, so the photo visibly "becomes" the small circular avatar rather than being swapped.
 */
@Composable
private fun PersonBioBody(person: PersonDetailUi, onCloseClick: () -> Unit) {
    val scrollState = rememberScrollState()
    val density = LocalDensity.current
    val maxCollapseRangePx = with(density) { (ExpandedBannerHeight - CollapsedBannerHeight).toPx() }
    val progress by remember {
        derivedStateOf {
            // Capped by the content's actual scrollable distance, not just maxCollapseRangePx —
            // with little bio/known-for content there may be less to scroll than that fixed
            // range, and without the cap the collapse would stall part-way at the scroll end
            // (banner stuck mid-shrink, header avatar/name faded in over the still-large name
            // text below it) instead of completing smoothly.
            val range = minOf(maxCollapseRangePx, scrollState.maxValue.toFloat())
            if (range <= 0f) 0f else (scrollState.value / range).coerceIn(0f, 1f)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.verticalScroll(scrollState)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(lerp(ExpandedBannerHeight, CollapsedBannerHeight, progress)),
                contentAlignment = Alignment.Center,
            ) {
                AsyncImage(
                    model = person.profileUrl,
                    contentDescription = person.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(lerp(ExpandedAvatarSize, CollapsedAvatarSize, progress))
                        .clip(RoundedCornerShape(percent = (progress * 50).toInt())),
                )
            }

            Text(
                text = person.name,
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .alpha(1f - progress),
            )

            val meta = person.meta
            if (meta != null) {
                Text(
                    text = meta,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 4.dp)
                        .alpha(1f - progress),
                )
            }

            Text(
                text = person.biography,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(top = 12.dp),
            )

            if (person.knownFor.isNotEmpty()) {
                Text(
                    text = "Known For",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(start = 24.dp, top = 24.dp, bottom = 8.dp),
                )
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    items(person.knownFor, key = { it.id }) { movie -> KnownForItem(movie) }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        // Sticky header: transparent over the expanded banner, solid once collapsed — the close
        // button stays in the same spot throughout, the avatar/name fade in as the banner shrinks.
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .height(CollapsedBannerHeight)
                .background(MaterialTheme.colorScheme.surface.copy(alpha = progress))
                .padding(horizontal = 8.dp),
        ) {
            CloseButton(
                onClick = onCloseClick,
                tint = lerpColor(Color.White, MaterialTheme.colorScheme.onSurface, progress),
                dimBackground = 1f - progress,
            )
            AsyncImage(
                model = person.profileUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .padding(start = 8.dp)
                    .size(32.dp)
                    .clip(CircleShape)
                    .alpha(progress),
            )
            Text(
                text = person.name,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .padding(start = 10.dp)
                    .alpha(progress),
            )
        }
    }
}

@Composable
private fun CloseButton(
    onClick: () -> Unit,
    tint: Color,
    modifier: Modifier = Modifier,
    dimBackground: Float = 1f,
) {
    IconButton(
        onClick = onClick,
        modifier = modifier
            .padding(12.dp)
            .size(36.dp)
            .clip(CircleShape)
            .background(Color.Black.copy(alpha = 0.35f * dimBackground)),
    ) {
        Icon(imageVector = Icons.Filled.Close, contentDescription = "Close", tint = tint)
    }
}

@Composable
private fun KnownForItem(movie: KnownForMovieUi) {
    Column(modifier = Modifier.width(104.dp)) {
        AsyncImage(
            model = movie.posterUrl,
            contentDescription = movie.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .width(104.dp)
                .aspectRatio(2f / 3f)
                .clip(RoundedCornerShape(12.dp)),
        )
        Text(
            text = movie.title,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(top = 6.dp),
        )
        val year = movie.year
        if (year != null) {
            Text(
                text = year,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PersonBioSheetContentPreview() {
    TheMovieShowTheme {
        PersonBioSheetContent(
            uiState = PersonDetailUiState(
                person = PersonDetailUi(
                    id = 1,
                    name = "Cillian Murphy",
                    biography = "Irish actor known for restrained, intense performances.",
                    meta = "Acting · Cork, Ireland",
                    profileUrl = null,
                    knownFor = emptyList(),
                ),
            ),
            onCloseClick = {},
        )
    }
}
