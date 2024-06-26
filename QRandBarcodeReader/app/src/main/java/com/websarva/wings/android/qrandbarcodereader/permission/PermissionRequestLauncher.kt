package com.websarva.wings.android.qrandbarcodereader.permission

import android.app.Activity
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat

interface PermissionRequestLauncher {
    fun launch()
}

fun ComponentActivity.registerForPermissionRequest(
    permission: String,
    callback: (granted: Boolean, succeedToShowDialog: Boolean) -> Unit,
): PermissionRequestLauncher =
    PermissionRequestLauncherImpl({ this }, permission).also { launcher ->
        launcher.launcher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            Log.d("TAG", "registerForPermissionRequest")
            callback(granted, launcher.succeedToShowDialog())
        }
    }

class PermissionRequestLauncherImpl(
    private val activitySupplier: () -> Activity,
    private val permission: String,
) : PermissionRequestLauncher {
    lateinit var launcher: ActivityResultLauncher<String>
    private var shouldShowRationalBefore: Boolean = false
    private var start: Long = 0L

    fun succeedToShowDialog(): Boolean {

        Log.d("TAG", "succeedToShowDialog")

        if (shouldShowRationalBefore) return true
        if (System.currentTimeMillis() - start > ENOUGH_DURATION) return true
        return ActivityCompat.shouldShowRequestPermissionRationale(activitySupplier(), permission)
    }

    override fun launch() {

        Log.d("TAG", "launch")

        start = System.currentTimeMillis()
        shouldShowRationalBefore =
            ActivityCompat.shouldShowRequestPermissionRationale(activitySupplier(), permission)
        launcher.launch(permission)
    }

    companion object {
        private const val ENOUGH_DURATION = 1000L
    }
}