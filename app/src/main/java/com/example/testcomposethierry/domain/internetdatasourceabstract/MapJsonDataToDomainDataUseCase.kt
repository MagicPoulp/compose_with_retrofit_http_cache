package com.example.testcomposethierry.domain.internetdatasourceabstract

import com.example.testcomposethierry.data.models.DomainDataUsersListElement
import com.example.testcomposethierry.data.models.JsonDataUsersListFull
import javax.inject.Inject

class MapJsonDataToDomainDataUseCase @Inject constructor() {
    operator fun invoke(fullData: JsonDataUsersListFull, pageIndex: Int): List<DomainDataUsersListElement> {
        val list = fullData.results
        var index = 0
        // TODO add timestamp and expiration
        return list.mapIndexed { positionInPage, it ->
            DomainDataUsersListElement(
                it.email,
                it.name.first,
                it.name.last,
                pageIndex,
                positionInPage,
                index++
            )
        }
    }
}