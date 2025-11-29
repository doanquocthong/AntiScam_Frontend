package com.example.antiscam.data.model.response

data class ScamCheckResponse(
    val phone: String,
    val reported: Boolean,
    val count: Long,
    val status: String,
    val lastReport: String?
)