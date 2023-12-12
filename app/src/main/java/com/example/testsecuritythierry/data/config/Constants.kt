package com.example.testsecuritythierry.data.config

const val analysisRefreshInterval = 30*1000L

object AppConfig {
    // it must end with a slash
    const val newsBaseUrl = "https://www.rijksmuseum.nl/"
    const val localNewsBaseUrl = "https://localhost:8443/"
    const val pagingSize = 3
    const val HTTPSPort = 8443
    const val apiKey = "rIl6yb6x"
}
