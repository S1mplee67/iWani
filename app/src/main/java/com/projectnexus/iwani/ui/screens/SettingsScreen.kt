package com.projectnexus.iwani.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeDown
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.projectnexus.iwani.AppLanguage
import com.projectnexus.iwani.R
import com.projectnexus.iwani.ui.components.*
import com.projectnexus.iwani.ui.sound.SoundProfile
import com.projectnexus.iwani.ui.sound.UiSoundManager
import com.projectnexus.iwani.ui.theme.*

@Composable
fun SettingsScreen(
    appLanguage: AppLanguage,
    onLanguageChange: (AppLanguage) -> Unit,
    isDarkTheme: Boolean,
    onThemeChange: (Boolean) -> Unit,
    fontStyle: AppFontStyle,
    onFontStyleChange: (AppFontStyle) -> Unit,
    fontScale: Float,
    onFontScaleChange: (Float) -> Unit,
    onNavigateToModelManager: () -> Unit,
    onNavigateToAbout: () -> Unit,
    onReplayIntro: () -> Unit,
    onOpenMascotTutorial: () -> Unit = {},
    onOpenTeamInbox: () -> Unit = {}
) {
    val context = LocalContext.current
    val isTagalog = appLanguage.isTagalog

    val bg = IWaniColors.background(isDarkTheme)
    val surface = IWaniColors.surface(isDarkTheme)
    val border = IWaniColors.border(isDarkTheme)
    val textPrimary = IWaniColors.textPrimary(isDarkTheme)
    val textSecondary = IWaniColors.textSecondary(isDarkTheme)
    val accent = IWaniColors.accent(isDarkTheme)
    val primary = IWaniColors.primary(isDarkTheme)

    var soundEnabled by remember { mutableStateOf(UiSoundManager.soundEnabled) }
    var hapticsEnabled by remember { mutableStateOf(UiSoundManager.hapticsEnabled) }
    var selectedProfile by remember { mutableStateOf(UiSoundManager.profile) }
    var soundVolume by remember { mutableFloatStateOf(UiSoundManager.volume) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bg)
            .padding(horizontal = 20.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(Modifier.height(8.dp))

        // Header — left Column uses weight(1f) so MetalBadge NEVER wraps or compresses
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 12.dp)
            ) {
                Text(
                    text = if (isTagalog) "Mga Setting" else "Settings & Studio",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    color = textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = if (isTagalog)
                        "I-customize ang tema, tunog, font at AI engine"
                    else
                        "Customize theme, sound, font & AI engine",
                    fontSize = 12.sp,
                    color = textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            MetalBadge(
                text = "PROJECT NEXUS",
                isDark = isDarkTheme
            )
        }

        // ================= 1. APPEARANCE & LANGUAGE =================
        SettingsSectionCard(
            title = if (isTagalog) "Hitsura at Wika" else "Appearance & Language",
            icon = Icons.Default.Palette,
            accentColor = primary,
            isDarkTheme = isDarkTheme
        ) {
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
                        text = if (isTagalog) "Dark Mode (Midnight Navy)" else "Dark Mode (Midnight Navy)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = textPrimary
                    )
                    Text(
                        text = if (isTagalog) "May wave transition kapag pinindot" else "Smooth portfolio wave transition",
                        fontSize = 12.sp,
                        color = textSecondary
                    )
                }
                Switch(
                    checked = isDarkTheme,
                    onCheckedChange = {
                        UiSoundManager.playToggle(context, it)
                        onThemeChange(it)
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = primary
                    )
                )
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 12.dp),
                color = border
            )

            Text(
                text = if (isTagalog) "Wika ng App" else "Application Language",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = textPrimary
            )
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                LanguageOptionTile(
                    modifier = Modifier.weight(1f),
                    flagRes = R.drawable.flag_usa,
                    label = "English (US)",
                    selected = appLanguage == AppLanguage.ENGLISH,
                    primaryColor = primary,
                    borderColor = border,
                    textPrimary = textPrimary,
                    onClick = { onLanguageChange(AppLanguage.ENGLISH) }
                )
                LanguageOptionTile(
                    modifier = Modifier.weight(1f),
                    flagRes = R.drawable.flag_philippines,
                    label = "Tagalog (PH)",
                    selected = appLanguage == AppLanguage.TAGALOG,
                    primaryColor = primary,
                    borderColor = border,
                    textPrimary = textPrimary,
                    onClick = { onLanguageChange(AppLanguage.TAGALOG) }
                )
            }
        }

        // ================= 2. TYPOGRAPHY & FONT STYLE =================
        SettingsSectionCard(
            title = if (isTagalog) "Estilo at Laki ng Font" else "Typography & Font Style",
            icon = Icons.Default.TextFields,
            accentColor = accent,
            isDarkTheme = isDarkTheme
        ) {
            Text(
                text = if (isTagalog) "Pumili ng Font sa Buong App" else "Select Active Typeface",
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = textSecondary
            )
            Spacer(Modifier.height(10.dp))

            val styles = AppFontStyle.values()
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                for (rowIdx in 0..1) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        for (colIdx in 0..1) {
                            val style = styles[rowIdx * 2 + colIdx]
                            val isSelected = fontStyle == style
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = if (isSelected) primary.copy(alpha = 0.2f) else Color.Transparent,
                                modifier = Modifier
                                    .weight(1f)
                                    .border(
                                        width = if (isSelected) 1.5.dp else 1.dp,
                                        color = if (isSelected) primary else border,
                                        shape = RoundedCornerShape(14.dp)
                                    )
                                    .interactiveBounceClick {
                                        onFontStyleChange(style)
                                    }
                            ) {
                                Column(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp)
                                ) {
                                    Text(
                                        text = "Aa Bb",
                                        fontFamily = style.fontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        color = if (isSelected) accent else textPrimary,
                                        maxLines = 1,
                                        softWrap = false
                                    )
                                    Spacer(Modifier.height(3.dp))
                                    Text(
                                        text = if (isTagalog) style.labelTl else style.labelEn,
                                        fontFamily = style.fontFamily,
                                        fontSize = 11.sp,
                                        color = textSecondary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isTagalog) "Laki ng Teksto" else "Text Size Scale",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = textPrimary
                )
                Text(
                    text = "${(fontScale * 100).toInt()}%",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = accent
                )
            }
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val scales = listOf(
                    0.92f to (if (isTagalog) "Maliit" else "Compact"),
                    1.0f to (if (isTagalog) "Normal" else "Default"),
                    1.10f to (if (isTagalog) "Malaki" else "Large")
                )
                scales.forEach { (scaleVal, label) ->
                    val isSel = kotlin.math.abs(fontScale - scaleVal) < 0.03f
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSel) primary else Color.Transparent,
                        modifier = Modifier
                            .weight(1f)
                            .border(1.dp, if (isSel) primary else border, RoundedCornerShape(12.dp))
                            .interactiveBounceClick { onFontScaleChange(scaleVal) }
                    ) {
                        Text(
                            text = label,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSel) Color.White else textSecondary,
                            maxLines = 1,
                            softWrap = false,
                            modifier = Modifier
                                .padding(vertical = 8.dp)
                                .wrapContentWidth(Alignment.CenterHorizontally)
                        )
                    }
                }
            }
        }

        // ================= 3. INTERACTIVE SOUND & HAPTICS =================
        SettingsSectionCard(
            title = if (isTagalog) "Tunog ng Button at Haptics" else "Interactive Sound & Haptics",
            icon = Icons.AutoMirrored.Filled.VolumeUp,
            accentColor = SuccessGreen,
            isDarkTheme = isDarkTheme
        ) {
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
                        text = if (isTagalog) "Tunog sa Pagpindot ng Button" else "Button Click Sound Effects",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = textPrimary
                    )
                    Text(
                        text = if (isTagalog) "Synthesized audio + Villager voice effects" else "Synthesized button chimes & chibi villager voices",
                        fontSize = 12.sp,
                        color = textSecondary
                    )
                }
                Switch(
                    checked = soundEnabled,
                    onCheckedChange = {
                        soundEnabled = it
                        UiSoundManager.soundEnabled = it
                        UiSoundManager.savePrefs(context)
                        if (it) UiSoundManager.playToggle(context, true)
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = SuccessGreen
                    )
                )
            }

            Spacer(Modifier.height(10.dp))

            Text(
                text = if (isTagalog) "Uri ng Tunog (Pindutin para marinig)" else "Sound Profile (Tap to preview)",
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = textSecondary
            )
            Spacer(Modifier.height(8.dp))

            val profiles = SoundProfile.values()
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                for (r in 0..1) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        for (c in 0..1) {
                            val prof = profiles[r * 2 + c]
                            val isSel = selectedProfile == prof
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSel) SuccessGreen.copy(alpha = 0.18f) else Color.Transparent,
                                modifier = Modifier
                                    .weight(1f)
                                    .border(
                                        width = if (isSel) 1.5.dp else 1.dp,
                                        color = if (isSel) SuccessGreen else border,
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .interactiveBounceClick {
                                        selectedProfile = prof
                                        UiSoundManager.profile = prof
                                        UiSoundManager.savePrefs(context)
                                        UiSoundManager.playClick(context)
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = if (isSel) Icons.Default.GraphicEq else Icons.Default.MusicNote,
                                        contentDescription = null,
                                        tint = if (isSel) SuccessGreen else textSecondary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(Modifier.width(6.dp))
                                    Text(
                                        text = if (isTagalog) prof.labelTl else prof.labelEn,
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSel) textPrimary else textSecondary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(10.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.AutoMirrored.Filled.VolumeDown, contentDescription = null, tint = textSecondary, modifier = Modifier.size(18.dp))
                Slider(
                    value = soundVolume,
                    onValueChange = {
                        soundVolume = it
                        UiSoundManager.volume = it
                    },
                    onValueChangeFinished = {
                        UiSoundManager.savePrefs(context)
                        UiSoundManager.playClick(context)
                    },
                    valueRange = 0.1f..1.0f,
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 8.dp)
                )
                Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = null, tint = accent, modifier = Modifier.size(18.dp))
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = border)

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
                        text = if (isTagalog) "Haptic Vibration" else "Haptic Tactile Feedback",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = textPrimary
                    )
                    Text(
                        text = if (isTagalog) "Bahagyang pag-vibrate kapag pumipindot" else "Subtle vibration pulse on button press",
                        fontSize = 12.sp,
                        color = textSecondary
                    )
                }
                Switch(
                    checked = hapticsEnabled,
                    onCheckedChange = {
                        hapticsEnabled = it
                        UiSoundManager.hapticsEnabled = it
                        UiSoundManager.savePrefs(context)
                        UiSoundManager.playToggle(context, it)
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = primary
                    )
                )
            }
        }

        // ================= 4. AI ENGINE EXTRACTION MANAGER =================
        BorderBeamBox(
            active = true,
            cornerRadius = 20.dp,
            beamColor = primary,
            secondaryColor = accent,
            baseBorderColor = border,
            backgroundColor = surface,
            modifier = Modifier
                .fillMaxWidth()
                .interactiveBounceClick { onNavigateToModelManager() }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ThinkingOrb(
                    size = 38.dp,
                    state = "working",
                    primaryColor = primary,
                    accentColor = accent
                )
                Spacer(Modifier.width(14.dp))
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp)
                ) {
                    Text(
                        text = if (isTagalog) "Extracting AI Model (Qwen 2.5)" else "Extracting AI Model (Qwen 2.5)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = if (isTagalog)
                            "Pamahalaan ang 350 MB offline neural weights"
                        else
                            "Extract & verify 350 MB offline neural weights",
                        fontSize = 12.sp,
                        color = textSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = accent)
            }
        }

        // ================= 5. CHIBI MASCOT GUIDE (GRACE, KIAN & SHOUKO) + TEAM INBOX =================
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = surface,
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, accent.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                .interactiveBounceClick { onOpenMascotTutorial() }
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.chibi_grace),
                        contentDescription = "Grace",
                        modifier = Modifier.size(46.dp)
                    )
                    Image(
                        painter = painterResource(id = R.drawable.chibi_kian),
                        contentDescription = "Kian",
                        modifier = Modifier.size(46.dp)
                    )
                    Image(
                        painter = painterResource(id = R.drawable.chibi_shouko),
                        contentDescription = "Shouko",
                        modifier = Modifier.size(46.dp)
                    )
                    Spacer(Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isTagalog) "Gabay sa App (Grace, Kian & Shouko)" else "Chibi Mascot Guide (Grace, Kian & Shouko)",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 13.5.sp,
                            color = textPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = if (isTagalog)
                                "Buksan ang interactive tutorial na may FSL at Villager sounds!"
                            else
                                "Interactive walkthrough of every feature with Villager voice sounds!",
                            fontSize = 11.5.sp,
                            color = textSecondary
                        )
                    }
                }

                Spacer(Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onOpenMascotTutorial,
                        colors = ButtonDefaults.buttonColors(containerColor = primary),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp)
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color.White, modifier = Modifier.size(15.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = if (isTagalog) "Simulan ang Gabay" else "Launch Guide",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            maxLines = 1,
                            softWrap = false
                        )
                    }

                    OutlinedButton(
                        onClick = onOpenTeamInbox,
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, accent),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp)
                    ) {
                        Icon(Icons.Default.Inbox, contentDescription = null, tint = accent, modifier = Modifier.size(15.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = if (isTagalog) "Team Inbox" else "Team Inbox",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = accent,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }
            }
        }

        // Quick Actions Row: About Team & Replay Cinematic Intro
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = surface,
                modifier = Modifier
                    .weight(1f)
                    .border(1.dp, border, RoundedCornerShape(18.dp))
                    .interactiveBounceClick { onNavigateToAbout() }
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Groups, contentDescription = null, tint = primary, modifier = Modifier.size(22.dp))
                    Spacer(Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isTagalog) "Tungkol sa Team" else "About Team",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = textPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "Project Nexus",
                            fontSize = 11.sp,
                            color = textSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Surface(
                shape = RoundedCornerShape(18.dp),
                color = surface,
                modifier = Modifier
                    .weight(1f)
                    .border(1.dp, border, RoundedCornerShape(18.dp))
                    .interactiveBounceClick { onReplayIntro() }
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.MovieFilter, contentDescription = null, tint = accent, modifier = Modifier.size(22.dp))
                    Spacer(Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isTagalog) "Ulitin ang Intro" else "Replay Intro",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = textPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "Ascendens",
                            fontSize = 11.sp,
                            color = textSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(28.dp))
    }
}

@Composable
private fun SettingsSectionCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accentColor: Color,
    isDarkTheme: Boolean,
    content: @Composable ColumnScope.() -> Unit
) {
    val surface = IWaniColors.surface(isDarkTheme)
    val border = IWaniColors.border(isDarkTheme)
    val textPrimary = IWaniColors.textPrimary(isDarkTheme)

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = surface,
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, border, RoundedCornerShape(20.dp))
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(accentColor.copy(alpha = 0.16f), RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(18.dp))
                }
                Spacer(Modifier.width(10.dp))
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(Modifier.height(14.dp))
            content()
        }
    }
}

@Composable
private fun LanguageOptionTile(
    modifier: Modifier = Modifier,
    flagRes: Int,
    label: String,
    selected: Boolean,
    primaryColor: Color,
    borderColor: Color,
    textPrimary: Color,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = if (selected) primaryColor.copy(alpha = 0.2f) else Color.Transparent,
        modifier = modifier
            .border(
                width = if (selected) 1.5.dp else 1.dp,
                color = if (selected) primaryColor else borderColor,
                shape = RoundedCornerShape(14.dp)
            )
            .interactiveBounceClick(isToggle = true, onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = flagRes),
                contentDescription = label,
                modifier = Modifier
                    .size(width = 22.dp, height = 15.dp)
                    .clip(RoundedCornerShape(3.dp))
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = label,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = textPrimary,
                maxLines = 1,
                softWrap = false
            )
        }
    }
}
