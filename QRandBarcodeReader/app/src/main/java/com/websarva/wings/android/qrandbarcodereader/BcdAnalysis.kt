/* クラス : バーコード解析 */

package com.websarva.wings.android.qrandbarcodereader

import android.annotation.SuppressLint
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import timber.log.Timber


class BcdAnalysis(
    private val scanner: BarcodeScanner,
    private val callback: (ImageProxy, List<Barcode>) -> Unit,
) : ImageAnalysis.Analyzer {
    private var bAnlysPaused: Boolean = false

    //画像解析処理
    @SuppressLint("UnsafeOptInUsageError")
    override fun analyze(imgProxy: ImageProxy) {

        //解析停止中
        if (bAnlysPaused) {
            //オブジェクト開放
            imgProxy.close()
            return
        }

        //プロキシから画像取得
        val img = imgProxy.image
        if (img == null) {
            //オブジェクト開放
            imgProxy.close()
            return
        }

        //バーコードスキャナーによって処理可能な形式に画像データを変換
        val inputImg = InputImage.fromMediaImage(img, imgProxy.imageInfo.rotationDegrees)

        //バーコード検出
        scanner.process(inputImg)
            //バーコードが正常に検出された場合に実行されるコールバック関数を指定
            .addOnSuccessListener { callback(imgProxy, it) }
            //処理中にエラーが発生した場合にTimberでエラー出力
            .addOnFailureListener { Timber.e(it) }
            //処理終了でオブジェクト開放
            .addOnCompleteListener { imgProxy.close() }
    }

    //解析再開
    fun AnlysResume() {
        bAnlysPaused = false
    }

    //解析停止
    fun AnlysPause() {
        bAnlysPaused = true
    }
}
