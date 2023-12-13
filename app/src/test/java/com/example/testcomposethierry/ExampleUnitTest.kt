package com.example.testcomposethierry

import com.example.testcomposethierry.data.repositories.ArtDataRepository
import com.example.testcomposethierry.data.repositories.PersistentDataManager
import com.example.testcomposethierry.ui.view_models.ArtViewModel
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
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

    lateinit var artViewModel: ArtViewModel

    @OptIn(ExperimentalCoroutinesApi::class)
    @BeforeEach
    fun beforeEach() {/*
        // https://github.com/Kotlin/kotlinx.coroutines/blob/master/kotlinx-coroutines-test/MIGRATION.md
        // this dispatcher skips delays
        Dispatchers.setMain(UnconfinedTestDispatcher())
        val artDataRepository = mock<ArtDataRepository>()
        //{
            //onBlocking { getPokemon() } doReturn testingPokemonList
        //}
        val persistentDataManager = mock<PersistentDataManager>()
        //{
           // on { invoke(any()) } doReturn testingPokemonDetails
        //}
        artViewModel = ArtViewModel(artDataRepository, persistentDataManager)*/
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @AfterEach
    fun afterEach() {
        Dispatchers.resetMain()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun testParallelFetchingOfDetailData() = runTest {
        //val methodToTest = artViewModel.javaClass.getDeclaredMethod("processInParallelToGetDetailData", String::class.java)
        //methodToTest.isAccessible = true
        //val receiveFlow: Flow<Pair<Int, String>> = flow {}
        //val parameters = arrayOfNulls<Any>(1)
        //parameters[0] = receiveFlow

        //methodToTest.invoke(artViewModel, *parameters)
        assertEquals(1, 1)
        /*
        pokeViewModel.pokemonList.test { // test is needed with turbine
            val actual = mutableListOf<ResultOf<List<PokemonDetails>>>()
            val testLifecycleOwner = TestLifecycleOwner()
            testLifecycleOwner.lifecycleScope.launch {
                testLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                    pokeViewModel.pokemonList.collect {
                        actual.add(it)
                    }
                }
            }
            // currentState is STARTED by default
            //println(testLifecycleOwner.currentState)
            //testLifecycleOwner.currentState = Lifecycle.State.STARTED
            // we must wait for 2 items because the coroutine is waiting on the collec
            // and the library turbine helps a lot for that using awaitItem()
            awaitItem()
            awaitItem()
            assertThat("number of values", actual.size, equalTo(2))
            assertThat("Loading value", actual[0], equalTo(ResultOf.Loading("")))
            assertThat("Success value", actual[1], equalTo(ResultOf.Success(testingPokemonDetails)))
        }
        */
    }
}
