package com.websarva.wings.android.qrandbarcodereader

import android.animation.ValueAnimator
import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.Point
import android.util.Log
import android.view.animation.DecelerateInterpolator
import android.widget.ImageView
import androidx.camera.core.ImageProxy
import androidx.core.animation.addListener
import androidx.core.view.isVisible
import com.google.mlkit.vision.barcode.common.Barcode
import com.websarva.wings.android.qrandbarcodereader.Barcode.BcdScanner
import com.websarva.wings.android.qrandbarcodereader.view.DetectedMarkerView

class DetectedPresenter(
    private val bcdScanner: BcdScanner,
    private val detectedMarker: DetectedMarkerView,
    private val stillImage: ImageView,
) {
    fun onDetected(
        imageProxy: ImageProxy,
        detectedCodes: List<Barcode>,
    ) {
        Log.d("TAG", "onDetected")

        bcdScanner.pause()
        val pointsList = detectedCodes.mapNotNull { it.toCornerPoints() }
        detectedMarker.setMarkers(imageProxy, pointsList)
        stillImage.setImageBitmap(toBitmap(imageProxy))
        stillImage.isVisible = true
        ValueAnimator.ofFloat(4f, 1.2f)
            .also {
                it.setDuration(ANIMATION_DURATION)
                it.setInterpolator(DecelerateInterpolator(3f))
                it.addUpdateListener {
                    detectedMarker.drawMarker(it.animatedValue as Float)
                }
                it.addListener(onEnd = { onEnd() })
            }.start()
    }

    private fun Barcode.toCornerPoints(): Array<Point>? {

        Log.d("TAG", "toCornerPoints")

        val cornerPoints = cornerPoints ?: return null
        if (cornerPoints.isEmpty()) return null
        return cornerPoints
    }

    private fun onEnd() {

        Log.d("TAG", "onEnd")

        detectedMarker.postDelayed({
            detectedMarker.clearMarker()
            stillImage.setImageBitmap(null)
            stillImage.isVisible = false
            bcdScanner.resume()
        }, RESUME_INTERVAL)
    }

    private fun toBitmap(imageProxy: ImageProxy): Bitmap =
        if (imageProxy.imageInfo.rotationDegrees == 0) {

            Log.d("TAG", "toBitmap true")

            imageProxy.toBitmap()
        } else {

            Log.d("TAG", "toBitmap false")

            val temp = imageProxy.toBitmap()
            val matrix = Matrix().apply {
                postRotate(imageProxy.imageInfo.rotationDegrees.toFloat())
            }
            Bitmap.createBitmap(temp, 0, 0, temp.width, temp.height, matrix, true)
        }

    companion object {
        private const val ANIMATION_DURATION = 1000L
        private const val RESUME_INTERVAL = 500L
    }
}