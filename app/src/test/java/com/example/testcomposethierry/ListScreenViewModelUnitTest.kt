package com.example.testcomposethierry

import android.content.res.Resources
import androidx.activity.ComponentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.testing.TestLifecycleOwner
import app.cash.turbine.test
import com.example.testcomposethierry.data.http.NetworkConnectionManager
import com.example.testcomposethierry.ui.MainActivityInternetErrorReporter
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test


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

class ListScreenViewModelUnitTest {

    //@get:Rule
    //val rule = InstantTaskExecutorRule()

    lateinit var mainActivityInternetErrorReporter: MainActivityInternetErrorReporter
    val networkConnectionManager = mockk<NetworkConnectionManager>()

    @OptIn(ExperimentalCoroutinesApi::class)
    @BeforeEach
    fun beforeEach() {
        // https://github.com/Kotlin/kotlinx.coroutines/blob/master/kotlinx-coroutines-test/MIGRATION.md
        // this dispatcher skips delays
        Dispatchers.setMain(UnconfinedTestDispatcher())
        //{
            //onBlocking { getPokemon() } doReturn testingPokemonList
        //}
        //{
        // on { invoke(any()) } doReturn testingPokemonDetails
        //}
        //on { invoke(any()) } doReturn testingPokemonDetails
        // TRICK: recordPrivateCalls = true
        mainActivityInternetErrorReporter = spyk(MainActivityInternetErrorReporter(networkConnectionManager), recordPrivateCalls = true)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @AfterEach
    fun afterEach() {
        Dispatchers.resetMain()
    }

    @Test
    fun testNominalCollectIfLifeCycleStared() = runTest {
        val flowIsConnected = MutableStateFlow(false).asStateFlow()
        every { networkConnectionManager.isConnected } returns flowIsConnected
        val errorString = "error"
        val activity = mockk<ComponentActivity>()
        val resources = mockk<Resources>()
        every { activity.resources } returns resources
        every { resources.getString(any()) } returns errorString
        val testLifecycleOwner = TestLifecycleOwner()
        every { activity.lifecycle } returns testLifecycleOwner.lifecycle
        justRun { mainActivityInternetErrorReporter invoke "showInternetConnectivityErrorToast" withArguments listOf(activity, errorString) }
        networkConnectionManager.isConnected.test {
            // TRICK  based on previous experiences, the collect in listScreenViewModel has consumed one awaitItem() for the initial value
            mainActivityInternetErrorReporter.prepareInternetConnectivityErrorToaster(activity)
            testLifecycleOwner.currentState = Lifecycle.State.STARTED
            awaitItem()
            ensureAllEventsConsumed()
        }
        // TRICK: test that a private method is called
        verify(exactly = 1) { mainActivityInternetErrorReporter invoke "showInternetConnectivityErrorToast" withArguments listOf(activity, errorString)}
    }

    @Test
    fun testNonNominalNoCollectIfLifeCycleNotStarted() = runTest {
        val flowIsConnected = MutableStateFlow(false).asStateFlow()
        every { networkConnectionManager.isConnected } returns flowIsConnected
        val errorString = "error"
        val activity = mockk<ComponentActivity>()
        val resources = mockk<Resources>()
        every { activity.resources } returns resources
        every { resources.getString(any()) } returns errorString
        val testLifecycleOwner = TestLifecycleOwner()
        every { activity.lifecycle } returns testLifecycleOwner.lifecycle
        justRun { mainActivityInternetErrorReporter invoke "showInternetConnectivityErrorToast" withArguments listOf(activity, errorString) }
        // TRICK: turbine and awaitItem() are used
        networkConnectionManager.isConnected.test {
            testLifecycleOwner.currentState = Lifecycle.State.INITIALIZED
            mainActivityInternetErrorReporter.prepareInternetConnectivityErrorToaster(activity)
            awaitItem()
            ensureAllEventsConsumed()
        }
        // TRICK: test that a private method is called
        verify(exactly = 0) { mainActivityInternetErrorReporter invoke "showInternetConnectivityErrorToast" withArguments listOf(activity, errorString)}
    }
}
