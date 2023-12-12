package com.example.testsecuritythierry.data.repositories

import com.example.testsecuritythierry.data.config.AppConfig
import com.example.testsecuritythierry.data.custom_structures.ResultOf
import com.example.testsecuritythierry.data.http.ArtApi
import com.example.testsecuritythierry.data.http.RetrofitHelper
import com.example.testsecuritythierry.data.models.DataArtElement
import com.example.testsecuritythierry.domain.ExtractDataArtUseCase
import javax.inject.Inject

class ArtDataRepository @Inject constructor(
    val extractDataArtUseCase: ExtractDataArtUseCase,
    private val retrofitHelper: RetrofitHelper,
) {

    private lateinit var api: ArtApi
    private var initialized = false
    private val apiKey = AppConfig.apiKey

    private fun createApi() = run {
        api = retrofitHelper.getInstance(
            baseUrl = AppConfig.artBaseUrl,
            okHttpClient = null,
            requestHeaders = null
        ).create(ArtApi::class.java)
    }

    suspend fun getArtPaged(pageSize: Int, pageOffset: Int): ResultOf<List<DataArtElement>> {
        try {
            if (!initialized) {
                initialized = true
                createApi()
            }
            // uncomment to test the error screen
            //if (pageOffset == 2)
            //    return ResultOf.Failure("TEST", null)
            val response = api.getArtPaged(apiKey, pageSize, pageOffset)
            if (response.isSuccessful) {
                response.body()?.let { artFull ->
                    for (o in artFull.artObjects) {
                        if (o.objectNumber == null) {
                            continue
                        }
                        val responseDetail = api.getArtObjectDetail(o.objectNumber, AppConfig.apiKey)
                        if (responseDetail.isSuccessful) {
                            responseDetail.body()?.let { detailFull ->
                                o.detail = detailFull.artObjectPage
                            } ?: return ResultOf.Failure(responseDetail.message(), null)
                        }
                        else {
                            return ResultOf.Failure(responseDetail.message(), null)
                        }
                    }
                    return ResultOf.Success(extractDataArtUseCase(artFull.artObjects))
                }
            }
            return ResultOf.Failure(response.message(), null)
        } catch (e: Exception) {
            return ResultOf.Failure(e.message, e)
        }
    }
}
