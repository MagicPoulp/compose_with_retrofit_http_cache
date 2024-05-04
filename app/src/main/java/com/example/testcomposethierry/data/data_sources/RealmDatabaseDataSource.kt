package com.example.testcomposethierry.data.data_sources

import com.example.testcomposethierry.data.config.AppConfig
import com.example.testcomposethierry.data.custom_structures.ResultOf
import com.example.testcomposethierry.data.models.DomainDataUsersListElement
import com.example.testcomposethierry.data.models.RealmDataUsersListElement
import io.realm.kotlin.Realm
import io.realm.kotlin.RealmConfiguration
import io.realm.kotlin.UpdatePolicy
import io.realm.kotlin.ext.query
import io.realm.kotlin.query.RealmResults
import javax.inject.Inject
import javax.inject.Singleton

// it must be a singleton because we close Realm in MainActivityViewModel
@Singleton
class RealmDatabaseDataSource @Inject constructor() {

    private var realm: Realm

    init {
        val config = RealmConfiguration.create(schema = setOf(RealmDataUsersListElement::class))
        realm = Realm.open(config)
    }

    fun close() {
        realm.close()
    }

    fun getUsersListPaged(
        pageSize: Int,
        pageOffset: Int
    ): ResultOf<List<DomainDataUsersListElement>> {
       return readUserDataWithRange((pageOffset * pageSize ..< (pageOffset + 1) * pageSize).toList())
    }

    fun saveUsersList(users: List<DomainDataUsersListElement>) {
        val realmList = users.map { RealmDataUsersListElement(
            email = it.email,
            firstname = it.firstname,
            lastname = it.lastname,
            pageIndex = it.pageIndex,
            positionInPage = it.positionInPage,
            index = it.pageIndex * AppConfig.pagingSize + it.positionInPage
        ) }
        realmList.map { saveUserData(it) }
    }

    private fun saveUserData(userData: RealmDataUsersListElement) {
        try {
            realm.writeBlocking {
                // TODO write in a batch a full list, which may require new Realm object definitions and a new schema
                copyToRealm(
                    userData, UpdatePolicy.ALL
                )
            }
        } catch(t: Throwable) {
            System.err.println(t.message)
            // TODO make a specific type of error for Realm access so it is not confused with the HTTP request
            throw t
        }
    }

    private fun readUserDataWithRange(list: List<Int>): ResultOf<List<DomainDataUsersListElement>> {
        try {
            val sortedList = list.sorted()
            return realm.writeBlocking {
                // https://www.mongodb.com/docs/atlas/device-sdks/realm-query-language/
                val result: RealmResults<RealmDataUsersListElement> = realm.query<RealmDataUsersListElement>("index BETWEEN {$0, $1}", sortedList.first(), sortedList.last()).find()
                val resultDomain = result.map {
                    DomainDataUsersListElement(
                        email = it.email,
                        firstname = it.firstname,
                        lastname = it.lastname,
                        pageIndex = it.pageIndex,
                        positionInPage = it.positionInPage,
                        index = it.pageIndex * AppConfig.pagingSize + it.positionInPage
                    )
                }
                ResultOf.Success(resultDomain)
            }
        } catch(t: Throwable) {
            // this case cannot be considered as an error, it just means a cache miss
            return ResultOf.Failure(t.message, t)
        }
    }
}
