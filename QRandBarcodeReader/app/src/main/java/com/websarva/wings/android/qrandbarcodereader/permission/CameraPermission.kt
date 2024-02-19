package com.websarva.wings.android.qrandbarcodereader.permission

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.ComponentActivity
import androidx.core.content.ContextCompat

object CameraPermission {
    fun hasPermission(context: Context) =
        ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED
}

fun ComponentActivity.registerForCameraPermissionRequest(
    callback: (granted: Boolean, succeedToShowDialog: Boolean) -> Unit,
): PermissionRequestLauncher = registerForPermissionRequest(Manifest.permission.CAMERA, callback)