package com.ankitt.themovieshow.core.data

/**
 * `listKey`/`resource` string constants shared by [MovieRepository]'s Room queries and the
 * `SyncStateEntity` row written for each list, so both tables agree on the same key with no
 * translation layer.
 */
object HomeListKeys {
    const val TRENDING = "trending_day"
    const val NOW_PLAYING = "now_playing"
    const val POPULAR = "popular"
    const val DISCOVER = "discover_popular"
    const val UPCOMING = "upcoming"
    const val GENRES = "genres"

    /**
     * Always-overwritten single slot for the current search box contents — not one row per
     * query, so this can't grow unbounded. That also means only the *last* search stays cached
     * for offline viewing, not a full history; a query-history feature would need its own entity.
     */
    const val SEARCH = "search"

    private const val GENRE_PREFIX = "genre_"

    /**
     * One row per genre (bounded — TMDB has ~19 movie genres, unlike [SEARCH]'s unbounded query
     * space — so a dedicated row per genre id is safe to keep around rather than a single
     * always-overwritten slot).
     */
    fun genre(genreId: Int) = "$GENRE_PREFIX$genreId"

    /** Reverses [genre], for code (e.g. [MovieRepository]'s refresh dispatch) that only has the
     * string key and needs the id back. Null if [listKey] isn't a genre key. */
    fun genreIdOrNull(listKey: String): Int? =
        listKey.takeIf { it.startsWith(GENRE_PREFIX) }?.removePrefix(GENRE_PREFIX)?.toIntOrNull()
}
