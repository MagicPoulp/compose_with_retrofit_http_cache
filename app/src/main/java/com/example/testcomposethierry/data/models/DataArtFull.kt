package com.example.testComposethierry.data.models

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import io.realm.kotlin.types.RealmObject

/*
curl https://www.rijksmuseum.nl/api/en/collection/AK-MAK-240?key=rIl6yb6x
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
    val objectNumber: String?,
    // added by the app from a second API call
    var detail: DataArtDetail?,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class DataArtDetailFull(
    @JsonProperty("artObjectPage")
    var artObjectPage: DataArtDetail?,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class DataArtDetail(
    @JsonProperty("plaqueDescription")
    var plaqueDescription: String?,
)
