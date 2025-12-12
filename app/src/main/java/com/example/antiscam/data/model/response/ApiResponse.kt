package com.example.antiscam.data.model.response

data class ApiResponse<T>(
    val code: Int,
    val message: String?,
    val data: T?
)