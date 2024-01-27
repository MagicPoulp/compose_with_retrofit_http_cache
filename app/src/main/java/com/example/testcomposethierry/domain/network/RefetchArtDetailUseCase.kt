package com.example.testcomposethierry.domain.network

import androidx.paging.compose.LazyPagingItems
import com.example.testcomposethierry.data.custom_structures.ResultOf
import com.example.testcomposethierry.data.models.DataArtDetail
import com.example.testcomposethierry.data.models.DataArtElement
import com.example.testcomposethierry.data.repositories.ArtDataRepository
import javax.inject.Inject

class RefetchArtDetailUseCase @Inject constructor(
    private val artDataRepository: ArtDataRepository,
) {
    suspend operator fun invoke(rowId: Int, listArtPagingItems: LazyPagingItems<DataArtElement>, mapArtDetail: MutableMap<Int, DataArtDetail>): DataArtDetail? {
        val itemData = listArtPagingItems.itemSnapshotList[rowId]
        itemData?.objectNumber?.let { objectNumber ->
            when (val resultDetail = artDataRepository.getArtObjectDetail(objectNumber)) {
                is ResultOf.Success -> {
                    mapArtDetail.getOrPut(rowId) {
                        resultDetail.value
                    }
                    return resultDetail.value
                }
                else -> Unit
            }
        }
        return null
    }
}