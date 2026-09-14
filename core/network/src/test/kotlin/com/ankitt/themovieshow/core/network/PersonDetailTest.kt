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

/** Covers `getPersonDetail`, including its nested `movie_credits` DTO and auth header. */
class PersonDetailTest {

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
    fun `getPersonDetail parses bio and movie credits from response body`() = runTest {
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody(
                    """
                    {
                      "id": 819,
                      "name": "Edward Norton",
                      "biography": "Edward Harrison Norton is an American actor.",
                      "birthday": "1969-08-18",
                      "deathday": null,
                      "place_of_birth": "Boston, Massachusetts, USA",
                      "profile_path": "/norton.jpg",
                      "known_for_department": "Acting",
                      "popularity": 25.3,
                      "movie_credits": {
                        "cast": [
                          {"id": 550, "title": "Fight Club", "poster_path": "/poster.jpg", "release_date": "1999-10-15", "popularity": 61.4}
                        ]
                      }
                    }
                    """.trimIndent(),
                ),
        )

        val result = apiService.getPersonDetail(personId = 819)

        assertEquals("Edward Norton", result.name)
        assertEquals("Boston, Massachusetts, USA", result.placeOfBirth)
        assertEquals(1, result.movieCredits.cast.size)
        assertEquals("Fight Club", result.movieCredits.cast[0].title)
    }

    @Test
    fun `getPersonDetail request carries the bearer auth header`() = runTest {
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody("""{"id":819,"name":"Edward Norton"}"""),
        )

        apiService.getPersonDetail(personId = 819)

        val recordedRequest = mockWebServer.takeRequest()
        assertTrue(recordedRequest.getHeader("Authorization")?.startsWith("Bearer") == true)
    }
}
