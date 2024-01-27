package com.example.testcomposethierry.domain.artdatarepository

import com.example.testcomposethierry.data.models.DataArtElement
import javax.inject.Inject

class ExtractDataArtUseCase @Inject constructor() {
    operator fun invoke(artObjects: List<DataArtElement>): List<DataArtElement> {
        return artObjects.filter { it.title != null && it.objectNumber != null }
    }
}
