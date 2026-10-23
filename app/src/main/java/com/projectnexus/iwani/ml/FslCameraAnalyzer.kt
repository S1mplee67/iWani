package com.projectnexus.iwani.ml

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.util.Log
import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mediapipe.framework.image.BitmapImageBuilder
import com.google.mediapipe.framework.image.MPImage
import com.google.mediapipe.tasks.components.containers.NormalizedLandmark
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarker
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarkerResult

class FslCameraAnalyzer(
    context: Context,
    private val onResult: (letter: String, confidence: Float, landmarks: List<NormalizedLandmark>?, committed: Boolean) -> Unit
) : ImageAnalysis.Analyzer {

    private val TAG = "FslCameraAnalyzer"
    private val classifier = FslClassifier(context)
    private var handLandmarker: HandLandmarker? = null
    private val mainHandler = Handler(Looper.getMainLooper())

    // Majority voting smoothing (15 frames)
    private val voteHistory = mutableListOf<String>()
    private val VOTE_WINDOW = 15
    private val COMMIT_THRESHOLD = 9
    private var lastCommitTime = 0L
    private val COMMIT_COOLDOWN_MS = 1200L

    init {
        setupHandLandmarker(context)
    }

    private fun setupHandLandmarker(context: Context) {
        try {
            val baseOptions = BaseOptions.builder()
                .setModelAssetPath("hand_landmarker.task")
                .build()

            val options = HandLandmarker.HandLandmarkerOptions.builder()
                .setBaseOptions(baseOptions)
                .setMinHandDetectionConfidence(0.3f)
                .setMinHandPresenceConfidence(0.3f)
                .setNumHands(1)
                .setRunningMode(RunningMode.LIVE_STREAM)
                .setResultListener { result: HandLandmarkerResult, _: MPImage ->
                    processLandmarks(result)
                }
                .setErrorListener { error: RuntimeException ->
                    Log.e(TAG, "MediaPipe error: ${error.message}", error)
                }
                .build()

            handLandmarker = HandLandmarker.createFromOptions(context, options)
            Log.i(TAG, "HandLandmarker initialized successfully in LIVE_STREAM mode.")
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up HandLandmarker: ${e.message}", e)
        }
    }

    @OptIn(ExperimentalGetImage::class)
    override fun analyze(imageProxy: ImageProxy) {
        val frameTime = SystemClock.uptimeMillis()
        val mediaImage = imageProxy.image

        if (mediaImage != null && handLandmarker != null) {
            val rotationDegrees = imageProxy.imageInfo.rotationDegrees
            val bitmap = imageProxy.toBitmap()

            // Rotate if necessary to match device portrait orientation
            val rotatedBitmap = if (rotationDegrees != 0) {
                val matrix = Matrix().apply { postRotate(rotationDegrees.toFloat()) }
                Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
            } else {
                bitmap
            }

            val mpImage = BitmapImageBuilder(rotatedBitmap).build()
            handLandmarker?.detectAsync(mpImage, frameTime)
        }

        imageProxy.close()
    }

    private fun processLandmarks(result: HandLandmarkerResult) {
        val landmarksList = result.landmarks()
        if (landmarksList.isNotEmpty() && landmarksList[0].isNotEmpty()) {
            val landmarks = landmarksList[0]
            val features = classifier.normalizeLandmarks(landmarks)
            val classification = classifier.classify(features)

            var isCommitted = false
            if (classification.confidence >= 0.45f) {
                voteHistory.add(classification.letter)
                if (voteHistory.size > VOTE_WINDOW) {
                    voteHistory.removeAt(0)
                }

                // Majority voting
                val counts = voteHistory.groupingBy { it }.eachCount()
                val topEntry = counts.maxByOrNull { it.value }

                val now = SystemClock.uptimeMillis()
                if (topEntry != null && topEntry.value >= COMMIT_THRESHOLD && (now - lastCommitTime > COMMIT_COOLDOWN_MS)) {
                    isCommitted = true
                    lastCommitTime = now
                    voteHistory.clear()
                }
            }

            mainHandler.post {
                onResult(classification.letter, classification.confidence, landmarks, isCommitted)
            }
        } else {
            mainHandler.post {
                voteHistory.clear()
                onResult("", 0.0f, null, false)
            }
        }
    }

    fun close() {
        handLandmarker?.close()
        handLandmarker = null
        classifier.close()
    }
}
