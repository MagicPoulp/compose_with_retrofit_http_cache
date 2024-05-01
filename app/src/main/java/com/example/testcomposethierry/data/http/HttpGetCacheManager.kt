package com.example.testcomposethierry.data.http

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.NetworkInfo
import dagger.hilt.android.qualifiers.ApplicationContext
import okhttp3.Cache
import okhttp3.Interceptor
import okhttp3.Request
import javax.inject.Inject

// inspired from:
// https://shishirthedev.medium.com/retrofit-2-http-response-caching-e769a27af29f
class HttpGetCacheManager @Inject constructor(
    private val networkConnectionManager: NetworkConnectionManager,
    @ApplicationContext applicationContext: Context) {

    private val cacheSize = (10 * 1024 * 1024).toLong() // 10 MB
    //val cache = Cache(applicationContext.cacheDir, cacheSize)
    // unlike cacheDir, filesDir cannot be deleted by the system to save space
    val cache = Cache(applicationContext.filesDir, cacheSize)

    // https://stackoverflow.com/questions/51141970/check-internet-connectivity-android-in-kotlin
    // https://shishirthedev.medium.com/retrofit-2-http-response-caching-e769a27af29f
    private fun isInternetAvailable(context: Context): Boolean {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            return networkConnectionManager.isConnected.value
        } else {
            // this stack overflow also checks the Build version (answer of WSBT)
            // https://stackoverflow.com/questions/32547006/connectivitymanager-getnetworkinfoint-deprecated
            var isConnected = false
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val activeNetwork: NetworkInfo? = connectivityManager.activeNetworkInfo
            if (activeNetwork != null && activeNetwork.isConnected) {
                isConnected = true
            }
            return isConnected
        }
    }

    var onlineInterceptor: Interceptor = Interceptor { chain ->
        val response = chain.proceed(chain.request())
        val maxAge = 0 // read from cache for X seconds even if there is internet connection
        response.newBuilder()
            .header("Cache-Control", "public, max-age=$maxAge")
            .removeHeader("Pragma")
            .build()
    }

    var offlineInterceptor: Interceptor = Interceptor { chain ->
        var request: Request = chain.request()
        if (!isInternetAvailable(applicationContext)) {
            val maxStale = 60 * 60 * 24 * 30 // Offline cache available for 30 days
            request = request.newBuilder()
                .header("Cache-Control", "public, only-if-cached, max-stale=$maxStale")
                .removeHeader("Pragma")
                .build()
        }
        chain.proceed(request)
    }
}