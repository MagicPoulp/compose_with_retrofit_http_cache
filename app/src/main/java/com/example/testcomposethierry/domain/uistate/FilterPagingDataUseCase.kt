package com.example.testcomposethierry.domain.uistate

import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.filter
import androidx.paging.map
import com.example.testcomposethierry.data.custom_structures.ResultOf
import com.example.testcomposethierry.data.models.DataArtDetail
import com.example.testcomposethierry.data.models.DataArtElement
import com.example.testcomposethierry.data.repositories.ArtDataRepository
import kotlinx.coroutines.channels.Channel
import javax.inject.Inject

class FilterPagingDataUseCase @Inject constructor(
) {
    suspend operator fun invoke(
        pagingData: PagingData<DataArtElement>,
        channelIndexesToPrefetch: Channel<Pair<Int, String>>,
    ): PagingData<DataArtElement> {
        // This filtering is optional, it is just because the API send us duplicates.
        // We filter duplicate object numbers.
        // https://stackoverflow.com/questions/69284841/how-to-avoid-duplicate-items-in-pagingadapter
        // The museum API has a bug since duplicate pages can be returned
        // the 2 following calls give duplicates, and then the next pages are totally distinct
        // curl https://www.rijksmuseum.nl/api/en/collection?key=rIl6yb6x\&ps=3\&p=0
        // curl https://www.rijksmuseum.nl/api/en/collection?key=rIl6yb6x\&ps=3\&p=1
        val objectNumbersHashSet = hashSetOf<String>()
        val pagingDataWithoutDuplicates = pagingData.filter { elem ->
            elem.objectNumber?.let { objectNumbersHashSet.add(elem.objectNumber) }
                ?: false
        }
        var itemIndex = -1
        pagingDataWithoutDuplicates.map { elem ->
            itemIndex += 1
            elem.objectNumber?.let {
                // We send to the Channel the IDs that need to be prefetched for getting the detail data
                // REMOVED, no need of filtering
                //channelIndexesToPrefetch.send(Pair(itemIndex, elem.objectNumber))
            }
            elem
        }
        return pagingDataWithoutDuplicates
    }
}