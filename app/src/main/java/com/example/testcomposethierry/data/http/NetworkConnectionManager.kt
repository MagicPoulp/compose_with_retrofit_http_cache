package com.example.testcomposethierry.data.http

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import dagger.Provides
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton
import java.io.IOException
import java.net.InetSocketAddress
import javax.net.SocketFactory
import javax.net.ssl.SSLSocketFactory

// https://medium.com/@rawatsumit115/smart-way-to-observe-internet-connection-for-whole-app-in-android-kotlin-bd77361c76fb
// https://medium.com/@veniamin.vynohradov/monitoring-internet-connection-state-in-android-da7ad915b5e5

@Module
@InstallIn(SingletonComponent::class)
class NetworkConnectionManagerModule {
    @Provides
    @Singleton // declares scoped singleton
    fun provideNetworkConnectionManager(@ApplicationContext appContext: Context): NetworkConnectionManager {
        val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
        return NetworkConnectionManagerImpl(appContext, coroutineScope, false)
    }
}

// https://medium.com/androiddevelopers/create-an-application-coroutinescope-using-hilt-dd444e721528

object DoesNetworkHaveInternet {
    // Make sure to execute this on a background thread.
    fun execute(socketFactory: SocketFactory): Boolean {
        return try{
            val socket = socketFactory.createSocket() ?: throw IOException("Socket is null.")
            socket.connect(InetSocketAddress("8.8.8.8", 53), 1500)
            socket.close()
            //Log.d(this.javaClass.name, "PING success.")
            true
        } catch (e: IOException){
            //Log.e(this.javaClass.name, "No internet connection.")
            false
        }
    }
}

interface InternetConnectionCallback {
    fun onConnected()
    fun onDisconnected()
}

interface NetworkConnectionManager {
    val isConnected: StateFlow<Boolean>
    val isInitialized: Boolean
    fun checkAgainInternet()
    fun unregister()
}

@Singleton
class NetworkConnectionManagerImpl @Inject constructor(
    @ApplicationContext context: Context,
    private val coroutineScope: CoroutineScope,
    private val unitTestMode: Boolean = false
) : NetworkConnectionManager {
    private lateinit var networkCallback: ConnectivityManager.NetworkCallback
    private var cm: ConnectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    private val validNetworks: MutableSet<Network> = HashSet()
    private var connectionCallback: InternetConnectionCallback? = null
    private val _isConnected = MutableStateFlow(false)
    override val isConnected: StateFlow<Boolean>
        get() = _isConnected.asStateFlow()
    override var isInitialized = false

    init {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            // a boolean for a test mode is needed here to make the code testable
            // because due to the test environment, addCapability() on a network parameters returns null
            if (!unitTestMode) {
                register()
            }
            setCallback(object : InternetConnectionCallback {
                override fun onConnected() {
                    _isConnected.tryEmit(true)
                }

                override fun onDisconnected() {
                    _isConnected.tryEmit(false)
                }

            })
        }
    }

    override fun checkAgainInternet() {
        val socket = SSLSocketFactory.getDefault()
        _isConnected.tryEmit(DoesNetworkHaveInternet.execute(socket))
    }

    private fun createNetworkCallback() = object : ConnectivityManager.NetworkCallback()
    {
        /*
          Called when a network is detected. If that network has internet, save it in the Set.
          Source: https://developer.android.com/reference/android/net/ConnectivityManager.NetworkCallback#onAvailable(android.net.Network)
         */
        override fun onAvailable(network: Network) {
            val networkCapabilities = cm.getNetworkCapabilities(network)
            val hasInternetCapability = networkCapabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            if (hasInternetCapability == true) {
                // check if this network actually has internet
                coroutineScope.launch {
                    val hasInternet = DoesNetworkHaveInternet.execute(network.socketFactory)
                    if (hasInternet) {
                        withContext(Dispatchers.IO) {
                            validNetworks.add(network)
                            checkValidNetworks()
                            isInitialized = true
                        }
                    }
                }
            }
        }

        /*
          If the callback was registered with registerNetworkCallback() it will be called for each network which no longer satisfies the criteria of the callback.
          Source: https://developer.android.com/reference/android/net/ConnectivityManager.NetworkCallback#onLost(android.net.Network)
         */
        override fun onLost(network: Network) {
            isInitialized = true
            validNetworks.remove(network)
            checkValidNetworks()
        }

    }

    private fun checkValidNetworks() {
        val status = validNetworks.size > 0
        if (status){
            connectionCallback?.onConnected()
        } else{
            connectionCallback?.onDisconnected()
        }
    }

    private fun register() {
        networkCallback = createNetworkCallback()
        val networkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
        cm.registerNetworkCallback(networkRequest, networkCallback)
    }

    override fun unregister(){
        cm.unregisterNetworkCallback(networkCallback)
    }

    private fun setCallback(connectionCallback: InternetConnectionCallback) {
        this.connectionCallback = connectionCallback
    }
}