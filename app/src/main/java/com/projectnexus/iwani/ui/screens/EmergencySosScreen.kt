package com.projectnexus.iwani.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.StopCircle
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.projectnexus.iwani.AppLanguage
import com.projectnexus.iwani.emergency.EmergencyPhrase
import com.projectnexus.iwani.emergency.EmergencySosManager
import com.projectnexus.iwani.ui.components.VoiceGlowBar
import com.projectnexus.iwani.ui.components.interactiveBounceClick
import com.projectnexus.iwani.ui.theme.*

@Composable
fun EmergencySosScreen(
    sosManager: EmergencySosManager,
    appLanguage: AppLanguage = AppLanguage.ENGLISH,
    isDarkTheme: Boolean = true
) {
    val isTagalog = appLanguage.isTagalog
    var activeSpeakingUtterance by remember { mutableStateOf<String?>(null) }
    val bg = IWaniColors.background(isDarkTheme)
    val cardSurface = IWaniColors.surface(isDarkTheme)
    val cardBorder = IWaniColors.border(isDarkTheme)
    val textPrim = IWaniColors.textPrimary(isDarkTheme)
    val textSec = IWaniColors.textSecondary(isDarkTheme)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bg)
            .padding(18.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Emergency SOS",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = EmergencyRed
                )
                Text(
                    text = if (isTagalog) "Pindutin ang sitwasyon para iparinig agad sa boses" else "Tap any situation for instant voice broadcast",
                    fontSize = 12.sp,
                    color = textSec
                )
            }

            if (activeSpeakingUtterance != null) {
                IconButton(
                    onClick = {
                        sosManager.stop()
                        activeSpeakingUtterance = null
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.StopCircle,
                        contentDescription = "Stop",
                        tint = EmergencyRed,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }
        }

        // Active Speaking Banner with VoiceGlowBar (Libraries.dev `voice-glow`)
        if (activeSpeakingUtterance != null) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = EmergencyRedDark.copy(alpha = if (isDarkTheme) 0.65f else 0.85f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 14.dp)
                    .border(1.5.dp, EmergencyRed, RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.VolumeUp, contentDescription = null, tint = Color.White)
                        Spacer(Modifier.width(12.dp))
                        Text(
                            text = (if (isTagalog) "Ipinapahayag: " else "Broadcasting: ") + "\"$activeSpeakingUtterance\"",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Spacer(Modifier.height(10.dp))
                    VoiceGlowBar(
                        active = true,
                        primaryColor = EmergencyRed,
                        secondaryColor = IcctGold
                    )
                }
            }
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(EmergencyPhrase.standardPhrases) { phrase ->
                val phraseUtterance = phrase.getUtterance(isTagalog)
                val phraseTitle = phrase.getTitle(isTagalog)

                EmergencyPhraseCard(
                    title = phraseTitle,
                    utterance = phraseUtterance,
                    isActive = activeSpeakingUtterance == phraseUtterance,
                    isDarkTheme = isDarkTheme,
                    cardSurface = cardSurface,
                    cardBorder = cardBorder,
                    textPrim = textPrim,
                    textSec = textSec,
                    onClick = {
                        if (activeSpeakingUtterance == phraseUtterance) {
                            sosManager.stop()
                            activeSpeakingUtterance = null
                        } else {
                            activeSpeakingUtterance = phraseUtterance
                            sosManager.speak(phraseUtterance) {
                                activeSpeakingUtterance = null
                            }
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun EmergencyPhraseCard(
    title: String,
    utterance: String,
    isActive: Boolean,
    isDarkTheme: Boolean,
    cardSurface: Color,
    cardBorder: Color,
    textPrim: Color,
    textSec: Color,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isActive) EmergencyRed.copy(alpha = if (isDarkTheme) 0.35f else 0.15f) else cardSurface
        ),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.dp,
                if (isActive) EmergencyRed else cardBorder,
                RoundedCornerShape(16.dp)
            )
            .interactiveBounceClick { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(EmergencyRed.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Campaign,
                    contentDescription = null,
                    tint = EmergencyRed,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = textPrim
                )
                Text(
                    text = utterance,
                    fontSize = 12.sp,
                    color = textSec,
                    modifier = Modifier.padding(top = 4.dp),
                    lineHeight = 17.sp
                )
            }
            Icon(
                imageVector = Icons.Default.VolumeUp,
                contentDescription = "Speak",
                tint = if (isActive) EmergencyRed else textSec,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}
