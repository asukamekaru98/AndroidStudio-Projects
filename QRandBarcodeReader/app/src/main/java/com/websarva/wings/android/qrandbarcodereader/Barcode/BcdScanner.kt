package com.websarva.wings.android.qrandbarcodereader.Barcode

import androidx.activity.ComponentActivity
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.core.TorchState
import androidx.camera.core.resolutionselector.AspectRatioStrategy
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import kotlinx.coroutines.flow.MutableStateFlow
import timber.log.Timber
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


class BcdScanner(
    private val activity: ComponentActivity,
    private val previewView: PreviewView,
    callback: (ImageProxy, List<Barcode>) -> Unit,
) {
    private val workerExecutor: ExecutorService = Executors.newSingleThreadExecutor()
    private val bcdScanner: BarcodeScanner = BarcodeScanning.getClient()
    private val bcdAnalysis: BcdAnalysis = BcdAnalysis(bcdScanner, callback)
    private var camera: Camera? = null
    val torchStateFlow: MutableStateFlow<Boolean> = MutableStateFlow(false)

    init {
        activity.lifecycle.addObserver(
            LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_DESTROY) {
                    workerExecutor.shutdown()
                    bcdScanner.close()
                }
            },
        )
    }

    fun start() {
        val future = ProcessCameraProvider.getInstance(activity)
        future.addListener({
            setUp(future.get())
        }, ContextCompat.getMainExecutor(activity))
    }

    private fun setUp(provider: ProcessCameraProvider) {
        val resolutionSelector = ResolutionSelector.Builder()
            .setAspectRatioStrategy(AspectRatioStrategy.RATIO_16_9_FALLBACK_AUTO_STRATEGY)
            .build()
        val preview = Preview.Builder()
            .setResolutionSelector(resolutionSelector)
            .build()
        preview.setSurfaceProvider(previewView.surfaceProvider)

        val analysis = ImageAnalysis.Builder()
            .setResolutionSelector(resolutionSelector)
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()
        analysis.setAnalyzer(workerExecutor, bcdAnalysis)

        try {
            provider.unbindAll()
            val camera = provider.bindToLifecycle(
                activity,
                CameraSelector.DEFAULT_BACK_CAMERA,
                preview,
                analysis,
            )
            camera.cameraInfo.torchState.observe(activity) { state ->
                torchStateFlow.tryEmit(state == TorchState.ON)
            }
            this.camera = camera
        } catch (e: Exception) {
            Timber.e(e)
        }
    }


    fun cameraTorch() {
        val camera = camera ?: return
        camera.cameraControl.enableTorch(!torchStateFlow.value)
    }

    /*解析再開*/
    fun resume() {
        bcdAnalysis.AnlysResume()
    }

    /*解析一時停止*/
    fun pause() {
        bcdAnalysis.AnlysPause()
    }
}
