package com.websarva.wings.android.qrandbarcodereader

import android.animation.ValueAnimator
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.ImageProxy
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.getSystemService
import androidx.core.view.isGone
import androidx.core.view.updateLayoutParams
import androidx.recyclerview.widget.DividerItemDecoration
import com.google.mlkit.vision.barcode.common.Barcode
import com.websarva.wings.android.qrandbarcodereader.Barcode.BcdScanner
import com.websarva.wings.android.qrandbarcodereader.BlueTooth.BlueTooth
import com.websarva.wings.android.qrandbarcodereader.constant.formatStr
import com.websarva.wings.android.qrandbarcodereader.constant.typeStr
import com.websarva.wings.android.qrandbarcodereader.databinding.ActivityScanBinding
import com.websarva.wings.android.qrandbarcodereader.permission.CameraPermission
import com.websarva.wings.android.qrandbarcodereader.permission.DialogPermission
import com.websarva.wings.android.qrandbarcodereader.permission.registerForCameraPermissionRequest
import com.websarva.wings.android.qrandbarcodereader.result.ScanResult
import com.websarva.wings.android.qrandbarcodereader.result.ScanResultAdapter
import com.websarva.wings.android.qrandbarcodereader.result.ScanResultDialog
import com.websarva.wings.android.qrandbarcodereader.setting.Settings
import com.websarva.wings.android.qrandbarcodereader.util.ReviewRequester
import com.websarva.wings.android.qrandbarcodereader.util.Updater
import com.websarva.wings.android.qrandbarcodereader.util.observe

class ScanBlueToothAddressActivity : AppCompatActivity(){

    companion object {
        private const val CAMERA_PERMISSION_REQUEST_KEY = "CAMERA_PERMISSION_REQUEST_KEY"
    }

    private lateinit var binding: ActivityScanBinding   //バインド
    private lateinit var bcdScanner: BcdScanner         //バーコードスキャナ
    //private lateinit var blueTooth: BlueTooth           //BlueTooth

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
        setContentView(R.layout.activity_bluetooth_address_scan)

        Log.d("TAG", "おあああああああ！！！！！！！！！！")

        //BlueToothアドレス
        //val blueToothAddress : String? = intent.getStringExtra(Constants.KEY_STAT_TRANS_BT_ADRS)

        // BlueToothクラスのインスタンスを生成して初期化
        //blueTooth = BlueTooth(this,intent.getStringExtra(Constants.KEY_STAT_TRANS_BT_ADRS).toString())
/*
        Thread(Runnable {
            try {
                //BlueTooth接続待機
                if (!blueTooth.setupBluetooth()) {

                    finish()//強制終了
                }
                //Thread.sleep(3000)

            } catch (e: InterruptedException) {
                return@Runnable
            }
            runOnUiThread {
                //描画切替 ロード画面 -> カメラ
                findViewById<View>(R.id.LL_Load).visibility = View.GONE
                findViewById<View>(R.id.LL_Main).visibility = View.VISIBLE
            }
        }).start()

 */

        binding = ActivityScanBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        //スキャン情報を見せる処理
        adapter = ScanResultAdapter(this) {

            ScanResultDialog.show(this, it)
            //blueTooth.sendBluetooth(it.value,it.format)
        }

        binding.resultList.adapter = adapter
        binding.resultList.addItemDecoration(
            DividerItemDecoration(this, DividerItemDecoration.VERTICAL),
        )

        // Vibratorをシステムサービスから取得する
        vibrator = getSystemService()!!

        bcdScanner = BcdScanner(this, binding.previewView, ::onDetectCode)

        // 検出プレゼンターを初期化する
        detectedPresenter = DetectedPresenter(
            bcdScanner = bcdScanner,
            detectedMarker = binding.detectedMarker,
            stillImage = binding.stillImage,
        )
        val size = viewModel.resultFlow.value.size
        if (size >= 2) {
            binding.dummy.updateLayoutParams<ConstraintLayout.LayoutParams> {
                height = 0
            }
        }

        // ビューモデルのスキャン結果フローを観察し、結果が変更されたときの処理を設定する
        viewModel.resultFlow.observe(this) {
            resultSet = it.toSet()
            adapter.onChanged(it)
            binding.resultList.scrollToPosition(adapter.itemCount - 1)
            if (it.isNotEmpty()) {
                binding.scanning.isGone = true
            }
            if (it.size == 2) {
                expandList()
            }
        }

        // カメラのパーミッションを持っているかどうかを確認し、カメラを開始するかリクエストする
        if (CameraPermission.hasPermission(this)) {
            startCamera()
            Updater.startIfAvailable(this)
        } else {
            launcher.launch()
        }

        // カメラのパーミッションのリクエスト結果を処理するリスナーを登録する
        DialogPermission.registerListener(this, CAMERA_PERMISSION_REQUEST_KEY) {
            finishByError()
        }

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

    private fun finishByError() {   //エラーで終了するときに呼ばれる関数

        toastPermissionError()
        super.finish()
    }

    override fun finish() { //処理終了時に呼び出す関数

        Log.d("TAG", "ScanActivity:finish")

        //blueTooth.finish()

        if (ReviewRequester.requestIfNecessary(this)) {
            return
        }

        super.finish()
    }

    private fun toastPermissionError() {    //パーミッション付与されてない時

        Toast.makeText(this, R.string.toast_permission_required, Toast.LENGTH_LONG).show()
    }

    private fun startCamera() { //カメラ描写開始

        if (started) return
        started = true
        bcdScanner.start()
    }


    private fun onDetectCode(imageProxy: ImageProxy, codes: List<Barcode>) {    //バーコード検出処理

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


    private fun expandList() {  //バーコードを2以上読み込んだときにリスト化させる

        ValueAnimator.ofInt(binding.dummy.height, 0)
            .also {
                it.addUpdateListener {
                    binding.dummy.updateLayoutParams<ConstraintLayout.LayoutParams> {
                        height = it.animatedValue as Int
                    }
                }
            }.start()
    }


    private fun vibrate() { //バイブレーション処理

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
}