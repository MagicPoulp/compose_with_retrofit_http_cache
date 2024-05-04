package com.example.testcomposethierry.data.http

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo
import dagger.hilt.android.qualifiers.ApplicationContext
import okhttp3.Cache
import okhttp3.Interceptor
import okhttp3.Request
import javax.inject.Inject

/*
--> Inconvenients of an HTTP cache compared to a local database:
- the content of filesDir contains many requests with Authorization Headers.
Because they all have the same certificate, statistics analysis can break the cypher and get the token.
- if we have a lot of data, any access to data would need to decrypt responses which is inefficient
whereas a database can be very fast
- a database allows structured searching and modification of our data
- if we want to modify data, and update it later, we lack an intermediate storage that
can be used as a unique "source of truth": This storage can be synchronized with the backend
in two directions (new backend data to download, new client data to send)
- the lifetime of data is not known by the application, since it is delegated to the network
caching mechanism
- in case of a server error 5xx, due to must-revalidate in the online interceptor, we will not
get data from the cache, as browser do or as we should like to do for the application to work
// excerpt from:
// https://datatracker.ietf.org/doc/html/rfc7234#section-4.3.3
"However, if a cache receives a 5xx (Server Error) response while
attempting to validate a response, it can either forward this
response to the requesting client, or act as if the server failed
to respond.  In the latter case, the cache MAY send a previously
stored response (see Section 4.2.4)."
- when internet connectivity changes from offline to online, the future requests will be fine
but in the other way around, if in online mode, internet is cut, and if a request is stale,
we will get a timeout on the revalidation of the request. We must retry, and we do not have
a good place to do it centralize in interceptors. So architecturally, we cannot isolate the caching
to the retrofit network layer with interceptors.
- not an inconvenient, but an improvement needed:
we cache everything here, whereas we could decide to cache or not depending on the URL
- not an inconvenient, but an improvement needed:
in the online interceptor, we change the headers of the response to force caching,
whereas the server may have a deliberate reason to restrict the cache.
And it becomes problematic to decide to force caching like we do,
or to follow the caching policy of the server's response.
If we do force caching, it should be for specific requests
We should have a set of URL wildcards that need a force cache
And a set of URL wildcards that use the server's policy

In the specification of the Cache-Control header, there are conditions on headers
sent by the backend. And we may depend on requests to servers that are not under our control.
This is why we need to force caching in the online interceptor

source:
https://datatracker.ietf.org/doc/html/rfc7234#section-2
Necessary condition:
the response either:

      *  contains an Expires header field (see Section 5.3), or

      *  contains a max-age response directive (see Section 5.2.2.8), or

      *  contains a s-maxage response directive (see Section 5.2.2.9)
         and the cache is shared, or

      *  contains a Cache Control Extension (see Section 5.2.3) that
         allows it to be cached, or

      *  has a status code that is defined as cacheable by default (see
         Section 4.2.2), or

      *  contains a public response directive (see Section 5.2.2.5).
 */

// HTTP cache working like a browser, inspired from:
// https://shishirthedev.medium.com/retrofit-2-http-response-caching-e769a27af29f
class HttpGetCacheManager @Inject constructor(
    private val networkConnectionManager: NetworkConnectionManager,
    @ApplicationContext applicationContext: Context) {

    private val cacheSize = (10 * 1024 * 1024).toLong() // 10 MB
    //val cache = Cache(applicationContext.cacheDir, cacheSize)
    // unlike cacheDir, filesDir cannot be deleted by the system to save space
    // TODO we would need to have a subfolder for the user
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
        val request: Request = chain.request()
        val response = chain.proceed(request)
        if (request.method != "GET") {
             return@Interceptor response
        }
        val maxAge = 10 // read from cache for X seconds even if there is internet connection
        response.newBuilder()
            // https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Cache-Control
            // https://datatracker.ietf.org/doc/html/rfc7234
            // mag-age=0, must-revalidate ensures we do not reuse a stale request
            // must-revalidate must not be added or the offline mode will not work
            .header("Cache-Control", "public, max-age=$maxAge")
            .removeHeader("Pragma")
            .build()
    }

    var offlineInterceptor: Interceptor = Interceptor { chain ->
        var request: Request = chain.request()
        if (request.method != "GET") {
            val response = chain.proceed(request)
            return@Interceptor response
        }
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