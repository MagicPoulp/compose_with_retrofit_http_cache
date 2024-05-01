package com.example.testcomposethierry.ui.view_models

import androidx.lifecycle.ViewModel
import androidx.paging.compose.LazyPagingItems
import com.example.testcomposethierry.data.models.DataArtDetail
import com.example.testcomposethierry.data.models.DataArtElement
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class DetailScreenViewModel @Inject constructor(
) : ViewModel() {
    private val _activeDetailData: MutableStateFlow<DataArtDetail?> = MutableStateFlow(null)
    val activeDetailData: StateFlow<DataArtDetail?>
        get() = _activeDetailData.asStateFlow()
    //private val mapArtDetail: MutableMap<Int, DataArtDetail> = hashMapOf()

    // ------------------------------------------

    suspend fun setActiveDetailData(newActiveDetailData: DataArtDetail?) {
        if (newActiveDetailData != _activeDetailData.value) {
            _activeDetailData.emit(newActiveDetailData)
        }
    }

    //fun getSavedArtDetail(rowId: Int): DataArtDetail? {
    //    return mapArtDetail[rowId]
    //}
}