package com.example.antiscam.data.model

data class ScamAlert(
    val phoneNumber: String,
    val contactName: String?,
    val count: Long,
    val status: String,
    val lastReport: String?
)