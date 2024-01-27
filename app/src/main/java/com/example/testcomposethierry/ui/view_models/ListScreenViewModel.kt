package com.example.testcomposethierry.ui.view_models


import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.lifecycleScope
import com.example.testcomposethierry.data.repositories.ArtDataRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class UiState {
    object Empty : UiState()
    object InProgress : UiState()
    class Error(val error: Throwable) : UiState()
    object Filled : UiState()
}

/**
 * Explanation for how the DetailScreen works with data from a separate API.
 * We parallelized the use of the APIs to increase the performance for the end user.
 * And when data is not ready, we show a Loading animation for a short time.
 *
 * There are 2 APIs, one with Global data, and one with detailed data
 * Detail data is prefetched when the general data emits to a Channel
 * See processInParallelToGetDetailData() that consumes the Channel.
 *
 * If the data is not ready yet when the user clicks on the list, it is refetched
 * See refetchArtDetail.
 * activeDetailData is used to update the UI when the data is correctly updated.
* */
@HiltViewModel
class ListScreenViewModel @Inject constructor(
    private val artDataRepository: ArtDataRepository,
) : ViewModel() {
    private val _activeRow: MutableStateFlow<Int> = MutableStateFlow(-1)
    val activeRow: StateFlow<Int>
        get() = _activeRow.asStateFlow()

    // ------------------------------------------

    fun setActiveRow(owner: LifecycleOwner, rowId: Int) {
        owner.lifecycleScope.launch {
            _activeRow.emit(rowId)
        }
    }

}
