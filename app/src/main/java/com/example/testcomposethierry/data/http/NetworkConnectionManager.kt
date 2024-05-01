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

// https://medium.com/@rawatsumit115/smart-way-to-observe-internet-connection-for-whole-app-in-android-kotlin-bd77361c76fb
// https://medium.com/@veniamin.vynohradov/monitoring-internet-connection-state-in-android-da7ad915b5e5

interface NetworkConnectionManager {
    val isConnected: StateFlow<Boolean>
}

@Module
@InstallIn(SingletonComponent::class)
abstract class NetworkConnectionManagerModule {

    @Binds
    abstract fun bind(impl: InternetConnectionObserver): NetworkConnectionManager
}

// https://medium.com/androiddevelopers/create-an-application-coroutinescope-using-hilt-dd444e721528
@InstallIn(SingletonComponent::class)
@Module
object CoroutinesScopesModule {

    @Singleton // Provide always the same instance
    @Provides
    fun providesCoroutineScope(): CoroutineScope {
        // Run this code when providing an instance of CoroutineScope
        return CoroutineScope(SupervisorJob() + Dispatchers.Default)
    }
}

object DoesNetworkHaveInternet {
    private val TAG = this.javaClass.name
    // Make sure to execute this on a background thread.
    fun execute(socketFactory: SocketFactory): Boolean {
        return try{
            val socket = socketFactory.createSocket() ?: throw IOException("Socket is null.")
            socket.connect(InetSocketAddress("8.8.8.8", 53), 1500)
            socket.close()
            //Log.d(TAG, "PING success.")
            true
        }catch (e: IOException){
            //Log.e(TAG, "No internet connection.")
            false
        }
    }
}

interface InternetConnectionCallback {
    fun onConnected()
    fun onDisconnected()
}

@Singleton
class InternetConnectionObserver @Inject constructor(
    @ApplicationContext context: Context,
    private val coroutineScope: CoroutineScope
) : NetworkConnectionManager {
    private lateinit var networkCallback: ConnectivityManager.NetworkCallback
    private var cm: ConnectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    private val validNetworks: MutableSet<Network> = HashSet()
    private var connectionCallback: InternetConnectionCallback? = null
    private val _isConnected = MutableStateFlow(false)
    override val isConnected: StateFlow<Boolean>
        get() = _isConnected.asStateFlow()

    init {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            register()
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
                    if(hasInternet){
                        withContext(Dispatchers.Main){
                            validNetworks.add(network)
                            checkValidNetworks()
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
            validNetworks.remove(network)
            checkValidNetworks()
        }

    }

    private fun checkValidNetworks() {
        val status = validNetworks.size > 0
        if(status){
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

    fun unRegister(){
        cm.unregisterNetworkCallback(networkCallback)
    }

    private fun setCallback(connectionCallback: InternetConnectionCallback) {
        this.connectionCallback = connectionCallback
    }
}