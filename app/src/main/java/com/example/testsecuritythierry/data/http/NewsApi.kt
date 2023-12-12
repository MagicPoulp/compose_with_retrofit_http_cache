package com.example.testsecuritythierry.data.http

// Retrofit interface

import com.example.testsecuritythierry.data.models.DataArtFull
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface ArtApi {
    // https://aec.lemonde.fr/ws/8/mobile/www/android-phone/en_continu/index.json
    @GET("/api/nl/collection")
    suspend fun getArt(@Query("key") key: String): Response<DataArtFull>
}
