package com.example.testsecuritythierry.data.http

// Retrofit interface

import com.example.testsecuritythierry.data.models.DataNewsFull
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface LocalNewsApi {
    @GET("/news")
    suspend fun getNewsPaged(@Query("pageSize") pageSize: Int, @Query("pageOffset") pageOffset: Int): Response<DataNewsFull>
}
