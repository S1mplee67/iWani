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
import com.projectnexus.iwani.ui.theme.*

data class TeamMember(
    val name: String,
    val role: String,
    val initials: String,
    val isLead: Boolean = false
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

        // 2. Design & Frontend
        TeamMember("Alon, Angel Andreian L.", "UI/UX Lead Designer", "AA", isLead = true),
        TeamMember("Tamarosa, Joycemay O.", "Frontend Developer", "JT"),

        // 3. Backend & Database Engineering
        TeamMember("Cunanan, Jhon Carlo A.", "Lead Backend Developer", "JC", isLead = true),
        TeamMember("Valmonte, Wency Miguel D.", "Backend Developer", "WV"),
        TeamMember("Payla, Dyle Jhoven E.", "Backend Developer", "DP"),
        TeamMember("Babagay, Mary Grace Ann N.", "Database Administrator", "MB"),

        // 4. DevOps & Quality Assurance
        TeamMember("Escaner, Leon B.", "DevOps & Deployment Specialist", "LE"),
        TeamMember("Cagadas, Kian R.", "Quality Assurance", "KC"),

        // 5. Technical Documentation & Presentation
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
                        Image(
                            painter = painterResource(id = R.drawable.logo_iwani),
                            contentDescription = "iWani Logo",
                            modifier = Modifier
                                .size(76.dp)
                                .clip(RoundedCornerShape(18.dp))
                        )

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
                        .border(1.dp, if (member.isLead) accent.copy(alpha = 0.45f) else cardBorder, RoundedCornerShape(14.dp))
                        .clickable {
                            Toast.makeText(context, "${member.name} • ${member.role}", Toast.LENGTH_SHORT).show()
                        }
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .background(if (member.isLead) accent.copy(alpha = 0.2f) else if (isDarkTheme) IcctBlue.copy(alpha = 0.2f) else TealPrimary.copy(alpha = 0.12f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = member.initials,
                                color = if (member.isLead) accent else if (isDarkTheme) Color(0xFF93C5FD) else TealPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }

                        Spacer(Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = member.name,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = textPrim
                            )
                            Text(
                                text = member.role,
                                fontSize = 12.sp,
                                color = if (member.isLead) accent else textSec,
                                fontWeight = if (member.isLead) FontWeight.SemiBold else FontWeight.Normal
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
