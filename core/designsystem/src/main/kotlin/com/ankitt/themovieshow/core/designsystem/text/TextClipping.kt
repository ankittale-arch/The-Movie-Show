package com.ankitt.themovieshow.core.designsystem.text

private val WHITESPACE = Regex("\\s+")

/**
 * Clips to the first [maxWords] words, appending an ellipsis if anything was cut. Movie titles
 * come straight from TMDB with no length guarantee — this keeps the odd very-long one from
 * blowing out a poster card's layout, independent of (and in addition to) a Text's own
 * `maxLines`/`TextOverflow` line-wrapping.
 */
fun String.clipToWords(maxWords: Int = 15): String {
    val words = trim().split(WHITESPACE)
    if (words.size <= maxWords) return this
    return words.take(maxWords).joinToString(" ") + "…"
}
