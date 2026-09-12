package com.ankitt.themovieshow.core.designsystem.animation

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier

/**
 * Provided once, at the nav host (`SharedTransitionLayout { ... }`), so any screen — regardless of
 * which feature module it lives in — can opt a poster image into the movie-poster shared-element
 * transition without every module needing to know about the nav host itself.
 */
val LocalSharedTransitionScope = compositionLocalOf<SharedTransitionScope?> { null }

/**
 * Applies a shared-element transition keyed by [key] when both a [LocalSharedTransitionScope] and
 * [animatedVisibilityScope] (pass Nav3's own `LocalNavAnimatedContentScope.current`, read at the
 * call site since it's owned by the navigation library, not this design-system module) are
 * available. No-ops otherwise, so previews and any screen composed outside the nav host still
 * render normally instead of crashing on a null scope.
 */
@Composable
fun Modifier.sharedMovieElement(key: String, animatedVisibilityScope: AnimatedVisibilityScope?): Modifier {
    val sharedTransitionScope = LocalSharedTransitionScope.current
    return if (sharedTransitionScope != null && animatedVisibilityScope != null) {
        with(sharedTransitionScope) {
            this@sharedMovieElement.sharedElement(
                sharedContentState = rememberSharedContentState(key = key),
                animatedVisibilityScope = animatedVisibilityScope,
            )
        }
    } else {
        this
    }
}
