package com.example.testcomposethierry.ui.view_models

import androidx.lifecycle.ViewModel
import com.example.testcomposethierry.data.models.DomainDataUsersListElement
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class DetailScreenViewModel @Inject constructor(
) : ViewModel() {
    private val _activeDetailData: MutableStateFlow<DomainDataUsersListElement?> = MutableStateFlow(null)
    val activeDetailData: StateFlow<DomainDataUsersListElement?>
        get() = _activeDetailData.asStateFlow()
    //private val mapArtDetail: MutableMap<Int, DataArtDetail> = hashMapOf()

    // ------------------------------------------

    suspend fun setActiveDetailData(newActiveDetailData: DomainDataUsersListElement?) {
        if (newActiveDetailData != _activeDetailData.value) {
            _activeDetailData.emit(newActiveDetailData)
        }
    }

    //fun getSavedArtDetail(rowId: Int): DataArtDetail? {
    //    return mapArtDetail[rowId]
    //}
}