package com.example.antiscam.screens.message

enum class ScanState {
    IDLE,       // chưa quét
    SCANNING,   // đang quét
    SAFE,       // an toàn
    SCAM        // lừa đảo
}