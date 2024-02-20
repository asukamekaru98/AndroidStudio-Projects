/*
 * Copyright (c) 2021 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package com.websarva.wings.android.qrandbarcodereader

//import com.websarva.wings.android.qrandbarcodereader.util.Launcher
//BlueTooth

import android.animation.ValueAnimator
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothSocket
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
import com.websarva.wings.android.qrandbarcodereader.util.Updater
import com.websarva.wings.android.qrandbarcodereader.util.observe
import java.util.UUID

class ScanActivity : AppCompatActivity() {

	companion object {
		private const val KEY_SCAN_RESULT = "KEY_SCAN_RESULT"

		private const val CAMERA_PERMISSION_REQUEST_KEY = "CAMERA_PERMISSION_REQUEST_KEY"
		private val REQUEST_BLUETOOTH_CONNECT = 100
		private val MY_UUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
	}

	private lateinit var binding: ActivityScanBinding   //バインド
	private lateinit var bcdScanner: BcdScanner         //バーコードスキャナ

	private var bluetoothAdapter: BluetoothAdapter? = null
	private var bluetoothSocket: BluetoothSocket? = null
	private var isConnected: Boolean = false

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

		//BlueTooth接続待機
		Thread(Runnable {
			//テストアプリなので3秒sleepしていますが、本来はここで実行したいバックグラウンド動作を実装してください。
			try {

				//アダプタ取得
				bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
				if (bluetoothAdapter == null) {
					/*  Bluetoothが利用できないとき    */
					Log.d("TAG", "bluetoothAdapter == null")

					Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show()
					finish()//強制終了
				}










				Thread.sleep(3000)
			} catch (e: InterruptedException) {
				return@Runnable
			}
			runOnUiThread {
				findViewById<View>(R.id.LL_Load).visibility = View.GONE
				findViewById<View>(R.id.LL_Main).visibility = View.VISIBLE
			}
		}).start()

		binding = ActivityScanBinding.inflate(layoutInflater)
		setContentView(binding.root)
		setSupportActionBar(binding.toolbar)

		//スキャン情報を見せる処理
		adapter = ScanResultAdapter(this) {

			//val result: ScanResult? = requireArguments().getParcelable(KEY_SCAN_RESULT)

			//bluetoothSocket?.outputStream?.write(sEditTextSendText.toByteArray())


		//ScanResultDialog.show(this, it)
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

		Log.d("TAG", "onOptionsItemSelected")

		// android.R.id.home に戻るボタンを押した時のidが取得できる
		if (item.itemId == android.R.id.home) {
			// 今回はActivityを終了させている
			finish()
		}
		return super.onOptionsItemSelected(item)
	}
/*
	private fun setupBluetoothConnection() {
		if (ContextCompat.checkSelfPermission(this,Manifest.permission.BLUETOOTH) == PackageManager.PERMISSION_GRANTED) {
			try {
				val pairedDevices: Set<BluetoothDevice>? = bluetoothAdapter?.bondedDevices
				val device = pairedDevices?.firstOrNull { it.address == "sEditTextBtText" }

				device?.let {
					if (bluetoothSocket == null || bluetoothSocket?.isConnected == false) {
						bluetoothSocket = it.createRfcommSocketToServiceRecord(MY_UUID)
						bluetoothSocket?.connect()
						isConnected = true
					}

					//bluetoothSocket?.outputStream?.write(sEditTextSendText.toByteArray(Charsets.UTF_8))
					bluetoothSocket?.outputStream?.write(sEditTextSendText.toByteArray())
				}


			} catch (e: IOException) {
				Toast.makeText(
					this,
					"Could not establish Bluetooth connection",
					Toast.LENGTH_SHORT
				).show()

				super.finish()//強制終了

			} catch (e: SecurityException) {
				Toast.makeText(
					this,
					"Bluetooth permission is required",
					Toast.LENGTH_SHORT
				).show()

				super.finish()//強制終了

			} catch (e: Exception) {
				Toast.makeText(
					this,
					"An unexpected error occurred",
					Toast.LENGTH_SHORT
				).show()

				super.finish()//強制終了
			}
		} else {
			Toast.makeText(
				this,
				"Bluetooth permission is required",
				Toast.LENGTH_SHORT
			).show()

			super.finish()//強制終了
		}
	}
*/
	override fun onRestart() {

	Log.d("TAG", "onRestart")

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

		Log.d("TAG", "finishByError")

		toastPermissionError()
		super.finish()
	}

	//処理終了時に呼び出す関数
	override fun finish() {

		if (ReviewRequester.requestIfNecessary(this)) {
			return
		}
		super.finish()
	}

	private fun toastPermissionError() {

		Log.d("TAG", "toastPermissionError")

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

		Log.d("TAG", "startCamera")

		if (started) return
		started = true
		bcdScanner.start()
	}

	//バーコード検出処理
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

	//バーコードを2以上読み込んだときにリスト化させる
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

	//バイブレーション処理
	private fun vibrate() {

		Log.d("TAG", "vibrate")

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