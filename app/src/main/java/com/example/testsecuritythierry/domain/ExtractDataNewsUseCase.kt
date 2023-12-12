package com.example.testsecuritythierry.domain

import com.example.testsecuritythierry.data.models.DataArtElement
import com.example.testsecuritythierry.data.models.DataArtFull
import javax.inject.Inject

class ExtractDataArtUseCase @Inject constructor() {
    operator fun invoke(dataArtFull: DataArtFull): List<DataArtElement> {
        return dataArtFull.artObjects.filter { it2 -> it2.title != null }
    }
}