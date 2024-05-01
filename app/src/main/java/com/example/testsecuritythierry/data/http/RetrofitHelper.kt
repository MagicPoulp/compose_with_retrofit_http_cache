package com.example.testsecuritythierry.data.http


import com.example.testsecuritythierry.BuildConfig
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import okhttp3.Dispatcher
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.jackson.JacksonConverterFactory
import java.util.concurrent.TimeUnit


// https://www.geeksforgeeks.org/retrofit-with-kotlin-coroutine-in-android/
// https://github.com/AsyncHttpClient/async-http-client/tree/master/extras/retrofit2
// https://stackoverflow.com/questions/52881862/throttle-or-limit-kotlin-coroutine-count
// add a header:
// https://stackoverflow.com/questions/32605711/adding-header-to-all-request-with-retrofit-2
object RetrofitHelper {

    // we can specify the base url, the max number of concurrent connections, and an extra API key header
    fun getInstance(baseUrl: String, okHttpClient: OkHttpClient?, requestHeaders: List<Pair<String, String>>?): Retrofit {

        val dispatcher = Dispatcher()
        //dispatcher.maxRequests = maxConnections

        val builder = when(okHttpClient) {
            null -> OkHttpClient()
            else -> okHttpClient
        }.newBuilder()
        builder.dispatcher(dispatcher)
        builder.readTimeout(8, TimeUnit.SECONDS)
        builder.connectTimeout(8, TimeUnit.SECONDS)

        if (BuildConfig.DEBUG) {
            val interceptor = HttpLoggingInterceptor()
            interceptor.level = HttpLoggingInterceptor.Level.BASIC
            builder.addInterceptor(interceptor)
        }

        requestHeaders?.let {
            builder.addInterceptor { chain: Interceptor.Chain ->
                val request: Request.Builder = chain.request().newBuilder()
                requestHeaders.forEach {
                    request.addHeader(it.first, it.second)
                }
                chain.proceed(request.build())
            }
        }

        val client = builder.build()

        return Retrofit.Builder()
            .client(client)
            .baseUrl(baseUrl)
            .addConverterFactory(JacksonConverterFactory.create(jacksonObjectMapper()))
            .build()
    }
}
