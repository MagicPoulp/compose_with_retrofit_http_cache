package com.example.testcomposethierry.data.repositories

import com.example.testcomposethierry.data.config.AppConfig
import com.example.testcomposethierry.data.custom_structures.ResultOf
import com.example.testcomposethierry.data.http.ArtApi
import com.example.testcomposethierry.data.http.RetrofitHelper
import com.example.testcomposethierry.data.models.DataArtDetail
import com.example.testcomposethierry.data.models.DataArtElement
import com.example.testcomposethierry.domain.ExtractDataArtUseCase
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
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
            // chaining the 2 API calls makes things very simple
            // but it slows down the display of the list
            // we could have a parallel coroutine that tries to prefetch
            // data, and clicking forces the fetching
            val response = api.getArtPaged(apiKey, pageSize, pageOffset)
            if (response.isSuccessful) {
                response.body()?.let { artFull ->
                    for (o in artFull.artObjects) {
                        if (o.objectNumber == null) {
                            continue
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

    suspend fun getArtObjectDetail(objectNumber: String): ResultOf<DataArtDetail> {
        if (!initialized) {
            initialized = true
            createApi()
        }
        val response = api.getArtObjectDetail(objectNumber, AppConfig.apiKey)
        if (response.isSuccessful) {
            response.body()?.artObjectPage?.let { artObjectPage ->
                return ResultOf.Success(artObjectPage)
            }
        }
        return ResultOf.Failure(response.message(), null)
    }

    fun onDestroy() {
        retrofitHelper.onDestroy()
    }
}
