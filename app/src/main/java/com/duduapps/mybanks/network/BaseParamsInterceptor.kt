package com.duduapps.mybanks.network

import com.duduapps.mybanks.BuildConfig
import com.duduapps.mybanks.data.repository.PreferencesRepository
import okhttp3.Interceptor
import okhttp3.Response

class BaseParamsInterceptor(
    private val preferencesRepository: PreferencesRepository,
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val originalUrl = original.url

        val urlBuilder = originalUrl.newBuilder()
            .addQueryParameter("identifier", preferencesRepository.deviceId)
            .addQueryParameter("identifier_old", preferencesRepository.deviceIdOld)
            .addQueryParameter("version", BuildConfig.VERSION_CODE.toString())
            .addQueryParameter("platform", "android")
            .addQueryParameter("debug", if (BuildConfig.DEBUG) "1" else "0")
            .addQueryParameter("api_v", "8")

        val requestBuilder = original.newBuilder().url(urlBuilder.build())
        return chain.proceed(requestBuilder.build())
    }
}
