package com.example.testcomposethierry.data.data_sources

import com.example.testcomposethierry.data.config.AppConfig
import com.example.testcomposethierry.data.custom_structures.ResultOf
import com.example.testcomposethierry.data.http.RetrofitHelperInterface
import com.example.testcomposethierry.data.http.UsersApi
import com.example.testcomposethierry.data.models.DomainDataUsersListElement
import com.example.testcomposethierry.data.models.JsonDataUsersListFull
import com.example.testcomposethierry.domain.internetdatasourceabstract.MapJsonDataToDomainDataUseCase
import com.example.testcomposethierry.domain.userslistdatarepository.FilterNonBlankUsersListDataUseCase
import javax.inject.Inject

open class InternetDataSourceAbstract @Inject constructor(
    private val retrofitHelper: RetrofitHelperInterface,
    private val mapJsonDataToDomainDataUseCase: MapJsonDataToDomainDataUseCase,
    private val filterNonBlankUsersListDataUseCase: FilterNonBlankUsersListDataUseCase,
) {
    private lateinit var api: UsersApi

    suspend fun getUsersListPaged(
        pageSize: Int,
        pageOffset: Int
    ): ResultOf<List<DomainDataUsersListElement>> {
        getUsersListPagedDoBefore(pageSize, pageOffset)
        return getUsersListPagedPerform(pageSize, pageOffset)
    }

    protected open suspend fun getUsersListPagedDoBefore(
        pageSize: Int,
        pageOffset: Int
    ) {
    }

    // ------------------------------------------------------------

    init {
        createApi()
    }

    private fun createApi() = run {
        api = retrofitHelper.getInstance(
            baseUrl = AppConfig.usersBaseUrl,
            okHttpClient = null,
            requestHeaders = null
        ).create(UsersApi::class.java)
    }

    private suspend fun getUsersListPagedPerform(
        pageSize: Int,
        pageOffset: Int
    ): ResultOf<List<DomainDataUsersListElement>> {
        try {
            // uncomment to test the error screen
            //if (pageOffset == 2)
            //    return ResultOf.Failure("TEST", null)

            val inc = "name,email"
            val response = api.getUsersListPaged(inc, pageSize, pageOffset, AppConfig.seed)
            if (response.isSuccessful) {
                return response.body()?.let { dataUsersListFull: JsonDataUsersListFull ->
                    val mappedData = mapJsonDataToDomainDataUseCase(dataUsersListFull)
                    ResultOf.Success(filterNonBlankUsersListDataUseCase(mappedData))
                } ?: run {  ResultOf.Failure(response.message(), null) }
            }
            return ResultOf.Failure(response.message(), null)
        } catch (e: Exception) {
            return ResultOf.Failure(e.message, e)
        }
    }
}
