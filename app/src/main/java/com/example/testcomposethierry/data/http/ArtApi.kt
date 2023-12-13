package com.example.testcomposethierry.data.http

import com.example.testcomposethierry.data.models.DataArtDetailFull
import com.example.testcomposethierry.data.models.DataArtFull
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

// Retrofit interface

interface ArtApi {
    @GET("/api/en/collection")
    suspend fun getArtPaged(@Query("key") key: String, @Query("ps") pageSize: Int, @Query("p") pageOffset: Int): Response<DataArtFull>

    @GET("/api/en/collection/{id}")
    suspend fun getArtObjectDetail(@Path("id") id: String, @Query("key") key: String): Response<DataArtDetailFull>
}
