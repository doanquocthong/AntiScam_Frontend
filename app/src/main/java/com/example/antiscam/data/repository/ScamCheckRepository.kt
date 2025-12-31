package com.example.antiscam.data.repository

import android.util.Log
import com.example.antiscam.data.model.request.ReportRequest
import com.example.antiscam.data.model.request.ScamPredictRequest
import com.example.antiscam.data.model.response.ApiResponse
import com.example.antiscam.data.model.response.MessageResponse
import com.example.antiscam.data.model.response.ReportResponse
import com.example.antiscam.data.model.response.ScamCheckResponse
import com.example.antiscam.data.network.RetrofitClient
import com.example.antiscam.data.network.ScamApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ScamCheckRepository(
    private val apiService: ScamApiService = RetrofitClient.apiService
) {
    suspend fun checkPhoneNumber(phoneNumber: String): ApiResponse<ScamCheckResponse>? {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.checkPhone(phoneNumber)
                Log.d("ScamCheckRepository", "Success checking scam number, response = $response")
                response
            } catch (e: Exception) {
                Log.e("ScamCheckRepository", "Error checking scam number", e)
                null
            }
        }
    }
    suspend fun checkMessage(request: ScamPredictRequest): ApiResponse<MessageResponse>? {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.checkMessage(request)
                Log.d("ScamCheckRepository", "Check message success, response = $response")
                response
            } catch (e: Exception) {
                Log.e("ScamCheckRepository", "Error checking scam message", e)
                null
            }
        }
    }

}

