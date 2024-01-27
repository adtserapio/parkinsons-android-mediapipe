package com.plcoding.landmarkrecognitiontensorflow.presentation

import LandmarkDetector
import android.graphics.Bitmap
import android.graphics.Matrix
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy

class LandmarkImageDetector(
    private val detector: LandmarkDetector,
): ImageAnalysis.Analyzer {

    private var frameSkipCounter = 0
    override fun analyze(imageProxy: ImageProxy) {
        if(frameSkipCounter % 60 == 0) {
            val rotationDegrees = imageProxy.imageInfo.rotationDegrees

            val bitmapBuffer =
                Bitmap.createBitmap(
                    imageProxy.width,
                    imageProxy.height,
                    Bitmap.Config.ARGB_8888
                )
            imageProxy.use { bitmapBuffer.copyPixelsFromBuffer(imageProxy.planes[0].buffer) }
            imageProxy.close()

            val matrix = Matrix().apply {
                // Rotate the frame received from the camera to be in the same direction as it'll be shown
                postRotate(imageProxy.imageInfo.rotationDegrees.toFloat())
                postScale(
                    -1f,
                    1f,
                    imageProxy.width.toFloat(),
                    imageProxy.height.toFloat()
                )
            }

            val bitmap = Bitmap.createBitmap(
                bitmapBuffer, 0, 0, bitmapBuffer.width, bitmapBuffer.height,
                matrix, true
            )

            detector.detect(bitmap)
        }
    }


}