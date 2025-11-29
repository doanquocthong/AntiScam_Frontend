package com.example.antiscam.data.network

import com.example.antiscam.data.model.response.ScamCheckResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface ScamApiService {

    @GET("api/reports/check")
    suspend fun checkPhone(@Query("phone") phone: String): ScamCheckResponse
}

