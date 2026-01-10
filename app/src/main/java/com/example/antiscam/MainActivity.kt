package com.example.antiscam

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.antiscam.observer.CallLogObserver
import com.example.antiscam.observer.SmsObserver
import com.example.antiscam.screens.navigation.AppNavGraph

class MainActivity : ComponentActivity() {

    private val REQUIRED_PERMISSIONS = arrayOf(
        Manifest.permission.READ_CALL_LOG,
        Manifest.permission.READ_SMS,
        Manifest.permission.RECEIVE_SMS
    )

    private val permissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { result ->

            val allGranted = result.values.all { it }

            if (allGranted) {
                Log.d("MainActivity", "Permissions granted")
                onPermissionGranted()
            } else {
                Log.w("MainActivity", "Permissions denied")
            }
        }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (hasAllPermissions()) {
            onPermissionGranted()
        } else {
            permissionLauncher.launch(REQUIRED_PERMISSIONS)
        }

        setContent {
             AppNavGraph()
        }
    }

    private fun onPermissionGranted() {
        SmsObserver.register(this)
        CallLogObserver.register(this)
    }

    private fun hasAllPermissions(): Boolean {
        return REQUIRED_PERMISSIONS.all {
            ContextCompat.checkSelfPermission(
                this,
                it
            ) == PackageManager.PERMISSION_GRANTED
        }
    }
}
