package com.example.testcomposethierry.domain.internetdatasourceabstract

import com.example.testcomposethierry.data.models.DomainDataUsersListElement
import javax.inject.Inject

class FilterNonBlankUsersListDataUseCase @Inject constructor() {
        operator fun invoke(artObjects: List<DomainDataUsersListElement>): List<DomainDataUsersListElement> {
                return artObjects.filter { it.email.isNotBlank() && it.firstname.isNotBlank() && it.lastname.isNotBlank() }
            }
    }