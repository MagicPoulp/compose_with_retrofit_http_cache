package com.example.testcomposethierry.data.data_sources

import com.example.testcomposethierry.data.http.NetworkConnectionManager
import com.example.testcomposethierry.data.http.RetrofitHelperWithGetHttpCache
import com.example.testcomposethierry.domain.internetdatasourceabstract.MapJsonDataToDomainDataUseCase
import com.example.testcomposethierry.domain.internetdatasourceabstract.FilterNonBlankUsersListDataUseCase
import kotlinx.coroutines.delay
import javax.inject.Inject

class InternetWithHttpCacheDataSource @Inject constructor(
    private val retrofitHelper: RetrofitHelperWithGetHttpCache,
    private val mapJsonDataToDomainDataUseCase: MapJsonDataToDomainDataUseCase,
    private val filterNonBlankUsersListDataUseCase: FilterNonBlankUsersListDataUseCase,
    private val networkConnectionManager: NetworkConnectionManager,
) : InternetDataSourceAbstract(retrofitHelper, mapJsonDataToDomainDataUseCase, filterNonBlankUsersListDataUseCase) {

    override suspend fun getUsersListPagedDoBefore(pageSize: Int, pageOffset: Int) {
        // needed delay so that internet connectivity can be detected before the first request
        var i = 0
        while (i < 10) {
            if (!networkConnectionManager.isInitialized) {
                delay(100L)
            }
            i += 1
        }
    }
}