package com.example.testsecuritythierry.data.repositories

import com.example.testsecuritythierry.data.config.AppConfig
import com.example.testsecuritythierry.data.custom_structures.ResultOf
import com.example.testsecuritythierry.data.http.LocalNewsApi
import com.example.testsecuritythierry.data.http.RetrofitHelper
import com.example.testsecuritythierry.data.http.UnsafeOkHttpClient
import com.example.testsecuritythierry.data.models.DataNewsElement
import javax.inject.Inject

class LocalNewsDataRepository @Inject constructor() {

    private lateinit var api: LocalNewsApi
    private var initialized = false

    private fun createApi() = run {
        api = RetrofitHelper.getInstance(
            baseUrl = AppConfig.localNewsBaseUrl,
            okHttpClient = UnsafeOkHttpClient.unsafeOkHttpClient,
            requestHeaders = listOf(Pair(first = "Accept", second = "application/json"))
        ).create(LocalNewsApi::class.java)
    }

    suspend fun getNewsPaged(pageSize: Int, pageOffset: Int): ResultOf<List<DataNewsElement>> {
        try {
            if (!initialized) {
                initialized = true
                createApi()
            }
            val response = api.getNewsPaged(pageSize, pageOffset)
            if (response.isSuccessful) {
                response.body()?.let {
                    return ResultOf.Success(it.artObjects)
                }
            }
            return ResultOf.Failure(response.message(), null)
        } catch (e: Exception) {
            return ResultOf.Failure(e.message, e)
        }
    }
}
