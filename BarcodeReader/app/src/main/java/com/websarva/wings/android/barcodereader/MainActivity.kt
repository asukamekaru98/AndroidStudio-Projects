package com.websarva.wings.android.barcodereader

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // ScanContractの結果を受け取る
        val barcodeLauncher = registerForActivityResult(
            ScanContract()
        ) { result ->
            if (result.contents == null) {
                // ここでQRコードを読み取れなかった場合の処理を書く
                Toast.makeText(this, "Cancelled", Toast.LENGTH_LONG)
                    .show()
            } else {
                // ここでQRコードを読み取れた場合の処理を書く
                // ここではトーストに結果を表示するだけ
                Toast.makeText(this, "Scanned: " + result.contents, Toast.LENGTH_LONG)
                    .show()
            }
        }

        // QRコードリーダーの立ち上げ
        fun onButtonClick() {
            // 縦画面に固定
            val options = ScanOptions().setOrientationLocked(false)
            barcodeLauncher.launch(options)
        }

        super.onCreate(savedInstanceState)
        setContent {
            QRCodeReaderTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    // ボタンタップで起動
                    TextButton(onClick = { onButtonClick() }) {
                        Text(text = "QRCodeReaderを起動する")
                    }
                }
            }
        }
    }
}