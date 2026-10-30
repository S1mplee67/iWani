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
import com.projectnexus.iwani.ui.sound.UiSoundManager
import java.util.Optional

class FslCameraAnalyzer(
    context: Context,
    private val onFrame: ((Bitmap) -> Unit)? = null,
    private val onResult: (letter: String, confidence: Float, landmarks: List<NormalizedLandmark>?, committed: Boolean) -> Unit
) : ImageAnalysis.Analyzer {

    private val TAG = "FslCameraAnalyzer"
    private val appContext = context.applicationContext
    private val classifier = FslClassifier(appContext)
    private var handLandmarker: HandLandmarker? = null
    private val mainHandler = Handler(Looper.getMainLooper())

    // Exponential Moving Average (EMA) state for smooth 21-joint hand tracing
    private var smoothedCoords: FloatArray? = null
    private val EMA_ALPHA = 0.64f

    // Time-based & majority-smoothed letter acceptance (configurable 1.0s Fastest .. 4.0s Slowest, default 2.5s Middle)
    private val voteHistory = mutableListOf<String>()
    private val VOTE_WINDOW = 11
    private var activeHoldLetter = ""
    private var holdStartTimeMs = 0L
    private var lastValidFrameTimeMs = 0L
    private var lastCommitTime = 0L

    init {
        setupHandLandmarker(appContext)
    }

    private fun setupHandLandmarker(context: Context) {
        try {
            val baseOptions = BaseOptions.builder()
                .setModelAssetPath("hand_landmarker.task")
                .build()

            val options = HandLandmarker.HandLandmarkerOptions.builder()
                .setBaseOptions(baseOptions)
                .setMinHandDetectionConfidence(0.35f)
                .setMinHandPresenceConfidence(0.35f)
                .setMinTrackingConfidence(0.35f)
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

            val rotatedBitmap = if (rotationDegrees != 0) {
                val matrix = Matrix().apply { postRotate(rotationDegrees.toFloat()) }
                Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
            } else {
                bitmap
            }

            val mpImage = BitmapImageBuilder(rotatedBitmap).build()
            handLandmarker?.detectAsync(mpImage, frameTime)
            onFrame?.invoke(rotatedBitmap)
        }

        imageProxy.close()
    }

    private fun smoothLandmarks(raw: List<NormalizedLandmark>): List<NormalizedLandmark> {
        if (raw.size < 21) return raw
        val prev = smoothedCoords
        if (prev == null || prev.size != 63) {
            val initArr = FloatArray(63)
            for (i in 0 until 21) {
                initArr[i * 3] = raw[i].x()
                initArr[i * 3 + 1] = raw[i].y()
                initArr[i * 3 + 2] = raw[i].z()
            }
            smoothedCoords = initArr
            return raw
        }

        val smoothedList = ArrayList<NormalizedLandmark>(21)
        for (i in 0 until 21) {
            val sx = EMA_ALPHA * raw[i].x() + (1f - EMA_ALPHA) * prev[i * 3]
            val sy = EMA_ALPHA * raw[i].y() + (1f - EMA_ALPHA) * prev[i * 3 + 1]
            val sz = EMA_ALPHA * raw[i].z() + (1f - EMA_ALPHA) * prev[i * 3 + 2]
            prev[i * 3] = sx
            prev[i * 3 + 1] = sy
            prev[i * 3 + 2] = sz
            smoothedList.add(
                NormalizedLandmark.create(
                    sx,
                    sy,
                    sz,
                    Optional.empty(),
                    Optional.empty()
                )
            )
        }
        return smoothedList
    }

    private fun processLandmarks(result: HandLandmarkerResult) {
        val now = SystemClock.uptimeMillis()
        val landmarksList = result.landmarks()
        if (landmarksList.isNotEmpty() && landmarksList[0].size >= 21) {
            val smoothed = smoothLandmarks(landmarksList[0])
            val classification = classifier.classifyLandmarks(smoothed)

            // Configurable acceptance duration: 1.0s (Fastest/current) up to 4.0s (Slowest), default 2.5s (Middle)
            val configuredSec = UiSoundManager.fslAcceptanceSeconds.coerceIn(1.0f, 4.0f)
            val requiredHoldMs = if (configuredSec <= 1.05f) 850L else (configuredSec * 1000f).toLong()
            val cooldownMs = maxOf(800L, (requiredHoldMs * 0.45f).toLong())

            var isCommitted = false
            var displayLetter = classification.letter

            if (classification.confidence >= 0.40f) {
                lastValidFrameTimeMs = now
                voteHistory.add(classification.letter)
                if (voteHistory.size > VOTE_WINDOW) {
                    voteHistory.removeAt(0)
                }

                // Majority letter in rolling window stabilizes brief 1-frame finger jitter
                val topEntry = voteHistory.groupingBy { it }.eachCount().maxByOrNull { it.value }
                val stableLetter = if (topEntry != null && topEntry.value >= 4) topEntry.key else classification.letter
                displayLetter = stableLetter

                if (stableLetter != activeHoldLetter) {
                    activeHoldLetter = stableLetter
                    holdStartTimeMs = now
                } else {
                    val heldMs = now - holdStartTimeMs
                    if (heldMs >= requiredHoldMs && (now - lastCommitTime >= cooldownMs)) {
                        isCommitted = true
                        lastCommitTime = now
                        holdStartTimeMs = now
                        voteHistory.clear()
                        UiSoundManager.playSuccess(appContext)
                    }
                }
            }

            mainHandler.post {
                onResult(displayLetter, classification.confidence, smoothed, isCommitted)
            }
        } else {
            // Reset smoothing & hold timer if hand leaves camera for > 350ms
            if (now - lastValidFrameTimeMs > 350L) {
                smoothedCoords = null
                voteHistory.clear()
                activeHoldLetter = ""
                holdStartTimeMs = 0L
            }
            mainHandler.post {
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
