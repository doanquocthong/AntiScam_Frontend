package com.example.antiscam.data.model.request

data class  ReportRequest(
    val reporterName: String,
    val reporterPhone: String,
    val phone: String,
    val email: String,
    val scamType: String,
    val description: String,
    val evidenceLink: String
)

