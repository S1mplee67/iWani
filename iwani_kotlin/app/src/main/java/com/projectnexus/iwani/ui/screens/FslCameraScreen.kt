package com.projectnexus.iwani.ui.screens

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Typeface
import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.media.MediaMuxer
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
import android.view.Surface
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.SpaceBar
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.mediapipe.tasks.components.containers.NormalizedLandmark
import com.projectnexus.iwani.AppLanguage
import com.projectnexus.iwani.emergency.EmergencySosManager
import com.projectnexus.iwani.ml.FslCameraAnalyzer
import com.projectnexus.iwani.ui.components.FslAlphabetFullModal
import com.projectnexus.iwani.ui.components.autoSlideIfOverflow
import com.projectnexus.iwani.ui.components.interactiveBounceClick
import com.projectnexus.iwani.ui.components.tutorialSpotlightTarget
import com.projectnexus.iwani.ui.sound.UiSoundManager
import com.projectnexus.iwani.ui.theme.*
import kotlinx.coroutines.delay
import java.io.File
import java.util.concurrent.Executors

/**
 * Hardware Surface MP4 Video Recorder that burns the live FSL Subtitle Bar,
 * Formed Word, Live Detected Letter Badge, and 21-Joint Hand Skeleton directly
 * into the recorded video frames saved to Gallery (Movies/iWani).
 */
private class SubtitleVideoRecorder(private val context: Context) {
    private val lock = Any()
    private var encoder: MediaCodec? = null
    private var inputSurface: Surface? = null
    private var muxer: MediaMuxer? = null
    private var trackIndex = -1
    private var muxerStarted = false
    private var tempFile: File? = null
    private var lastPtsUs = 0L

    @Volatile
    var isRecording = false
        private set

    private val width = 720
    private val height = 1280
    private val bufferInfo = MediaCodec.BufferInfo()

    private val bitmapPaint = Paint(Paint.FILTER_BITMAP_FLAG)
    private val cardBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.argb(232, 11, 19, 38)
        style = Paint.Style.FILL
    }
    private val cardBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.rgb(245, 158, 11)
        style = Paint.Style.STROKE
        strokeWidth = 3.5f
    }
    private val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.rgb(147, 197, 253)
        textSize = 22f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }
    private val signStatusPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.rgb(52, 211, 153)
        textSize = 22f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        textAlign = Paint.Align.RIGHT
    }
    private val subtitlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.rgb(251, 191, 36)
        textSize = 40f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        textAlign = Paint.Align.CENTER
    }
    private val bigLetterPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.rgb(251, 191, 36)
        textSize = 68f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        textAlign = Paint.Align.CENTER
    }
    private val bonePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.argb(200, 252, 211, 77)
        strokeWidth = 6f
        style = Paint.Style.STROKE
    }
    private val jointOuterPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.rgb(245, 158, 11)
        style = Paint.Style.FILL
    }
    private val jointInnerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.WHITE
        style = Paint.Style.FILL
    }

    private val fingerBones = listOf(
        0 to 1, 1 to 2, 2 to 3, 3 to 4,
        0 to 5, 5 to 6, 6 to 7, 7 to 8,
        0 to 9, 9 to 10, 10 to 11, 11 to 12,
        0 to 13, 13 to 14, 14 to 15, 15 to 16,
        0 to 17, 17 to 18, 18 to 19, 19 to 20,
        5 to 9, 9 to 13, 13 to 17
    )

    fun start(): Boolean = synchronized(lock) {
        if (isRecording) return true
        return try {
            val out = File(context.cacheDir, "iwani_rec_${System.currentTimeMillis()}.mp4")
            tempFile = out
            val format = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC, width, height).apply {
                setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface)
                setInteger(MediaFormat.KEY_BIT_RATE, 4_000_000)
                setInteger(MediaFormat.KEY_FRAME_RATE, 20)
                setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1)
            }
            val enc = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_VIDEO_AVC)
            enc.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
            val surface = enc.createInputSurface()
            enc.start()

            encoder = enc
            inputSurface = surface
            muxer = MediaMuxer(out.absolutePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
            trackIndex = -1
            muxerStarted = false
            lastPtsUs = 0L
            isRecording = true
            true
        } catch (e: Exception) {
            Log.e("SubtitleVideoRecorder", "Failed to start recorder: ${e.message}", e)
            cleanupLocked()
            false
        }
    }

    fun recordFrame(
        frameBitmap: Bitmap,
        isFrontCamera: Boolean,
        detectedLetter: String,
        detectedConfidence: Float,
        accumulatedWord: String,
        landmarks: List<NormalizedLandmark>?,
        isTagalog: Boolean
    ) {
        synchronized(lock) {
            if (!isRecording) return
            val surface = inputSurface ?: return
            try {
                val canvas = surface.lockCanvas(null) ?: return
            try {
                // 1. Draw Camera Frame (center-cropped to 720x1280, mirrored if front camera)
                canvas.drawColor(android.graphics.Color.BLACK)
                canvas.save()
                if (isFrontCamera) {
                    canvas.scale(-1f, 1f, width / 2f, height / 2f)
                }
                val srcRatio = frameBitmap.width.toFloat() / frameBitmap.height.toFloat()
                val dstRatio = width.toFloat() / height.toFloat()
                val srcRect = if (srcRatio > dstRatio) {
                    val cropW = (frameBitmap.height * dstRatio).toInt()
                    val left = (frameBitmap.width - cropW) / 2
                    Rect(left, 0, left + cropW, frameBitmap.height)
                } else {
                    val cropH = (frameBitmap.width / dstRatio).toInt()
                    val top = (frameBitmap.height - cropH) / 2
                    Rect(0, top, frameBitmap.width, top + cropH)
                }
                canvas.drawBitmap(frameBitmap, srcRect, Rect(0, 0, width, height), bitmapPaint)
                canvas.restore()

                // 2. Draw 21-Joint MediaPipe Hand Skeleton
                if (!landmarks.isNullOrEmpty()) {
                    for ((s, e) in fingerBones) {
                        if (s < landmarks.size && e < landmarks.size) {
                            val p1 = landmarks[s]
                            val p2 = landmarks[e]
                            val x1 = (if (isFrontCamera) 1f - p1.x() else p1.x()) * width
                            val y1 = p1.y() * height
                            val x2 = (if (isFrontCamera) 1f - p2.x() else p2.x()) * width
                            val y2 = p2.y() * height
                            canvas.drawLine(x1, y1, x2, y2, bonePaint)
                        }
                    }
                    for (lm in landmarks) {
                        val px = (if (isFrontCamera) 1f - lm.x() else lm.x()) * width
                        val py = lm.y() * height
                        canvas.drawCircle(px, py, 10f, jointOuterPaint)
                        canvas.drawCircle(px, py, 4.5f, jointInnerPaint)
                    }
                }

                // 3. Draw Floating Live Detected Letter Badge on Right if active
                val hasValidDetection = detectedLetter.isNotEmpty() && detectedConfidence >= 0.35f
                if (hasValidDetection) {
                    val badgeRect = RectF(width - 154f, height * 0.44f, width - 28f, height * 0.44f + 126f)
                    canvas.drawRoundRect(badgeRect, 28f, 28f, cardBgPaint)
                    canvas.drawRoundRect(badgeRect, 28f, 28f, cardBorderPaint)
                    canvas.drawText(
                        detectedLetter,
                        badgeRect.centerX(),
                        badgeRect.centerY() + 24f,
                        bigLetterPaint
                    )
                }

                // 4. Draw Burned-In Bottom FSL Subtitle Bar (always visible in saved video!)
                val subRect = RectF(26f, height - 250f, width - 26f, height - 42f)
                canvas.drawRoundRect(subRect, 32f, 32f, cardBgPaint)
                canvas.drawRoundRect(subRect, 32f, 32f, cardBorderPaint)

                val headerLeft = if (isTagalog) "iWani • NABUBUONG SALITA / SUBTITLE:" else "iWani • FORMED WORD / FSL SUBTITLE:"
                canvas.drawText(headerLeft, subRect.left + 26f, subRect.top + 44f, labelPaint)

                val signStatus = if (hasValidDetection) {
                    "FSL: $detectedLetter (${(detectedConfidence * 100).toInt()}%)"
                } else {
                    if (isTagalog) "Naghahanap..." else "Scanning..."
                }
                canvas.drawText(signStatus, subRect.right - 26f, subRect.top + 44f, signStatusPaint)

                val mainSubtitle = when {
                    accumulatedWord.isNotBlank() && hasValidDetection -> "$accumulatedWord  [$detectedLetter]"
                    accumulatedWord.isNotBlank() -> accumulatedWord
                    hasValidDetection -> if (isTagalog) " Titik: $detectedLetter " else " Signing: $detectedLetter "
                    else -> if (isTagalog) "[ Mag-sign upang mabuo ang subtitle ]" else "[ Sign FSL letters to display subtitle ]"
                }

                subtitlePaint.textSize = when {
                    mainSubtitle.length > 28 -> 28f
                    mainSubtitle.length > 20 -> 33f
                    else -> 40f
                }
                canvas.drawText(
                    mainSubtitle,
                    subRect.centerX(),
                    subRect.top + 132f,
                    subtitlePaint
                )
            } finally {
                surface.unlockCanvasAndPost(canvas)
            }
            drainEncoderLocked(endOfStream = false)
            } catch (e: Exception) {
                Log.e("SubtitleVideoRecorder", "Frame encode error: ${e.message}")
            }
        }
    }

    fun stopAndSaveToGallery(isTagalog: Boolean, onComplete: (Boolean) -> Unit) {
        Thread {
            var savedOk = false
            synchronized(lock) {
                if (!isRecording) return@Thread
                isRecording = false
                try {
                    encoder?.signalEndOfInputStream()
                    drainEncoderLocked(endOfStream = true)
                } catch (e: Exception) {
                    Log.e("SubtitleVideoRecorder", "EOS drain error: ${e.message}")
                }
                val recordedFile = tempFile
                cleanupLocked()

                if (recordedFile != null && recordedFile.exists() && recordedFile.length() > 0L) {
                    savedOk = copyFileToMediaStore(recordedFile)
                    recordedFile.delete()
                }
            }
            Handler(Looper.getMainLooper()).post {
                onComplete(savedOk)
            }
        }.start()
    }

    private fun drainEncoderLocked(endOfStream: Boolean) {
        val enc = encoder ?: return
        val mux = muxer ?: return
        while (true) {
            val outIndex = enc.dequeueOutputBuffer(bufferInfo, if (endOfStream) 10_000L else 0L)
            when {
                outIndex == MediaCodec.INFO_TRY_AGAIN_LATER -> {
                    if (!endOfStream) break
                }
                outIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED -> {
                    if (!muxerStarted) {
                        trackIndex = mux.addTrack(enc.outputFormat)
                        mux.start()
                        muxerStarted = true
                    }
                }
                outIndex >= 0 -> {
                    val encodedData = enc.getOutputBuffer(outIndex)
                    if (encodedData != null && bufferInfo.size > 0 && muxerStarted) {
                        if ((bufferInfo.flags and MediaCodec.BUFFER_FLAG_CODEC_CONFIG) == 0) {
                            val safePts = maxOf(lastPtsUs + 1000L, bufferInfo.presentationTimeUs)
                            bufferInfo.presentationTimeUs = safePts
                            lastPtsUs = safePts
                            encodedData.position(bufferInfo.offset)
                            encodedData.limit(bufferInfo.offset + bufferInfo.size)
                            mux.writeSampleData(trackIndex, encodedData, bufferInfo)
                        }
                    }
                    enc.releaseOutputBuffer(outIndex, false)
                    if ((bufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                        break
                    }
                }
            }
        }
    }

    private fun copyFileToMediaStore(sourceFile: File): Boolean {
        return try {
            val resolver = context.contentResolver
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, "iWani_FSL_${System.currentTimeMillis()}.mp4")
                put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    put(MediaStore.Video.Media.RELATIVE_PATH, "Movies/iWani")
                    put(MediaStore.Video.Media.IS_PENDING, 1)
                }
            }
            val uri = resolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, contentValues) ?: return false
            resolver.openOutputStream(uri)?.use { outStream ->
                sourceFile.inputStream().use { inStream ->
                    inStream.copyTo(outStream)
                }
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val doneValues = ContentValues().apply {
                    put(MediaStore.Video.Media.IS_PENDING, 0)
                }
                resolver.update(uri, doneValues, null, null)
            }
            true
        } catch (e: Exception) {
            Log.e("SubtitleVideoRecorder", "MediaStore save error: ${e.message}", e)
            false
        }
    }

    private fun cleanupLocked() {
        try {
            encoder?.stop()
        } catch (_: Exception) {}
        try {
            encoder?.release()
        } catch (_: Exception) {}
        encoder = null

        try {
            inputSurface?.release()
        } catch (_: Exception) {}
        inputSurface = null

        try {
            if (muxerStarted) {
                muxer?.stop()
            }
        } catch (_: Exception) {}
        try {
            muxer?.release()
        } catch (_: Exception) {}
        muxer = null
        muxerStarted = false
        trackIndex = -1
    }
}

@Composable
fun FslCameraScreen(
    sosManager: EmergencySosManager,
    appLanguage: AppLanguage = AppLanguage.ENGLISH,
    isDarkTheme: Boolean = true
) {
    val isTagalog = appLanguage.isTagalog
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val focusManager = LocalFocusManager.current

    val surfaceColor = if (isDarkTheme) DarkSurfaceGlass else Color.White.copy(alpha = 0.94f)
    val borderColor = if (isDarkTheme) DarkSurfaceBorder else Color(0xFFE2E8F0)
    val textPrimaryColor = if (isDarkTheme) TextPrimary else Color(0xFF0F172A)
    val textSecondaryColor = if (isDarkTheme) TextSecondary else Color(0xFF64748B)

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasCameraPermission = granted
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            launcher.launch(Manifest.permission.CAMERA)
        }
    }

    var detectedLetter by remember { mutableStateOf("") }
    var detectedConfidence by remember { mutableFloatStateOf(0.0f) }

    // Editable TextFieldValue so users can tap between any formed letters, move cursor, and type/insert mid-word
    var wordFieldValue by remember { mutableStateOf(TextFieldValue(text = "", selection = TextRange(0))) }
    val accumulatedWord = wordFieldValue.text

    fun insertAtCursor(insertStr: String) {
        val currentText = wordFieldValue.text
        val cursor = wordFieldValue.selection.start.coerceIn(0, currentText.length)
        val end = wordFieldValue.selection.end.coerceIn(cursor, currentText.length)
        val newText = currentText.substring(0, cursor) + insertStr + currentText.substring(end)
        val newCursor = cursor + insertStr.length
        wordFieldValue = TextFieldValue(
            text = newText,
            selection = TextRange(newCursor)
        )
    }

    fun deleteAtCursor() {
        val currentText = wordFieldValue.text
        if (currentText.isEmpty()) return
        val start = wordFieldValue.selection.start.coerceIn(0, currentText.length)
        val end = wordFieldValue.selection.end.coerceIn(start, currentText.length)
        if (start != end) {
            val newText = currentText.removeRange(start, end)
            wordFieldValue = TextFieldValue(text = newText, selection = TextRange(start))
        } else if (start > 0) {
            val newText = currentText.removeRange(start - 1, start)
            wordFieldValue = TextFieldValue(text = newText, selection = TextRange(start - 1))
        } else {
            // If cursor is at index 0, delete last character as fallback convenience
            val newText = currentText.dropLast(1)
            wordFieldValue = TextFieldValue(text = newText, selection = TextRange(newText.length))
        }
    }

    var handLandmarks by remember { mutableStateOf<List<NormalizedLandmark>?>(null) }
    var lensFacing by remember { mutableIntStateOf(CameraSelector.LENS_FACING_FRONT) }
    var showCheatSheet by remember { mutableStateOf(false) }

    // Video Recording State (records camera + burned-in FSL subtitles + 21-joint hand skeleton)
    var isRecording by remember { mutableStateOf(false) }
    var recordingDuration by remember { mutableIntStateOf(0) }
    val subtitleVideoRecorder = remember { SubtitleVideoRecorder(context) }

    val currentDetectedLetter by rememberUpdatedState(detectedLetter)
    val currentDetectedConfidence by rememberUpdatedState(detectedConfidence)
    val currentAccumulatedWord by rememberUpdatedState(accumulatedWord)
    val currentHandLandmarks by rememberUpdatedState(handLandmarks)
    val currentIsTagalog by rememberUpdatedState(isTagalog)

    var previewView by remember { mutableStateOf<PreviewView?>(null) }
    var cameraProvider by remember { mutableStateOf<ProcessCameraProvider?>(null) }
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }

    // Initialize CameraProvider
    LaunchedEffect(hasCameraPermission) {
        if (hasCameraPermission) {
            val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
            cameraProviderFuture.addListener({
                cameraProvider = cameraProviderFuture.get()
            }, ContextCompat.getMainExecutor(context))
        }
    }

    // Dynamic Camera Binding
    LaunchedEffect(cameraProvider, previewView, lensFacing, lifecycleOwner) {
        val provider = cameraProvider ?: return@LaunchedEffect
        val pView = previewView ?: return@LaunchedEffect

        val cameraSelector = CameraSelector.Builder().requireLensFacing(lensFacing).build()

        if (!provider.hasCamera(cameraSelector)) {
            val unavailableName = if (lensFacing == CameraSelector.LENS_FACING_FRONT) "Front" else "Back"
            Toast.makeText(context, "$unavailableName camera is not available on this device", Toast.LENGTH_SHORT).show()
            return@LaunchedEffect
        }

        val preview = Preview.Builder().build().also {
            it.setSurfaceProvider(pView.surfaceProvider)
        }

        val isFront = lensFacing == CameraSelector.LENS_FACING_FRONT
        val analyzer = FslCameraAnalyzer(
            context = context,
            onFrame = { frameBitmap ->
                if (subtitleVideoRecorder.isRecording) {
                    subtitleVideoRecorder.recordFrame(
                        frameBitmap = frameBitmap,
                        isFrontCamera = isFront,
                        detectedLetter = currentDetectedLetter,
                        detectedConfidence = currentDetectedConfidence,
                        accumulatedWord = currentAccumulatedWord,
                        landmarks = currentHandLandmarks,
                        isTagalog = currentIsTagalog
                    )
                }
            }
        ) { letter, conf, landmarks, committed ->
            detectedLetter = letter
            detectedConfidence = conf
            handLandmarks = landmarks
            if (committed && letter.isNotEmpty()) {
                insertAtCursor(letter)
            }
        }

        val imageAnalysis = ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
            .build()
            .also {
                it.setAnalyzer(cameraExecutor, analyzer)
            }

        try {
            provider.unbindAll()
            provider.bindToLifecycle(lifecycleOwner, cameraSelector, preview, imageAnalysis)
        } catch (ex: Exception) {
            Log.e("FslCamera", "Failed to bind camera: ${ex.message}", ex)
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            if (subtitleVideoRecorder.isRecording) {
                subtitleVideoRecorder.stopAndSaveToGallery(isTagalog) {}
            }
            cameraExecutor.shutdown()
        }
    }

    LaunchedEffect(isRecording) {
        if (isRecording) {
            recordingDuration = 0
            while (isRecording) {
                delay(1000L)
                recordingDuration++
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(DarkBackground)) {
        if (hasCameraPermission) {
            AndroidView(
                factory = { ctx ->
                    PreviewView(ctx).apply {
                        implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                    }.also {
                        previewView = it
                    }
                },
                modifier = Modifier.fillMaxSize()
            )

            // Live MediaPipe Hand Landmark Skeleton Overlay
            val landmarks = handLandmarks
            val isFrontCamera = lensFacing == CameraSelector.LENS_FACING_FRONT
            if (landmarks != null && landmarks.isNotEmpty()) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val fingerBones = listOf(
                        0 to 1, 1 to 2, 2 to 3, 3 to 4,
                        0 to 5, 5 to 6, 6 to 7, 7 to 8,
                        0 to 9, 9 to 10, 10 to 11, 11 to 12,
                        0 to 13, 13 to 14, 14 to 15, 15 to 16,
                        0 to 17, 17 to 18, 18 to 19, 19 to 20,
                        5 to 9, 9 to 13, 13 to 17
                    )

                    for ((s, e) in fingerBones) {
                        if (s < landmarks.size && e < landmarks.size) {
                            val p1 = landmarks[s]
                            val p2 = landmarks[e]
                            val x1 = if (isFrontCamera) (1f - p1.x()) * size.width else p1.x() * size.width
                            val y1 = p1.y() * size.height
                            val x2 = if (isFrontCamera) (1f - p2.x()) * size.width else p2.x() * size.width
                            val y2 = p2.y() * size.height

                            drawLine(
                                color = IcctGoldLight.copy(alpha = 0.75f),
                                start = Offset(x1, y1),
                                end = Offset(x2, y2),
                                strokeWidth = 5f
                            )
                        }
                    }

                    for (lm in landmarks) {
                        val px = if (isFrontCamera) (1f - lm.x()) * size.width else lm.x() * size.width
                        val py = lm.y() * size.height

                        drawCircle(
                            color = IcctGold,
                            radius = 9f,
                            center = Offset(px, py)
                        )
                        drawCircle(
                            color = Color.White,
                            radius = 4f,
                            center = Offset(px, py)
                        )
                    }
                }
            }

            // Viewfinder target region for Tutorial Spotlight
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 80.dp, bottom = 240.dp, start = 16.dp, end = 16.dp)
                    .align(Alignment.Center)
                    .fillMaxHeight()
                    .tutorialSpotlightTarget("camera_viewfinder")
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 80.dp, bottom = 240.dp, start = 16.dp, end = 16.dp)
                    .align(Alignment.Center)
                    .fillMaxHeight()
                    .tutorialSpotlightTarget("camera_viewfinder"),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (isTagalog) "Kinakailangan ang Pahintulot sa Kamera" else "Camera Permission Required",
                        color = textPrimaryColor,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(14.dp))
                    Button(
                        onClick = { launcher.launch(Manifest.permission.CAMERA) },
                        colors = ButtonDefaults.buttonColors(containerColor = IcctBlue)
                    ) {
                        Text(if (isTagalog) "Payagan ang Kamera" else "Grant Camera Permission")
                    }
                }
            }
        }

            // Top Status Bar & Action Controls
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp, start = 16.dp, end = 16.dp)
                    .tutorialSpotlightTarget("camera_top_bar"),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = surfaceColor,
                    modifier = Modifier.border(1.dp, borderColor, RoundedCornerShape(20.dp))
                ) {
                    val hasValidDetection = detectedLetter.isNotEmpty() && detectedConfidence >= 0.35f
                    val statusLabel = if (hasValidDetection) {
                        "FSL: $detectedLetter (${(detectedConfidence * 100).toInt()}%)"
                    } else if (handLandmarks != null && handLandmarks!!.isNotEmpty()) {
                        if (isTagalog) "Ayusin ang kamay..." else "Hold hand steady..."
                    } else {
                        if (isTagalog) "Naghahanap ng kamay..." else "Scanning for hand..."
                    }

                    Text(
                        text = statusLabel,
                        color = if (hasValidDetection) SuccessGreen else textPrimaryColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        maxLines = 1,
                        softWrap = false,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(
                        onClick = {
                            UiSoundManager.playClick(context)
                            showCheatSheet = true
                        },
                        modifier = Modifier
                            .background(surfaceColor, CircleShape)
                            .border(1.dp, borderColor, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.MenuBook,
                            contentDescription = "FSL Guide",
                            tint = IcctGold
                        )
                    }

                    IconButton(
                        onClick = {
                            UiSoundManager.playClick(context)
                            if (isRecording) {
                                Toast.makeText(
                                    context,
                                    if (isTagalog) "Ihinto muna ang pag-record bago ilipat ang kamera." else "Stop recording before switching camera.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                lensFacing = if (lensFacing == CameraSelector.LENS_FACING_FRONT) {
                                    CameraSelector.LENS_FACING_BACK
                                } else {
                                    CameraSelector.LENS_FACING_FRONT
                                }
                                handLandmarks = null
                                detectedLetter = ""
                                detectedConfidence = 0f
                                Toast.makeText(
                                    context,
                                    if (lensFacing == CameraSelector.LENS_FACING_FRONT) {
                                        if (isTagalog) "Kamera: Harap (Front)" else "Camera: Front (Selfie)"
                                    } else {
                                        if (isTagalog) "Kamera: Likod (Back)" else "Camera: Back"
                                    },
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        },
                        modifier = Modifier
                            .background(surfaceColor, CircleShape)
                            .border(1.dp, borderColor, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Cameraswitch,
                            contentDescription = "Flip Camera",
                            tint = if (lensFacing == CameraSelector.LENS_FACING_BACK) IcctGold else textPrimaryColor
                        )
                    }
                }
            }

            // Big Live Letter Overlay
            if (detectedLetter.isNotEmpty() && detectedConfidence > 0.45f) {
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 20.dp)
                        .size(80.dp)
                        .background(surfaceColor, RoundedCornerShape(20.dp))
                        .border(1.5.dp, IcctGold, RoundedCornerShape(20.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = detectedLetter,
                        color = IcctGold,
                        fontSize = 42.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }

            // Live Recording Badge
            if (isRecording) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = EmergencyRed.copy(alpha = 0.9f),
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 74.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.FiberManualRecord, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        val mins = recordingDuration / 60
                        val secs = recordingDuration % 60
                        Text(
                            text = String.format("REC %02d:%02d", mins, secs),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            // Bottom Word Control & Editable Mid-Word Typing Bar
            Surface(
                shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                color = surfaceColor,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .tutorialSpotlightTarget("camera_formed_word")
                    .border(1.dp, borderColor, RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
            ) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isTagalog) "Nabubuong Salita / Salin:" else "Formed Word / Translation:",
                                color = textSecondaryColor,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                softWrap = false,
                                modifier = Modifier.autoSlideIfOverflow()
                            )
                            Text(
                                text = if (isTagalog)
                                    "Pindutin ang pagitan ng mga titik o ◀ ▶ para mag-type"
                                else
                                    "Tap between letters or use ◀ ▶ to type anywhere",
                                color = textSecondaryColor.copy(alpha = 0.78f),
                                fontSize = 10.sp,
                                maxLines = 1,
                                softWrap = false,
                                modifier = Modifier.autoSlideIfOverflow()
                            )
                        }

                        // Record Button Toggle (Records with Burned-In FSL Subtitles directly to Gallery)
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = if (isRecording) EmergencyRed else (if (isDarkTheme) IcctBlue.copy(alpha = 0.25f) else Color(0xFFEFF6FF)),
                            modifier = Modifier
                                .border(1.dp, if (isRecording) EmergencyRed else IcctBlue, RoundedCornerShape(20.dp))
                                .clip(RoundedCornerShape(20.dp))
                        ) {
                            TextButton(
                                onClick = {
                                    UiSoundManager.playClick(context)
                                    if (!isRecording) {
                                        val started = subtitleVideoRecorder.start()
                                        if (started) {
                                            isRecording = true
                                            Toast.makeText(
                                                context,
                                                if (isTagalog) "🔴 Nagre-record na (kasama ang FSL Subtitle sa video)..." else "🔴 Recording started (with burned-in FSL Subtitles)...",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        } else {
                                            Toast.makeText(
                                                context,
                                                if (isTagalog) "Hindi masimulan ang pag-record." else "Could not start video recorder.",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    } else {
                                        isRecording = false
                                        subtitleVideoRecorder.stopAndSaveToGallery(isTagalog) { savedOk ->
                                            Toast.makeText(
                                                context,
                                                if (savedOk) {
                                                    if (isTagalog) "✓ Na-save ang video na may FSL Subtitle sa Gallery (Movies/iWani)!" else "✓ Video with FSL Subtitles saved to Gallery (Movies/iWani)!"
                                                } else {
                                                    if (isTagalog) "✓ Na-save na ang na-record na video sa Gallery!" else "✓ Recorded video saved to Gallery!"
                                                },
                                                Toast.LENGTH_LONG
                                            ).show()
                                        }
                                    }
                                },
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Icon(
                                    imageVector = if (isRecording) Icons.Default.Stop else Icons.Default.FiberManualRecord,
                                    contentDescription = "Record",
                                    tint = if (isRecording) Color.White else EmergencyRed,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    text = if (isRecording) (if (isTagalog) "Ihinto" else "Stop") else (if (isTagalog) "I-record" else "Record to Gallery"),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    softWrap = false,
                                    color = if (isRecording) Color.White else textPrimaryColor,
                                    modifier = Modifier.autoSlideIfOverflow()
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    // Interactive Editable Formed Word Box + Left/Right Cursor Controls
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Move Cursor Left (◀) between formed letters
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isDarkTheme) DarkSurfaceHigh else Color(0xFFF1F5F9),
                            border = androidx.compose.foundation.BorderStroke(1.dp, borderColor),
                            modifier = Modifier
                                .size(38.dp)
                                .interactiveBounceClick {
                                    val pos = (wordFieldValue.selection.start - 1).coerceAtLeast(0)
                                    wordFieldValue = wordFieldValue.copy(selection = TextRange(pos))
                                }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                                    contentDescription = "Cursor Left",
                                    tint = IcctGold,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        // Editable Formed Word TextField — Tap between any letters to place cursor & type!
                        OutlinedTextField(
                            value = wordFieldValue,
                            onValueChange = { newVal ->
                                wordFieldValue = newVal.copy(text = newVal.text.uppercase())
                            },
                            placeholder = {
                                Text(
                                    text = if (isTagalog) "Mag-sign o mag-type dito..." else "Sign or tap to type here...",
                                    fontSize = 15.sp,
                                    color = textSecondaryColor
                                )
                            },
                            textStyle = TextStyle(
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.5.sp,
                                color = TextGold
                            ),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                capitalization = KeyboardCapitalization.Characters,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = { focusManager.clearFocus() }
                            ),
                            trailingIcon = {
                                if (accumulatedWord.isNotEmpty()) {
                                    IconButton(
                                        onClick = {
                                            UiSoundManager.playClick(context)
                                            wordFieldValue = TextFieldValue("", TextRange(0))
                                            Toast.makeText(
                                                context,
                                                if (isTagalog) "Nabura ang buong salita." else "Cleared all letters.",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Clear All",
                                            tint = EmergencyRed,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            },
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = if (isDarkTheme) Color(0xFF0F172A) else Color(0xFFF8FAFC),
                                unfocusedContainerColor = if (isDarkTheme) Color(0xFF0F172A).copy(alpha = 0.7f) else Color(0xFFF8FAFC),
                                focusedBorderColor = IcctGold,
                                unfocusedBorderColor = borderColor,
                                cursorColor = IcctGold
                            ),
                            modifier = Modifier.weight(1f)
                        )

                        // Move Cursor Right (▶) between formed letters
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isDarkTheme) DarkSurfaceHigh else Color(0xFFF1F5F9),
                            border = androidx.compose.foundation.BorderStroke(1.dp, borderColor),
                            modifier = Modifier
                                .size(38.dp)
                                .interactiveBounceClick {
                                    val len = wordFieldValue.text.length
                                    val pos = (wordFieldValue.selection.start + 1).coerceAtMost(len)
                                    wordFieldValue = wordFieldValue.copy(selection = TextRange(pos))
                                }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                    contentDescription = "Cursor Right",
                                    tint = IcctGold,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(10.dp))

                    // 3 Weighted Interactive Control Buttons (Space at cursor, Delete at cursor, Speak)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                UiSoundManager.playClick(context)
                                insertAtCursor(" ")
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp),
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 6.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isDarkTheme) DarkSurfaceHigh else Color(0xFFF1F5F9)
                            ),
                            border = androidx.compose.foundation.BorderStroke(1.dp, borderColor)
                        ) {
                            Icon(Icons.Default.SpaceBar, contentDescription = "Space", modifier = Modifier.size(16.dp), tint = IcctGold)
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = if (isTagalog) "Espasyo" else "Space",
                                maxLines = 1,
                                softWrap = false,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = textPrimaryColor,
                                modifier = Modifier.autoSlideIfOverflow()
                            )
                        }

                        Button(
                            onClick = {
                                UiSoundManager.playClick(context)
                                if (accumulatedWord.isNotEmpty()) {
                                    deleteAtCursor()
                                } else {
                                    Toast.makeText(
                                        context,
                                        if (isTagalog) "Walang titik na buburahin." else "No letters to erase.",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp),
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 6.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isDarkTheme) DarkSurfaceHigh else Color(0xFFF1F5F9)
                            ),
                            border = androidx.compose.foundation.BorderStroke(1.dp, borderColor)
                        ) {
                            Icon(Icons.Default.Backspace, contentDescription = "Backspace", modifier = Modifier.size(16.dp), tint = EmergencyRed)
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = if (isTagalog) "Burahin" else "Delete",
                                maxLines = 1,
                                softWrap = false,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = textPrimaryColor,
                                modifier = Modifier.autoSlideIfOverflow()
                            )
                        }

                        Button(
                            onClick = {
                                UiSoundManager.playClick(context)
                                if (accumulatedWord.isNotBlank()) {
                                    sosManager.speak(accumulatedWord)
                                } else {
                                    Toast.makeText(
                                        context,
                                        if (isTagalog) "Mag-sign o mag-type muna ng salita bago magsalita." else "Sign or type letters first before speaking.",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp),
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 6.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = IcctBlue),
                            border = androidx.compose.foundation.BorderStroke(1.dp, IcctBlue)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = "Speak", modifier = Modifier.size(16.dp), tint = Color.White)
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = if (isTagalog) "Salita" else "Speak",
                                maxLines = 1,
                                softWrap = false,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White,
                                modifier = Modifier.autoSlideIfOverflow()
                            )
                        }
                    }
                }
            }

        // Enlarged Full-Portrait FSL Alphabet Chart (A-Z) with Ambient Aurora Glow
        if (showCheatSheet) {
            FslAlphabetFullModal(
                isTagalog = isTagalog,
                onDismiss = { showCheatSheet = false }
            )
        }
    }
}
