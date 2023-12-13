package com.example.testcomposethierry.domain

import com.example.testcomposethierry.data.models.DataArtElement
import com.example.testcomposethierry.data.models.DataArtFull
import javax.inject.Inject

class ExtractDataArtUseCase @Inject constructor() {
    operator fun invoke(artObjects: List<DataArtElement>): List<DataArtElement> {
        return artObjects.filter { it -> it.title != null && it.objectNumber != null }
    }
}