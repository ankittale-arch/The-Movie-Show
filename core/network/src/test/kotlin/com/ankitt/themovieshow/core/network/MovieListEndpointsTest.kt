package com.ankitt.themovieshow.core.network

import com.ankitt.themovieshow.core.network.api.TmdbApiService
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

/**
 * Covers the list-shaped endpoints on [TmdbApiService] (trending, now playing, popular, discover,
 * upcoming, genres) that [TmdbApiServiceTest] doesn't touch: does each endpoint's raw TMDB JSON
 * deserialize into the right DTO, and does `discoverMovies` build its query params correctly.
 */
class MovieListEndpointsTest {

    private lateinit var mockWebServer: MockWebServer
    private lateinit var apiService: TmdbApiService

    private val moviePageJson = """
        {
          "page": 1,
          "results": [
            {
              "id": 550,
              "title": "Fight Club",
              "overview": "A ticking-time-bomb insomniac.",
              "poster_path": "/poster.jpg",
              "backdrop_path": "/backdrop.jpg",
              "release_date": "1999-10-15",
              "vote_average": 8.4,
              "vote_count": 26280,
              "popularity": 61.4
            }
          ],
          "total_pages": 100,
          "total_results": 2000
        }
    """.trimIndent()

    @Before
    fun setUp() {
        mockWebServer = MockWebServer().apply { start() }

        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor())
            .build()

        val json = Json { ignoreUnknownKeys = true }

        apiService = Retrofit.Builder()
            .baseUrl(mockWebServer.url("/"))
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(TmdbApiService::class.java)
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }

    @Test
    fun `getTrendingMovies parses movie page from response body`() = runTest {
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(moviePageJson))

        val result = apiService.getTrendingMovies()

        assertEquals(1, result.results.size)
        assertEquals(550, result.results[0].id)
        assertEquals("Fight Club", result.results[0].title)
    }

    @Test
    fun `getNowPlayingMovies parses movie page from response body`() = runTest {
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(moviePageJson))

        val result = apiService.getNowPlayingMovies()

        assertEquals(1, result.results.size)
        assertEquals("Fight Club", result.results[0].title)
    }

    @Test
    fun `getPopularMovies parses movie page from response body`() = runTest {
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(moviePageJson))

        val result = apiService.getPopularMovies()

        assertEquals(2000, result.totalResults)
    }

    @Test
    fun `getUpcomingMovies parses movie page from response body`() = runTest {
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(moviePageJson))

        val result = apiService.getUpcomingMovies()

        assertEquals(100, result.totalPages)
    }

    @Test
    fun `discoverMovies parses movie page from response body`() = runTest {
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(moviePageJson))

        val result = apiService.discoverMovies()

        assertEquals(550, result.results[0].id)
    }

    @Test
    fun `discoverMovies sends page sort_by and with_genres as query params`() = runTest {
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(moviePageJson))

        apiService.discoverMovies(page = 3, sortBy = "vote_average.desc", withGenres = 28)

        val recordedRequest = mockWebServer.takeRequest()
        assertEquals(
            "/discover/movie?page=3&sort_by=vote_average.desc&with_genres=28",
            recordedRequest.path,
        )
    }

    @Test
    fun `getMovieGenres parses genre list from response body`() = runTest {
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody(
                    """{"genres":[{"id":28,"name":"Action"},{"id":35,"name":"Comedy"}]}""",
                ),
        )

        val result = apiService.getMovieGenres()

        assertEquals(2, result.genres.size)
        assertEquals("Action", result.genres[0].name)
        assertEquals(35, result.genres[1].id)
    }
}
