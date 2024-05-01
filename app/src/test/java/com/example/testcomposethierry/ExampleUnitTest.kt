package com.example.testcomposethierry

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.testing.TestLifecycleOwner
import app.cash.turbine.test
import com.example.testcomposethierry.data.models.DataArtDetail
import com.example.testcomposethierry.data.repositories.ArtDataRepository
import com.example.testcomposethierry.domain.detailscreen.GetDetailDataInParallelUseCase
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
        detailScreenViewModel = DetailScreenViewModel()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @AfterEach
    fun afterEach() {
        Dispatchers.resetMain()
    }

    @Test
    fun test1() = runTest {

    }
}
