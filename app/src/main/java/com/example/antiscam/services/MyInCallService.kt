package com.example.antiscam.services

import android.telecom.InCallService

class MyInCallService : InCallService() {
    override fun onCallAdded(call: android.telecom.Call) {
        super.onCallAdded(call)
    }

    override fun onCallRemoved(call: android.telecom.Call) {
        super.onCallRemoved(call)
    }
}
