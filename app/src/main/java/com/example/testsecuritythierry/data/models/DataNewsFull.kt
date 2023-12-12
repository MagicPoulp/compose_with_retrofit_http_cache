package com.example.testsecuritythierry.data.models

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

/*
curl https://www.rijksmuseum.nl/api/nl/collection/AK-MAK-240?key=rIl6yb6x
*/
@JsonIgnoreProperties(ignoreUnknown = true)
data class DataNewsFull(
    @JsonProperty("artObjects")
    val artObjects: List<DataNewsElement>,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class DataNewsElement(
    @JsonProperty("title")
    var title: String?,
    /*
    @JsonProperty("objectNumber")
    val objectNumber: String,*/
)
