package com.example.testcomposethierry.data.http


import com.example.testcomposethierry.BuildConfig
import com.example.testcomposethierry.data.repositories.PersistentDataManager
import com.example.testcomposethierry.domain.ExtractDataArtUseCase
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import okhttp3.Dispatcher
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.jackson.JacksonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Inject


// https://www.geeksforgeeks.org/retrofit-with-kotlin-coroutine-in-android/
// https://github.com/AsyncHttpClient/async-http-client/tree/master/extras/retrofit2
// https://stackoverflow.com/questions/52881862/throttle-or-limit-kotlin-coroutine-count
// add a header:
// https://stackoverflow.com/questions/32605711/adding-header-to-all-request-with-retrofit-2
class RetrofitHelper @Inject constructor(
    val persistentDataManager: PersistentDataManager,
) {
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

        /*
        if (BuildConfig.DEBUG) {
            val interceptor = HttpLoggingInterceptor()
            interceptor.level = HttpLoggingInterceptor.Level.BASIC
            builder.addInterceptor(interceptor)
        }*/

        // https://stackoverflow.com/questions/44046695/get-response-class-type-in-retrofit2-interceptor-caching-for-realm
        builder.addInterceptor(Interceptor { chain ->
            val request = chain.request()
            val response: Response = (try {
                chain.proceed(request)
            }
            catch (t: Throwable)
            {
                print(t.message)
                if (t.message?.contains("Unable to resolve host") == true) { // no internet
                    return@Interceptor persistentDataManager.loadResponse(request.url.toString(), request, null)
                }
                throw t
            })
            if (response.isSuccessful) {
                persistentDataManager.saveResponse(request.url.toString(), response)
            }
            if (response.code == 503) {
                persistentDataManager.loadResponse(request.url.toString(), request, response)
            }
            response
            // TOTEST: to develop without using the server, use this
            // persistentDataManager.loadResponse(request.url.toString(), request, response)
        })

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
