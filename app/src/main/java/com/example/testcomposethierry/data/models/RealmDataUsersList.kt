package com.example.testcomposethierry.data.models

import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey

class RealmDataUsersListElement(
    var email: String,
    var firstname: String,
    var lastname: String,
    var pageIndex: Int,
    var positionInPage: Int,
    // this assumes that the backed returns the data sorted by integers ID order in the backend
    @PrimaryKey
    var index: Int,
) : RealmObject {
    constructor() : this("", "", "", 0, 0, 0)
}
