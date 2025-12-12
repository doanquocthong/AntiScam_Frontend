package com.example.antiscam.data.model.response

data class ReportResponse(
    val success: Boolean,
    val message: String,
    val reportId: Int?
)

