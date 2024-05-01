package com.example.testcomposethierry.data.http

import com.example.testcomposethierry.data.models.DataArtDetailFull
import com.example.testcomposethierry.data.models.DataArtFull
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

// Retrofit interface

// doc:
// https://randomuser.me/documentation
// example of use:
// https://randomuser.me/api/?inc=name,email&results=10&page=1&seed=merlin
interface ArtApi {
    @GET("/api/en/collection")
    suspend fun getArtPaged(@Query("key") key: String, @Query("ps") pageSize: Int, @Query("p") pageOffset: Int): Response<DataArtFull>

    @GET("/api/en/collection/{id}")
    suspend fun getArtObjectDetail(@Path("id") id: String, @Query("key") key: String): Response<DataArtDetailFull>
}
