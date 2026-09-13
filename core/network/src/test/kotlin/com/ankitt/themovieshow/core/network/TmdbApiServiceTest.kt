package com.ankitt.themovieshow.core.network

import com.ankitt.themovieshow.core.network.api.TmdbApiService
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.OkHttpClient
import org.junit.*
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

/**
 * Verifies the network layer end to end at the boundary that matters: given raw TMDB JSON on the
 * wire, does [TmdbApiService] produce the right DTO, and does every request carry the auth
 * header. This is a network-layer test, not a "does Retrofit work" test — Retrofit/OkHttp are
 * trusted libraries; what we own and must verify is [AuthInterceptor] and the DTOs.
 */
class TmdbApiServiceTest {

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
    fun `getApiConfiguration parses images configuration from response body`() = runTest {
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody(
                    """
                    {
                      "images": {
                        "secure_base_url": "https://image.tmdb.org/t/p/",
                        "poster_sizes": ["w92", "w154", "w500"],
                        "backdrop_sizes": ["w300", "w780"],
                        "profile_sizes": ["w45", "w185"]
                      }
                    }
                    """.trimIndent(),
                ),
        )

        val result = apiService.getApiConfiguration()

        assertEquals("https://image.tmdb.org/t/p/", result.images.secureBaseUrl)
        assertEquals(listOf("w92", "w154", "w500"), result.images.posterSizes)
    }

    @Test
    fun `every request carries the bearer auth header`() = runTest {
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody(
                    """{"images":{"secure_base_url":"x","poster_sizes":[],"backdrop_sizes":[],"profile_sizes":[]}}""",
                ),
        )

        apiService.getApiConfiguration()

        val recordedRequest = mockWebServer.takeRequest()
        // OkHttp trims header values, so an empty local dev token (see local.properties)
        // collapses "Bearer " to "Bearer" — assert the scheme, not a literal trailing space.
        assertTrue(recordedRequest.getHeader("Authorization")?.startsWith("Bearer") == true)
    }
}
