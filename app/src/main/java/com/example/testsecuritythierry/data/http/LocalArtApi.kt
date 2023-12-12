package com.example.testsecuritythierry.data.http

import com.example.testsecuritythierry.data.models.DataArtFull
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

// Retrofit interface

interface LocalArtApi {
    @GET("/news")
    suspend fun getArtPaged(@Query("pageSize") pageSize: Int, @Query("pageOffset") pageOffset: Int): Response<DataArtFull>
}
