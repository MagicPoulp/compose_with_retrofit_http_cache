package com.example.testcomposethierry.data.data_sources

import com.example.testcomposethierry.data.http.RetrofitHelper
import com.example.testcomposethierry.domain.internetdatasourceabstract.MapJsonDataToDomainDataUseCase
import com.example.testcomposethierry.domain.internetdatasourceabstract.FilterNonBlankUsersListDataUseCase
import javax.inject.Inject

class InternetDataSource @Inject constructor(
    private val retrofitHelper: RetrofitHelper,
    private val mapJsonDataToDomainDataUseCase: MapJsonDataToDomainDataUseCase,
    private val filterNonBlankUsersListDataUseCase: FilterNonBlankUsersListDataUseCase,
) : InternetDataSourceAbstract(retrofitHelper, mapJsonDataToDomainDataUseCase, filterNonBlankUsersListDataUseCase) {
}
