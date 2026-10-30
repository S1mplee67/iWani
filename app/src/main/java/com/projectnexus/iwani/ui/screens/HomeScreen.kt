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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    onNavigateToAbout: () -> Unit,
    onOpenMascotTutorial: () -> Unit = {},
    onOpenTeamInbox: () -> Unit = {}
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bg)
    ) {
        // Soft Non-Repetitive Ambient Aurora Glow in the background
        if (isDarkTheme) {
            AmbientAuroraGlow(
                primaryOrbColor = Color(0xFF1D4ED8),
                secondaryOrbColor = Color(0xFFF59E0B),
                tertiaryOrbColor = Color(0xFF0D9488),
                alphaMultiplier = 0.65f
            )
        }

        val scrollState = rememberScrollState()
        LaunchedEffect(TutorialSpotlightRegistry.activeTargetId) {
            val activeId = TutorialSpotlightRegistry.activeTargetId
            if (activeId != null && (activeId.startsWith("home_") || activeId.startsWith("top_bar_"))) {
                scrollState.animateScrollTo(0)
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Spacer(modifier = Modifier.height(6.dp))

            // ================= 1. MINIMALIST EDITORIAL HEADER =================
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 10.dp)
                ) {
                    Text(
                        text = greeting,
                        fontSize = 25.sp,
                        fontWeight = FontWeight.Black,
                        color = textPrimary,
                        maxLines = 1,
                        softWrap = false,
                        modifier = Modifier.autoSlideIfOverflow()
                    )
                    Text(
                        text = if (isTagalog)
                            "Filipino Sign Language • Project Nexus"
                        else
                            "Filipino Sign Language • Project Nexus",
                        fontSize = 12.sp,
                        color = textSecondary,
                        maxLines = 1,
                        softWrap = false,
                        modifier = Modifier.autoSlideIfOverflow()
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
                    .height(184.dp)
                    .tutorialSpotlightTarget("home_hero_carousel")
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
                                color = Color(0xFFFDE68A),
                                maxLines = 1,
                                modifier = Modifier.autoSlideIfOverflow()
                            )
                            Spacer(Modifier.height(2.dp))
                            Text(
                                text = if (isTagalog) currentSlide.descTl else currentSlide.descEn,
                                fontSize = 11.sp,
                                color = Color.White.copy(alpha = 0.88f),
                                lineHeight = 15.sp,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Spacer(Modifier.width(10.dp))

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

            // ================= 3. CHIBI DEVELOPER GUIDE & INBOX BAR (GRACE & KIAN) =================
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = surface,
                modifier = Modifier
                    .fillMaxWidth()
                    .tutorialSpotlightTarget("home_walkthrough_banner")
                    .border(1.dp, accent.copy(alpha = 0.45f), RoundedCornerShape(20.dp))
                    .interactiveBounceClick { onOpenMascotTutorial() }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        ChibiFaceAvatar(
                            drawableRes = R.drawable.chibi_grace,
                            contentDescription = "Grace Chibi",
                            size = 38.dp,
                            borderColor = Color(0xFF38BDF8).copy(alpha = 0.7f)
                        )
                        ChibiFaceAvatar(
                            drawableRes = R.drawable.chibi_kian,
                            contentDescription = "Kian Chibi",
                            size = 38.dp,
                            borderColor = Color(0xFFF59E0B).copy(alpha = 0.7f)
                        )
                    }
                    Spacer(Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isTagalog)
                                "Gabay nina Grace at Kian (Walkthrough)"
                            else
                                "Grace & Kian • Interactive Walkthrough",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 13.sp,
                            color = textPrimary,
                            maxLines = 1,
                            modifier = Modifier.autoSlideIfOverflow()
                        )
                        Text(
                            text = if (isTagalog)
                                "Pumili ng Bahagi (Part 1–6) o buksan ang Team Inbox"
                            else
                                "Play visual novel guide (Parts 1–6) & Team Inbox",
                            fontSize = 11.sp,
                            color = textSecondary,
                            maxLines = 1,
                            modifier = Modifier.autoSlideIfOverflow()
                        )
                    }
                    Spacer(Modifier.width(6.dp))
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = accent.copy(alpha = 0.18f),
                        modifier = Modifier.interactiveBounceClick { onOpenTeamInbox() }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Inbox,
                                contentDescription = "Inbox",
                                tint = accent,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = "Inbox",
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = accent,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }
                }
            }

            // ================= 4. MINIMALIST 2x2 BENTO GRID =================
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .tutorialSpotlightTarget("home_core_workspace"),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = if (isTagalog) "MGA PANGUNAHING FEATURE" else "CORE WORKSPACE",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.5.sp,
                    color = textSecondary
                )

                // Row 1: FSL Camera (with stationary BorderBeam highlight) + AI Translate
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    BorderBeamBox(
                        active = true,
                        cornerRadius = 22.dp,
                        beamColor = primary,
                        secondaryColor = accent,
                        baseBorderColor = border,
                        backgroundColor = surface,
                        modifier = Modifier
                            .weight(1f)
                            .height(150.dp)
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

                    Surface(
                        shape = RoundedCornerShape(22.dp),
                        color = surface,
                        modifier = Modifier
                            .weight(1f)
                            .height(150.dp)
                            .border(1.dp, border, RoundedCornerShape(22.dp))
                            .interactiveBounceClick { onNavigateToTranslate() }
                    ) {
                        BentoTileContent(
                            icon = Icons.Default.Forum,
                            badge = "QWEN 2.5",
                            title = if (isTagalog) "Pagsasalin" else "Translate",
                            subtitle = if (isTagalog) "Quick words patungong boses" else "Quick words to spoken voice",
                            accentColor = accent,
                            textPrimary = textPrimary,
                            textSecondary = textSecondary
                        )
                    }
                }
            }

            // Row 2: Emergency SOS + 26-Letter FSL Reference Chart
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
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
                        subtitle = if (isTagalog) "Full-portrait gabay sa A–Z" else "Full-portrait 26 hand signs",
                        accentColor = SuccessGreen,
                        textPrimary = textPrimary,
                        textSecondary = textSecondary
                    )
                }
            }

            // ================= 5. MINIMALIST AI ENGINE STATUS PILL (WITH THINKING ORB) =================
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
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 8.dp)
                    ) {
                        Text(
                            text = if (isTagalog) "On-Device AI Model (Qwen 2.5)" else "On-Device AI Engine (Qwen 2.5)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = textPrimary,
                            maxLines = 1,
                            modifier = Modifier.autoSlideIfOverflow()
                        )
                        Text(
                            text = if (isTagalog)
                                "I-extract at pamahalaan ang 350 MB offline neural model"
                            else
                                "Extract & manage 350 MB offline neural weights",
                            fontSize = 11.sp,
                            color = textSecondary,
                            maxLines = 1,
                            modifier = Modifier.autoSlideIfOverflow()
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
                            maxLines = 1,
                            softWrap = false,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }

    // Enlarged Full-Portrait FSL Alphabet Chart (A-Z) with Ambient Aurora Glow
    if (showChartDialog) {
        FslAlphabetFullModal(
            isTagalog = isTagalog,
            onDismiss = { showChartDialog = false }
        )
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
                    maxLines = 1,
                    softWrap = false,
                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                )
            }
        }

        Column {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = textPrimary,
                maxLines = 1,
                modifier = Modifier.autoSlideIfOverflow()
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = textSecondary,
                lineHeight = 14.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
