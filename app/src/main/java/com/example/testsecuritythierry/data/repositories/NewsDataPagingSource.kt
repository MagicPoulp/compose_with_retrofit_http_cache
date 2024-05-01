package com.example.testsecuritythierry.data.repositories

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.testsecuritythierry.data.config.AppConfig
import com.example.testsecuritythierry.data.custom_structures.ResultOf
import com.example.testsecuritythierry.data.models.DataNewsElement

// https://betterprogramming.pub/turn-the-page-overview-of-android-paging3-library-integration-with-jetpack-compose-3a7881ed75b4
class NewsDataPagingSource (
    private val unexpectedServerDataErrorString: String,
    private val localNewsDataRepository: LocalNewsDataRepository
) : PagingSource<Int, DataNewsElement>() {

    override suspend fun load(params: LoadParams<Int>):  LoadResult<Int, DataNewsElement> {
        val nextPageNumber = params.key ?: 1
        val response = localNewsDataRepository.getNewsPaged(AppConfig.pagingSize, nextPageNumber)
        return when (response) {
            is ResultOf.Success -> {
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
    override fun getRefreshKey(state: PagingState<Int, DataNewsElement>): Int =
        ((state.anchorPosition ?: 0) - state.config.initialLoadSize / 2)
            .coerceAtLeast(1)
}
