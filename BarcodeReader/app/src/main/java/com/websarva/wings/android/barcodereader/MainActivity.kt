package com.websarva.wings.android.barcodereader

import android.content.Context
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.lifecycle.ReportFragment.Companion.reportFragment
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanIntentResult
import com.journeyapps.barcodescanner.ScanOptions
import com.websarva.wings.android.barcodereader.databinding.ActivityMainBinding


class MainActivity : ComponentActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var qrReaderHandler: QRReaderHandler // カメラ処理用のクラス

    private fun setResult(string: String){
        //スキャンされたQRコードを結果のテキストに設定
        binding.textResult.text = string
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)



        //QRコードリーダー
        initBinding()
        initqrReaderHandlerHandler()
        initViews()
    }



    //QRコードリーダー

    private fun initBinding() {
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    private fun initqrReaderHandlerHandler() {
        qrReaderHandler = QRReaderHandler(this) { result ->
            // スキャン結果を処理するコールバック
            if (result == null) {
                Toast.makeText(this, "キャンセルされました", Toast.LENGTH_SHORT).show()
            } else {
                binding.textResult.text = result
            }
        }
    }

    private fun initViews() {
        //FABにクリックリスナーを設定してカメラパーミッションを確認
        binding.fab.setOnClickListener {
            qrReaderHandler.checkPermissionQRReader(this)
        }
    }

}