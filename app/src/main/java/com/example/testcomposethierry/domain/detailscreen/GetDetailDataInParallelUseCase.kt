package com.example.testcomposethierry.domain.detailscreen

import androidx.paging.compose.LazyPagingItems
import com.example.testcomposethierry.BuildConfig
import com.example.testcomposethierry.data.custom_structures.ResultOf
import com.example.testcomposethierry.data.models.DataArtDetail
import com.example.testcomposethierry.data.models.DataArtElement
import com.example.testcomposethierry.data.repositories.ArtDataRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.receiveAsFlow
import javax.inject.Inject

class GetDetailDataInParallelUseCase @Inject constructor(
    private val artDataRepository: ArtDataRepository,
) {
    private val numberOfConcurrentDetailPrefetching = 10

    operator fun invoke(channel: Channel<Pair<Int, String>>, mapArtDetail: MutableMap<Int, DataArtDetail>): Flow<Unit> {
        val receiveFlow = channel.receiveAsFlow()
        return processInParallelToGetDetailData(receiveFlow, mapArtDetail)
    }

    // how to test: see the unit test in the test suite
    @OptIn(ExperimentalCoroutinesApi::class)
    private fun processInParallelToGetDetailData(receiveFlow: Flow<Pair<Int, String>>, mapArtDetail: MutableMap<Int, DataArtDetail>): Flow<Unit> {
        return receiveFlow.flatMapMerge(concurrency = numberOfConcurrentDetailPrefetching) { elementData ->
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
}