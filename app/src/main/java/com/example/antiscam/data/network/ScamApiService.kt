package com.example.antiscam.data.network

import com.example.antiscam.data.model.request.ReportRequest
import com.example.antiscam.data.model.request.ScamPredictRequest
import com.example.antiscam.data.model.response.ApiResponse
import com.example.antiscam.data.model.response.MessageResponse
import com.example.antiscam.data.model.response.ReportResponse
import com.example.antiscam.data.model.response.ScamCheckResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface ScamApiService {

    @GET("api/reports/check")
    suspend fun checkPhone(@Query("phone") phone: String): ApiResponse<ScamCheckResponse>

    @POST("api/reports/create")
    suspend fun reportPhone(@Body request: ReportRequest): ApiResponse<ReportResponse>

    @POST("api/scam/check")
    suspend fun checkMessage(@Body request: ScamPredictRequest): ApiResponse<MessageResponse>
}

