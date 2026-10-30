package com.projectnexus.iwani.ml

import android.content.Context
import android.util.Log
import com.google.mediapipe.tasks.components.containers.NormalizedLandmark
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.hypot

data class ClassificationResult(
    val letter: String,
    val confidence: Float,
    val allProbabilities: FloatArray
)

/**
 * High-Performance On-Device Neural + 3D Biomechanical Classifier for
 * Filipino Sign Language (FSL) 26 Alphabet Gestures (A–Z).
 */
class FslClassifier(private val context: Context) {
    private val TAG = "FslClassifier"
    private val labels = mutableListOf<String>()

    // Neural Network Architecture: 42 -> 128 (ReLU) -> 64 (ReLU) -> 26 (Softmax)
    private val w0 = FloatArray(42 * 128)
    private val b0 = FloatArray(128)
    private val w1 = FloatArray(128 * 64)
    private val b1 = FloatArray(64)
    private val w2 = FloatArray(64 * 26)
    private val b2 = FloatArray(26)
    private var isNeuralLoaded = false

    init {
        loadModelAndLabels()
    }

    private fun loadModelAndLabels() {
        try {
            labels.clear()
            for (c in 'A'..'Z') {
                labels.add(c.toString())
            }

            context.assets.open("fsl_weights.bin").use { input ->
                val bytes = input.readBytes()
                val buffer = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN)
                val floatBuffer = buffer.asFloatBuffer()
                floatBuffer.get(w0)
                floatBuffer.get(b0)
                floatBuffer.get(w1)
                floatBuffer.get(b1)
                floatBuffer.get(w2)
                floatBuffer.get(b2)
                isNeuralLoaded = true
                Log.i(TAG, "FslClassifier forward neural engine loaded successfully (26 classes).")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading neural weights: ${e.message}", e)
            isNeuralLoaded = false
        }
    }

    /**
     * Bounding Box Normalization
     * Takes 21 NormalizedLandmarks from MediaPipe and scales x and y into [0, 1].
     */
    fun normalizeLandmarks(landmarks: List<NormalizedLandmark>, mirrorX: Boolean = false): FloatArray {
        if (landmarks.isEmpty()) return FloatArray(42)

        var minX = Float.MAX_VALUE
        var minY = Float.MAX_VALUE
        var maxX = Float.MIN_VALUE
        var maxY = Float.MIN_VALUE

        for (lm in landmarks) {
            val lx = if (mirrorX) (1f - lm.x()) else lm.x()
            val ly = lm.y()
            if (lx < minX) minX = lx
            if (ly < minY) minY = ly
            if (lx > maxX) maxX = lx
            if (ly > maxY) maxY = ly
        }

        val width = maxOf(maxX - minX, 1e-6f)
        val height = maxOf(maxY - minY, 1e-6f)

        val features = FloatArray(42)
        for (i in landmarks.indices) {
            val lx = if (mirrorX) (1f - landmarks[i].x()) else landmarks[i].x()
            features[i * 2] = (lx - minX) / width
            features[i * 2 + 1] = (landmarks[i].y() - minY) / height
        }
        return features
    }

    /**
     * Evaluates both normal and horizontally-mirrored hand orientations (so Left/Right hands
     * and Front/Back cameras work identically) and applies 3D biomechanical calibration
     * for precision finger postures (especially 'K', 'V', 'U', 'R', 'D', 'F', 'W', 'L', 'Y').
     */
    fun classifyLandmarks(landmarks: List<NormalizedLandmark>): ClassificationResult {
        if (landmarks.size < 21) {
            return ClassificationResult("?", 0.0f, FloatArray(26))
        }

        val normalRes = classify(normalizeLandmarks(landmarks, mirrorX = false))
        val mirrorRes = classify(normalizeLandmarks(landmarks, mirrorX = true))
        val baseProbs = if (mirrorRes.confidence > normalRes.confidence) {
            mirrorRes.allProbabilities.clone()
        } else {
            normalRes.allProbabilities.clone()
        }

        // Biomechanical 3D calibration using palm-normalized distances
        val wrist = landmarks[0]
        val palmScale = maxOf(
            dist(wrist, landmarks[5]),
            dist(wrist, landmarks[9]),
            dist(wrist, landmarks[17]),
            0.05f
        )

        fun normDist(a: Int, b: Int): Float = dist(landmarks[a], landmarks[b]) / palmScale

        // Finger extension checks (tip higher than PIP/MCP and further from wrist than PIP)
        val indexUp = landmarks[8].y() < landmarks[6].y() && dist(wrist, landmarks[8]) > dist(wrist, landmarks[6]) * 1.12f
        val middleUp = landmarks[12].y() < landmarks[10].y() && dist(wrist, landmarks[12]) > dist(wrist, landmarks[10]) * 1.10f
        // In 'K', the middle finger can also angle forward toward the camera (slightly lower or level with PIP in Y, but extended from MCP)
        val middleUpOrForward = middleUp || (landmarks[12].y() < landmarks[9].y() + palmScale * 0.15f && normDist(9, 12) > 0.42f)

        val ringCurled = landmarks[16].y() > landmarks[14].y() - palmScale * 0.08f || dist(wrist, landmarks[16]) < dist(wrist, landmarks[14]) * 1.08f
        val pinkyCurled = landmarks[20].y() > landmarks[18].y() - palmScale * 0.08f || dist(wrist, landmarks[20]) < dist(wrist, landmarks[18]) * 1.08f
        val ringUp = !ringCurled && landmarks[16].y() < landmarks[14].y()
        val pinkyUp = !pinkyCurled && landmarks[20].y() < landmarks[18].y()

        // Thumb posture metrics
        val thumbTip = landmarks[4]
        val thumbIp = landmarks[3]
        val thumbMcp = landmarks[2]
        val thumbPointingUp = thumbTip.y() < thumbIp.y() && thumbTip.y() < landmarks[5].y() + palmScale * 0.18f
        val thumbNearIndexMiddleBase = normDist(4, 6) < 0.68f || normDist(4, 10) < 0.68f || normDist(4, 5) < 0.62f || normDist(4, 9) < 0.62f
        val thumbOverRing = normDist(4, 14) < 0.55f || normDist(4, 15) < 0.55f
        val indexMiddleTipSpread = normDist(8, 12)
        val indexBaseToMiddleBase = maxOf(normDist(5, 9), 0.15f)

        fun boostLetter(ch: Char, boostScore: Float) {
            val idx = ch - 'A'
            if (idx in 0..25) {
                baseProbs[idx] = maxOf(baseProbs[idx] + boostScore, boostScore)
            }
        }

        // 1. Recalibrated 'K' vs 'V' vs 'U' vs 'R' (2-finger up + ring/pinky curled)
        if (indexUp && middleUpOrForward && ringCurled && pinkyCurled) {
            val minIdxMidX = minOf(landmarks[5].x(), landmarks[6].x(), landmarks[9].x(), landmarks[10].x()) - palmScale * 0.22f
            val maxIdxMidX = maxOf(landmarks[5].x(), landmarks[6].x(), landmarks[9].x(), landmarks[10].x()) + palmScale * 0.22f
            val thumbHorizontallyBetween = thumbTip.x() in minIdxMidX..maxIdxMidX

            // 'K': Index up, middle up/forward, ring & pinky curled, AND thumb reaching UP toward/between index & middle!
            val isThumbInKPosition = (thumbPointingUp && (thumbNearIndexMiddleBase || thumbHorizontallyBetween)) ||
                (thumbTip.y() < landmarks[6].y() + palmScale * 0.22f && normDist(4, 10) < normDist(4, 15)) ||
                (!middleUp && middleUpOrForward && thumbNearIndexMiddleBase)

            if (isThumbInKPosition) {
                boostLetter('K', 0.91f)
            } else if (indexMiddleTipSpread < indexBaseToMiddleBase * 0.85f) {
                // Fingers crossed or pressed tightly together -> 'R' or 'U'
                val tipsCrossed = (landmarks[8].x() - landmarks[12].x()) * (landmarks[5].x() - landmarks[9].x()) < 0f
                if (tipsCrossed) {
                    boostLetter('R', 0.88f)
                } else {
                    boostLetter('U', 0.86f)
                }
            } else if (thumbOverRing && indexMiddleTipSpread >= indexBaseToMiddleBase * 0.95f) {
                boostLetter('V', 0.86f)
            }
        }

        // 2. 'L' vs 'D' (Index up, middle/ring/pinky curled)
        if (indexUp && !middleUp && ringCurled && pinkyCurled) {
            val thumbExtendedSideways = normDist(4, 5) > 0.65f && normDist(4, 12) > 0.55f
            val thumbTouchingMiddle = normDist(4, 12) < 0.45f || normDist(4, 11) < 0.45f
            if (thumbExtendedSideways) {
                boostLetter('L', 0.89f)
            } else if (thumbTouchingMiddle) {
                boostLetter('D', 0.88f)
            }
        }

        // 3. 'Y' vs 'I' (Pinky up, index/middle/ring curled)
        if (!indexUp && !middleUp && ringCurled && pinkyUp) {
            val thumbOut = normDist(4, 5) > 0.62f
            if (thumbOut) {
                boostLetter('Y', 0.90f)
            } else {
                boostLetter('I', 0.88f)
            }
        }

        // 4. 'F' vs 'W' vs 'B'
        if (!indexUp && middleUp && ringUp && pinkyUp && normDist(4, 8) < 0.48f) {
            boostLetter('F', 0.90f)
        } else if (indexUp && middleUp && ringUp && pinkyCurled) {
            boostLetter('W', 0.89f)
        } else if (indexUp && middleUp && ringUp && pinkyUp && normDist(4, 13) < 0.65f) {
            boostLetter('B', 0.88f)
        }

        // Re-normalize probabilities and pick top class
        var sum = 0f
        for (p in baseProbs) sum += p
        var bestIdx = 0
        var bestProb = 0f
        if (sum > 0f) {
            for (i in baseProbs.indices) {
                baseProbs[i] /= sum
                if (baseProbs[i] > bestProb) {
                    bestProb = baseProbs[i]
                    bestIdx = i
                }
            }
        }

        // Keep confidence intuitive for UI display when boosted
        val displayConf = if (bestProb in 0.45f..0.99f) {
            bestProb.coerceAtLeast(maxOf(normalRes.confidence, mirrorRes.confidence))
        } else {
            bestProb
        }

        val letter = if (bestIdx in labels.indices) labels[bestIdx] else "?"
        return ClassificationResult(letter, displayConf.coerceAtMost(0.99f), baseProbs)
    }

    private fun dist(a: NormalizedLandmark, b: NormalizedLandmark): Float {
        return hypot((a.x() - b.x()).toDouble(), (a.y() - b.y()).toDouble()).toFloat()
    }

    /**
     * Executes real-time neural forward pass on 42 landmark coordinates.
     */
    fun classify(features42: FloatArray): ClassificationResult {
        if (!isNeuralLoaded || features42.size < 42) {
            return ClassificationResult("?", 0.0f, FloatArray(26))
        }

        val a1 = FloatArray(128)
        for (j in 0 until 128) {
            var sum = b0[j]
            for (i in 0 until 42) {
                sum += features42[i] * w0[i * 128 + j]
            }
            a1[j] = if (sum > 0f) sum else 0f
        }

        val a2 = FloatArray(64)
        for (j in 0 until 64) {
            var sum = b1[j]
            for (i in 0 until 128) {
                sum += a1[i] * w1[i * 64 + j]
            }
            a2[j] = if (sum > 0f) sum else 0f
        }

        val logits = FloatArray(26)
        var maxLogit = Float.NEGATIVE_INFINITY
        for (j in 0 until 26) {
            var sum = b2[j]
            for (i in 0 until 64) {
                sum += a2[i] * w2[i * 26 + j]
            }
            logits[j] = sum
            if (sum > maxLogit) maxLogit = sum
        }

        val expVals = FloatArray(26)
        var sumExp = 0f
        for (j in 0 until 26) {
            val ev = exp(logits[j] - maxLogit)
            expVals[j] = ev
            sumExp += ev
        }

        var maxIdx = 0
        var maxProb = 0f
        val probabilities = FloatArray(26)
        for (j in 0 until 26) {
            val prob = if (sumExp > 0f) expVals[j] / sumExp else 0f
            probabilities[j] = prob
            if (prob > maxProb) {
                maxProb = prob
                maxIdx = j
            }
        }

        val letter = if (maxIdx in labels.indices) labels[maxIdx] else "?"
        return ClassificationResult(letter, maxProb, probabilities)
    }

    fun close() {
        // Pure memory engine, no native handles to leak
    }
}
