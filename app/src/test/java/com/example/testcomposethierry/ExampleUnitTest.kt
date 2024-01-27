package com.example.testcomposethierry

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.testing.TestLifecycleOwner
import app.cash.turbine.test
import com.example.testcomposethierry.data.models.DataArtDetail
import com.example.testcomposethierry.data.repositories.ArtDataRepository
import com.example.testcomposethierry.domain.detailscreen.GetDetailDataInParallelUseCase
import com.example.testcomposethierry.domain.detailscreen.RefetchArtDetailUseCase
import com.example.testcomposethierry.ui.view_models.DetailScreenViewModel
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import java.lang.reflect.Method


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
class ExampleUnitTest {

    lateinit var detailScreenViewModel: DetailScreenViewModel
    lateinit var getDetailDataInParallelUseCase: GetDetailDataInParallelUseCase

    @OptIn(ExperimentalCoroutinesApi::class)
    @BeforeEach
    fun beforeEach() {
        // https://github.com/Kotlin/kotlinx.coroutines/blob/master/kotlinx-coroutines-test/MIGRATION.md
        // this dispatcher skips delays
        Dispatchers.setMain(UnconfinedTestDispatcher())
        val artDataRepository = mock<ArtDataRepository>()
        val refetchArtDetailUseCase = mock<RefetchArtDetailUseCase>()
        //{
            //onBlocking { getPokemon() } doReturn testingPokemonList
        //}
        //{
        // on { invoke(any()) } doReturn testingPokemonDetails
        //}
        getDetailDataInParallelUseCase = GetDetailDataInParallelUseCase(artDataRepository)
        detailScreenViewModel = DetailScreenViewModel(refetchArtDetailUseCase, getDetailDataInParallelUseCase)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @AfterEach
    fun afterEach() {
        Dispatchers.resetMain()
    }

    @Test
    fun testParallelFetchingOfDetailData() = runTest {
        // commented cde out since we can run a use case directly instead of using a private function
        // we use getDeclaredMethod to access a private method
        // the types of the method's arguments are needed in the second parameter
        /*val methodToTest: Method = detailScreenViewModel.javaClass.getDeclaredMethod("consumeChannelAndPrefetchInParallel", Channel::class.java)
        methodToTest.isAccessible = true
        */
        val channel = Channel<Pair<Int, String>>(Channel.UNLIMITED)
        //val parameters = arrayOfNulls<Any>(1)
        //parameters[0] = channel
        //@Suppress("UNCHECKED_CAST")
        //val methodResult: Flow<Unit> = methodToTest.invoke(detailScreenViewModel, *parameters) as Flow<Unit>

        // input data
        val channelInputList = listOf(Pair(1, "ObjectNumber1"), Pair(2, "ObjectNumber2"), Pair(3, "ObjectNumber3"))

        val mapArtDetail: MutableMap<Int, DataArtDetail> = mutableMapOf()

        // testLifecycleOwner starts on the state STARTED
        val testLifecycleOwner = TestLifecycleOwner()
        // the command below is needed to activate turbine on methodResult's flow
        val methodResult: Flow<Unit> = getDetailDataInParallelUseCase(channel, mapArtDetail)
        methodResult.test {
            var numCollect = 0
            testLifecycleOwner.lifecycleScope.launch {
                testLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                    methodResult.collect {
                        numCollect += 1
                    }
                }
            }
            var numSend = 0
            for (channelInput in channelInputList) {
                numSend += 1
                channel.send(channelInput)
            }
            var numAwaitItem = 0
            // it seems that a collect will remove one awaitItem, hence we subtract numCollect
            for (i in (channelInputList.indices - numCollect)) {
                // awaitItem waits for an emit on methodResult
                awaitItem()
                numAwaitItem += 1
            }
            // this ensures we that have treated all the emit() events
            ensureAllEventsConsumed()
            // we do not awaitComplete() because the flow is still alive receiving on the channel
            assertThat("channel sent value", numSend, equalTo(channelInputList.size))
            assertThat("num send compared to num awaitItem", numSend, equalTo(numAwaitItem + numCollect))
        }
        // putting this or removing it shows that a lifecycle change cleans up correctly the coroutines
        testLifecycleOwner.currentState = Lifecycle.State.DESTROYED
    }
}
