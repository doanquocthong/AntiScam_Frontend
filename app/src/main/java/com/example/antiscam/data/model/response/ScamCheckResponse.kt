package com.example.antiscam.data.model.response

data class ScamCheckResponse(
    val phone: String = "",
    val reported: Boolean = false,
    val count: Long = 0,
    val status: String = "",
    val lastReport: String = "",
    val isScam: Boolean = false
)