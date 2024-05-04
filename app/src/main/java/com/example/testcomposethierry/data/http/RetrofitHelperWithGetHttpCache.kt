package com.example.testcomposethierry.data.http

import com.example.testcomposethierry.data.config.AppConfig
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import okhttp3.Dispatcher
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import retrofit2.Retrofit
import retrofit2.converter.jackson.JacksonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Inject

const val internetReadTimeoutS = 8L
const val internetconnectTimeoutS = 8L

// https://www.geeksforgeeks.org/retrofit-with-kotlin-coroutine-in-android/
// https://github.com/AsyncHttpClient/async-http-client/tree/master/extras/retrofit2
// https://stackoverflow.com/questions/52881862/throttle-or-limit-kotlin-coroutine-count
// add a header:
// https://stackoverflow.com/questions/32605711/adding-header-to-all-request-with-retrofit-2
class RetrofitHelperWithGetHttpCache @Inject constructor(
    private val httpGetCacheManager: HttpGetCacheManager,
    private val networkConnectionManager: NetworkConnectionManager,
) : RetrofitHelperInterface {

    // we can specify the base url, the max number of concurrent connections, and an extra API key header
    override fun getInstance(baseUrl: String, okHttpClient: OkHttpClient?, requestHeaders: List<Pair<String, String>>?): Retrofit {
        val dispatcher = Dispatcher()
        //dispatcher.maxRequests = maxConnections

        val builder = when(okHttpClient) {
            null -> OkHttpClient()
            else -> okHttpClient
        }.newBuilder()
        builder.dispatcher(dispatcher)
        builder.readTimeout(internetReadTimeoutS, TimeUnit.SECONDS)
        builder.connectTimeout(internetconnectTimeoutS, TimeUnit.SECONDS)

        // to log requests
        //if (BuildConfig.DEBUG) {
        //    val interceptor = HttpLoggingInterceptor()
        //    interceptor.level = HttpLoggingInterceptor.Level.BASIC
        //    builder.addInterceptor(interceptor)
        //}

        // https://stackoverflow.com/questions/44046695/get-response-class-type-in-retrofit2-interceptor-caching-for-realm
        //
        // https://stackoverflow.com/questions/53168964/adding-multiple-interceptors-to-an-okhttpclient
        // The interceptors are run in order of addition for requests and in reverse-order of addition for responses
        // so here from bottom to top
        builder.addInterceptor(Interceptor { chain ->
            val request = chain.request()
            val response: Response = (try {
                chain.proceed(request)
            }
            catch (t: Throwable)
            {
                print(t.message)
                // errors below could be because of no internet connectivity
                // we need to update the internet status so that next requests can use the cache
                if (t.message?.contains("Unable to resolve host") == true) {
                    networkConnectionManager.checkAgainInternet()
                    //TODO retru the request but once only, and here is not the right place
                    // it should be where the retrofit request was done.
                    // Moreover, retry only GET requests for safety reasons.
                }
                if (t.message?.contains("timeout") == true) {
                    networkConnectionManager.checkAgainInternet()
                }
                throw t
            })
            response
        })

        // https://shishirthedev.medium.com/retrofit-2-http-response-caching-e769a27af29f
        // this config can help debug, but the dependencies (NetworkConnectionManager)
        // are still injected and active
        if (AppConfig.httpGetCacheActive) {
            builder.addInterceptor(httpGetCacheManager.offlineInterceptor)
            builder.addNetworkInterceptor(httpGetCacheManager.onlineInterceptor)
            builder.cache(httpGetCacheManager.cache)
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
