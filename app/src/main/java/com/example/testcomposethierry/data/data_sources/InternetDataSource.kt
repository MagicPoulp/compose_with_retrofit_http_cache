package com.example.testcomposethierry.data.data_sources

import com.example.testcomposethierry.data.http.RetrofitHelper
import com.example.testcomposethierry.domain.userslistdatarepository.FilterNonBlankUsersListDataUseCase
import javax.inject.Inject

class InternetDataSource @Inject constructor(
    private val retrofitHelper: RetrofitHelper,
    private val filterNonBlankUsersListDataUseCase: FilterNonBlankUsersListDataUseCase,
) : InternetDataSourceAbstract(retrofitHelper, filterNonBlankUsersListDataUseCase) {
}
