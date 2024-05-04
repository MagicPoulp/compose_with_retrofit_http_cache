package com.example.testcomposethierry.data.http

import com.example.testcomposethierry.data.models.JsonDataUsersListFull
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

// Retrofit interface

// doc:
// https://randomuser.me/documentation
// example of use:
// https://randomuser.me/api/?inc=name,email&results=10&page=1&seed=merlin
interface UsersApi {
    @GET("/api/")
    suspend fun getUsersListPaged(@Query("inc") inc: String, @Query("results") pageSize: Int, @Query("page") pageOffset: Int, @Query("seed") seed: String): Response<JsonDataUsersListFull>
}
