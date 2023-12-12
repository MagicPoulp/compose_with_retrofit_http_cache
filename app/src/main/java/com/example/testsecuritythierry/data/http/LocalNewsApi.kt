package com.example.testsecuritythierry.data.http

// Retrofit interface

import com.example.testsecuritythierry.data.models.DataArtFull
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface LocalArtApi {
    @GET("/news")
    suspend fun getArtPaged(@Query("pageSize") pageSize: Int, @Query("pageOffset") pageOffset: Int): Response<DataArtFull>
}
