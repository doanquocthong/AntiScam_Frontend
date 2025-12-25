package com.example.antiscam.data.model.response

data class MessageResponse (
    val label: String, //scam, normal, nghi ngờ
    val confidence: Double
)