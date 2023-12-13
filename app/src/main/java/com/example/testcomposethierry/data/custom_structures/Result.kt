package com.example.testComposethierry.data.custom_structures

// https://medium.com/swlh/kotlin-sealed-class-for-success-and-error-handling-d3054bef0d4e
// Any prevents null data
sealed class ResultOf<out T: Any> {
    data class Loading(val string: String = ""): ResultOf<Nothing>()
    data class Success<out R: Any>(val value: R): ResultOf<R>()
    data class Failure(
        val message: String?,
        val throwable: Throwable?
    ): ResultOf<Nothing>()
}
