package com.projectnexus.iwani.ui.screens

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.projectnexus.iwani.AppLanguage
import com.projectnexus.iwani.R
import com.projectnexus.iwani.ui.components.ChibiFaceAvatar
import com.projectnexus.iwani.ui.theme.*

data class TeamMember(
    val name: String,
    val role: String,
    val initials: String,
    val isLead: Boolean = false,
    val isGoldName: Boolean = false,
    val chibiRes: Int? = null
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

    val teamMembers = listOf(
        // 1. Leadership & Systems Architecture
        TeamMember("Cueto, Lander John D.", "Project Manager", "LC", isLead = true),
        TeamMember("Arteche, Justine Jay", "System Analyst", "JA"),

        // 2. Design, Frontend & Backend Engineering
        TeamMember("Alon, Angel Andreian L.", "UI/UX Lead Designer", "AA", isLead = true),
        TeamMember(
            name = "Valmonte, Wency Miguel D.",
            role = "Frontend & Backend Developer",
            initials = "WV",
            isLead = true,
            isGoldName = true
        ),
        TeamMember("Cunanan, Jhon Carlo A.", "Lead Backend Developer", "JC", isLead = true),
        TeamMember("Payla, Dyle Jhoven E.", "Backend Developer", "DP"),
        TeamMember(
            name = "Babagay, Mary Grace Ann N.",
            role = "Database Administrator",
            initials = "MB",
            chibiRes = R.drawable.chibi_grace
        ),

        // 3. DevOps & Quality Assurance
        TeamMember("Escaner, Leon B.", "DevOps & Deployment Specialist", "LE"),
        TeamMember(
            name = "Cagadas, Kian R.",
            role = "Quality Assurance",
            initials = "KC",
            chibiRes = R.drawable.chibi_kian
        ),

        // 4. Technical Documentation & Presentation
        TeamMember("Lomibao, Milbert A.", "Technical Writer & UML Architect", "ML"),
        TeamMember("Lugay, Brandon Ashley P.", "UAT Coordinator & Pitch Presenter", "BL")
    )

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

            // Team Members List
            items(teamMembers) { member ->
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = cardSurface),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            width = if (member.isGoldName) 1.5.dp else 1.dp,
                            color = when {
                                member.isGoldName -> accent.copy(alpha = 0.85f)
                                member.isLead -> accent.copy(alpha = 0.45f)
                                else -> cardBorder
                            },
                            shape = RoundedCornerShape(14.dp)
                        )
                        .clickable {
                            Toast.makeText(context, "${member.name} • ${member.role}", Toast.LENGTH_SHORT).show()
                        }
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (member.chibiRes != null) {
                            ChibiFaceAvatar(
                                drawableRes = member.chibiRes,
                                contentDescription = member.name,
                                size = 46.dp,
                                shape = CircleShape,
                                backgroundColor = if (isDarkTheme) Color(0xFF0B1326) else Color(0xFFE0F2FE),
                                borderColor = if (member.isLead || member.isGoldName) accent else Color(0xFF38BDF8).copy(alpha = 0.7f),
                                zoomScale = 2.15f
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .background(
                                        when {
                                            member.isGoldName -> accent.copy(alpha = 0.25f)
                                            member.isLead -> accent.copy(alpha = 0.2f)
                                            isDarkTheme -> IcctBlue.copy(alpha = 0.2f)
                                            else -> TealPrimary.copy(alpha = 0.12f)
                                        },
                                        CircleShape
                                    )
                                    .then(
                                        if (member.isGoldName) Modifier.border(1.2.dp, accent, CircleShape) else Modifier
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = member.initials,
                                    color = if (member.isLead || member.isGoldName) accent else if (isDarkTheme) Color(0xFF93C5FD) else TealPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }

                        Spacer(Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = member.name,
                                fontWeight = if (member.isGoldName) FontWeight.ExtraBold else FontWeight.Bold,
                                fontSize = 14.5.sp,
                                color = if (member.isGoldName) accent else textPrim
                            )
                            Text(
                                text = member.role,
                                fontSize = 12.sp,
                                color = if (member.isLead || member.isGoldName) accent else textSec,
                                fontWeight = if (member.isLead || member.isGoldName) FontWeight.SemiBold else FontWeight.Normal
                            )
                        }
                    }
                }
            }

            item {
                Spacer(Modifier.height(18.dp))
            }
        }
    }
}
