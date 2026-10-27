package com.projectnexus.iwani

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.projectnexus.iwani.emergency.EmergencySosManager
import com.projectnexus.iwani.ui.screens.*
import com.projectnexus.iwani.ui.theme.*

class MainActivity : ComponentActivity() {
    private lateinit var sosManager: EmergencySosManager

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sosManager = EmergencySosManager(this)

        setContent {
            var isDarkTheme by remember { mutableStateOf(true) }
            var showIntro by remember { mutableStateOf(true) }

            // Theme smooth color transitions (interpolates every theme token seamlessly)
            val currentBg by animateColorAsState(
                targetValue = IWaniColors.background(isDarkTheme),
                animationSpec = tween(500, easing = FastOutSlowInEasing),
                label = "currentBg"
            )
            val currentSurface by animateColorAsState(
                targetValue = IWaniColors.surface(isDarkTheme),
                animationSpec = tween(500, easing = FastOutSlowInEasing),
                label = "currentSurface"
            )
            val currentBorder by animateColorAsState(
                targetValue = IWaniColors.border(isDarkTheme),
                animationSpec = tween(500, easing = FastOutSlowInEasing),
                label = "currentBorder"
            )
            val currentAccent by animateColorAsState(
                targetValue = IWaniColors.accent(isDarkTheme),
                animationSpec = tween(500, easing = FastOutSlowInEasing),
                label = "currentAccent"
            )
            val currentPrimary by animateColorAsState(
                targetValue = IWaniColors.primary(isDarkTheme),
                animationSpec = tween(500, easing = FastOutSlowInEasing),
                label = "currentPrimary"
            )
            val currentTextPrimary by animateColorAsState(
                targetValue = IWaniColors.textPrimary(isDarkTheme),
                animationSpec = tween(500, easing = FastOutSlowInEasing),
                label = "currentTextPrimary"
            )
            val currentTextSecondary by animateColorAsState(
                targetValue = IWaniColors.textSecondary(isDarkTheme),
                animationSpec = tween(500, easing = FastOutSlowInEasing),
                label = "currentTextSecondary"
            )

            // Theme icon rotation animation
            val themeIconRotation by animateFloatAsState(
                targetValue = if (isDarkTheme) 0f else 360f,
                animationSpec = tween(550, easing = FastOutSlowInEasing),
                label = "themeIconRotation"
            )

            // Dynamic Wave / Sweep Reveal Overlay during Theme Transition (inspired by s1mplee67.github.io)
            val themeWaveProgress = remember { Animatable(1f) }
            var previousThemeState by remember { mutableStateOf(isDarkTheme) }

            LaunchedEffect(isDarkTheme) {
                if (previousThemeState != isDarkTheme) {
                    previousThemeState = isDarkTheme
                    themeWaveProgress.snapTo(0f)
                    themeWaveProgress.animateTo(
                        targetValue = 1f,
                        animationSpec = tween(durationMillis = 650, easing = CubicBezierEasing(0.2f, 0.8f, 0.25f, 1f))
                    )
                }
            }

            IWaniTheme(isDarkTheme = isDarkTheme) {
                Crossfade(
                    targetState = showIntro,
                    animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing),
                    label = "IntroCrossfade"
                ) { isIntroActive ->
                    if (isIntroActive) {
                        SplashScreen(
                            onFinish = { showIntro = false }
                        )
                    } else {
                        // Main App Container with Scaffold & Theme Wave Reveal Overlay
                        Box(modifier = Modifier.fillMaxSize()) {
                            val navController = rememberNavController()
                            val context = this@MainActivity
                            val prefs = remember { context.getSharedPreferences("iwani_ai_prefs", android.content.Context.MODE_PRIVATE) }
                            val modelFile = remember { java.io.File(context.filesDir, "models/qwen2.5_0.5b_instruct.bin") }
                            val isModelInstalled = remember {
                                prefs.getBoolean("qwen_downloaded", false) && modelFile.exists() && modelFile.length() >= (350L * 1024L * 1024L * 0.98).toLong()
                            }
                            val startRoute = if (isModelInstalled) "home" else "model_manager"
                            var currentRoute by remember { mutableStateOf(startRoute) }
                            // Default to ENGLISH as requested by user
                            var appLanguage by remember { mutableStateOf(AppLanguage.ENGLISH) }

                            LaunchedEffect(appLanguage) {
                                sosManager.setLanguage(appLanguage.isTagalog)
                            }

                            val isTagalog = appLanguage.isTagalog

                            Scaffold(
                                containerColor = currentBg,
                                topBar = {
                                    TopAppBar(
                                        title = {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.clickable {
                                                    if (currentRoute != "home") {
                                                        currentRoute = "home"
                                                        navController.navigate("home") {
                                                            popUpTo("home") { inclusive = true }
                                                        }
                                                    }
                                                }
                                            ) {
                                                Image(
                                                    painter = painterResource(id = R.drawable.logo_iwani),
                                                    contentDescription = "iWani Logo",
                                                    modifier = Modifier.size(36.dp)
                                                )
                                                Spacer(Modifier.width(9.dp))
                                                Text(
                                                    text = "iWani",
                                                    fontWeight = FontWeight.Black,
                                                    fontSize = 20.sp,
                                                    color = currentAccent
                                                )
                                                Spacer(Modifier.width(5.dp))
                                                Text(
                                                    text = "Nexus",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (isDarkTheme) Color(0xFF93C5FD) else TextSecondary
                                                )
                                            }
                                        },
                                        actions = {
                                            // Theme Toggle: [ 🌙 Dark / ☀️ Light ] with rotation animation
                                            IconButton(
                                                onClick = { isDarkTheme = !isDarkTheme }
                                            ) {
                                                Icon(
                                                    imageVector = if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                                                    contentDescription = "Toggle Theme",
                                                    tint = currentAccent,
                                                    modifier = Modifier.rotate(themeIconRotation)
                                                )
                                            }

                                            // High-Visibility Language Switcher with USA (EN) & Philippine (TL) Flags
                                            Surface(
                                                shape = RoundedCornerShape(22.dp),
                                                color = currentSurface,
                                                modifier = Modifier
                                                    .padding(end = 4.dp)
                                                    .border(1.dp, currentBorder, RoundedCornerShape(22.dp))
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(horizontal = 3.dp, vertical = 3.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    // English (USA Flag)
                                                    val isEn = appLanguage == AppLanguage.ENGLISH
                                                    Surface(
                                                        shape = RoundedCornerShape(18.dp),
                                                        color = if (isEn) currentPrimary else Color.Transparent,
                                                        modifier = Modifier
                                                            .clip(RoundedCornerShape(18.dp))
                                                            .clickable { appLanguage = AppLanguage.ENGLISH }
                                                    ) {
                                                        Row(
                                                            verticalAlignment = Alignment.CenterVertically,
                                                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp)
                                                        ) {
                                                            Image(
                                                                painter = painterResource(id = R.drawable.flag_usa),
                                                                contentDescription = "USA Flag (English)",
                                                                modifier = Modifier
                                                                    .size(width = 20.dp, height = 13.dp)
                                                                    .clip(RoundedCornerShape(2.dp))
                                                            )
                                                            Spacer(Modifier.width(4.dp))
                                                            Text(
                                                                text = "EN",
                                                                fontSize = 11.sp,
                                                                fontWeight = FontWeight.ExtraBold,
                                                                color = if (isEn) Color.White else currentTextSecondary
                                                            )
                                                        }
                                                    }

                                                    Spacer(Modifier.width(2.dp))

                                                    // Tagalog (Philippine Flag)
                                                    val isTl = appLanguage == AppLanguage.TAGALOG
                                                    Surface(
                                                        shape = RoundedCornerShape(18.dp),
                                                        color = if (isTl) currentPrimary else Color.Transparent,
                                                        modifier = Modifier
                                                            .clip(RoundedCornerShape(18.dp))
                                                            .clickable { appLanguage = AppLanguage.TAGALOG }
                                                    ) {
                                                        Row(
                                                            verticalAlignment = Alignment.CenterVertically,
                                                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp)
                                                        ) {
                                                            Image(
                                                                painter = painterResource(id = R.drawable.flag_philippines),
                                                                contentDescription = "Philippine Flag (Tagalog)",
                                                                modifier = Modifier
                                                                    .size(width = 20.dp, height = 13.dp)
                                                                    .clip(RoundedCornerShape(2.dp))
                                                            )
                                                            Spacer(Modifier.width(4.dp))
                                                            Text(
                                                                text = "TL",
                                                                fontSize = 11.sp,
                                                                fontWeight = FontWeight.ExtraBold,
                                                                color = if (isTl) Color.White else currentTextSecondary
                                                            )
                                                        }
                                                    }
                                                }
                                            }

                                            // Model AI Lokal Manager Button
                                            IconButton(
                                                onClick = {
                                                    currentRoute = "model_manager"
                                                    navController.navigate("model_manager")
                                                }
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.CloudDownload,
                                                    contentDescription = "Model AI Lokal",
                                                    tint = currentAccent
                                                )
                                            }

                                            // About Us Info Button
                                            IconButton(
                                                onClick = {
                                                    currentRoute = "about"
                                                    navController.navigate("about")
                                                }
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Info,
                                                    contentDescription = "About Project Nexus",
                                                    tint = currentAccent
                                                )
                                            }
                                        },
                                        colors = TopAppBarDefaults.topAppBarColors(
                                            containerColor = currentBg
                                        )
                                    )
                                },
                                bottomBar = {
                                    NavigationBar(
                                        containerColor = currentBg,
                                        tonalElevation = 0.dp
                                    ) {
                                        val navColors = NavigationBarItemDefaults.colors(
                                            selectedIconColor = currentAccent,
                                            selectedTextColor = currentAccent,
                                            indicatorColor = currentPrimary.copy(alpha = 0.22f),
                                            unselectedIconColor = currentTextSecondary,
                                            unselectedTextColor = currentTextSecondary
                                        )

                                        NavigationBarItem(
                                            icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                                            label = { Text("Home") },
                                            selected = currentRoute == "home",
                                            colors = navColors,
                                            onClick = {
                                                currentRoute = "home"
                                                navController.navigate("home")
                                            }
                                        )
                                        NavigationBarItem(
                                            icon = { Icon(Icons.Default.Videocam, contentDescription = "Camera") },
                                            label = { Text(if (isTagalog) "Kamera" else "Camera") },
                                            selected = currentRoute == "camera",
                                            colors = navColors,
                                            onClick = {
                                                currentRoute = "camera"
                                                navController.navigate("camera")
                                            }
                                        )
                                        NavigationBarItem(
                                            icon = { Icon(Icons.Default.Campaign, contentDescription = "SOS") },
                                            label = { Text("SOS") },
                                            selected = currentRoute == "sos",
                                            colors = navColors,
                                            onClick = {
                                                currentRoute = "sos"
                                                navController.navigate("sos")
                                            }
                                        )
                                        NavigationBarItem(
                                            icon = { Icon(Icons.Default.Forum, contentDescription = "Translate") },
                                            label = { Text(if (isTagalog) "Salin" else "Translate") },
                                            selected = currentRoute == "translate",
                                            colors = navColors,
                                            onClick = {
                                                currentRoute = "translate"
                                                navController.navigate("translate")
                                            }
                                        )
                                    }
                                }
                            ) { innerPadding ->
                                NavHost(
                                    navController = navController,
                                    startDestination = startRoute,
                                    modifier = Modifier.padding(innerPadding)
                                ) {
                                    composable("home") {
                                        HomeScreen(
                                            appLanguage = appLanguage,
                                            isDarkTheme = isDarkTheme,
                                            onNavigateToCamera = {
                                                currentRoute = "camera"
                                                navController.navigate("camera")
                                            },
                                            onNavigateToEmergency = {
                                                currentRoute = "sos"
                                                navController.navigate("sos")
                                            },
                                            onNavigateToTranslate = {
                                                currentRoute = "translate"
                                                navController.navigate("translate")
                                            },
                                            onNavigateToModelManager = {
                                                currentRoute = "model_manager"
                                                navController.navigate("model_manager")
                                            },
                                            onNavigateToAbout = {
                                                currentRoute = "about"
                                                navController.navigate("about")
                                            }
                                        )
                                    }
                                    composable("camera") {
                                        FslCameraScreen(
                                            sosManager = sosManager,
                                            appLanguage = appLanguage,
                                            isDarkTheme = isDarkTheme
                                        )
                                    }
                                    composable("sos") {
                                        EmergencySosScreen(
                                            sosManager = sosManager,
                                            appLanguage = appLanguage,
                                            isDarkTheme = isDarkTheme
                                        )
                                    }
                                    composable("translate") {
                                        CommunicationScreen(
                                            sosManager = sosManager,
                                            appLanguage = appLanguage,
                                            isDarkTheme = isDarkTheme
                                        )
                                    }
                                    composable("model_manager") {
                                        AiModelManagerScreen(
                                            appLanguage = appLanguage,
                                            isDarkTheme = isDarkTheme,
                                            onNavigateBack = {
                                                currentRoute = "home"
                                                if (!navController.popBackStack()) {
                                                    navController.navigate("home")
                                                }
                                            },
                                            onNavigateToTranslate = {
                                                currentRoute = "translate"
                                                navController.navigate("translate")
                                            },
                                            onContinueToHome = {
                                                currentRoute = "home"
                                                navController.navigate("home") {
                                                    popUpTo("model_manager") { inclusive = true }
                                                }
                                            }
                                        )
                                    }
                                    composable("about") {
                                        AboutScreen(
                                            appLanguage = appLanguage,
                                            isDarkTheme = isDarkTheme,
                                            onNavigateBack = {
                                                currentRoute = "home"
                                                navController.popBackStack()
                                            }
                                        )
                                    }
                                }
                            }

                            // Dynamic Wave / Sweep Reveal Overlay during Theme Transition (inspired by s1mplee67.github.io)
                            if (themeWaveProgress.value < 1f) {
                                val p = themeWaveProgress.value
                                val waveColor = if (isDarkTheme) DarkBackground else Color(0xFFF8FAFC)
                                val crestColor = if (isDarkTheme) IcctGold else TealPrimary
                                val density = LocalDensity.current

                                Canvas(
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    val origin = Offset(size.width - 130f * density.density, 42f * density.density)
                                    val maxR = kotlin.math.hypot(size.width.toDouble(), size.height.toDouble()).toFloat() * 1.05f
                                    val r = p * maxR

                                    drawCircle(
                                        color = waveColor,
                                        radius = r,
                                        center = origin
                                    )
                                    if (p in 0.02f..0.98f) {
                                        drawCircle(
                                            color = crestColor.copy(alpha = (1f - p) * 0.8f),
                                            radius = r,
                                            center = origin,
                                            style = Stroke(width = 8f)
                                        )
                                        drawCircle(
                                            color = crestColor.copy(alpha = (1f - p) * 0.35f),
                                            radius = r + 4f,
                                            center = origin,
                                            style = Stroke(width = 16f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        sosManager.shutdown()
    }
}
