package com.example.testcomposethierry.data.repositories

import com.example.testcomposethierry.data.config.AppConfig
import com.example.testcomposethierry.data.custom_structures.ResultOf
import com.example.testcomposethierry.data.http.NetworkConnectionManager
import com.example.testcomposethierry.data.http.UsersApi
import com.example.testcomposethierry.data.http.RetrofitHelper
import com.example.testcomposethierry.data.models.DataUsersListElement
import com.example.testcomposethierry.data.models.DataUsersListFull
import com.example.testcomposethierry.domain.userslistdatarepository.FilterNonBLankUsersListDataUseCase
import kotlinx.coroutines.delay
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UsersListDataRepository @Inject constructor(
    private val retrofitHelper: RetrofitHelper,
    private val filterNonBLankUsersListDataUseCase: FilterNonBLankUsersListDataUseCase,
    private val networkConnectionManager: NetworkConnectionManager,
) {
    private lateinit var api: UsersApi

    init {
        createApi()
    }

    private fun createApi() = run {
        api = retrofitHelper.getInstance(
            baseUrl = AppConfig.artBaseUrl,
            okHttpClient = null,
            requestHeaders = null
        ).create(UsersApi::class.java)
    }

    suspend fun getUsersListPaged(pageSize: Int, pageOffset: Int): ResultOf<List<DataUsersListElement>> {
        try {
            // uncomment to test the error screen
            //if (pageOffset == 2)
            //    return ResultOf.Failure("TEST", null)
            var i = 0
            while (i < 10) {
                if (!networkConnectionManager.isInitialized) {
                    delay(100L)
                }
                i += 1
            }
            val inc="name,email"
            val response = api.getUsersListPaged(inc, pageSize, pageOffset, AppConfig.seed)
            if (response.isSuccessful) {
                response.body()?.let { dataUsersListFull: DataUsersListFull ->
                    return ResultOf.Success(filterNonBLankUsersListDataUseCase(dataUsersListFull.results))
                }
            }
            return ResultOf.Failure(response.message(), null)
        } catch (e: Exception) {
            return ResultOf.Failure(e.message, e)
        }
    }
}
