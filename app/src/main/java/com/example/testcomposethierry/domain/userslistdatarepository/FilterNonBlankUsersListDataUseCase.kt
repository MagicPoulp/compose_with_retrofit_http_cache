package com.example.testcomposethierry.domain.userslistdatarepository

import com.example.testcomposethierry.data.models.DataUsersListElement
import javax.inject.Inject

class FilterNonBlankUsersListDataUseCase @Inject constructor() {
        operator fun invoke(artObjects: List<DataUsersListElement>): List<DataUsersListElement> {
                return artObjects.filter { it.email.isNotBlank() && it.name.first.isNotBlank() && it.name.last.isNotBlank() }
            }
    }