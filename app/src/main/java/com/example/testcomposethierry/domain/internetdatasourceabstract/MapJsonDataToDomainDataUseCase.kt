package com.example.testcomposethierry.domain.internetdatasourceabstract

import com.example.testcomposethierry.data.models.DomainDataUsersListElement
import com.example.testcomposethierry.data.models.JsonDataUsersListFull
import javax.inject.Inject

class MapJsonDataToDomainDataUseCase @Inject constructor() {
    operator fun invoke(fullData: JsonDataUsersListFull): List<DomainDataUsersListElement> {
        val list = fullData.results
        return list.map {
            DomainDataUsersListElement(
                it.email,
                it.name.first,
                it.name.last,
            )
        }
    }
}