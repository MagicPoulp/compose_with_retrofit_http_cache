package com.example.testsecuritythierry.domain

import com.example.testsecuritythierry.data.models.DataArtElement
import com.example.testsecuritythierry.data.models.DataArtFull
import javax.inject.Inject

class ExtractDataArtUseCase @Inject constructor() {
    operator fun invoke(artObjects: List<DataArtElement>): List<DataArtElement> {
        return artObjects.filter { it2 -> it2.title != null && it2.objectNumber != null && it2.detail != null}
    }
}