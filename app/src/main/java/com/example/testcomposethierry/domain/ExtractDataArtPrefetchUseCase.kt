package com.example.testcomposethierry.domain

import com.example.testcomposethierry.data.models.DataArtElement
import com.example.testcomposethierry.data.models.DataArtFull
import javax.inject.Inject

class ExtractDataArtPrefetchUseCase @Inject constructor() {
    operator fun invoke(dataArtFull: DataArtFull): List<DataArtElement> {
        return dataArtFull.artObjects.filter { it2 -> it2.title != null && it2.objectNumber != null}
    }
}