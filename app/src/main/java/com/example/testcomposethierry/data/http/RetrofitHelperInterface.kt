package com.example.testcomposethierry.data.http

import okhttp3.OkHttpClient
import retrofit2.Retrofit

interface RetrofitHelperInterface {
    fun getInstance(baseUrl: String, okHttpClient: OkHttpClient?, requestHeaders: List<Pair<String, String>>?): Retrofit
}
