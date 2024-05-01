package com.example.testcomposethierry

import android.app.Activity
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Build.VERSION
import app.cash.turbine.test
import com.example.testcomposethierry.data.http.DoesNetworkHaveInternet
import com.example.testcomposethierry.data.http.NetworkConnectionManager
import com.example.testcomposethierry.data.http.NetworkConnectionManagerImpl
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.spyk
import io.mockk.verify
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.lang.reflect.Field
import java.lang.reflect.Method
import java.lang.reflect.Modifier


/*
1. Adapt Hilt for the test
Mock all the injected dependencies, and isolate the ViewModel
@HiltAndroidTest
2. to skip delays in the tests
Dispatchers.setMain(UnconfinedTestDispatcher())
3. to simulate the a test lifecycleOwner to test the flow
4. Turbine and awaitItem to wait for the emits
5. assertThat to have nice assertions with good error reporting
testLifecycleOwner = TestLifecycleOwner()
 */

class NetworkConnectionManagerUnitTest {

    // https://stackoverflow.com/questions/38074224/stub-value-of-build-version-sdk-int-in-local-unit-test
    fun setStaticFieldViaReflection(field: Field, value: Any) {
        field.isAccessible = true
        getModifiersField().also {
            it.isAccessible = true
            it.set(field, field.modifiers and Modifier.FINAL.inv())
        }
        field.set(null, value)
    }

    fun getModifiersField(): Field {
        return try {
            Field::class.java.getDeclaredField("modifiers")
        } catch (e: NoSuchFieldException) {
            try {
                val getDeclaredFields0: Method =
                    Class::class.java.getDeclaredMethod(
                        "getDeclaredFields0",
                        Boolean::class.javaPrimitiveType
                    )
                getDeclaredFields0.isAccessible = true
                val fields = getDeclaredFields0.invoke(Field::class.java, false) as Array<Field>
                for (field in fields) {
                    if ("modifiers" == field.name) {
                        return field
                    }
                }
            } catch (ex: ReflectiveOperationException) {
                e.addSuppressed(ex)
            }
            throw e
        }
    }

    lateinit var networkConnectionManager: NetworkConnectionManager
    private val cm = mockk<ConnectivityManager>()

    @OptIn(ExperimentalCoroutinesApi::class)
    @BeforeEach
    fun beforeEach() {
        // needed otherwise the build number equals 0
        setStaticFieldViaReflection(Build.VERSION::class.java.getDeclaredField("SDK_INT"), 123)


        // https://github.com/Kotlin/kotlinx.coroutines/blob/master/kotlinx-coroutines-test/MIGRATION.md
        // this dispatcher skips delays
        Dispatchers.setMain(UnconfinedTestDispatcher())
        val activity = mockk<Activity>()
        val coroutineScope = CoroutineScope(Dispatchers.Default)
        every { activity.getSystemService(any()) } returns cm
        networkConnectionManager = NetworkConnectionManagerImpl(activity, coroutineScope, true)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @AfterEach
    fun afterEach() {
        Dispatchers.resetMain()
    }

    @Test
    fun testInitializationAllFalse() = runTest {
        assert(!networkConnectionManager.isInitialized)
        assert(!networkConnectionManager.isConnected.value)
    }

    enum class CallbackToTest {
        onAvailable,
        onLost,
    }

    // DRY, we do not copy paste
    private fun testConnectivityManagerNetworkCallbackCommon(callbackToTest: CallbackToTest) = runTest {
        val mockNetwork = mockk<Network>()
        mockkStatic(DoesNetworkHaveInternet::class)
        every { DoesNetworkHaveInternet.execute(mockNetwork.socketFactory) } returns true

        val mockCapabilities = mockk<NetworkCapabilities>()
        every { mockCapabilities.hasCapability(any()) } returns true
        every { cm.getNetworkCapabilities(any()) } returns mockCapabilities

        // we use getDeclaredMethod to access a private method
        // the types of the method's arguments are needed in the second parameter
        val methodToTest: Method = networkConnectionManager.javaClass.getDeclaredMethod("createNetworkCallback")
        methodToTest.isAccessible = true
        @Suppress("UNCHECKED_CAST")
        val methodResult: ConnectivityManager.NetworkCallback = methodToTest.invoke(networkConnectionManager) as ConnectivityManager.NetworkCallback
        // test extension from turbine
        networkConnectionManager.isConnected.test {
            // this is for the initial value
            awaitItem()
            if (callbackToTest == CallbackToTest.onAvailable) {
                methodResult.onAvailable(mockNetwork)
                awaitItem()
                ensureAllEventsConsumed()
                assert(networkConnectionManager.isInitialized)
                assert(networkConnectionManager.isConnected.value)
            }
            if (callbackToTest == CallbackToTest.onLost) {
                methodResult.onLost(mockNetwork)
                // no item to wait for since in the disconnect we cannot emit the same value false
                ensureAllEventsConsumed()
                assert(networkConnectionManager.isInitialized)
                assert(!networkConnectionManager.isConnected.value)
            }
        }
        verify(exactly = 1) { networkConnectionManager.checkValidNetworks() }
    }

    @Test
    fun testThatOnAvailableCausesConnectedAnInitialized() = runTest {
        testConnectivityManagerNetworkCallbackCommon(CallbackToTest.onAvailable)
    }

    @Test
    fun testThatOnLostCausesNotConnectedAndInitialized() = runTest {
        testConnectivityManagerNetworkCallbackCommon(CallbackToTest.onLost)
    }
}
