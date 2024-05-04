package com.example.testcomposethierry.data.data_sources

import com.example.testcomposethierry.data.config.AppConfig
import com.example.testcomposethierry.data.custom_structures.ResultOf
import com.example.testcomposethierry.data.http.RetrofitHelperInterface
import com.example.testcomposethierry.data.http.UsersApi
import com.example.testcomposethierry.data.models.DataUsersListElement
import com.example.testcomposethierry.data.models.DataUsersListFull
import com.example.testcomposethierry.domain.userslistdatarepository.FilterNonBlankUsersListDataUseCase
import javax.inject.Inject

open class InternetDataSourceAbstract @Inject constructor(
    private val retrofitHelper: RetrofitHelperInterface,
    private val filterNonBlankUsersListDataUseCase: FilterNonBlankUsersListDataUseCase,
) {
    private lateinit var api: UsersApi

    suspend fun getUsersListPaged(
        pageSize: Int,
        pageOffset: Int
    ): ResultOf<List<DataUsersListElement>> {
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
    ): ResultOf<List<DataUsersListElement>> {
        try {
            // uncomment to test the error screen
            //if (pageOffset == 2)
            //    return ResultOf.Failure("TEST", null)

            // needed delay so that internet connectivity can be detected before the first request
            val inc = "name,email"
            val response = api.getUsersListPaged(inc, pageSize, pageOffset, AppConfig.seed)
            if (response.isSuccessful) {
                response.body()?.let { dataUsersListFull: DataUsersListFull ->
                    return ResultOf.Success(filterNonBlankUsersListDataUseCase(dataUsersListFull.results))
                }
            }
            return ResultOf.Failure(response.message(), null)
        } catch (e: Exception) {
            return ResultOf.Failure(e.message, e)
        }
    }
}
