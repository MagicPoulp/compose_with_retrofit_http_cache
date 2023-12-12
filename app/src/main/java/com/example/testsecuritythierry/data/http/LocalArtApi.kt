package com.example.testsecuritythierry.data.http

import com.example.testsecuritythierry.data.models.DataArtFull
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

// Retrofit interface

interface LocalArtApi {
    @GET("/api/en/collection")
    suspend fun getArtPaged(@Query("key") key: String, @Query("ps") pageSize: Int, @Query("p") pageOffset: Int): Response<DataArtFull>
}
