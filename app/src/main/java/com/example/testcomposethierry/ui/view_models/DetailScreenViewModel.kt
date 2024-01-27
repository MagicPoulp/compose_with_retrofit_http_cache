package com.example.testcomposethierry.ui.view_models

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.paging.compose.LazyPagingItems
import com.example.testcomposethierry.BuildConfig
import com.example.testcomposethierry.data.custom_structures.ResultOf
import com.example.testcomposethierry.data.models.DataArtDetail
import com.example.testcomposethierry.data.models.DataArtElement
import com.example.testcomposethierry.data.repositories.ArtDataRepository
import com.example.testcomposethierry.domain.network.RefetchArtDetailUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DetailScreenViewModel @Inject constructor(
    private val artDataRepository: ArtDataRepository,
    private val refetchArtDetailUseCase: RefetchArtDetailUseCase,
) : ViewModel() {
    private val _activeDetailData: MutableStateFlow<DataArtDetail?> = MutableStateFlow(null)
    val activeDetailData: StateFlow<DataArtDetail?>
        get() = _activeDetailData.asStateFlow()
    private val numberOfConcurrentDetailPrefetching = 10
    private val mapArtDetail: MutableMap<Int, DataArtDetail> = hashMapOf()
    private var isDataSavinginitialized = false

    // ------------------------------------------
    fun startDataSaving(
        owner: LifecycleOwner,
        artElementIndexesToProcess: Channel<Pair<Int, String>>
    ) {
        if (isDataSavinginitialized) {
            return
        }
        isDataSavinginitialized = true
        // the repeatOnLifecycle is here to stop consuming resources when the app goes in background
        // it does not matter if we drop certain elements, because we refetch details if we click
        owner.lifecycleScope.launch(Dispatchers.IO) {
            owner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                consumeChannelAndPrefetchInParallel(artElementIndexesToProcess)
                    // a cold flow is not executed without a collector
                    .collect()
            }
        }
    }

    private fun consumeChannelAndPrefetchInParallel(channel: Channel<Pair<Int, String>>): Flow<Unit> {
        val receiveFlow = channel.receiveAsFlow()
        return processInParallelToGetDetailData(receiveFlow)
    }

    // how to test: see the unit test in the test suite
    @OptIn(ExperimentalCoroutinesApi::class)
    private fun processInParallelToGetDetailData(receiveFlow: Flow<Pair<Int, String>>): Flow<Unit> {
        return receiveFlow.flatMapMerge<Pair<Int, String>, Unit>(concurrency = numberOfConcurrentDetailPrefetching) { elementData ->
            flow {
                // how to test: comment this part so that there is no prefetching of the detail data
                // data is refetched when clicking on a row
                when (val resultDetail = artDataRepository.getArtObjectDetail(elementData.second)) {
                    is ResultOf.Success -> mapArtDetail.getOrPut(elementData.first) {
                        resultDetail.value
                    }
                    else -> Unit
                }
                // for testing
                if (BuildConfig.DEBUG) {
                    emit(Unit)
                }
            }
        }
    }

    suspend fun setActiveDetailData(newActiveDetailData: DataArtDetail?) {
        if (newActiveDetailData != _activeDetailData.value) {
            _activeDetailData.emit(newActiveDetailData)
        }
    }

    fun getSavedArtDetail(rowId: Int): DataArtDetail? {
        return mapArtDetail[rowId]
    }

    suspend fun refetchArtDetail(rowId: Int, listArtPagingItems: LazyPagingItems<DataArtElement>): DataArtDetail? {
        return refetchArtDetailUseCase(rowId, listArtPagingItems, mapArtDetail)
    }
}