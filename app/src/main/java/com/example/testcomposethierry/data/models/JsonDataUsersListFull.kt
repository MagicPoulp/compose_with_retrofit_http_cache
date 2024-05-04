package com.example.testcomposethierry.data.models

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

/*
curl https://randomuser.me/api/?inc=name,email&results=10&page=1&seed=merlin

{"results":[{"name":{"title":"Miss","first":"Caroline","last":"Reyes"},"email":"caroline.reyes@example.com"},{"name":{"title":"Mr","first":"Soham","last":"Wells"},"email":"soham.wells@example.com"},{"name":{"title":"Mr","first":"Cildo","last":"Costa"},"email":"cildo.costa@example.com"},{"name":{"title":"Miss","first":"Diana","last":"Ramirez"},"email":"diana.ramirez@example.com"},{"name":{"title":"Mrs","first":"Gül","last":"Doğan"},"email":"gul.dogan@example.com"},{"name":{"title":"Miss","first":"Debbie","last":"Peterson"},"email":"debbie.peterson@example.com"},{"name":{"title":"Mr","first":"Soham","last":"Wood"},"email":"soham.wood@example.com"},{"name":{"title":"Mr","first":"Adam","last":"Thomsen"},"email":"adam.thomsen@example.com"},{"name":{"title":"Mr","first":"Odeberto","last":"Caldeira"},"email":"odeberto.caldeira@example.com"},{"name":{"title":"Ms","first":"Veera","last":"Salmi"},"email":"veera.salmi@example.com"}],"info":{"seed":"merlin","results":10,"page":1,"version":"1.4"}}
*/
@JsonIgnoreProperties(ignoreUnknown = true)
data class JsonDataUsersListFull(
    @JsonProperty("results")
            val results: List<JsonDataUsersListElement>,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class JsonDataUsersListElement(
    @JsonProperty("email")
    var email: String,
    @JsonProperty("name")
    val name: JsonDataUsersListName
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class JsonDataUsersListName(
    @JsonProperty("first")
    var first: String,
    @JsonProperty("last")
    val last: String,
)
