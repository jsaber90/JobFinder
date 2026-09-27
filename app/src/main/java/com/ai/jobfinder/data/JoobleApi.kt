package com.ai.jobfinder.data

import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path

data class JoobleSearchRequest(
    val keywords: String,
    val location: String,
    val radius: String = "80",
    val page: String = "1",
    val companysearch: Boolean = false
)

data class JoobleResponse(
    val jobs: List<JoobleJob> = emptyList()
)

data class JoobleJob(
    val id: Long? = null,
    val title: String? = null,
    val location: String? = null,
    val snippet: String? = null,
    val source: String? = null,
    val link: String? = null,
    val company: String? = null,
    val updated: String? = null
)

interface JoobleApi {
    @POST("api/{apiKey}")
    suspend fun search(
        @Path("apiKey") apiKey: String,
        @Body request: JoobleSearchRequest
    ): JoobleResponse
}

