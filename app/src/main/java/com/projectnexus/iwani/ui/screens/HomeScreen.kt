package com.projectnexus.iwani.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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

@OptIn(ExperimentalAnimationApi::class)
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
                descEn = "Assisting barangay officers and citizens during urgent public inquiries.",
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

    // Auto-advancing slideshow timer (advances every 4 seconds)
    LaunchedEffect(isUserInteracting) {
        if (!isUserInteracting) {
            while (true) {
                delay(4000)
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
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Greeting Header
        Text(
            text = if (isTagalog) "Kamusta, $greeting!" else "Welcome, $greeting!",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = textPrimary
        )
        Text(
            text = if (isTagalog) "Pagsasalin ng Filipino Sign Language sa Boses" else "Filipino Sign Language Translation & Voice Assistance",
            fontSize = 13.sp,
            color = textSecondary,
            modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
        )

        // 6-Photo Slideshow Hero Card
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = surface),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, border, RoundedCornerShape(20.dp))
        ) {
            Column {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(175.dp)
                        .clickable {
                            isUserInteracting = true
                            currentSlideIndex = (currentSlideIndex + 1) % slides.size
                        }
                ) {
                    AnimatedContent(
                        targetState = currentSlideIndex,
                        transitionSpec = {
                            fadeIn(animationSpec = tween(500)) togetherWith fadeOut(animationSpec = tween(500))
                        },
                        label = "SlideTransition"
                    ) { targetIndex ->
                        val slide = slides[targetIndex]
                        Image(
                            painter = painterResource(id = slide.drawableRes),
                            contentDescription = slide.titleEn,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(175.dp)
                                .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)),
                            contentScale = ContentScale.Crop
                        )
                    }

                    // Previous / Next Subtle Touch Controls
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color.Black.copy(alpha = 0.45f),
                            modifier = Modifier
                                .size(32.dp)
                                .clickable {
                                    isUserInteracting = true
                                    currentSlideIndex = if (currentSlideIndex == 0) slides.size - 1 else currentSlideIndex - 1
                                }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.ChevronLeft,
                                    contentDescription = "Previous Slide",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        Surface(
                            shape = CircleShape,
                            color = Color.Black.copy(alpha = 0.45f),
                            modifier = Modifier
                                .size(32.dp)
                                .clickable {
                                    isUserInteracting = true
                                    currentSlideIndex = (currentSlideIndex + 1) % slides.size
                                }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.ChevronRight,
                                    contentDescription = "Next Slide",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }

                // Slide Caption & Indicator Dots
                val currentSlide = slides[currentSlideIndex]
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)
                ) {
                    Text(
                        text = if (isTagalog) currentSlide.titleTl else currentSlide.titleEn,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = accent
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (isTagalog) currentSlide.descTl else currentSlide.descEn,
                        fontSize = 12.sp,
                        color = textSecondary,
                        lineHeight = 16.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Indicator Dots (● ○ ○ ○ ○ ○)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        slides.indices.forEach { index ->
                            val isSelected = index == currentSlideIndex
                            Box(
                                modifier = Modifier
                                    .padding(horizontal = 3.dp)
                                    .size(
                                        width = if (isSelected) 18.dp else 6.dp,
                                        height = 6.dp
                                    )
                                    .background(
                                        color = if (isSelected) accent else textSecondary.copy(alpha = 0.35f),
                                        shape = RoundedCornerShape(3.dp)
                                    )
                                    .clickable {
                                        isUserInteracting = true
                                        currentSlideIndex = index
                                    }
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Emergency SOS Quick Banner
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isDarkTheme) EmergencyRedDark.copy(alpha = 0.35f) else Color(0xFFFEE2E2)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    1.dp,
                    if (isDarkTheme) EmergencyRed.copy(alpha = 0.5f) else Color(0xFFFCA5A5),
                    RoundedCornerShape(20.dp)
                )
                .clickable { onNavigateToEmergency() }
        ) {
            Row(
                modifier = Modifier.padding(18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(EmergencyRed.copy(alpha = 0.25f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Campaign,
                        contentDescription = "SOS",
                        tint = EmergencyRed,
                        modifier = Modifier.size(28.dp)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = if (isTagalog) "Emergency SOS (Saklolo)" else "Emergency SOS",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = if (isDarkTheme) Color(0xFFFCA5A5) else Color(0xFF991B1B)
                    )
                    Text(
                        text = if (isTagalog) "1-tap agarang boses para sa tulong" else "1-tap loud voice broadcast for immediate assistance",
                        fontSize = 12.sp,
                        color = textSecondary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // FSL Camera Recognition Card
        ModeCard(
            title = if (isTagalog) "Pagsasanay sa FSL (Kamera)" else "FSL Camera Recognition",
            subtitle = if (isTagalog) "Kilalanin ang 26 titik ng alpabeto gamit ang camera" else "Recognize 26 alphabet letters live with camera",
            icon = Icons.Default.School,
            accentColor = primary,
            isDarkTheme = isDarkTheme,
            onClick = onNavigateToCamera
        )

        Spacer(modifier = Modifier.height(14.dp))

        // FSL Communication & Translation Card
        ModeCard(
            title = if (isTagalog) "Komunikasyon & Pagsasalin" else "Communication & Translation",
            subtitle = if (isTagalog) "Gawing natural na pangungusap ang mga senyas" else "Turn sign glosses into fluent spoken sentences",
            icon = Icons.Default.Forum,
            accentColor = accent,
            isDarkTheme = isDarkTheme,
            onClick = onNavigateToTranslate
        )

        Spacer(modifier = Modifier.height(14.dp))

        // FSL 26-Letter Reference Chart Button
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = surface),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, border, RoundedCornerShape(20.dp))
                .clickable { showChartDialog = true }
        ) {
            Row(
                modifier = Modifier.padding(18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .background(accent.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.MenuBook,
                        contentDescription = "Alphabet Chart",
                        tint = accent,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (isTagalog) "Tingnan ang 26-Titik FSL Tsart" else "View 26-Letter FSL Chart",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = textPrimary
                    )
                    Text(
                        text = if (isTagalog) "Gabay sa tamang posisyon ng kamay para sa A-Z" else "Visual reference guide for letters A to Z",
                        fontSize = 12.sp,
                        color = textSecondary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Model AI Lokal (On-Device AI Manager)
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = surface),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, border, RoundedCornerShape(20.dp))
                .clickable { onNavigateToModelManager() }
        ) {
            Row(
                modifier = Modifier.padding(18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .background(accent.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Psychology,
                        contentDescription = "Model AI",
                        tint = accent,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (isTagalog) "Model AI Lokal" else "On-Device AI Models",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = textPrimary
                        )
                        Spacer(Modifier.width(8.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = SuccessGreen.copy(alpha = 0.18f)
                        ) {
                            Text(
                                text = "Qwen 2.5",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = SuccessGreen,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Text(
                        text = if (isTagalog) "Pamahalaan ang offline neural AI models (~350 MB)" else "Download and manage offline neural AI models (~350 MB)",
                        fontSize = 12.sp,
                        color = textSecondary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // About Project Nexus Card
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = surface),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, border, RoundedCornerShape(20.dp))
                .clickable { onNavigateToAbout() }
        ) {
            Row(
                modifier = Modifier.padding(18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .background(primary.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "About",
                        tint = primary,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (isTagalog) "Tungkol sa Project Nexus" else "About Project Nexus & Team",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = textPrimary
                    )
                    Text(
                        text = "Computer Engineering • A.Y. 2026–2027",
                        fontSize = 12.sp,
                        color = textSecondary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))
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
                shape = RoundedCornerShape(20.dp),
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
                            text = "FSL Alphabet Chart (A-Z)",
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
                            .padding(horizontal = 8.dp, vertical = 8.dp),
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
fun ModeCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color,
    isDarkTheme: Boolean = true,
    onClick: () -> Unit
) {
    val surface = IWaniColors.surface(isDarkTheme)
    val border = IWaniColors.border(isDarkTheme)
    val textPrimary = IWaniColors.textPrimary(isDarkTheme)
    val textSecondary = IWaniColors.textSecondary(isDarkTheme)

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = surface),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, border, RoundedCornerShape(20.dp))
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .background(accentColor.copy(alpha = 0.2f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = accentColor,
                    modifier = Modifier.size(26.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = textPrimary
                )
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = textSecondary,
                    lineHeight = 16.sp
                )
            }
        }
    }
}
