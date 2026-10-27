package com.projectnexus.iwani.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.projectnexus.iwani.AppLanguage
import com.projectnexus.iwani.R
import com.projectnexus.iwani.ui.components.*
import com.projectnexus.iwani.ui.theme.*
import kotlinx.coroutines.delay
import java.util.Calendar

data class HeroSlide(
    val drawableRes: Int,
    val titleEn: String,
    val titleTl: String,
    val descEn: String,
    val descTl: String
)

@Composable
fun HomeScreen(
    appLanguage: AppLanguage = AppLanguage.ENGLISH,
    isDarkTheme: Boolean = true,
    onNavigateToCamera: () -> Unit,
    onNavigateToEmergency: () -> Unit,
    onNavigateToTranslate: () -> Unit,
    onNavigateToModelManager: () -> Unit = {},
    onNavigateToAbout: () -> Unit
) {
    val isTagalog = appLanguage.isTagalog
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    val greeting = if (isTagalog) {
        when {
            hour < 12 -> "Magandang Umaga"
            hour < 18 -> "Magandang Hapon"
            else -> "Magandang Gabi"
        }
    } else {
        when {
            hour < 12 -> "Good Morning"
            hour < 18 -> "Good Afternoon"
            else -> "Good Evening"
        }
    }

    val bg = IWaniColors.background(isDarkTheme)
    val surface = IWaniColors.surface(isDarkTheme)
    val border = IWaniColors.border(isDarkTheme)
    val textPrimary = IWaniColors.textPrimary(isDarkTheme)
    val textSecondary = IWaniColors.textSecondary(isDarkTheme)
    val accent = IWaniColors.accent(isDarkTheme)
    val primary = IWaniColors.primary(isDarkTheme)

    val slides = remember {
        listOf(
            HeroSlide(
                drawableRes = R.drawable.hero_bayanihan,
                titleEn = "Bayanihan in Communication",
                titleTl = "Bayanihan sa Komunikasyon",
                descEn = "Bridging Deaf & Hearing communities through accessible AI vision.",
                descTl = "Pagbubuklod ng Bingi at Nakakarinig sa pamamagitan ng AI vision."
            ),
            HeroSlide(
                drawableRes = R.drawable.slide_classroom,
                titleEn = "Inclusive Classroom Education",
                titleTl = "Edukasyong Bukas sa Lahat",
                descEn = "Empowering Deaf college students in active academic discussions.",
                descTl = "Pagpapalakas sa mga mag-aaral na bingi sa talakayan sa silid-aralan."
            ),
            HeroSlide(
                drawableRes = R.drawable.slide_hospital,
                titleEn = "Empathetic Healthcare Access",
                titleTl = "Malasakit sa Kalusugan",
                descEn = "Facilitating clear medical communication in clinics & hospitals.",
                descTl = "Malinaw at may malasakit na komunikasyon sa mga klinika at ospital."
            ),
            HeroSlide(
                drawableRes = R.drawable.slide_emergency,
                titleEn = "Barangay & Public Service",
                titleTl = "Serbisyo sa Pamayanan",
                descEn = "Assisting barangay officers and citizens during urgent inquiries.",
                descTl = "Pagtulong sa mga opisyal ng barangay at mamamayan sa oras ng saklolo."
            ),
            HeroSlide(
                drawableRes = R.drawable.slide_community,
                titleEn = "Daily Social Connections",
                titleTl = "Pakikipagkapwa-Tao",
                descEn = "Conversing naturally with friends and family anywhere in the Philippines.",
                descTl = "Likas na pakikipag-usap sa mga kaibigan at pamilya saanman sa Pilipinas."
            ),
            HeroSlide(
                drawableRes = R.drawable.slide_empower,
                titleEn = "PWD Empowerment & AI",
                titleTl = "Makabagong Teknolohiya para sa PWD",
                descEn = "Interactive self-learning for mastering 26 FSL alphabet gestures.",
                descTl = "Mabilis at sariling pagsasanay sa 26 na titik ng alpabetong FSL."
            )
        )
    }

    var currentSlideIndex by remember { mutableIntStateOf(0) }
    var isUserInteracting by remember { mutableStateOf(false) }

    LaunchedEffect(isUserInteracting) {
        if (!isUserInteracting) {
            while (true) {
                delay(4200)
                currentSlideIndex = (currentSlideIndex + 1) % slides.size
            }
        }
    }

    var showChartDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bg)
            .padding(horizontal = 20.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(modifier = Modifier.height(6.dp))

        // ================= 1. MINIMALIST EDITORIAL HEADER =================
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = greeting,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Black,
                    color = textPrimary
                )
                Text(
                    text = if (isTagalog)
                        "Filipino Sign Language • Project Nexus"
                    else
                        "Filipino Sign Language • Project Nexus",
                    fontSize = 12.sp,
                    color = textSecondary
                )
            }

            MetalBadge(
                text = "PROJECT NEXUS",
                isDark = isDarkTheme,
                modifier = Modifier.interactiveBounceClick { onNavigateToAbout() }
            )
        }

        // ================= 2. SLEEK EDGE-TO-EDGE HERO CAROUSEL =================
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(188.dp)
                .clip(RoundedCornerShape(24.dp))
                .border(1.dp, border, RoundedCornerShape(24.dp))
                .interactiveBounceClick {
                    isUserInteracting = true
                    currentSlideIndex = (currentSlideIndex + 1) % slides.size
                }
        ) {
            AnimatedContent(
                targetState = currentSlideIndex,
                transitionSpec = {
                    fadeIn(animationSpec = tween(480)) togetherWith fadeOut(animationSpec = tween(480))
                },
                label = "SlideTransition"
            ) { targetIndex ->
                val slide = slides[targetIndex]
                Image(
                    painter = painterResource(id = slide.drawableRes),
                    contentDescription = slide.titleEn,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            // Smooth dark gradient scrim for readable overlay text
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.25f),
                                Color.Black.copy(alpha = 0.82f)
                            )
                        )
                    )
            )

            // Bottom Overlay Caption + Minimalist Indicator Dots
            val currentSlide = slides[currentSlideIndex]
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isTagalog) currentSlide.titleTl else currentSlide.titleEn,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color(0xFFFDE68A)
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = if (isTagalog) currentSlide.descTl else currentSlide.descEn,
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.88f),
                            lineHeight = 15.sp,
                            maxLines = 2
                        )
                    }

                    Spacer(Modifier.width(10.dp))

                    // Minimalist Pill Dots
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        slides.indices.forEach { index ->
                            val isSelected = index == currentSlideIndex
                            Box(
                                modifier = Modifier
                                    .size(
                                        width = if (isSelected) 16.dp else 5.dp,
                                        height = 5.dp
                                    )
                                    .background(
                                        color = if (isSelected) IcctGold else Color.White.copy(alpha = 0.4f),
                                        shape = RoundedCornerShape(3.dp)
                                    )
                            )
                        }
                    }
                }
            }
        }

        // ================= 3. MINIMALIST 2x2 BENTO GRID (UNCROWDED & INTERACTIVE) =================
        Text(
            text = if (isTagalog) "MGA PANGUNAHING FEATURE" else "CORE WORKSPACE",
            fontSize = 11.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 1.5.sp,
            color = textSecondary
        )

        // Row 1: FSL Camera (with BorderBeam highlight) + AI Translate
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Tile 1: FSL Camera Recognition (Wrapped in Libraries.dev BorderBeamBox)
            BorderBeamBox(
                active = true,
                cornerRadius = 22.dp,
                beamColor = primary,
                secondaryColor = accent,
                baseBorderColor = border,
                backgroundColor = surface,
                modifier = Modifier
                    .weight(1f)
                    .height(152.dp)
                    .interactiveBounceClick { onNavigateToCamera() }
            ) {
                BentoTileContent(
                    icon = Icons.Default.Videocam,
                    badge = "97.4% AI",
                    title = if (isTagalog) "FSL Kamera" else "FSL Camera",
                    subtitle = if (isTagalog) "Live 26-titik 3D hand tracking" else "Live 26-letter 3D hand vision",
                    accentColor = primary,
                    textPrimary = textPrimary,
                    textSecondary = textSecondary
                )
            }

            // Tile 2: Communication & Translation
            Surface(
                shape = RoundedCornerShape(22.dp),
                color = surface,
                modifier = Modifier
                    .weight(1f)
                    .height(152.dp)
                    .border(1.dp, border, RoundedCornerShape(22.dp))
                    .interactiveBounceClick { onNavigateToTranslate() }
            ) {
                BentoTileContent(
                    icon = Icons.Default.Forum,
                    badge = "QWEN 2.5",
                    title = if (isTagalog) "Pagsasalin" else "Translate",
                    subtitle = if (isTagalog) "Senyas patungong buong boses" else "Sign glosses to spoken voice",
                    accentColor = accent,
                    textPrimary = textPrimary,
                    textSecondary = textSecondary
                )
            }
        }

        // Row 2: Emergency SOS + 26-Letter FSL Reference Chart
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Tile 3: Emergency SOS
            Surface(
                shape = RoundedCornerShape(22.dp),
                color = if (isDarkTheme) EmergencyRedDark.copy(alpha = 0.28f) else Color(0xFFFEE2E2),
                modifier = Modifier
                    .weight(1f)
                    .height(146.dp)
                    .border(
                        1.dp,
                        if (isDarkTheme) EmergencyRed.copy(alpha = 0.45f) else Color(0xFFFCA5A5),
                        RoundedCornerShape(22.dp)
                    )
                    .interactiveBounceClick { onNavigateToEmergency() }
            ) {
                BentoTileContent(
                    icon = Icons.Default.Campaign,
                    badge = "1-TAP SOS",
                    title = if (isTagalog) "Saklolo SOS" else "Emergency SOS",
                    subtitle = if (isTagalog) "Agarang malakas na boses" else "Instant loud voice broadcast",
                    accentColor = EmergencyRed,
                    textPrimary = if (isDarkTheme) Color(0xFFFCA5A5) else Color(0xFF991B1B),
                    textSecondary = textSecondary
                )
            }

            // Tile 4: 26-Letter FSL Chart Modal
            Surface(
                shape = RoundedCornerShape(22.dp),
                color = surface,
                modifier = Modifier
                    .weight(1f)
                    .height(146.dp)
                    .border(1.dp, border, RoundedCornerShape(22.dp))
                    .interactiveBounceClick { showChartDialog = true }
            ) {
                BentoTileContent(
                    icon = Icons.AutoMirrored.Filled.MenuBook,
                    badge = "A – Z GUIDE",
                    title = if (isTagalog) "FSL Tsart" else "FSL Alphabet",
                    subtitle = if (isTagalog) "Gabay sa 26 posisyon ng kamay" else "Visual guide for 26 hand signs",
                    accentColor = SuccessGreen,
                    textPrimary = textPrimary,
                    textSecondary = textSecondary
                )
            }
        }

        // ================= 4. MINIMALIST AI ENGINE STATUS PILL (WITH THINKING ORB) =================
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = surface,
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, border, RoundedCornerShape(20.dp))
                .interactiveBounceClick { onNavigateToModelManager() }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ThinkingOrb(
                    size = 32.dp,
                    state = "breathing",
                    primaryColor = primary,
                    accentColor = accent
                )
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (isTagalog) "On-Device AI Model (Qwen 2.5)" else "On-Device AI Engine (Qwen 2.5)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = textPrimary
                    )
                    Text(
                        text = if (isTagalog)
                            "I-extract at pamahalaan ang 350 MB offline neural model"
                        else
                            "Extract & manage 350 MB offline neural weights",
                        fontSize = 11.sp,
                        color = textSecondary
                    )
                }
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = primary.copy(alpha = 0.16f)
                ) {
                    Text(
                        text = if (isTagalog) "Buksan ›" else "Manage ›",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = primary,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }

    // Full FSL Alphabet Chart Dialog
    if (showChartDialog) {
        Dialog(
            onDismissRequest = { showChartDialog = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                shape = RoundedCornerShape(24.dp),
                color = bg,
                border = BorderStroke(1.dp, border)
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
                            text = "FSL Alphabet Chart (A–Z)",
                            color = accent,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        IconButton(onClick = { showChartDialog = false }) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = textPrimary)
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
                            contentDescription = "FSL Full Chart",
                            modifier = Modifier.fillMaxWidth(),
                            contentScale = ContentScale.Fit
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BentoTileContent(
    icon: ImageVector,
    badge: String,
    title: String,
    subtitle: String,
    accentColor: Color,
    textPrimary: Color,
    textSecondary: Color
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(accentColor.copy(alpha = 0.18f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = accentColor,
                    modifier = Modifier.size(22.dp)
                )
            }

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = accentColor.copy(alpha = 0.14f)
            ) {
                Text(
                    text = badge,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.6.sp,
                    color = accentColor,
                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                )
            }
        }

        Column {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = textPrimary
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = textSecondary,
                lineHeight = 14.sp,
                maxLines = 2
            )
        }
    }
}
