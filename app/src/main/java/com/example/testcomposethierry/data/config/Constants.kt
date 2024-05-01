package com.example.testcomposethierry.data.config

object AppConfig {
    const val artBaseUrl = "https://randomuser.me/"
    const val pagingSize = 10
    // we need a seed to simulate pages so that when we scroll, we keep the same data
    const val seed = "merlin"
    const val httpGetCacheActive = true
}
