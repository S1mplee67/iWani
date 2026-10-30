package com.projectnexus.iwani.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.projectnexus.iwani.AppLanguage
import com.projectnexus.iwani.emergency.EmergencySosManager
import com.projectnexus.iwani.nlp.QwenFslTranslator
import com.projectnexus.iwani.ui.components.TutorialSpotlightRegistry
import com.projectnexus.iwani.ui.components.autoSlideIfOverflow
import com.projectnexus.iwani.ui.components.interactiveBounceClick
import com.projectnexus.iwani.ui.components.tutorialSpotlightTarget
import com.projectnexus.iwani.ui.sound.UiSoundManager
import com.projectnexus.iwani.ui.theme.*

enum class GlossCategory {
    FREQUENT,
    DAILY,
    GREETINGS,
    EMERGENCY,
    MEDICAL,
    CAMPUS,
    COMMUTE
}

data class QuickWordItem(
    val glossKey: String,
    val labelTl: String,
    val labelEn: String
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CommunicationScreen(
    sosManager: EmergencySosManager,
    appLanguage: AppLanguage = AppLanguage.ENGLISH,
    isDarkTheme: Boolean = true
) {
    val isTagalog = appLanguage.isTagalog
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    var selectedCategory by remember { mutableStateOf(GlossCategory.FREQUENT) }
    var inputText by remember { mutableStateOf("PAGKAIN") }
    var translatedText by remember { mutableStateOf("") }

    val bg = IWaniColors.background(isDarkTheme)
    val surface = IWaniColors.surface(isDarkTheme)
    val border = IWaniColors.border(isDarkTheme)
    val textPrimary = IWaniColors.textPrimary(isDarkTheme)
    val textSecondary = IWaniColors.textSecondary(isDarkTheme)
    val accent = IWaniColors.accent(isDarkTheme)
    val primary = IWaniColors.primary(isDarkTheme)

    LaunchedEffect(inputText, isTagalog) {
        val tokens = inputText.split("\\s+".toRegex()).filter { it.isNotBlank() }
        translatedText = QwenFslTranslator.translate(tokens, isTagalog)
    }

    val quickWordsByCategory = remember {
        mapOf(
            GlossCategory.FREQUENT to listOf(
                QuickWordItem("PAGKAIN", "Pagkain", "Food / Pagkain"),
                QuickWordItem("TUBIG", "Tubig", "Water / Tubig"),
                QuickWordItem("BILI", "Bili", "Buy / Bili"),
                QuickWordItem("PAHINGA", "Pahinga", "Rest / Pahinga"),
                QuickWordItem("TULONG", "Tulong", "Help / Tulong"),
                QuickWordItem("SALAMAT", "Salamat", "Thank You"),
                QuickWordItem("OPO", "Opo / Oo", "Yes / Opo"),
                QuickWordItem("HINDI PO", "Hindi po", "No / Hindi po"),
                QuickWordItem("MAGKANO", "Magkano", "How Much"),
                QuickWordItem("SAAN ANG CR", "Saan ang CR", "Restroom / CR"),
                QuickWordItem("SANDALI LANG", "Sandali lang", "Wait a Moment"),
                QuickWordItem("PASENSYA NA", "Pasensya na", "Sorry / Excuse Me"),
                QuickWordItem("KUMUSTA", "Kumusta", "How Are You"),
                QuickWordItem("UWI NA AKO", "Uwi na ako", "Heading Home")
            ),
            GlossCategory.DAILY to listOf(
                QuickWordItem("GUSTO KAIN AKO", "Pagkain", "Eat / Food"),
                QuickWordItem("INOM TUBIG KAILANGAN", "Tubig", "Drink Water"),
                QuickWordItem("BILI PAGKAIN MAGKANO", "Bili", "Buy / Price"),
                QuickWordItem("PAHINGA PAGOD AKO", "Pahinga", "Rest / Tired"),
                QuickWordItem("GUTOM", "Gutom", "Hungry"),
                QuickWordItem("UHAW", "Uhaw", "Thirsty"),
                QuickWordItem("SAAN CR PALIKURAN", "CR / Banyo", "Restroom"),
                QuickWordItem("TAWAD PO", "Tawad po", "Discount"),
                QuickWordItem("SUKLI PO", "Sukli po", "My Change"),
                QuickWordItem("MATULOG", "Matulog", "Sleep"),
                QuickWordItem("TRABAHO", "Trabaho", "Work"),
                QuickWordItem("BAHAY", "Bahay", "Home")
            ),
            GlossCategory.GREETINGS to listOf(
                QuickWordItem("SALAMAT TULONG IYO", "Salamat po", "Thank You"),
                QuickWordItem("MAGANDANG UMAGA PO", "Magandang Umaga", "Good Morning"),
                QuickWordItem("MAGANDANG HAPON PO", "Magandang Hapon", "Good Afternoon"),
                QuickWordItem("MAGANDANG GABI PO", "Magandang Gabi", "Good Evening"),
                QuickWordItem("KUMUSTA KA", "Kumusta", "Hello / How Are You"),
                QuickWordItem("PAALAM INGAT IYO", "Paalam", "Goodbye"),
                QuickWordItem("INGAT PO", "Ingat po", "Take Care"),
                QuickWordItem("WALANG ANUMAN", "Walang Anuman", "You're Welcome"),
                QuickWordItem("PASENSIYA NA PO", "Pasensya po", "Apologies")
            ),
            GlossCategory.EMERGENCY to listOf(
                QuickWordItem("TULONG BINGI AKO", "Tulong (Bingi)", "Help (Deaf)"),
                QuickWordItem("PUNTA OSPITAL KAILANGAN", "Ospital", "Hospital"),
                QuickWordItem("TAWAG PULIS NGAYON", "Pulis / Tanod", "Police / Security"),
                QuickWordItem("MAY SUNOG DITO", "Sunog", "Fire Alert"),
                QuickWordItem("AKSIDENTE DAAN TULONG", "Aksidente", "Road Accident"),
                QuickWordItem("TAWAGAN PAMILYA", "Tawagan Pamilya", "Call Family"),
                QuickWordItem("BINGI AKO", "Pipi at Bingi", "I Am Deaf"),
                QuickWordItem("NAWAWALA AKO BAHAY", "Naliligaw", "I Am Lost")
            ),
            GlossCategory.MEDICAL to listOf(
                QuickWordItem("MASAKIT ULO GAMOT", "Sakit ng Ulo", "Headache"),
                QuickWordItem("MASAKIT TIYAN DOKTOR", "Sakit ng Tiyan", "Stomachache"),
                QuickWordItem("HINDI MAKAHINGA TULONG", "Hirap Huminga", "Hard to Breathe"),
                QuickWordItem("NAHIHILO AKO", "Nahihilo", "Feeling Dizzy"),
                QuickWordItem("MAY LAGNAT AKO", "May Lagnat", "Have a Fever"),
                QuickWordItem("KAILANGAN GAMOT", "Gamot", "Need Medicine"),
                QuickWordItem("KAILANGAN DOKTOR", "Doktor", "Need a Doctor")
            ),
            GlossCategory.CAMPUS to listOf(
                QuickWordItem("GURO PROF TANONG", "Guro / Prof", "Ask Professor"),
                QuickWordItem("KAKLASE TULONG ARAL", "Kaklase", "Classmate Help"),
                QuickWordItem("PAGSUSULIT EXAM NGAYON", "Exam / Pagsusulit", "Exam Today"),
                QuickWordItem("SAAN LIBRARY", "Library", "Where is Library"),
                QuickWordItem("ENROLLMENT REGISTRAR", "Enrollment", "Registrar / Enroll"),
                QuickWordItem("COMPUTER LAB", "Computer Lab", "Computer Lab")
            ),
            GlossCategory.COMMUTE to listOf(
                QuickWordItem("BAYAD PO PAMASAHE", "Bayad po", "Fare Payment"),
                QuickWordItem("PARA PO SA TABI", "Para po", "Stop Here / Pull Over"),
                QuickWordItem("SAAN ANG SAKAYAN", "Sakayan", "Terminal / Ride"),
                QuickWordItem("MAGKANO PAMASAHE", "Magkano Pamasahe", "How Much Fare"),
                QuickWordItem("LILIGAW NAWAWALA BAHAY", "Naliligaw po", "Lost Directions")
            )
        )
    }

    val scrollState = rememberScrollState()
    val activeTargetId = TutorialSpotlightRegistry.activeTargetId
    LaunchedEffect(activeTargetId) {
        when (activeTargetId) {
            "translate_categories", "translate_quick_words", "translate_output_box" -> scrollState.animateScrollTo(0)
            "translate_ai_card" -> scrollState.animateScrollTo(scrollState.maxValue)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bg)
            .padding(horizontal = 20.dp)
            .verticalScroll(scrollState)
    ) {
        Spacer(Modifier.height(14.dp))

        Text(
            text = if (isTagalog) "Komunikasyon at Pagsasalin" else "Communication & Translation",
            fontSize = 24.sp,
            fontWeight = FontWeight.Black,
            color = textPrimary,
            maxLines = 1,
            modifier = Modifier.autoSlideIfOverflow()
        )
        Text(
            text = if (isTagalog)
                "Pumili ng madalas gamiting salita o mag-type para sa natural na salin"
            else
                "Tap frequently used quick words or type below for natural speech",
            fontSize = 12.5.sp,
            color = textSecondary,
            maxLines = 1,
            modifier = Modifier
                .padding(top = 3.dp, bottom = 12.dp)
                .autoSlideIfOverflow()
        )

        // ================= TOP CATEGORY BAR (WITH FREQUENTLY USED ON TOP) =================
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .tutorialSpotlightTarget("translate_categories")
        ) {
            Text(
                text = if (isTagalog) "MGA KATEGORYA (MADALAS GAMITIN):" else "CATEGORIES (FREQUENTLY USED):",
                fontSize = 10.5.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.1.sp,
                color = accent,
                maxLines = 1,
                modifier = Modifier.autoSlideIfOverflow()
            )
            Spacer(Modifier.height(6.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CategoryChipItem(
                    label = if (isTagalog) "★ Madalas Gamitin" else "★ Frequently Used",
                    selected = selectedCategory == GlossCategory.FREQUENT,
                    activeColor = accent,
                    activeTextColor = Color(0xFF0F172A),
                    surface = surface,
                    textSecondary = textSecondary,
                    onClick = {
                        selectedCategory = GlossCategory.FREQUENT
                        inputText = "PAGKAIN"
                    }
                )
                CategoryChipItem(
                    label = if (isTagalog) "Araw-araw" else "Daily Needs",
                    selected = selectedCategory == GlossCategory.DAILY,
                    activeColor = primary,
                    activeTextColor = Color.White,
                    surface = surface,
                    textSecondary = textSecondary,
                    onClick = {
                        selectedCategory = GlossCategory.DAILY
                        inputText = "GUSTO KAIN AKO"
                    }
                )
                CategoryChipItem(
                    label = if (isTagalog) "Pagbati" else "Greetings",
                    selected = selectedCategory == GlossCategory.GREETINGS,
                    activeColor = SuccessGreen,
                    activeTextColor = Color.White,
                    surface = surface,
                    textSecondary = textSecondary,
                    onClick = {
                        selectedCategory = GlossCategory.GREETINGS
                        inputText = "MAGANDANG UMAGA PO"
                    }
                )
                CategoryChipItem(
                    label = if (isTagalog) "Saklolo (SOS)" else "Emergency",
                    selected = selectedCategory == GlossCategory.EMERGENCY,
                    activeColor = EmergencyRed,
                    activeTextColor = Color.White,
                    surface = surface,
                    textSecondary = textSecondary,
                    onClick = {
                        selectedCategory = GlossCategory.EMERGENCY
                        inputText = "TULONG BINGI AKO"
                    }
                )
                CategoryChipItem(
                    label = if (isTagalog) "Kalusugan" else "Medical",
                    selected = selectedCategory == GlossCategory.MEDICAL,
                    activeColor = TealPrimary,
                    activeTextColor = Color.White,
                    surface = surface,
                    textSecondary = textSecondary,
                    onClick = {
                        selectedCategory = GlossCategory.MEDICAL
                        inputText = "MASAKIT ULO GAMOT"
                    }
                )
                CategoryChipItem(
                    label = if (isTagalog) "Paaralan" else "School",
                    selected = selectedCategory == GlossCategory.CAMPUS,
                    activeColor = Color(0xFF6366F1),
                    activeTextColor = Color.White,
                    surface = surface,
                    textSecondary = textSecondary,
                    onClick = {
                        selectedCategory = GlossCategory.CAMPUS
                        inputText = "GURO PROF TANONG"
                    }
                )
                CategoryChipItem(
                    label = if (isTagalog) "Biyahe" else "Commute",
                    selected = selectedCategory == GlossCategory.COMMUTE,
                    activeColor = Color(0xFFF97316),
                    activeTextColor = Color.White,
                    surface = surface,
                    textSecondary = textSecondary,
                    onClick = {
                        selectedCategory = GlossCategory.COMMUTE
                        inputText = "BAYAD PO PAMASAHE"
                    }
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        // ================= MULTI-CHIP QUICK WORDS SECTION =================
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .tutorialSpotlightTarget("translate_quick_words")
        ) {
            Text(
                text = if (isTagalog) "QUICK WORDS (PINDUTIN PARA ISALIN):" else "QUICK WORDS (TAP TO TRANSLATE):",
                fontSize = 10.5.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.1.sp,
                color = textSecondary,
                maxLines = 1,
                modifier = Modifier.autoSlideIfOverflow()
            )
            Spacer(Modifier.height(6.dp))

            val currentWords = quickWordsByCategory[selectedCategory].orEmpty()
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                currentWords.forEach { item ->
                    val isSelected = inputText.equals(item.glossKey, ignoreCase = true)
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) primary.copy(alpha = 0.22f) else surface,
                        border = BorderStroke(
                            width = if (isSelected) 1.5.dp else 1.dp,
                            color = if (isSelected) primary else border
                        ),
                        modifier = Modifier.interactiveBounceClick {
                            inputText = item.glossKey
                        }
                    ) {
                        Text(
                            text = if (isTagalog) item.labelTl else item.labelEn,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.SemiBold,
                            color = if (isSelected) accent else textPrimary,
                            maxLines = 1,
                            softWrap = false,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(14.dp))

        // ================= INPUT BOX =================
        OutlinedTextField(
            value = inputText,
            onValueChange = { inputText = it },
            label = { Text(if (isTagalog) "Mag-input dito" else "Input here") },
            placeholder = {
                Text(
                    if (isTagalog)
                        "Hal: PAGKAIN, TUBIG, BILI, PAHINGA..."
                    else
                        "e.g. PAGKAIN, TUBIG, BILI, PAHINGA..."
                )
            },
            trailingIcon = {
                if (inputText.isNotEmpty()) {
                    IconButton(onClick = {
                        UiSoundManager.playClick(context)
                        inputText = ""
                    }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Clear Input",
                            tint = textSecondary
                        )
                    }
                }
            },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .tutorialSpotlightTarget("translate_output_box"),
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = surface,
                unfocusedContainerColor = surface,
                focusedTextColor = textPrimary,
                unfocusedTextColor = textPrimary,
                focusedBorderColor = accent,
                unfocusedBorderColor = border,
                focusedLabelColor = accent,
                unfocusedLabelColor = textSecondary
            )
        )

        Spacer(Modifier.height(16.dp))

        // ================= AI REFINED SENTENCE OUTPUT CARD =================
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = surface),
            modifier = Modifier
                .fillMaxWidth()
                .tutorialSpotlightTarget("translate_ai_card")
                .border(1.dp, border, RoundedCornerShape(20.dp))
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = accent,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = if (isTagalog) "QWEN 2.5 • NATURAL NA SALIN" else "QWEN 2.5 • NATURAL TRANSLATION",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = accent,
                            letterSpacing = 0.8.sp,
                            maxLines = 1,
                            softWrap = false,
                            modifier = Modifier.autoSlideIfOverflow()
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = accent.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = if (isTagalog) "Tagalog (PH)" else "English (US)",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = accent,
                            maxLines = 1,
                            softWrap = false,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))

                Text(
                    text = if (translatedText.isNotEmpty()) {
                        "\"$translatedText\""
                    } else {
                        if (isTagalog) "Pumili ng quick word o mag-type sa itaas..." else "Select a quick word or type above..."
                    },
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = textPrimary,
                    lineHeight = 24.sp
                )

                Spacer(Modifier.height(16.dp))

                // Action Buttons Row — single-line non-wrapping text so it never clips!
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            UiSoundManager.playClick(context)
                            val toSpeak = translatedText.ifBlank { inputText }
                            if (toSpeak.isNotBlank()) {
                                sosManager.speak(toSpeak)
                            } else {
                                Toast.makeText(
                                    context,
                                    if (isTagalog) "Pumili o mag-type muna ng salita." else "Select or type words first.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = primary),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                            contentDescription = "Speak",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = if (isTagalog) "Iparinig sa Boses" else "Speak Out Loud",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = Color.White,
                            maxLines = 1,
                            softWrap = false,
                            modifier = Modifier.autoSlideIfOverflow()
                        )
                    }

                    OutlinedButton(
                        onClick = {
                            UiSoundManager.playClick(context)
                            val toCopy = translatedText.ifBlank { inputText }
                            if (toCopy.isNotBlank()) {
                                clipboardManager.setText(AnnotatedString(toCopy))
                                Toast.makeText(
                                    context,
                                    if (isTagalog) "Na-kopya na ang salin sa clipboard!" else "Translation copied to clipboard!",
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                Toast.makeText(
                                    context,
                                    if (isTagalog) "Walang salin na kokopyahin." else "No translation to copy.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                        modifier = Modifier.height(48.dp),
                        border = BorderStroke(1.dp, border),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = textPrimary)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy",
                            tint = accent,
                            modifier = Modifier.size(17.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = if (isTagalog) "Kopyahin" else "Copy",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.5.sp,
                            color = textPrimary,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(28.dp))
    }
}

@Composable
private fun CategoryChipItem(
    label: String,
    selected: Boolean,
    activeColor: Color,
    activeTextColor: Color,
    surface: Color,
    textSecondary: Color,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    FilterChip(
        selected = selected,
        onClick = {
            UiSoundManager.playClick(context)
            onClick()
        },
        label = {
            Text(
                text = label,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.SemiBold,
                maxLines = 1,
                softWrap = false
            )
        },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = activeColor,
            selectedLabelColor = activeTextColor,
            containerColor = surface,
            labelColor = textSecondary
        )
    )
}
