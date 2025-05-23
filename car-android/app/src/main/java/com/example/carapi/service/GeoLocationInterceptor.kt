package com.example.carapi.service

import com.example.carapi.database.dao.UserLocationDao
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response

class GeoLocationInterceptor(private val userLocationDao: UserLocationDao): Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val userLastLocation = runBlocking {
            userLocationDao.getLastLocation()
        }

        val originalRequest: Request = chain.request()

        val newRequest = userLastLocation?.let {
            originalRequest.newBuilder()
                .addHeader("x-data-latitude", userLastLocation.latitude.toString())
                .addHeader("x-data-longitude", userLastLocation.longitude.toString())
                .build()
        } ?: originalRequest
        return chain.proceed(newRequest)
    }
}