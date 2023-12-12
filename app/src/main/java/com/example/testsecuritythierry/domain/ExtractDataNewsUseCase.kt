package com.example.testsecuritythierry.domain

import com.example.testsecuritythierry.data.models.DataNewsElement
import com.example.testsecuritythierry.data.models.DataNewsFull
import javax.inject.Inject

class ExtractDataNewsUseCase @Inject constructor() {
    operator fun invoke(dataNewsFull: DataNewsFull): List<DataNewsElement> {
        return dataNewsFull.artObjects.filter { it2 -> it2.title != null }
    }
}