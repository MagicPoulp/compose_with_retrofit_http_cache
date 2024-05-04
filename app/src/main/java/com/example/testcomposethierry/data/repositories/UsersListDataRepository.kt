package com.example.testcomposethierry.data.repositories

import com.example.testcomposethierry.data.custom_structures.ResultOf
import com.example.testcomposethierry.data.data_sources.InternetDataSource
import com.example.testcomposethierry.data.data_sources.RealmDatabaseDataSource
import com.example.testcomposethierry.data.http.NetworkConnectionManager
import com.example.testcomposethierry.data.models.DomainDataUsersListElement
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UsersListDataRepository @Inject constructor(
    //private val internetWithHttpCacheDataSource: InternetWithHttpCacheDataSource,
    private val internetWithHttp: InternetDataSource,
    private val realmDatabaseDataSource: RealmDatabaseDataSource,
    private val networkConnectionManager: NetworkConnectionManager,
) {

    suspend fun getUsersListPaged(pageSize: Int, pageOffset: Int): ResultOf<List<DomainDataUsersListElement>> {
        if (networkConnectionManager.isConnected.value) {
            val result = internetWithHttp.getUsersListPaged(pageSize, pageOffset)
            if (result is ResultOf.Success) {
                realmDatabaseDataSource.saveUsersList(result.value)
            }
            return result
        }
        return realmDatabaseDataSource.getUsersListPaged(pageSize, pageOffset)
    }
}
