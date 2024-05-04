package com.example.testcomposethierry.data.models

data class DomainDataUsersListElement(
    var email: String,
    var firstname: String,
    val lastname: String,
    val pageIndex: Int,
    val positionInPage: Int,
    val index: Int
)
