package com.projectnexus.iwani

import android.content.Context
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
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
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
import com.projectnexus.iwani.ui.components.ChibiMascotTutorialDialog
import com.projectnexus.iwani.ui.components.TeamFeedbackInboxDialog
import com.projectnexus.iwani.ui.components.interactiveBounceClick
import com.projectnexus.iwani.ui.components.tutorialSpotlightTarget
import com.projectnexus.iwani.ui.screens.*
import com.projectnexus.iwani.ui.sound.UiSoundManager
import com.projectnexus.iwani.ui.theme.*
import java.io.File

class MainActivity : ComponentActivity() {
    private lateinit var sosManager: EmergencySosManager

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sosManager = EmergencySosManager(this)
        UiSoundManager.loadPrefs(this)

        val uiPrefs = getSharedPreferences("iwani_ui_settings", Context.MODE_PRIVATE)
        val initialFontName = uiPrefs.getString("font_style", AppFontStyle.GEOMETRIC.name) ?: AppFontStyle.GEOMETRIC.name
        val initialFontStyle = try {
            AppFontStyle.valueOf(initialFontName)
        } catch (_: Exception) {
            AppFontStyle.GEOMETRIC
        }
        val initialFontScale = uiPrefs.getFloat("font_scale", 1.0f)

        setContent {
            var isDarkTheme by remember { mutableStateOf(true) }
            var showIntro by remember { mutableStateOf(true) }
            var fontStyle by remember { mutableStateOf(initialFontStyle) }
            var fontScale by remember { mutableFloatStateOf(initialFontScale) }

            // Smooth color interpolation across all theme tokens
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
            val currentTextSecondary by animateColorAsState(
                targetValue = IWaniColors.textSecondary(isDarkTheme),
                animationSpec = tween(500, easing = FastOutSlowInEasing),
                label = "currentTextSecondary"
            )

            // Theme icon 360° rotation
            val themeIconRotation by animateFloatAsState(
                targetValue = if (isDarkTheme) 0f else 360f,
                animationSpec = tween(550, easing = FastOutSlowInEasing),
                label = "themeIconRotation"
            )

            // Portfolio Circular Wave Reveal Overlay during Theme Transition
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

            IWaniTheme(
                isDarkTheme = isDarkTheme,
                fontStyle = fontStyle,
                fontScale = fontScale
            ) {
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
                        Box(modifier = Modifier.fillMaxSize()) {
                            val navController = rememberNavController()
                            val context = this@MainActivity
                            val prefs = remember { context.getSharedPreferences("iwani_ai_prefs", Context.MODE_PRIVATE) }
                            val modelFile = remember { File(context.filesDir, "models/qwen2.5_0.5b_instruct.bin") }
                            val isModelInstalled = remember {
                                prefs.getBoolean("qwen_downloaded", false) &&
                                    modelFile.exists() &&
                                    modelFile.length() >= (350L * 1024L * 1024L * 0.98).toLong()
                            }
                            val startRoute = if (isModelInstalled) "home" else "model_manager"
                            var currentRoute by remember { mutableStateOf(startRoute) }
                            var appLanguage by remember { mutableStateOf(AppLanguage.ENGLISH) }
                            var showMascotTutorial by remember {
                                mutableStateOf(!uiPrefs.getBoolean("mascot_tutorial_v5_seen", false))
                            }
                            var blurAppBackground by remember { mutableStateOf(false) }
                            var showTeamInbox by remember { mutableStateOf(false) }

                            LaunchedEffect(appLanguage) {
                                sosManager.setLanguage(appLanguage.isTagalog)
                            }

                            val isTagalog = appLanguage.isTagalog

                            Scaffold(
                                modifier = Modifier.blur(if (showMascotTutorial && blurAppBackground) 8.dp else 0.dp),
                                containerColor = currentBg,
                                topBar = {
                                    // Uncrowded Minimalist TopAppBar — prevents any text wrapping
                                    TopAppBar(
                                        modifier = Modifier.tutorialSpotlightTarget("top_bar_main"),
                                        title = {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.interactiveBounceClick {
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
                                                Spacer(Modifier.width(10.dp))
                                                Column {
                                                    Text(
                                                        text = "iWani",
                                                        fontWeight = FontWeight.Black,
                                                        fontSize = 19.sp,
                                                        color = currentAccent,
                                                        maxLines = 1,
                                                        softWrap = false
                                                    )
                                                    Text(
                                                        text = "PROJECT NEXUS",
                                                        fontSize = 9.sp,
                                                        fontWeight = FontWeight.ExtraBold,
                                                        letterSpacing = 1.4.sp,
                                                        color = if (isDarkTheme) Color(0xFF93C5FD) else currentTextSecondary,
                                                        maxLines = 1,
                                                        softWrap = false
                                                    )
                                                }
                                            }
                                        },
                                        actions = {
                                            // Compact Flag Language Pill: [ 🇺🇸 EN | 🇵🇭 TL ]
                                            Surface(
                                                shape = RoundedCornerShape(22.dp),
                                                color = currentSurface,
                                                modifier = Modifier
                                                    .padding(end = 6.dp)
                                                    .border(1.dp, currentBorder, RoundedCornerShape(22.dp))
                                                    .tutorialSpotlightTarget("top_bar_flags")
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(horizontal = 3.dp, vertical = 3.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    val isEn = appLanguage == AppLanguage.ENGLISH
                                                    Surface(
                                                        shape = RoundedCornerShape(18.dp),
                                                        color = if (isEn) currentPrimary else Color.Transparent,
                                                        modifier = Modifier
                                                            .clip(RoundedCornerShape(18.dp))
                                                            .interactiveBounceClick(isToggle = true) {
                                                                appLanguage = AppLanguage.ENGLISH
                                                            }
                                                    ) {
                                                        Row(
                                                            verticalAlignment = Alignment.CenterVertically,
                                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                                                        ) {
                                                            Image(
                                                                painter = painterResource(id = R.drawable.flag_usa),
                                                                contentDescription = "USA Flag",
                                                                modifier = Modifier
                                                                    .size(width = 18.dp, height = 12.dp)
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

                                                    val isTl = appLanguage == AppLanguage.TAGALOG
                                                    Surface(
                                                        shape = RoundedCornerShape(18.dp),
                                                        color = if (isTl) currentPrimary else Color.Transparent,
                                                        modifier = Modifier
                                                            .clip(RoundedCornerShape(18.dp))
                                                            .interactiveBounceClick(isToggle = true) {
                                                                appLanguage = AppLanguage.TAGALOG
                                                            }
                                                    ) {
                                                        Row(
                                                            verticalAlignment = Alignment.CenterVertically,
                                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                                                        ) {
                                                            Image(
                                                                painter = painterResource(id = R.drawable.flag_philippines),
                                                                contentDescription = "Philippine Flag",
                                                                modifier = Modifier
                                                                    .size(width = 18.dp, height = 12.dp)
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

                                            // Theme Toggle Button with 360° Spin & Sound
                                            IconButton(
                                                modifier = Modifier.tutorialSpotlightTarget("top_bar_theme"),
                                                onClick = {
                                                    UiSoundManager.playToggle(context, !isDarkTheme)
                                                    isDarkTheme = !isDarkTheme
                                                }
                                            ) {
                                                Icon(
                                                    imageVector = if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                                                    contentDescription = "Toggle Theme",
                                                    tint = currentAccent,
                                                    modifier = Modifier.rotate(themeIconRotation)
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
                                            label = { Text("Home", fontSize = 11.sp) },
                                            selected = currentRoute == "home",
                                            colors = navColors,
                                            onClick = {
                                                UiSoundManager.playNav(context)
                                                currentRoute = "home"
                                                navController.navigate("home")
                                            }
                                        )
                                        NavigationBarItem(
                                            icon = { Icon(Icons.Default.Videocam, contentDescription = "Camera") },
                                            label = { Text(if (isTagalog) "Kamera" else "Camera", fontSize = 11.sp) },
                                            selected = currentRoute == "camera",
                                            colors = navColors,
                                            onClick = {
                                                UiSoundManager.playNav(context)
                                                currentRoute = "camera"
                                                navController.navigate("camera")
                                            }
                                        )
                                        NavigationBarItem(
                                            icon = { Icon(Icons.Default.Campaign, contentDescription = "SOS") },
                                            label = { Text("SOS", fontSize = 11.sp) },
                                            selected = currentRoute == "sos",
                                            colors = navColors,
                                            onClick = {
                                                UiSoundManager.playNav(context)
                                                currentRoute = "sos"
                                                navController.navigate("sos")
                                            }
                                        )
                                        NavigationBarItem(
                                            icon = { Icon(Icons.Default.Forum, contentDescription = "Translate") },
                                            label = { Text(if (isTagalog) "Salin" else "Translate", fontSize = 11.sp) },
                                            selected = currentRoute == "translate",
                                            colors = navColors,
                                            onClick = {
                                                UiSoundManager.playNav(context)
                                                currentRoute = "translate"
                                                navController.navigate("translate")
                                            }
                                        )
                                        NavigationBarItem(
                                            icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                                            label = { Text(if (isTagalog) "Setting" else "Settings", fontSize = 11.sp) },
                                            selected = currentRoute == "settings",
                                            colors = navColors,
                                            onClick = {
                                                UiSoundManager.playNav(context)
                                                currentRoute = "settings"
                                                navController.navigate("settings")
                                            }
                                        )
                                    }
                                }
                            ) { innerPadding ->
                                NavHost(
                                    navController = navController,
                                    startDestination = startRoute,
                                    enterTransition = {
                                        if (showMascotTutorial) {
                                            fadeIn(animationSpec = tween(180))
                                        } else {
                                            fadeIn(animationSpec = tween(260)) + scaleIn(initialScale = 0.96f, animationSpec = tween(260))
                                        }
                                    },
                                    exitTransition = {
                                        fadeOut(animationSpec = tween(180))
                                    },
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
                                            },
                                            onOpenMascotTutorial = {
                                                showMascotTutorial = true
                                            },
                                            onOpenTeamInbox = {
                                                showTeamInbox = true
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
                                    composable("settings") {
                                        SettingsScreen(
                                            appLanguage = appLanguage,
                                            onLanguageChange = { appLanguage = it },
                                            isDarkTheme = isDarkTheme,
                                            onThemeChange = { isDarkTheme = it },
                                            fontStyle = fontStyle,
                                            onFontStyleChange = {
                                                fontStyle = it
                                                uiPrefs.edit().putString("font_style", it.name).apply()
                                            },
                                            fontScale = fontScale,
                                            onFontScaleChange = {
                                                fontScale = it
                                                uiPrefs.edit().putFloat("font_scale", it).apply()
                                            },
                                            onNavigateToModelManager = {
                                                currentRoute = "model_manager"
                                                navController.navigate("model_manager")
                                            },
                                            onNavigateToAbout = {
                                                currentRoute = "about"
                                                navController.navigate("about")
                                            },
                                            onReplayIntro = {
                                                showIntro = true
                                            },
                                            onOpenMascotTutorial = {
                                                showMascotTutorial = true
                                            },
                                            onOpenTeamInbox = {
                                                showTeamInbox = true
                                            }
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

                            // Portfolio Wave Reveal Overlay during Theme Transition
                            if (themeWaveProgress.value < 1f) {
                                val p = themeWaveProgress.value
                                val waveColor = if (isDarkTheme) DarkBackground else Color(0xFFF8FAFC)
                                val crestColor = if (isDarkTheme) IcctGold else TealPrimary
                                val density = LocalDensity.current

                                Canvas(
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    val origin = Offset(size.width - 36f * density.density, 42f * density.density)
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

                            // Chibi Mascots Interactive Walkthrough (Grace, Kian & Special Guest Shouko)
                            if (showMascotTutorial && currentRoute != "model_manager") {
                                ChibiMascotTutorialDialog(
                                    appLanguage = appLanguage,
                                    isDarkTheme = isDarkTheme,
                                    onToggleTheme = {
                                        UiSoundManager.playToggle(context, !isDarkTheme)
                                        isDarkTheme = !isDarkTheme
                                    },
                                    onDismiss = {
                                        uiPrefs.edit().putBoolean("mascot_tutorial_v5_seen", true).apply()
                                        blurAppBackground = false
                                        showMascotTutorial = false
                                    },
                                    onNavigateToRoute = { targetRoute ->
                                        if (currentRoute != targetRoute) {
                                            currentRoute = targetRoute
                                            navController.navigate(targetRoute) {
                                                launchSingleTop = true
                                            }
                                        }
                                    },
                                    onOpenFeedbackInbox = {
                                        uiPrefs.edit().putBoolean("mascot_tutorial_v5_seen", true).apply()
                                        blurAppBackground = false
                                        showMascotTutorial = false
                                        showTeamInbox = true
                                    },
                                    onBlurBackgroundChange = { shouldBlur ->
                                        blurAppBackground = shouldBlur
                                    }
                                )
                            }

                            // Team Feedback Inbox Dialog
                            if (showTeamInbox) {
                                TeamFeedbackInboxDialog(
                                    appLanguage = appLanguage,
                                    onDismiss = {
                                        showTeamInbox = false
                                    }
                                )
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
