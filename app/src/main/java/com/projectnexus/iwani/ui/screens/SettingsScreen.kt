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
    var fslAcceptanceSeconds by remember { mutableFloatStateOf(UiSoundManager.fslAcceptanceSeconds) }

    val scrollState = rememberScrollState()
    val activeTargetId = TutorialSpotlightRegistry.activeTargetId
    LaunchedEffect(activeTargetId) {
        when (activeTargetId) {
            "settings_header", "settings_appearance" -> scrollState.animateScrollTo(0)
            "settings_typography" -> scrollState.animateScrollTo((scrollState.maxValue * 0.28f).toInt())
            "settings_fsl_speed" -> scrollState.animateScrollTo((scrollState.maxValue * 0.55f).toInt())
            "settings_sound" -> scrollState.animateScrollTo((scrollState.maxValue * 0.80f).toInt())
            "settings_ai" -> scrollState.animateScrollTo(scrollState.maxValue)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bg)
            .padding(horizontal = 20.dp)
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(Modifier.height(8.dp))

        // Header — left Column uses weight(1f) so MetalBadge NEVER wraps or compresses
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .tutorialSpotlightTarget("settings_header"),
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
                    modifier = Modifier.autoSlideIfOverflow()
                )
                Text(
                    text = if (isTagalog)
                        "I-customize ang tema, tunog, font at AI engine"
                    else
                        "Customize theme, sound, font & AI engine",
                    fontSize = 12.sp,
                    color = textSecondary,
                    maxLines = 1,
                    modifier = Modifier.autoSlideIfOverflow()
                )
            }
            MetalBadge(
                text = "PROJECT NEXUS",
                isDark = isDarkTheme,
                onClick = onNavigateToAbout
            )
        }

        // ================= 1. APPEARANCE & LANGUAGE =================
        SettingsSectionCard(
            title = if (isTagalog) "Hitsura at Wika" else "Appearance & Language",
            icon = Icons.Default.Palette,
            accentColor = primary,
            isDarkTheme = isDarkTheme,
            modifier = Modifier.tutorialSpotlightTarget("settings_appearance")
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
                        text = if (isTagalog) "Lumipat sa pagitan ng Dark Mode at Day Mode" else "Switch between Dark Mode and Day Mode",
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
            isDarkTheme = isDarkTheme,
            modifier = Modifier.tutorialSpotlightTarget("settings_typography")
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
                                        modifier = Modifier.autoSlideIfOverflow()
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
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 14.sp,
                    color = accent
                )
            }

            Slider(
                value = fontScale,
                onValueChange = { newScale ->
                    val rounded = (kotlin.math.round(newScale * 20f) / 20f).coerceIn(0.85f, 1.25f)
                    if (kotlin.math.abs(rounded - fontScale) >= 0.02f) {
                        UiSoundManager.playClick(context)
                        onFontScaleChange(rounded)
                    }
                },
                valueRange = 0.85f..1.25f,
                steps = 7,
                colors = SliderDefaults.colors(
                    thumbColor = accent,
                    activeTrackColor = primary,
                    inactiveTrackColor = border
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val scales = listOf(
                    0.85f to (if (isTagalog) "85% Maliit" else "85% Small"),
                    1.00f to (if (isTagalog) "100% Normal" else "100% Default"),
                    1.15f to (if (isTagalog) "115% Malaki" else "115% Large"),
                    1.25f to "125% XL"
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
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSel) Color.White else textSecondary,
                            maxLines = 1,
                            softWrap = false,
                            modifier = Modifier
                                .padding(horizontal = 6.dp, vertical = 8.dp)
                                .autoSlideIfOverflow()
                        )
                    }
                }
            }

            Spacer(Modifier.height(10.dp))

            // Live Text Scale Preview Strip
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = primary.copy(alpha = 0.10f),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, border, RoundedCornerShape(12.dp))
            ) {
                Text(
                    text = if (isTagalog)
                        "Preview (${(fontScale * 100).toInt()}%): Kumusta! Ganito kalaki ang teksto sa buong iWani app."
                    else
                        "Live Preview (${(fontScale * 100).toInt()}%): Hello! All text across iWani scales to this size.",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = textPrimary,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
                )
            }
        }

        // ================= 3. FSL ALPHABET ACCEPTANCE SPEED =================
        SettingsSectionCard(
            title = if (isTagalog) "Bilis ng Pagtanggap ng FSL Titik" else "FSL Alphabet Acceptance Speed",
            icon = Icons.Default.Speed,
            accentColor = Color(0xFF38BDF8),
            isDarkTheme = isDarkTheme,
            modifier = Modifier.tutorialSpotlightTarget("settings_fsl_speed")
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f).padding(end = 10.dp)) {
                    Text(
                        text = if (isTagalog) "Oras ng Pag-hold sa Senyas" else "Sign Hold Acceptance Time",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = textPrimary
                    )
                    Text(
                        text = if (isTagalog)
                            "1.0s ang pinakamabilis; pwedeng bagalan hanggang 4.0s (Default: 2.5s sa gitna)"
                        else
                            "1.0s is the fastest; slow down up to 4.0s (Default: 2.5s in the middle)",
                        fontSize = 12.sp,
                        color = textSecondary
                    )
                }
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = accent.copy(alpha = 0.18f),
                    modifier = Modifier.border(1.dp, accent, RoundedCornerShape(10.dp))
                ) {
                    Text(
                        text = String.format("%.1fs", fslAcceptanceSeconds),
                        fontWeight = FontWeight.Black,
                        fontSize = 14.sp,
                        color = accent,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }
            }

            Spacer(Modifier.height(6.dp))

            Slider(
                value = fslAcceptanceSeconds,
                onValueChange = { raw ->
                    val stepped = (kotlin.math.round(raw * 2f) / 2f).coerceIn(1.0f, 4.0f)
                    if (kotlin.math.abs(stepped - fslAcceptanceSeconds) >= 0.1f) {
                        fslAcceptanceSeconds = stepped
                        UiSoundManager.fslAcceptanceSeconds = stepped
                        UiSoundManager.savePrefs(context)
                        UiSoundManager.playClick(context)
                    }
                },
                valueRange = 1.0f..4.0f,
                steps = 5,
                colors = SliderDefaults.colors(
                    thumbColor = accent,
                    activeTrackColor = primary,
                    inactiveTrackColor = border
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val speedPresets = listOf(
                    1.0f to (if (isTagalog) "1.0s Mabilis" else "1.0s Fastest"),
                    2.5f to (if (isTagalog) "2.5s Gitna (Default)" else "2.5s Middle (Default)"),
                    4.0f to (if (isTagalog) "4.0s Mabagal" else "4.0s Slowest")
                )
                speedPresets.forEach { (secVal, label) ->
                    val isSel = kotlin.math.abs(fslAcceptanceSeconds - secVal) < 0.15f
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSel) primary else Color.Transparent,
                        modifier = Modifier
                            .weight(1f)
                            .border(1.dp, if (isSel) primary else border, RoundedCornerShape(12.dp))
                            .interactiveBounceClick {
                                fslAcceptanceSeconds = secVal
                                UiSoundManager.fslAcceptanceSeconds = secVal
                                UiSoundManager.savePrefs(context)
                            }
                    ) {
                        Text(
                            text = label,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSel) Color.White else textSecondary,
                            maxLines = 1,
                            softWrap = false,
                            modifier = Modifier
                                .padding(horizontal = 6.dp, vertical = 8.dp)
                                .autoSlideIfOverflow()
                        )
                    }
                }
            }
        }

        // ================= 4. INTERACTIVE SOUND & HAPTICS =================
        SettingsSectionCard(
            title = if (isTagalog) "Tunog ng Button at Haptics" else "Interactive Sound & Haptics",
            icon = Icons.AutoMirrored.Filled.VolumeUp,
            accentColor = SuccessGreen,
            isDarkTheme = isDarkTheme,
            modifier = Modifier.tutorialSpotlightTarget("settings_sound")
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
                                        softWrap = false,
                                        modifier = Modifier.autoSlideIfOverflow()
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
                        text = if (isTagalog) "Haptic Vibration (Button at Boses/TTS)" else "Haptic Tactile & Speech Vibration",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = textPrimary
                    )
                    Text(
                        text = if (isTagalog)
                            "Nagba-vibrate kapag pumipindot at habang nagsasalita ang SOS/TTS, at hihinto pagkatapos"
                        else
                            "Vibrates on button clicks & continuously while SOS/TTS is speaking so Deaf users know",
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
                .tutorialSpotlightTarget("settings_ai")
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
                        modifier = Modifier.autoSlideIfOverflow()
                    )
                    Text(
                        text = if (isTagalog)
                            "Pamahalaan ang 350 MB offline neural weights"
                        else
                            "Extract & verify 350 MB offline neural weights",
                        fontSize = 12.sp,
                        color = textSecondary,
                        maxLines = 1,
                        modifier = Modifier.autoSlideIfOverflow()
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
                    Row(horizontalArrangement = Arrangement.spacedBy((-6).dp)) {
                        ChibiFaceAvatar(
                            drawableRes = R.drawable.chibi_grace,
                            contentDescription = "Grace",
                            size = 42.dp,
                            borderColor = Color(0xFF38BDF8),
                            backgroundColor = Color(0xFF0F172A)
                        )
                        ChibiFaceAvatar(
                            drawableRes = R.drawable.chibi_kian,
                            contentDescription = "Kian",
                            size = 42.dp,
                            borderColor = Color(0xFFFBBF24),
                            backgroundColor = Color(0xFF0F172A)
                        )
                        ChibiFaceAvatar(
                            drawableRes = R.drawable.chibi_shouko,
                            contentDescription = "Shouko",
                            size = 42.dp,
                            borderColor = Color(0xFFF472B6),
                            backgroundColor = Color(0xFF0F172A)
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isTagalog) "Gabay sa App (Grace, Kian & Shouko)" else "Chibi Mascot Guide (Grace, Kian & Shouko)",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 13.5.sp,
                            color = textPrimary,
                            maxLines = 1,
                            modifier = Modifier.autoSlideIfOverflow()
                        )
                        Text(
                            text = if (isTagalog)
                                "Pumili ng Bahagi (Part 1–6) o simulan ang buong walkthrough!"
                            else
                                "Pick any Tutorial Part (Part 1–6) or start the full walkthrough!",
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
                            text = if (isTagalog) "Buksan ang Gabay" else "Open Walkthrough",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            maxLines = 1,
                            softWrap = false,
                            modifier = Modifier.autoSlideIfOverflow()
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
                            softWrap = false,
                            modifier = Modifier.autoSlideIfOverflow()
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
                            modifier = Modifier.autoSlideIfOverflow()
                        )
                        Text(
                            text = "Project Nexus",
                            fontSize = 11.sp,
                            color = textSecondary,
                            maxLines = 1,
                            modifier = Modifier.autoSlideIfOverflow()
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
                            modifier = Modifier.autoSlideIfOverflow()
                        )
                        Text(
                            text = "Ascendens",
                            fontSize = 11.sp,
                            color = textSecondary,
                            maxLines = 1,
                            modifier = Modifier.autoSlideIfOverflow()
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
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    val surface = IWaniColors.surface(isDarkTheme)
    val border = IWaniColors.border(isDarkTheme)
    val textPrimary = IWaniColors.textPrimary(isDarkTheme)

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = surface,
        modifier = modifier
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
                    softWrap = false,
                    modifier = Modifier.autoSlideIfOverflow()
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
