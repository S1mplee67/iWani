package com.projectnexus.iwani

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import androidx.compose.ui.graphics.Color
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

            IWaniTheme(isDarkTheme = isDarkTheme) {
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
                val currentBg = IWaniColors.background(isDarkTheme)
                val currentAccent = IWaniColors.accent(isDarkTheme)

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
                                        modifier = Modifier
                                            .size(34.dp)
                                            .clip(CircleShape)
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
                                // Theme Toggle: [ 🌙 Dark / ☀️ Light ]
                                IconButton(
                                    onClick = { isDarkTheme = !isDarkTheme }
                                ) {
                                    Icon(
                                        imageVector = if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                                        contentDescription = "Toggle Theme",
                                        tint = currentAccent
                                    )
                                }

                                // Language Toggle Pill: [ EN | TL ]
                                Surface(
                                    shape = RoundedCornerShape(20.dp),
                                    color = IWaniColors.surface(isDarkTheme),
                                    modifier = Modifier
                                        .padding(end = 4.dp)
                                        .border(1.dp, IWaniColors.border(isDarkTheme), RoundedCornerShape(20.dp))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Surface(
                                            shape = RoundedCornerShape(16.dp),
                                            color = if (appLanguage == AppLanguage.ENGLISH) IWaniColors.primary(isDarkTheme) else Color.Transparent,
                                            modifier = Modifier.clickable { appLanguage = AppLanguage.ENGLISH }
                                        ) {
                                            Text(
                                                text = "EN",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (appLanguage == AppLanguage.ENGLISH) Color.White else IWaniColors.textSecondary(isDarkTheme),
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                            )
                                        }

                                        Surface(
                                            shape = RoundedCornerShape(16.dp),
                                            color = if (appLanguage == AppLanguage.TAGALOG) IWaniColors.primary(isDarkTheme) else Color.Transparent,
                                            modifier = Modifier.clickable { appLanguage = AppLanguage.TAGALOG }
                                        ) {
                                            Text(
                                                text = "TL",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (appLanguage == AppLanguage.TAGALOG) Color.White else IWaniColors.textSecondary(isDarkTheme),
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                            )
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
                                indicatorColor = IWaniColors.primary(isDarkTheme).copy(alpha = 0.22f),
                                unselectedIconColor = IWaniColors.textSecondary(isDarkTheme),
                                unselectedTextColor = IWaniColors.textSecondary(isDarkTheme)
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
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        sosManager.shutdown()
    }
}
