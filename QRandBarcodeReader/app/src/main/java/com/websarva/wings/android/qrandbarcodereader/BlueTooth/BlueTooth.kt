package com.websarva.wings.android.qrandbarcodereader.BlueTooth

//BlueTooth
import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.IOException
import java.util.TimerTask
import java.util.UUID

class BlueTooth : AppCompatActivity{
	companion object {
		private const val REQUEST_BLUETOOTH_CONNECT = 100
		private val MY_UUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
	}

	private val context: Context

	//BlueToothアダプタ
	private var bluetoothAdapter: BluetoothAdapter? = null
	private var bluetoothSocket: BluetoothSocket? = null
	private var isConnected: Boolean = false

	//タイマータスク
	private var timerTask: TimerTask? = null


	fun sendBluetooth(getValue:String,getType:String){   //接続機に値を送信する処理

		Log.d("TAG", "getValue:$getValue")
		Log.d("TAG", "getType:$getType")
		val bcdType = when (getType) {
			"CODE128" -> 'K'
			"CODE39" -> 'M'
			//Barcode.FORMAT_CODE_93 -> ""
			"Codabar/NW-7" -> 'N'
			//Barcode.FORMAT_DATA_MATRIX -> ""
			"JAN13/EAN13" -> 'A'
			"JAN8/EAN8" -> 'B'
			"ITF" -> 'I'
			"QR" -> 'q'
			"UPC-A" -> 'A'
			"UPC-E" -> 'C'
			"PDF417" -> 'p'
			//Barcode.FORMAT_AZTEC -> ""
			else -> 'X'
		}

		//新盛識別ID + スキャンコード + CR
		val sendValue = bcdType + getValue + 0x0D

		Log.d("TAG", "sendValue:$sendValue")

		bluetoothSocket?.outputStream?.write(sendValue.toByteArray())

	}


	constructor(text: Context){
		context = text

		checkBluetoothPermissions()
	}

	fun setupBluetooth():Boolean{   //BlueToothのセットアップ

		Log.d("TAG", "Bt_A")

		//BlueToothアダプタ取得
		if(!getBluetoothAdapter())return false

		//BlueTooth接続開始、失敗でfalse返却
		return setupBluetoothConnection()
	}


	private fun getBluetoothAdapter():Boolean{	//BlueToothアダプタ取得処理

		bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

		Log.d("TAG", "1.bluetoothAdapter = $bluetoothAdapter")

		return bluetoothAdapter != null
	}

	//BlueToothのPermission確認
	private fun checkBluetoothPermissions():Boolean {   //パーミッション確認

		//アンドロイドverの確認(12以上か) と BlueToothバーミッション付与の確認
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && ContextCompat.checkSelfPermission(context,Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED)
		{
			Log.d("TAG", "Bt_E")

			//パーミッションリクエスト
			ActivityCompat.requestPermissions(context as Activity,arrayOf(Manifest.permission.BLUETOOTH_CONNECT),REQUEST_BLUETOOTH_CONNECT)
		} else {
			Log.d("TAG", "Bt_DD")

		}
		Log.d("TAG", "Bt_DDD")
		return true
	}

	private fun setupBluetoothConnection():Boolean {    //BlueTooth接続処理

		Log.d("TAG", "Bt_F")

		if (ContextCompat.checkSelfPermission(context,Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
			Log.d("TAG", "Bt_FF")
			//パーミッションがが付与されていれば接続を試みる
			try {
				val pairedDevices: Set<BluetoothDevice>? = bluetoothAdapter?.bondedDevices
				Log.d("TAG", "pairedDevices = $pairedDevices")
				val device = pairedDevices?.firstOrNull { it.address == "D8:80:39:F6:01:AF" }
				Log.d("TAG", "device = $device")

				device?.let {
					if (bluetoothSocket == null || bluetoothSocket?.isConnected == false) {
						bluetoothSocket = it.createRfcommSocketToServiceRecord(MY_UUID)

						Log.d("TAG", "Bt_H")

						bluetoothSocket?.connect()

						Log.d("TAG", "Bt_I")

						isConnected = true
					}
				}
			} catch (e: Exception) {
				//接続失敗したとき
				Log.d("TAG", "Bt_J")
				return false
			}
		} else {
			//パーミッションが付与されてないとき
			Log.d("TAG", "Bt_K")
			return false
		}

		//接続成功したとき
		Log.d("TAG", "Bt_L")
		return true
	}

	override fun onDestroy() {  //アクティビティ破棄
		Log.d("TAG", "Btデストロイ")
		super.onDestroy()
		timerTask?.cancel()
		try {
			bluetoothSocket?.close()
		} catch (e: IOException) {
			//
		}
	}

	override fun onRequestPermissionsResult(
		requestCode: Int,
		permissions: Array<String>,
		grantResults: IntArray
	) { //パーミッションのリクエストを確認した結果

		when (requestCode) {
			REQUEST_BLUETOOTH_CONNECT -> {
				if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
					//パーミッション許可をもらったとき
					setupBluetoothConnection()
				}
			}

			//リクエストがBluetoothのパーミッションじゃなければ。
			else -> super.onRequestPermissionsResult(requestCode, permissions, grantResults)
		}
	}

}