package com.example.testComposethierry.data.models

import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey

class DataResponseRealm(
    @PrimaryKey
    var url: String,
    var body: ByteArray,
) : RealmObject {
    constructor() : this("", byteArrayOf())
}
