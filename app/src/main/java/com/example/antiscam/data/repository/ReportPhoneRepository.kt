package com.example.antiscam.data.repository

import android.util.Log
import com.example.antiscam.data.model.request.ReportRequest
import com.example.antiscam.data.model.response.ReportResponse
import com.example.antiscam.data.model.response.ScamCheckResponse
import com.example.antiscam.data.network.RetrofitClient
import com.example.antiscam.data.network.ScamApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ReportRepository(
    private val apiService: ScamApiService = RetrofitClient.apiService
) {
    suspend fun reportPhoneNumber(request: ReportRequest): ReportResponse? {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.reportPhone(request)

                return@withContext if (response.code == 200 && response.data != null) {
                    response.data
                } else {
                    Log.e(
                        "ScamCheckRepository",
                        "Error reporting phone: ${response.message}"
                    )
                    null
                }

            } catch (e: Exception) {
                Log.e("ScamCheckRepository", "Exception when reporting phone", e)
                null
            }
        }
    }
}