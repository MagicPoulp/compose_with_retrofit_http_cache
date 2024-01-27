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
import com.example.testcomposethierry.domain.detailscreen.GetDetailDataInParallelUseCase
import com.example.testcomposethierry.domain.detailscreen.RefetchArtDetailUseCase
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
    private val refetchArtDetailUseCase: RefetchArtDetailUseCase,
    private val getDetailDataInParallelUseCase: GetDetailDataInParallelUseCase,
) : ViewModel() {
    private val _activeDetailData: MutableStateFlow<DataArtDetail?> = MutableStateFlow(null)
    val activeDetailData: StateFlow<DataArtDetail?>
        get() = _activeDetailData.asStateFlow()
    private val mapArtDetail: MutableMap<Int, DataArtDetail> = hashMapOf()
    private var isDataSavinginitialized = false

    // ------------------------------------------

    fun startDataSaving(
        owner: LifecycleOwner,
        channelIndexesToPrefetch: Channel<Pair<Int, String>>
    ) {
        if (isDataSavinginitialized) {
            return
        }
        isDataSavinginitialized = true
        // the repeatOnLifecycle is here to stop consuming resources when the app goes in background
        // it does not matter if we drop certain elements, because we refetch details if we click
        owner.lifecycleScope.launch(Dispatchers.IO) {
            owner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                getDetailDataInParallelUseCase(channelIndexesToPrefetch, mapArtDetail)
                    // a cold flow is not executed without a collector
                    .collect()
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