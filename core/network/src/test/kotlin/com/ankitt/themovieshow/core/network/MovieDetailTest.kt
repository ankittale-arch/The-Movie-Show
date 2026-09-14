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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

/** Covers `getMovieDetail`, including its nested `credits`/`videos` DTOs and auth header. */
class MovieDetailTest {

    private lateinit var mockWebServer: MockWebServer
    private lateinit var apiService: TmdbApiService

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
    fun `getMovieDetail parses details credits and videos from response body`() = runTest {
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody(
                    """
                    {
                      "id": 550,
                      "title": "Fight Club",
                      "overview": "A ticking-time-bomb insomniac.",
                      "poster_path": "/poster.jpg",
                      "backdrop_path": "/backdrop.jpg",
                      "release_date": "1999-10-15",
                      "runtime": 139,
                      "vote_average": 8.4,
                      "vote_count": 26280,
                      "popularity": 61.4,
                      "tagline": "Mischief. Mayhem. Soap.",
                      "original_language": "en",
                      "genres": [{"id": 18, "name": "Drama"}],
                      "credits": {
                        "cast": [
                          {"id": 819, "name": "Edward Norton", "character": "The Narrator", "profile_path": "/norton.jpg", "order": 0}
                        ]
                      },
                      "videos": {
                        "results": [
                          {"key": "BdJKm16Co6M", "site": "YouTube", "type": "Trailer", "official": true}
                        ]
                      }
                    }
                    """.trimIndent(),
                ),
        )

        val result = apiService.getMovieDetail(movieId = 550)

        assertEquals("Fight Club", result.title)
        assertEquals(139, result.runtime)
        assertEquals(1, result.genres.size)
        assertEquals("Drama", result.genres[0].name)
        assertEquals(1, result.credits.cast.size)
        assertEquals("Edward Norton", result.credits.cast[0].name)
        assertEquals(1, result.videos.results.size)
        assertEquals("BdJKm16Co6M", result.videos.results[0].key)
    }

    @Test
    fun `getMovieDetail sends movie id in path and append_to_response as query param`() = runTest {
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody("""{"id":550,"title":"Fight Club"}"""),
        )

        apiService.getMovieDetail(movieId = 550)

        val recordedRequest = mockWebServer.takeRequest()
        assertEquals("/movie/550?append_to_response=credits%2Cvideos", recordedRequest.path)
    }

    @Test
    fun `getMovieDetail request carries the bearer auth header`() = runTest {
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody("""{"id":550,"title":"Fight Club"}"""),
        )

        apiService.getMovieDetail(movieId = 550)

        val recordedRequest = mockWebServer.takeRequest()
        assertTrue(recordedRequest.getHeader("Authorization")?.startsWith("Bearer") == true)
    }
}
