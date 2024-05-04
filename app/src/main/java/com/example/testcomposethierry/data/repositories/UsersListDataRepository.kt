package com.example.testcomposethierry.data.repositories

import com.example.testcomposethierry.data.custom_structures.ResultOf
import com.example.testcomposethierry.data.data_sources.InternetDataSource
import com.example.testcomposethierry.data.models.DomainDataUsersListElement
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UsersListDataRepository @Inject constructor(
    //private val internetWithHttpCacheDataSource: InternetWithHttpCacheDataSource,
    private val internetWithHttp: InternetDataSource,
) {

    suspend fun getUsersListPaged(pageSize: Int, pageOffset: Int): ResultOf<List<DomainDataUsersListElement>> {
        return internetWithHttp.getUsersListPaged(pageSize, pageOffset)
    }
}
