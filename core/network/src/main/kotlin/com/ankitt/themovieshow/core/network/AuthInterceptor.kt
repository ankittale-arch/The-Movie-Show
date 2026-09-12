package com.ankitt.themovieshow.core.network

import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

/**
 * Attaches the TMDB v4 Bearer read-access token to every request. Using the v4 token (instead of
 * appending `?api_key=...` to every URL, as the legacy v3 auth does) keeps the credential out of
 * request URLs entirely, so it never ends up in logs, screenshots of logcat, or a proxy's access
 * log.
 */
class AuthInterceptor @Inject constructor() : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request().newBuilder()
            .addHeader("Authorization", "Bearer ${BuildConfig.TMDB_READ_ACCESS_TOKEN}")
            .addHeader("Accept", "application/json")
            .build()
        return chain.proceed(request)
    }
}
