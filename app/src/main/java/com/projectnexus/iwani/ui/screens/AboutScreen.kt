package com.projectnexus.iwani.ui.screens

import android.widget.Toast
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.projectnexus.iwani.AppLanguage
import com.projectnexus.iwani.R
import com.projectnexus.iwani.ui.components.BorderBeamBox
import com.projectnexus.iwani.ui.components.ChibiFaceAvatar
import com.projectnexus.iwani.ui.components.autoSlideIfOverflow
import com.projectnexus.iwani.ui.sound.UiSoundManager
import com.projectnexus.iwani.ui.theme.*
import kotlinx.coroutines.delay

data class TeamMember(
    val name: String,
    val role: String,
    val initials: String,
    val profileRes: Int,
    val chibiRes: Int,
    val isLead: Boolean = false,
    val hasGoldGradient: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(
    appLanguage: AppLanguage = AppLanguage.ENGLISH,
    isDarkTheme: Boolean = true,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val isTagalog = appLanguage.isTagalog
    val bg = IWaniColors.background(isDarkTheme)
    val cardSurface = IWaniColors.surface(isDarkTheme)
    val cardBorder = IWaniColors.border(isDarkTheme)
    val textPrim = IWaniColors.textPrimary(isDarkTheme)
    val textSec = IWaniColors.textSecondary(isDarkTheme)
    val accent = IWaniColors.accent(isDarkTheme)

    val teamMembers = remember {
        listOf(
            // All Gold Leads on Top: 1. Lander (Moving Gold Status Box) -> 2. Wency (Moving Gold Status Box) -> 3. Cunanan -> 4. Alon -> followed by regular members
            TeamMember(
                name = "Cueto, Lander John D.",
                role = "Project Manager",
                initials = "LC",
                profileRes = R.drawable.profile_lander,
                chibiRes = R.drawable.chibi_lander,
                isLead = true,
                hasGoldGradient = true
            ),
            TeamMember(
                name = "Valmonte, Wency Miguel D.",
                role = "Frontend & Backend Developer",
                initials = "WV",
                profileRes = R.drawable.profile_wency,
                chibiRes = R.drawable.chibi_wency,
                isLead = true,
                hasGoldGradient = true
            ),
            TeamMember(
                name = "Cunanan, Jhon Carlo A.",
                role = "Lead Backend Developer",
                initials = "JC",
                profileRes = R.drawable.profile_cunanan,
                chibiRes = R.drawable.chibi_cunanan,
                isLead = true
            ),
            TeamMember(
                name = "Alon, Angel Andreian L.",
                role = "UI/UX Lead Designer",
                initials = "AA",
                profileRes = R.drawable.profile_alon,
                chibiRes = R.drawable.chibi_alon,
                isLead = true
            ),
            TeamMember(
                name = "Arteche, Justine Jay",
                role = "System Analyst",
                initials = "JA",
                profileRes = R.drawable.profile_arteche,
                chibiRes = R.drawable.chibi_arteche
            ),
            TeamMember(
                name = "Payla, Dyle Jhoven E.",
                role = "Backend Developer",
                initials = "DP",
                profileRes = R.drawable.profile_payla,
                chibiRes = R.drawable.chibi_payla
            ),
            TeamMember(
                name = "Babagay, Mary Grace Ann N.",
                role = "Database Administrator",
                initials = "MB",
                profileRes = R.drawable.profile_grace,
                chibiRes = R.drawable.chibi_grace
            ),
            TeamMember(
                name = "Escaner, Leon B.",
                role = "DevOps & Deployment Specialist",
                initials = "LE",
                profileRes = R.drawable.profile_escaner,
                chibiRes = R.drawable.chibi_escaner
            ),
            TeamMember(
                name = "Cagadas, Kian R.",
                role = "Quality Assurance",
                initials = "KC",
                profileRes = R.drawable.profile_kian,
                chibiRes = R.drawable.chibi_kian
            ),
            TeamMember(
                name = "Lomibao, Milbert A.",
                role = "Technical Writer & UML Architect",
                initials = "ML",
                profileRes = R.drawable.profile_lomibao,
                chibiRes = R.drawable.chibi_lomibao
            ),
            TeamMember(
                name = "Lugay, Brandon Ashley P.",
                role = "UAT Coordinator & Pitch Presenter",
                initials = "BL",
                profileRes = R.drawable.profile_lugay,
                chibiRes = R.drawable.chibi_lugay
            )
        )
    }

    // Christmas-light sequential rotation:
    // Real profile picture shown first for 3s -> rotates one-by-one from Lander (#1) to Wency (#2) to Cunanan (#3) -> ...
    // to Chibi, holds 3s, then rotates in sequence back to Real Profile -> and back to Chibi again.
    val showChibiByMember = remember { mutableStateListOf(*Array(teamMembers.size) { false }) }
    var activeLitIndex by remember { mutableIntStateOf(-1) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(3000L)
            for (i in teamMembers.indices) {
                activeLitIndex = i
                showChibiByMember[i] = true
                delay(230L)
            }
            activeLitIndex = -1
            delay(3000L)
            for (i in teamMembers.indices) {
                activeLitIndex = i
                showChibiByMember[i] = false
                delay(230L)
            }
            activeLitIndex = -1
        }
    }

    Scaffold(
        containerColor = bg,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (isTagalog) "Tungkol sa Proyekto" else "About Project Nexus",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = textPrim
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = accent
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = bg)
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(bg)
                .padding(paddingValues)
                .padding(horizontal = 18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Spacer(Modifier.height(4.dp))

                // Institutional Card
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = cardSurface),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, cardBorder, RoundedCornerShape(20.dp))
                ) {
                    Column(
                        modifier = Modifier.padding(22.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Institutional Logos: ICCT Seal, iWani PH-Flag Logo, Engineering Society, Engineering 1992
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.icct_seal),
                                contentDescription = "ICCT Seal",
                                modifier = Modifier.size(52.dp)
                            )
                            Spacer(Modifier.width(12.dp))
                            Image(
                                painter = painterResource(id = R.drawable.logo_iwani),
                                contentDescription = "iWani Logo",
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(Modifier.width(12.dp))
                            Image(
                                painter = painterResource(id = R.drawable.icct_eng_soc_2002),
                                contentDescription = "ICCT Engineering Society",
                                modifier = Modifier.size(52.dp)
                            )
                            Spacer(Modifier.width(12.dp))
                            Image(
                                painter = painterResource(id = R.drawable.icct_engineering_1992),
                                contentDescription = "ICCT Engineering 1992",
                                modifier = Modifier.size(52.dp)
                            )
                        }

                        Spacer(Modifier.height(14.dp))

                        Text(
                            text = "ICCT COLLEGES FOUNDATION INC.",
                            fontWeight = FontWeight.Black,
                            fontSize = 17.sp,
                            color = accent,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "College of Engineering and Digital Technology",
                            fontSize = 13.sp,
                            color = textSec,
                            textAlign = TextAlign.Center
                        )
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isDarkTheme) IcctBlue.copy(alpha = 0.18f) else TealPrimary.copy(alpha = 0.12f),
                            modifier = Modifier.padding(top = 8.dp)
                        ) {
                            Text(
                                text = "Computer Engineering • A.Y. 2026–2027",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isDarkTheme) Color(0xFF93C5FD) else TealPrimary,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }

            item {
                // Project Description Card
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = cardSurface),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, cardBorder, RoundedCornerShape(20.dp))
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = "PROJECT NEXUS • iWani",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = accent
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = if (isTagalog)
                                "Pagsasalin ng Filipino Sign Language (FSL) patungong boses at teksto upang pag-ugnayin ang komunidad ng mga pipi/bingi at ang lipunan."
                            else
                                "Bridging Communication Between the Deaf Community and Hearing Majority Through Filipino Sign Language Translation.",
                            fontSize = 13.sp,
                            color = textPrim,
                            lineHeight = 19.sp
                        )

                        Spacer(Modifier.height(12.dp))
                        HorizontalDivider(color = cardBorder)
                        Spacer(Modifier.height(12.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.School, contentDescription = null, tint = accent, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(10.dp))
                            Text(
                                text = "Republic Act No. 11106 (The Filipino Sign Language Act)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = textSec
                            )
                        }

                        Spacer(Modifier.height(8.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = accent, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(10.dp))
                            Text(
                                text = "On-Device FSL Computer Vision (MediaPipe + TFLite) • Privacy-Focused",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = textSec
                            )
                        }

                        Spacer(Modifier.height(8.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.School, contentDescription = null, tint = accent, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(10.dp))
                            Text(
                                text = "Language & NLP Engine: Qwen2.5-0.5B-Instruct (On-Device Neural NLP)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = textSec
                            )
                        }
                    }
                }
            }

            item {
                // Section Header for Team Roster
                Text(
                    text = if (isTagalog) "MGA MIYEMBRO NG PROYEKTO (HIYARKIYA)" else "PROJECT TEAM & ROLE HIERARCHY",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = accent,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                )
            }

            // Team Members List with Christmas-Light Profile <-> Chibi 3D Rotation
            itemsIndexed(teamMembers) { index, member ->
                val isShowingChibi = showChibiByMember.getOrElse(index) { false }
                val isLit = activeLitIndex == index
                val flipRotationY by animateFloatAsState(
                    targetValue = if (isShowingChibi) 180f else 0f,
                    animationSpec = tween(durationMillis = 420, easing = FastOutSlowInEasing),
                    label = "avatarFlip_$index"
                )
                val avatarBg = if (isDarkTheme) Color(0xFF0B1326) else Color(0xFFE0F2FE)
                val avatarBorderColor = when {
                    isLit -> accent
                    member.isLead -> accent.copy(alpha = 0.85f)
                    else -> Color(0xFF38BDF8).copy(alpha = 0.7f)
                }

                val goldTextBrush = Brush.linearGradient(
                    colors = if (isDarkTheme) {
                        listOf(Color(0xFFFEF08A), Color(0xFFFBBF24), Color(0xFFF59E0B))
                    } else {
                        listOf(Color(0xFFB45309), Color(0xFFD97706), Color(0xFF92400E))
                    }
                )

                val rowContent: @Composable () -> Unit = {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .graphicsLayer {
                                    rotationY = flipRotationY
                                    cameraDistance = 12f * density
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            if (flipRotationY <= 90f) {
                                // Real centered face-only portrait (background removed, same circular backdrop)
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(CircleShape)
                                        .background(avatarBg, CircleShape)
                                        .border(if (isLit) 2.dp else 1.4.dp, avatarBorderColor, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Image(
                                        painter = painterResource(id = member.profileRes),
                                        contentDescription = "${member.name} Profile",
                                        contentScale = ContentScale.Crop,
                                        alignment = Alignment.Center,
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clip(CircleShape)
                                    )
                                }
                            } else {
                                // Flipped back side: Chibi in matching ICCT uniform (un-mirrored via rotationY = 180f)
                                Box(
                                    modifier = Modifier.graphicsLayer { rotationY = 180f },
                                    contentAlignment = Alignment.Center
                                ) {
                                    ChibiFaceAvatar(
                                        drawableRes = member.chibiRes,
                                        contentDescription = "${member.name} Chibi",
                                        size = 48.dp,
                                        shape = CircleShape,
                                        backgroundColor = avatarBg,
                                        borderColor = avatarBorderColor,
                                        zoomScale = 2.15f
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            if (member.hasGoldGradient) {
                                Text(
                                    text = member.name,
                                    style = TextStyle(
                                        brush = goldTextBrush,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 15.sp
                                    ),
                                    maxLines = 1,
                                    softWrap = false,
                                    modifier = Modifier.autoSlideIfOverflow()
                                )
                            } else {
                                Text(
                                    text = member.name,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.5.sp,
                                    color = textPrim,
                                    maxLines = 1,
                                    softWrap = false,
                                    modifier = Modifier.autoSlideIfOverflow()
                                )
                            }
                            Text(
                                text = member.role,
                                fontSize = 12.sp,
                                color = if (member.isLead) accent else textSec,
                                fontWeight = if (member.isLead) FontWeight.SemiBold else FontWeight.Normal,
                                maxLines = 1,
                                softWrap = false,
                                modifier = Modifier.autoSlideIfOverflow()
                            )
                        }
                    }
                }

                if (member.hasGoldGradient) {
                    // Moving Gold BorderBeam + Interior Lighting Box (just like FSL Camera button, in Gold status)
                    BorderBeamBox(
                        active = true,
                        cornerRadius = 14.dp,
                        borderWidth = if (isLit) 2.2.dp else 1.8.dp,
                        beamColor = Color(0xFFF59E0B),
                        secondaryColor = Color(0xFFFEF08A),
                        baseBorderColor = accent.copy(alpha = 0.55f),
                        backgroundColor = cardSurface,
                        durationMillis = if (index == 0) 3400 else 3800,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                UiSoundManager.playClick(context)
                                showChibiByMember[index] = !showChibiByMember[index]
                                Toast.makeText(context, "${member.name} • ${member.role}", Toast.LENGTH_SHORT).show()
                            }
                    ) {
                        rowContent()
                    }
                } else {
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = cardSurface),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                width = if (isLit) 1.5.dp else 1.dp,
                                color = when {
                                    isLit -> accent
                                    member.isLead -> accent.copy(alpha = 0.45f)
                                    else -> cardBorder
                                },
                                shape = RoundedCornerShape(14.dp)
                            )
                            .clickable {
                                UiSoundManager.playClick(context)
                                showChibiByMember[index] = !showChibiByMember[index]
                                Toast.makeText(context, "${member.name} • ${member.role}", Toast.LENGTH_SHORT).show()
                            }
                    ) {
                        rowContent()
                    }
                }
            }

            item {
                Spacer(Modifier.height(18.dp))
            }
        }
    }
}
