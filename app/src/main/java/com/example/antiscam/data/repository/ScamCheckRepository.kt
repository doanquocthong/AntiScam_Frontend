package com.example.antiscam.data.repository

import android.util.Log
import com.example.antiscam.data.model.response.ScamCheckResponse
import com.example.antiscam.data.network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ScamCheckRepository(
    private val apiService: com.example.antiscam.data.network.ScamApiService = RetrofitClient.apiService
) {

    suspend fun checkPhoneNumber(phoneNumber: String): ScamCheckResponse? {
        return withContext(Dispatchers.IO) {
            try {
                apiService.checkPhone(phoneNumber)
            } catch (e: Exception) {
                Log.e("ScamCheckRepository", "Error checking scam number", e)
                null
            }
        }
    }
}

