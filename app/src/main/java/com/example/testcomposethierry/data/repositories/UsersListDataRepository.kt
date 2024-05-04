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
            try {
                val result = internetWithHttp.getUsersListPaged(pageSize, pageOffset)
                if (result is ResultOf.Success) {
                    realmDatabaseDataSource.saveUsersList(result.value)
                }
                return result
            }
            catch (t: Throwable)
            {
                networkConnectionManager.checkAgainInternet()
                // TODO make a nicer mechanism of retry (several times, not for all requests, well tested)
                // if internet works fine but an isolated URL is down,
                // we will check internet every time on that url
                System.err.println(t.message)
                return realmDatabaseDataSource.getUsersListPaged(pageSize, pageOffset)
            }
        }
        // TODO, when internet comes back online, we may ask the user to refresh cached data
        return realmDatabaseDataSource.getUsersListPaged(pageSize, pageOffset)
    }
}
