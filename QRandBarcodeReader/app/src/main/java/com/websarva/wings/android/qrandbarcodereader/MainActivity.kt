package com.websarva.wings.android.qrandbarcodereader

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.websarva.wings.android.qrandbarcodereader.Constants.PREF_INPUT_VALUES
import java.util.TimerTask

class MainActivity : AppCompatActivity() {

    //private lateinit var binding: ActivityMainBinding           //バインド
    private lateinit var etBluetoothAddress: EditText           //UI EditText:Bluetoothアドレス
    private lateinit var btnBluetoothAdrScan: MaterialButton    //UI Button：Bluetoothアドレススキャン
    private lateinit var btnBcdScan: MaterialButton             //UI Button：バーコードスキャン


    companion object {
        private const val CAMERA_REQUEST_CODE = 100
        private const val STORAGE_REQUEST_CODE = 101

        private const val TAG = "MAIN_TAG"
    }

    /* BlueTooth */
    private var bluetoothAdapter: BluetoothAdapter? = null
    private var bluetoothSocket: BluetoothSocket? = null
    private var isConnected: Boolean = false
    private var timerTask: TimerTask? = null

    private var sEditTextBtText:String = ""
    private var sEditTextSendText:String = ""

    private lateinit var cameraPermission: Array<String>
    private lateinit var storagePermission: Array<String>

    private var imageUri: Uri? = null

    private var barcodeScannerOptions: BarcodeScannerOptions? = null
    private var barcodeScanner: BarcodeScanner? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        etBluetoothAddress = findViewById(R.id.etBluetoothAddress)      //EdTx：Bluetoothアドレス
        btnBluetoothAdrScan = findViewById(R.id.btnBluetoothAdrScan)    //Btn：Bluetoothアドレススキャン
        btnBcdScan = findViewById(R.id.btnBcdScan)                      //Btn：バーコードスキャン

        //getSharedPreferencesメソッドでSharedPreferencesオブジェクトを取得
        val sharedPref = getSharedPreferences(PREF_INPUT_VALUES, Context.MODE_PRIVATE)

        // getString()を呼び出して保存されている文字列を読み込む
        // まだ保存されていない場合はデフォルトの文字列を返す
        var savedEditTextBtText = sharedPref.getString(Constants.KEY_BT_ADRS, Constants.TXT_DEF_BT_ADRS)

        //取得した文字列が空白ならデフォルトのBlueToothアドレスを代入する
        if(savedEditTextBtText == ""){
            savedEditTextBtText = Constants.TXT_DEF_BT_ADRS    //テキスト設定 (Bluetoothアドレス EditText)
        }

        etBluetoothAddress.setText(savedEditTextBtText)    //テキスト設定 (Bluetoothアドレス EditText)
        etBluetoothAddress.requestFocus()                  // フォーカスを設定

        //ボタン：Bluetoothアドレススキャン押下
        btnBluetoothAdrScan.setOnClickListener{

        }

        //カメラボタン押下
        btnBcdScan.setOnClickListener{

            // テキストボックスに入力されている文字列を取得
            //sEditTextBtText = binding.etBluetoothAddress.text.toString()
            sEditTextBtText = etBluetoothAddress.text.toString()

            // プリファレンスに書き込む
            sharedPref.edit().putString(Constants.KEY_BT_ADRS, sEditTextBtText).apply()

            //ScanActivityに遷移
            val intent = Intent(
                application,
                ScanActivity::class.java
            )
            startActivity(intent)
        }
    }

    /*  トースト表示_ショートタイム    */
    private fun showToast_shortTime(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }

    /*  トースト表示_ロングタイム    */
    private fun showToast_longTime(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
    }
}