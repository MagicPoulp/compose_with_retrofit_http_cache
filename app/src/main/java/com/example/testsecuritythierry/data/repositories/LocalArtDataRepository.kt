package com.example.testsecuritythierry.data.repositories

import com.example.testsecuritythierry.data.config.AppConfig
import com.example.testsecuritythierry.data.custom_structures.ResultOf
import com.example.testsecuritythierry.data.http.LocalArtApi
import com.example.testsecuritythierry.data.http.RetrofitHelper
import com.example.testsecuritythierry.data.http.UnsafeOkHttpClient
import com.example.testsecuritythierry.data.models.DataArtElement
import javax.inject.Inject

class LocalArtDataRepository @Inject constructor() {

    private lateinit var api: LocalArtApi
    private var initialized = false

    private fun createApi() = run {
        api = RetrofitHelper.getInstance(
            baseUrl = AppConfig.localArtBaseUrl,
            okHttpClient = UnsafeOkHttpClient.unsafeOkHttpClient,
            requestHeaders = listOf(Pair(first = "Accept", second = "application/json"))
        ).create(LocalArtApi::class.java)
    }

    suspend fun getArtPaged(pageSize: Int, pageOffset: Int): ResultOf<List<DataArtElement>> {
        try {
            if (!initialized) {
                initialized = true
                createApi()
            }
            val response = api.getArtPaged(pageSize, pageOffset)
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
