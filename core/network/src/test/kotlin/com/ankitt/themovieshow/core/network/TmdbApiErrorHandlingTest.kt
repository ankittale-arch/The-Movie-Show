package com.ankitt.themovieshow.core.network

import com.ankitt.themovieshow.core.network.api.TmdbApiService
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

/**
 * [TmdbApiService] methods are plain Retrofit suspend functions with no custom response wrapper
 * or error-mapping layer in `core:network` — any unwrapping/retry logic lives above this module.
 * That means the two failure boundaries this module is actually responsible for are: (1) Retrofit
 * turning a non-2xx response into [HttpException], and (2) kotlinx.serialization refusing to
 * silently invent values for a required field that's missing from the body.
 */
class TmdbApiErrorHandlingTest {

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
    fun `a 404 response throws HttpException with the status code`() {
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(404)
                .setBody("""{"status_code":34,"status_message":"The resource you requested could not be found."}"""),
        )

        val exception = assertThrows(HttpException::class.java) {
            runBlocking { apiService.getMovieDetail(movieId = 999_999) }
        }

        assertEquals(404, exception.code())
    }

    @Test
    fun `a 500 response throws HttpException with the status code`() {
        mockWebServer.enqueue(MockResponse().setResponseCode(500).setBody("Internal Server Error"))

        val exception = assertThrows(HttpException::class.java) {
            runBlocking { apiService.getTrendingMovies() }
        }

        assertEquals(500, exception.code())
    }

    @Test
    fun `a movie missing the required title field fails to parse with SerializationException`() {
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody(
                    """
                    {
                      "page": 1,
                      "results": [
                        {"id": 550, "overview": "Missing its title field entirely."}
                      ]
                    }
                    """.trimIndent(),
                ),
        )

        assertThrows(SerializationException::class.java) {
            runBlocking { apiService.getPopularMovies() }
        }
    }

    @Test
    fun `a person detail missing the required id field fails to parse with SerializationException`() {
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody("""{"name": "Edward Norton"}"""),
        )

        assertThrows(SerializationException::class.java) {
            runBlocking { apiService.getPersonDetail(personId = 819) }
        }
    }
}
