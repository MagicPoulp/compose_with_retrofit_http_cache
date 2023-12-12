package com.example.testsecuritythierry.data.repositories

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.example.testsecuritythierry.data.config.AppConfig
import com.example.testsecuritythierry.data.http.NewsApi
import com.example.testsecuritythierry.data.http.RetrofitHelper
import com.example.testsecuritythierry.data.models.DataNewsElement
import com.example.testsecuritythierry.domain.ExtractDataNewsUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted.Companion.WhileSubscribed
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

class NewsDataRepository @Inject constructor(
    val extractDataNewsUseCase: ExtractDataNewsUseCase
) {

    private lateinit var api: NewsApi
    private var initialized = false
    private val apiKey = AppConfig.apiKey

    private fun createApi() = run {
        api = RetrofitHelper.getInstance(
            baseUrl = AppConfig.newsBaseUrl,
            okHttpClient = null,
            requestHeaders = null
        ).create(NewsApi::class.java)
    }

    fun getNewsFlow(owner: LifecycleOwner): Flow<List<DataNewsElement>> {
        return flow {
            if (!initialized) {
                initialized = true
                createApi()
            }
            val response = api.getNews(apiKey)
            if (response.isSuccessful) {
                response.body()?.let { emit(extractDataNewsUseCase(it)) }
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
