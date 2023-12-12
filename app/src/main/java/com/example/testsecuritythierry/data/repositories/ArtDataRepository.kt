package com.example.testsecuritythierry.data.repositories

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.example.testsecuritythierry.data.config.AppConfig
import com.example.testsecuritythierry.data.http.ArtApi
import com.example.testsecuritythierry.data.http.RetrofitHelper
import com.example.testsecuritythierry.data.models.DataArtElement
import com.example.testsecuritythierry.domain.ExtractDataArtPrefetchUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted.Companion.WhileSubscribed
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

class ArtDataRepository @Inject constructor(
    val extractDataArtPrefetchUseCase: ExtractDataArtPrefetchUseCase
) {

    private lateinit var api: ArtApi
    private var initialized = false
    private val apiKey = AppConfig.apiKey

    private fun createApi() = run {
        api = RetrofitHelper.getInstance(
            baseUrl = AppConfig.artBaseUrl,
            okHttpClient = null,
            requestHeaders = null
        ).create(ArtApi::class.java)
    }

    fun getArtFlow(owner: LifecycleOwner): Flow<List<DataArtElement>> {
        return flow {
            if (!initialized) {
                initialized = true
                createApi()
            }
            val response = api.getArt(apiKey, 0, 100)
            if (response.isSuccessful) {
                response.body()?.let { emit(extractDataArtPrefetchUseCase(it)) }
                return@flow
            }
        }.stateIn(
            scope = owner.lifecycleScope,
            // to pause when in the background but not when rotating
            started = WhileSubscribed(500),
            initialValue = emptyList()
        )
    }

}
