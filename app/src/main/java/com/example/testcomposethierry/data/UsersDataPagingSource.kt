package com.example.testcomposethierry.data

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.testcomposethierry.data.config.AppConfig
import com.example.testcomposethierry.data.custom_structures.ResultOf
import com.example.testcomposethierry.data.models.DataArtElement
import com.example.testcomposethierry.data.repositories.ArtDataRepository

// https://betterprogramming.pub/turn-the-page-overview-of-android-paging3-library-integration-with-jetpack-compose-3a7881ed75b4
class UsersDataPagingSource(
    private val unexpectedServerDataErrorString: String,
    private val artDataRepository: ArtDataRepository,
) : PagingSource<Int, DataArtElement>() {

    override suspend fun load(params: LoadParams<Int>):  LoadResult<Int, DataArtElement> {
        // here we wait for the MainActivityViewModel to have loaded
        val nextPageNumber = params.key ?: 0
        val pagingSize = AppConfig.pagingSize
        return when (val response = artDataRepository.getArtPaged(pagingSize, nextPageNumber)) {
            // Try this line below to test the LaunchEffect(LaunchedEffect(stateListArt.loadState) {)
            // is ResultOf.Success -> LoadResult.Error(Exception(unexpectedServerDataErrorString))
            is ResultOf.Success -> if (response.value.isEmpty()) LoadResult.Error(Exception(unexpectedServerDataErrorString)) else {
                // here we could prefetch detail data for each element on another API point
                LoadResult.Page(
                    data = response.value,
                    prevKey = if (nextPageNumber > 1) nextPageNumber - 1 else null,
                    nextKey = if (response.value.isNotEmpty()) nextPageNumber + 1 else null
                )
            }
            is ResultOf.Failure -> {
                response.throwable?.let { LoadResult.Error(it) }
                    ?: run { LoadResult.Error(Exception(unexpectedServerDataErrorString)) }
            }
            else -> { LoadResult.Error(Exception(unexpectedServerDataErrorString)) }
        }
    }

    // The getRefreshKey() method provides information to the library on which page to load in case the data is invalidated.
    override fun getRefreshKey(state: PagingState<Int, DataArtElement>): Int =
        ((state.anchorPosition ?: 0) - state.config.initialLoadSize / 2)
            .coerceAtLeast(1)
}
