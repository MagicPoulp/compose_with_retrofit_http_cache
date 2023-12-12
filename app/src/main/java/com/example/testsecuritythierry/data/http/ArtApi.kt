package com.example.testsecuritythierry.data.http

import com.example.testsecuritythierry.data.models.DataArtDetailFull
import com.example.testsecuritythierry.data.models.DataArtFull
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

// Retrofit interface

interface ArtApi {
    // https://aec.lemonde.fr/ws/8/mobile/www/android-phone/en_continu/index.json
    @GET("/api/en/collection")
    suspend fun getArt(@Query("key") key: String, @Query("p") p: Int, @Query("ps") ps: Int): Response<DataArtFull>

    @GET("/api/en/collection/{id}")
    suspend fun getArtObjectDetail(@Path("id") id: String, @Query("key") key: String): Response<DataArtDetailFull>
}
