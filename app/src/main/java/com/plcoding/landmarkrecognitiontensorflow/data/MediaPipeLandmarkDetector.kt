package com.plcoding.landmarkrecognitiontensorflow.data
import LandmarkDetector
import android.content.Context
import android.graphics.Bitmap
import android.os.SystemClock

import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.framework.image.BitmapImageBuilder
import com.google.mediapipe.framework.image.MPImage
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarker
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarkerResult
class MediaPipeLandmarkDetector (
    private val context: Context,
    private val landmarkListener: (List<HandLandmarkerResult>) -> Unit
): LandmarkDetector {

    private var detector: HandLandmarker? = null

    private fun setupLandmarkDetector() {
        val baseOptionBuilder = BaseOptions.builder()
            .setModelAssetPath(MP_HAND_LANDMARKER_TASK)
        try {
            val baseOptions = baseOptionBuilder.build()
            val optionsBuilder =
                HandLandmarker.HandLandmarkerOptions.builder()
                    .setBaseOptions(baseOptions)
                    .setMinHandDetectionConfidence(DEFAULT_HAND_DETECTION_CONFIDENCE)
                    .setMinTrackingConfidence(DEFAULT_HAND_TRACKING_CONFIDENCE)
                    .setMinHandPresenceConfidence(DEFAULT_HAND_PRESENCE_CONFIDENCE)
                    .setNumHands(DEFAULT_NUM_HANDS)
                    .setResultListener(this::returnLivestreamResult)
                    .setRunningMode(RunningMode.LIVE_STREAM)

            val options = optionsBuilder.build()
            try {
                detector =
                    HandLandmarker.createFromOptions(context, options)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    override fun detect(bitmap: Bitmap) {
        if (detector == null) {
            setupLandmarkDetector()
        }
        val mpImage = BitmapImageBuilder(bitmap).build()
        val frameTime = SystemClock.uptimeMillis()
        detector?.detectAsync(mpImage, frameTime)
    }
    private fun returnLivestreamResult(
        result: HandLandmarkerResult,
        input: MPImage
    ) {
        println(listOf(result))
        landmarkListener(
            listOf(result)
        )
    }
    companion object {
        private const val MP_HAND_LANDMARKER_TASK = "hand_landmarker.task"
        const val DEFAULT_HAND_DETECTION_CONFIDENCE = 0.5F
        const val DEFAULT_HAND_TRACKING_CONFIDENCE = 0.5F
        const val DEFAULT_HAND_PRESENCE_CONFIDENCE = 0.5F
        const val DEFAULT_NUM_HANDS = 2
    }

}