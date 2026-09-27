package com.ai.jobfinder.data

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object JoobleClient {
    val api: JoobleApi = Retrofit.Builder()
        .baseUrl("https://eg.jooble.org/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(JoobleApi::class.java)
}

