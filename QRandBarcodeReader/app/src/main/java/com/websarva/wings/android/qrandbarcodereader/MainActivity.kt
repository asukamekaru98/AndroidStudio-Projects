package com.websarva.wings.android.qrandbarcodereader

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.websarva.wings.android.qrandbarcodereader.Constants.PREF_INPUT_VALUES

class MainActivity : AppCompatActivity() {



    //private lateinit var binding: ActivityMainBinding           //バインド
    private lateinit var etBluetoothAddress: EditText           //UI EditText:Bluetoothアドレス
    private lateinit var btnBluetoothAdrScan: MaterialButton    //UI Button：Bluetoothアドレススキャン
    private lateinit var btnBcdScan: MaterialButton             //UI Button：バーコードスキャン

    private var sEditTextBtText:String = ""


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

            //ScanBlueToothAddressActivityに遷移
            val intent = Intent(
                application,
                ScanBlueToothAddressActivity::class.java
            )

            //intent.putExtra(Constants.KEY_STAT_TRANS_BT_ADRS,sEditTextBtText)

            startActivity(intent)
        }

        //カメラボタン押下
        btnBcdScan.setOnClickListener{

            // テキストボックスに入力されている文字列を取得
            sEditTextBtText = etBluetoothAddress.text.toString()

            // プリファレンスに書き込む
            sharedPref.edit().putString(Constants.KEY_BT_ADRS, sEditTextBtText).apply()

            //ScanActivityに遷移
            val intent = Intent(
                application,
                ScanActivity::class.java
            )

            intent.putExtra(Constants.KEY_STAT_TRANS_BT_ADRS,sEditTextBtText)

            startActivity(intent)
        }
    }

}