package com.projectnexus.iwani.ui.screens

import android.Manifest
import android.content.ContentValues
import android.content.pm.PackageManager
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.*
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.SpaceBar
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import com.google.mediapipe.tasks.components.containers.NormalizedLandmark
import com.projectnexus.iwani.AppLanguage
import com.projectnexus.iwani.R
import com.projectnexus.iwani.emergency.EmergencySosManager
import com.projectnexus.iwani.ml.FslCameraAnalyzer
import com.projectnexus.iwani.ui.theme.*
import kotlinx.coroutines.delay
import java.util.concurrent.Executors

@Composable
fun FslCameraScreen(
    sosManager: EmergencySosManager,
    appLanguage: AppLanguage = AppLanguage.ENGLISH,
    isDarkTheme: Boolean = true
) {
    val isTagalog = appLanguage.isTagalog
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val surfaceColor = if (isDarkTheme) DarkSurfaceGlass else Color.White.copy(alpha = 0.92f)
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
    var accumulatedWord by remember { mutableStateOf("") }
    var handLandmarks by remember { mutableStateOf<List<NormalizedLandmark>?>(null) }
    var lensFacing by remember { mutableIntStateOf(CameraSelector.LENS_FACING_FRONT) }
    var showCheatSheet by remember { mutableStateOf(false) }

    // Video Recording State
    var isRecording by remember { mutableStateOf(false) }
    var recordingDuration by remember { mutableIntStateOf(0) }
    var activeRecording by remember { mutableStateOf<Recording?>(null) }
    var videoCaptureInstance by remember { mutableStateOf<VideoCapture<Recorder>?>(null) }

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

    // Dynamic Camera Binding — Re-binds smoothly when lensFacing (front/back) changes
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

        val analyzer = FslCameraAnalyzer(context) { letter, conf, landmarks, committed ->
            detectedLetter = letter
            detectedConfidence = conf
            handLandmarks = landmarks
            if (committed && letter.isNotEmpty()) {
                accumulatedWord += letter
            }
        }

        val imageAnalysis = ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
            .build()
            .also {
                it.setAnalyzer(cameraExecutor, analyzer)
            }

        val recorder = Recorder.Builder()
            .setQualitySelector(QualitySelector.from(Quality.SD, FallbackStrategy.lowerQualityOrHigherThan(Quality.SD)))
            .build()
        val videoCapture = VideoCapture.withOutput(recorder)
        videoCaptureInstance = videoCapture

        try {
            provider.unbindAll()
            provider.bindToLifecycle(lifecycleOwner, cameraSelector, preview, imageAnalysis, videoCapture)
        } catch (e: Exception) {
            try {
                provider.unbindAll()
                provider.bindToLifecycle(lifecycleOwner, cameraSelector, preview, imageAnalysis)
            } catch (ex: Exception) {
                Log.e("FslCamera", "Failed to bind camera: ${ex.message}", ex)
            }
        }
    }

    // Clean disposal on leaving screen
    DisposableEffect(Unit) {
        onDispose {
            activeRecording?.stop()
            cameraExecutor.shutdown()
        }
    }

    // Recording duration timer
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
            // Camera Stream View
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
                        // Thumb
                        0 to 1, 1 to 2, 2 to 3, 3 to 4,
                        // Index
                        0 to 5, 5 to 6, 6 to 7, 7 to 8,
                        // Middle
                        0 to 9, 9 to 10, 10 to 11, 11 to 12,
                        // Ring
                        0 to 13, 13 to 14, 14 to 15, 15 to 16,
                        // Pinky
                        0 to 17, 17 to 18, 18 to 19, 19 to 20,
                        // Palm
                        5 to 9, 9 to 13, 13 to 17
                    )

                    // Draw connecting bones
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

                    // Draw landmark joint points
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

            // Top Status Bar & Action Controls
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 40.dp, start = 16.dp, end = 16.dp),
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
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Cheat sheet / reference chart button
                    IconButton(
                        onClick = { showCheatSheet = true },
                        modifier = Modifier
                            .background(surfaceColor, CircleShape)
                            .border(1.dp, borderColor, CircleShape)
                    ) {
                        Icon(imageVector = Icons.Default.MenuBook, contentDescription = "FSL Guide", tint = IcctGold)
                    }

                    // Flip camera button (Now fully dynamic with feedback)
                    IconButton(
                        onClick = {
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
                        .padding(top = 90.dp)
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

            // Bottom Word Control & Recording Bar (Translucent Glassmorphism)
            Surface(
                shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                color = surfaceColor,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .border(1.dp, borderColor, RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
            ) {
                Column(modifier = Modifier.padding(horizontal = 18.dp, vertical = 18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isTagalog) "Nabubuong Salita / Salin:" else "Formed Word / Translation:",
                            color = textSecondaryColor,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )

                        // Record Button Toggle (Sends directly to Gallery)
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = if (isRecording) EmergencyRed else (if (isDarkTheme) IcctBlue.copy(alpha = 0.25f) else Color(0xFFEFF6FF)),
                            modifier = Modifier
                                .border(1.dp, if (isRecording) EmergencyRed else IcctBlue, RoundedCornerShape(20.dp))
                                .clip(RoundedCornerShape(20.dp))
                        ) {
                            TextButton(
                                onClick = {
                                    if (!isRecording) {
                                        // Start recording
                                        val vc = videoCaptureInstance
                                        if (vc != null) {
                                            val contentValues = ContentValues().apply {
                                                put(MediaStore.MediaColumns.DISPLAY_NAME, "iWani_FSL_${System.currentTimeMillis()}")
                                                put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4")
                                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                                    put(MediaStore.Video.Media.RELATIVE_PATH, "Movies/iWani")
                                                }
                                            }
                                            val outputOptions = MediaStoreOutputOptions.Builder(
                                                context.contentResolver,
                                                MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                                            ).setContentValues(contentValues).build()

                                            activeRecording = vc.output
                                                .prepareRecording(context, outputOptions)
                                                .start(ContextCompat.getMainExecutor(context)) { event ->
                                                    when (event) {
                                                        is VideoRecordEvent.Start -> {
                                                            isRecording = true
                                                        }
                                                        is VideoRecordEvent.Finalize -> {
                                                            isRecording = false
                                                            if (!event.hasError()) {
                                                                Toast.makeText(context, "✓ Translation video saved to Gallery (Movies/iWani)!", Toast.LENGTH_LONG).show()
                                                            }
                                                        }
                                                    }
                                                }
                                        } else {
                                            isRecording = true
                                        }
                                    } else {
                                        // Stop recording
                                        activeRecording?.stop()
                                        activeRecording = null
                                        isRecording = false
                                        Toast.makeText(context, "✓ Translation recorded and saved to Gallery!", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Icon(
                                    imageVector = if (isRecording) Icons.Default.Stop else Icons.Default.FiberManualRecord,
                                    contentDescription = "Record",
                                    tint = if (isRecording) Color.White else EmergencyRed,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    text = if (isRecording) (if (isTagalog) "Ihinto" else "Stop") else (if (isTagalog) "I-record sa Gallery" else "Record to Gallery"),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isRecording) Color.White else textPrimaryColor
                                )
                            }
                        }
                    }

                    // Formed Word display with instant Clear-All (x) button
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (accumulatedWord.isNotEmpty()) accumulatedWord else (if (isTagalog) "[Wala pang titik]" else "[No letters yet]"),
                            color = if (accumulatedWord.isNotEmpty()) TextGold else textSecondaryColor,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f)
                        )

                        if (accumulatedWord.isNotEmpty()) {
                            IconButton(
                                onClick = {
                                    accumulatedWord = ""
                                    Toast.makeText(context, if (isTagalog) "Nabura ang buong salita." else "Cleared all letters.", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Clear All",
                                    tint = EmergencyRed,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(10.dp))

                    // 3 Weighted Interactive Control Buttons (Space, Backspace, Speak)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Space button
                        Button(
                            onClick = {
                                accumulatedWord += " "
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp),
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
                                color = textPrimaryColor
                            )
                        }

                        // Backspace / Erase letter button
                        Button(
                            onClick = {
                                if (accumulatedWord.isNotEmpty()) {
                                    accumulatedWord = accumulatedWord.dropLast(1)
                                } else {
                                    Toast.makeText(context, if (isTagalog) "Walang titik na buburahin." else "No letters to erase.", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp),
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
                                color = textPrimaryColor
                            )
                        }

                        // Speak Out Loud button
                        Button(
                            onClick = {
                                if (accumulatedWord.isNotBlank()) {
                                    sosManager.speak(accumulatedWord)
                                } else {
                                    Toast.makeText(context, if (isTagalog) "Mag-sign muna ng mga titik bago magsalita." else "Sign letters first before speaking.", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp),
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
                                color = Color.White
                            )
                        }
                    }
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (isTagalog) "Kinakailangan ang Pahintulot sa Camera" else "Camera Permission Required",
                    color = textPrimaryColor,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(14.dp))
                Button(
                    onClick = { launcher.launch(Manifest.permission.CAMERA) },
                    colors = ButtonDefaults.buttonColors(containerColor = IcctBlue)
                ) {
                    Text(if (isTagalog) "Payagan ang Camera" else "Grant Camera Permission")
                }
            }
        }

        // Live FSL Alphabet Reference Dialog
        if (showCheatSheet) {
            Dialog(
                onDismissRequest = { showCheatSheet = false },
                properties = DialogProperties(usePlatformDefaultWidth = false)
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    shape = RoundedCornerShape(20.dp),
                    color = if (isDarkTheme) DarkBackground else Color.White,
                    border = androidx.compose.foundation.BorderStroke(1.dp, borderColor)
                ) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "FSL Alphabet Guide (A-Z)",
                                color = IcctGold,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                            IconButton(onClick = { showCheatSheet = false }) {
                                Icon(Icons.Default.Close, contentDescription = "Close", tint = textPrimaryColor)
                            }
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.alfabet_fsl_full),
                                contentDescription = "FSL Guide",
                                modifier = Modifier.fillMaxWidth(),
                                contentScale = ContentScale.Fit
                            )
                        }
                    }
                }
            }
        }
    }
}
