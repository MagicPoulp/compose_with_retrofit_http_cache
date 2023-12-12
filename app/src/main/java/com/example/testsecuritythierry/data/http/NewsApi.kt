package com.example.testsecuritythierry.data.http

// Retrofit interface

import com.example.testsecuritythierry.data.models.DataNewsFull
import retrofit2.Response
import retrofit2.http.GET

interface NewsApi {
    // https://aec.lemonde.fr/ws/8/mobile/www/android-phone/en_continu/index.json
    @GET("/ws/8/mobile/www/android-phone/en_continu/index.json")
    suspend fun getNews(): Response<DataNewsFull>
}
