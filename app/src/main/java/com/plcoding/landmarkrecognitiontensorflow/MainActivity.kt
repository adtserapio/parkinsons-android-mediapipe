package com.plcoding.landmarkrecognitiontensorflow

import CameraPreview
import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarkerResult
import com.plcoding.landmarkrecognitiontensorflow.data.MediaPipeLandmarkDetector
import com.plcoding.landmarkrecognitiontensorflow.presentation.LandmarkImageDetector
import com.plcoding.landmarkrecognitiontensorflow.ui.theme.LandmarkRecognitionTensorflowTheme
import java.util.concurrent.Executors

class MainActivity : ComponentActivity() {
    private lateinit var cameraExecutor: java.util.concurrent.ExecutorService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        cameraExecutor = Executors.newSingleThreadExecutor()

        if (!hasCameraPermission()) {
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.CAMERA), 0
            )
        } else {
            startCamera()
        }
    }

    private fun startCamera() {
        setContent {
            LandmarkRecognitionTensorflowTheme {
                var classifications by remember {
                    mutableStateOf(emptyList<HandLandmarkerResult>())
                }

                val analyzer = remember {
                    LandmarkImageDetector(
                        detector = MediaPipeLandmarkDetector(
                            context = applicationContext,
                            landmarkListener = { classifications = it }
                        )
                    )
                }

                val imageAnalysis = ImageAnalysis.Builder()
                    .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
                    .build()
                    .also {
                        it.setAnalyzer(cameraExecutor, analyzer)
                    }

                val preview = Preview.Builder().build()

                val cameraProviderFuture = ProcessCameraProvider.getInstance(applicationContext)

                LaunchedEffect(Unit) {
                    val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
                    val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA

                    try {
                        cameraProvider.unbindAll()
                        cameraProvider.bindToLifecycle(
                            this@MainActivity,
                            cameraSelector,
                            preview,
                            imageAnalysis
                        )
                    } catch (exc: Exception) {
                        // Handle exceptions
                    }
                }
                CameraPreview(preview = preview, modifier = Modifier.fillMaxSize())
            }
        }
    }

    private fun hasCameraPermission() = ContextCompat.checkSelfPermission(
        this, Manifest.permission.CAMERA
    ) == PackageManager.PERMISSION_GRANTED

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (requestCode == 0 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startCamera()
        } else {
            // Handle the case where the user denied the permissions
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }
}