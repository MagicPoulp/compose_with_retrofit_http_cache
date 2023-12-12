package com.example.testsecuritythierry.data.models

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

/*
curl https://www.rijksmuseum.nl/api/nl/collection/AK-MAK-240?key=rIl6yb6x
*/
@JsonIgnoreProperties(ignoreUnknown = true)
data class DataArtFull(
    @JsonProperty("artObjects")
    val artObjects: List<DataArtElement>,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class DataArtElement(
    @JsonProperty("title")
    var title: String?,
    @JsonProperty("objectNumber")
    val objectNumber: String,
)
