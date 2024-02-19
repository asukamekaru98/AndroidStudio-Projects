/*
 * Copyright (c) 2021 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package com.websarva.wings.android.qrandbarcodereader

//import com.websarva.wings.android.qrandbarcodereader.util.Launcher
import android.animation.ValueAnimator
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.camera.core.ImageProxy
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.updateLayoutParams
import com.google.mlkit.vision.barcode.common.Barcode
import com.websarva.wings.android.qrandbarcodereader.Barcode.BcdScanner
import com.websarva.wings.android.qrandbarcodereader.constant.formatStr
import com.websarva.wings.android.qrandbarcodereader.constant.typeStr
import com.websarva.wings.android.qrandbarcodereader.databinding.ActivityScanBinding
import com.websarva.wings.android.qrandbarcodereader.permission.CameraPermission
import com.websarva.wings.android.qrandbarcodereader.permission.DialogPermission
import com.websarva.wings.android.qrandbarcodereader.permission.registerForCameraPermissionRequest
import com.websarva.wings.android.qrandbarcodereader.result.ScanResult
import com.websarva.wings.android.qrandbarcodereader.result.ScanResultAdapter
import com.websarva.wings.android.qrandbarcodereader.setting.Settings
import com.websarva.wings.android.qrandbarcodereader.util.ReviewRequester

class ScanActivity : AppCompatActivity() {
    private lateinit var binding: ActivityScanBinding   //バインド
    private lateinit var bcdScanner: BcdScanner         //バーコードスキャナ
    private var started: Boolean = false
    private val launcher = registerForCameraPermissionRequest { granted, succeedToShowDialog ->
        if (granted) {
            startCamera()
        } else if (!succeedToShowDialog) {
            DialogPermission.show(this, CAMERA_PERMISSION_REQUEST_KEY)
        } else {
            finishByError()
        }
    }

    private lateinit var adapter: ScanResultAdapter
    private lateinit var vibrator: Vibrator
    private lateinit var detectedPresenter: DetectedPresenter
    private val viewModel: MainActivityViewModel by viewModels()
    private val settings: Settings by lazy {
        Settings.get()
    }
    private var resultSet: Set<ScanResult> = emptySet()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scan)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)   //ツールバー

        setSupportActionBar(toolbar)                        //アクションバーにツールバーをセット
        supportActionBar?.setDisplayHomeAsUpEnabled(true)   //ツールバーに戻るボタンを設置
    }


    // ツールバーのアイテムを押した時の処理を記述（今回は戻るボタンのみのため、戻るボタンを押した時の処理しか記述していない）
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // android.R.id.home に戻るボタンを押した時のidが取得できる
        if (item.itemId == android.R.id.home) {
            // 今回はActivityを終了させている
            finish()
        }
        return super.onOptionsItemSelected(item)
    }

/*
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.license -> LicenseActivity.start(this)
            R.id.source_code -> Launcher.openSourceCode(this)
            R.id.privacy_policy -> Launcher.openPrivacyPolicy(this)
            R.id.share_this_app -> Launcher.shareThisApp(this)
            R.id.play_store -> Launcher.openGooglePlay(this)
            R.id.settings -> SettingsActivity.start(this)
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }
 */
    override fun onRestart() {
        super.onRestart()
        if (!started) {
            if (CameraPermission.hasPermission(this)) {
                startCamera()
            } else {
                finishByError()
            }
        }
    }

    private fun finishByError() {
        toastPermissionError()
        super.finish()
    }

    override fun finish() {
        if (ReviewRequester.requestIfNecessary(this)) {
            return
        }
        super.finish()
    }

    private fun toastPermissionError() {
        Toast.makeText(this, R.string.toast_permission_required, Toast.LENGTH_LONG).show()
    }

/*
    private fun onFlashOn(on: Boolean) {
        val icon = if (on) {
            R.drawable.ic_flash_on
        } else {
            R.drawable.ic_flash_off
        }
        binding.flash.setImageResource(icon)
    }

 */

    private fun startCamera() {
        if (started) return
        started = true
        bcdScanner.start()
    }

    private fun onDetectCode(imageProxy: ImageProxy, codes: List<Barcode>) {
        val detected = mutableListOf<Barcode>()
        codes.forEach {
            val value = it.rawValue ?: return@forEach
            val result = ScanResult(
                value = value,
                type = it.typeStr(),
                format = it.formatStr(),
                isUrl = it.valueType == Barcode.TYPE_URL,
            )
            if (!resultSet.contains(result)) {
                viewModel.add(result)
                vibrate()
                detected.add(it)
            }
        }
        if (detected.isEmpty()) return
        detectedPresenter.onDetected(imageProxy, detected)
    }

    private fun expandList() {
        ValueAnimator.ofInt(binding.dummy.height, 0)
            .also {
                it.addUpdateListener {
                    binding.dummy.updateLayoutParams<ConstraintLayout.LayoutParams> {
                        height = it.animatedValue as Int
                    }
                }
            }.start()
    }

    private fun vibrate() {
        if (!settings.vibrate) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(
                VibrationEffect.createOneShot(30, VibrationEffect.DEFAULT_AMPLITUDE),
            )
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(30)
        }
    }

    /*
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

     */

    companion object {
        private const val CAMERA_PERMISSION_REQUEST_KEY = "CAMERA_PERMISSION_REQUEST_KEY"
    }
}