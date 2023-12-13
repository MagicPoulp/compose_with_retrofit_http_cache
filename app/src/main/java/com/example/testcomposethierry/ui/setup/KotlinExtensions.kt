package com.example.testComposethierry.ui.setup

// https://stackoverflow.com/questions/59553126/java-or-kotlin-create-as-much-of-sublist-as-possible
fun <T> List<T>.safeSubList(fromIndex: Int, toIndex: Int): List<T> =
    this.subList(fromIndex, toIndex.coerceAtMost(this.size))
