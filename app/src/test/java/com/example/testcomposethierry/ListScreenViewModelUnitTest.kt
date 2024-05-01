package com.example.testcomposethierry

import android.app.Activity
import android.os.Looper
import androidx.activity.ComponentActivity
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.testing.TestLifecycleOwner
import com.example.testcomposethierry.data.http.NetworkConnectionManager
import com.example.testcomposethierry.data.repositories.UsersListDataRepository
import com.example.testcomposethierry.ui.view_models.ListScreenViewModel
import dagger.hilt.android.testing.HiltAndroidTest
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.spyk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Rule
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.robolectric.Robolectric


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

@HiltAndroidTest
class ListScreenViewModelUnitTest {

    //@get:Rule
    //val rule = InstantTaskExecutorRule()

    lateinit var listScreenViewModel: ListScreenViewModel
    val networkConnectionManager = mockk<NetworkConnectionManager>()

    @OptIn(ExperimentalCoroutinesApi::class)
    @BeforeEach
    fun beforeEach() {
        // https://github.com/Kotlin/kotlinx.coroutines/blob/master/kotlinx-coroutines-test/MIGRATION.md
        // this dispatcher skips delays
        Dispatchers.setMain(UnconfinedTestDispatcher())
        val usersListDataRepository = mockk<UsersListDataRepository>()
        //{
            //onBlocking { getPokemon() } doReturn testingPokemonList
        //}
        //{
        // on { invoke(any()) } doReturn testingPokemonDetails
        //}
        //on { invoke(any()) } doReturn testingPokemonDetails
        listScreenViewModel = spyk(ListScreenViewModel(usersListDataRepository, networkConnectionManager))
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
        val testLifecycleOwner = TestLifecycleOwner()
        every { activity.lifecycle } returns testLifecycleOwner.lifecycle
        every { listScreenViewModel.showInternetConnectivityErrorToast(activity, errorString) } returns Unit
        listScreenViewModel.prepareInternetConnectivityErrorToaster(activity, errorString)
        testLifecycleOwner.currentState = Lifecycle.State.STARTED
        // delay to give time to the function prepareInternetConnectivityErrorToaster()
        delay(2000L)
        verify(exactly = 1) { listScreenViewModel.showInternetConnectivityErrorToast(any(), any()) }
    }

    @Test
    fun testNonNominalNoCollectIfLifeCycleStarted() = runTest {
        val flowIsConnected = MutableStateFlow(false).asStateFlow()
        every { networkConnectionManager.isConnected } returns flowIsConnected
        val errorString = "error"
        val activity = mockk<ComponentActivity>()
        val testLifecycleOwner = TestLifecycleOwner()
        every { activity.lifecycle } returns testLifecycleOwner.lifecycle
        every { listScreenViewModel.showInternetConnectivityErrorToast(any(), any()) } returns Unit
        testLifecycleOwner.currentState = Lifecycle.State.INITIALIZED
        listScreenViewModel.prepareInternetConnectivityErrorToaster(activity, errorString)
        delay(2000L)
        verify(exactly = 0) { listScreenViewModel.showInternetConnectivityErrorToast(any(), any()) }
    }
}
