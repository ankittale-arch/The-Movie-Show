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

/**
 * Covers `searchMovies`: response parsing, the `query`/`page`/`include_adult` params it builds,
 * and that it too carries the auth header (proving [AuthInterceptor] applies globally, not just
 * to the one endpoint [TmdbApiServiceTest] checks).
 */
class SearchMoviesTest {

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
    fun `searchMovies parses movie page from response body`() = runTest {
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody(
                    """
                    {
                      "page": 1,
                      "results": [
                        {"id": 603, "title": "The Matrix"}
                      ],
                      "total_pages": 1,
                      "total_results": 1
                    }
                    """.trimIndent(),
                ),
        )

        val result = apiService.searchMovies(query = "matrix")

        assertEquals(1, result.results.size)
        assertEquals("The Matrix", result.results[0].title)
    }

    @Test
    fun `searchMovies sends query page and include_adult as query params`() = runTest {
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody("""{"page":1,"results":[]}"""),
        )

        apiService.searchMovies(query = "the matrix", page = 2, includeAdult = true)

        val recordedRequest = mockWebServer.takeRequest()
        assertEquals(
            "/search/movie?query=the%20matrix&page=2&include_adult=true",
            recordedRequest.path,
        )
    }

    @Test
    fun `searchMovies request carries the bearer auth header`() = runTest {
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody("""{"page":1,"results":[]}"""),
        )

        apiService.searchMovies(query = "matrix")

        val recordedRequest = mockWebServer.takeRequest()
        assertTrue(recordedRequest.getHeader("Authorization")?.startsWith("Bearer") == true)
    }
}
