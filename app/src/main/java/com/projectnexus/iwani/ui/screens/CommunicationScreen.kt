package com.projectnexus.iwani.ui.screens

import android.widget.Toast
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
import com.projectnexus.iwani.nlp.GemmaTagalogNlp
import com.projectnexus.iwani.ui.theme.*

enum class GlossCategory {
    EMERGENCY,
    GREETINGS,
    DAILY,
    MEDICAL
}

@Composable
fun CommunicationScreen(
    sosManager: EmergencySosManager,
    appLanguage: AppLanguage = AppLanguage.ENGLISH,
    isDarkTheme: Boolean = true
) {
    val isTagalog = appLanguage.isTagalog
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    var selectedCategory by remember { mutableStateOf(GlossCategory.EMERGENCY) }
    var inputText by remember { mutableStateOf("TULONG BINGI AKO") }
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
        translatedText = com.projectnexus.iwani.nlp.QwenFslTranslator.translate(tokens, isTagalog)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bg)
            .padding(horizontal = 20.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(Modifier.height(16.dp))

        Text(
            text = if (isTagalog) "Komunikasyon & Pagsasalin" else "Communication & Translation",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = textPrimary
        )
        Text(
            text = if (isTagalog) "Gawing natural na pangungusap ang mga senyas ng FSL" else "Translate FSL sign glosses into fluent speech",
            fontSize = 13.sp,
            color = textSecondary,
            modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
        )

        // Category Filter Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = selectedCategory == GlossCategory.EMERGENCY,
                onClick = {
                    selectedCategory = GlossCategory.EMERGENCY
                    inputText = "TULONG BINGI AKO"
                },
                label = { Text(if (isTagalog) "Saklolo (SOS)" else "Emergency") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = EmergencyRed,
                    selectedLabelColor = Color.White,
                    containerColor = surface,
                    labelColor = textSecondary
                )
            )
            FilterChip(
                selected = selectedCategory == GlossCategory.GREETINGS,
                onClick = {
                    selectedCategory = GlossCategory.GREETINGS
                    inputText = "MAGANDANG UMAGA PO"
                },
                label = { Text(if (isTagalog) "Pagbati" else "Greetings") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = primary,
                    selectedLabelColor = Color.White,
                    containerColor = surface,
                    labelColor = textSecondary
                )
            )
            FilterChip(
                selected = selectedCategory == GlossCategory.DAILY,
                onClick = {
                    selectedCategory = GlossCategory.DAILY
                    inputText = "GUSTO KAIN AKO"
                },
                label = { Text(if (isTagalog) "Araw-araw" else "Daily Needs") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = accent,
                    selectedLabelColor = if (isDarkTheme) Color.Black else Color.White,
                    containerColor = surface,
                    labelColor = textSecondary
                )
            )
            FilterChip(
                selected = selectedCategory == GlossCategory.MEDICAL,
                onClick = {
                    selectedCategory = GlossCategory.MEDICAL
                    inputText = "MASAKIT ULO GAMOT"
                },
                label = { Text(if (isTagalog) "Kalusugan" else "Medical") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = TealPrimary,
                    selectedLabelColor = Color.White,
                    containerColor = surface,
                    labelColor = textSecondary
                )
            )
        }

        Spacer(Modifier.height(14.dp))

        // Preset Quick Chips for Current Category
        val chipColors = SuggestionChipDefaults.suggestionChipColors(
            containerColor = surface,
            labelColor = textPrimary
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            when (selectedCategory) {
                GlossCategory.EMERGENCY -> {
                    SuggestionChip(onClick = { inputText = "TULONG BINGI AKO" }, label = { Text("Tulong") }, colors = chipColors)
                    SuggestionChip(onClick = { inputText = "PUNTA OSPITAL KAILANGAN" }, label = { Text("Ospital") }, colors = chipColors)
                    SuggestionChip(onClick = { inputText = "TAWAG PULIS NGAYON" }, label = { Text("Pulis") }, colors = chipColors)
                    SuggestionChip(onClick = { inputText = "MAY SUNOG DITO" }, label = { Text("Sunog") }, colors = chipColors)
                }
                GlossCategory.GREETINGS -> {
                    SuggestionChip(onClick = { inputText = "SALAMAT TULONG IYO" }, label = { Text("Salamat") }, colors = chipColors)
                    SuggestionChip(onClick = { inputText = "MAGANDANG UMAGA PO" }, label = { Text("Magandang Umaga") }, colors = chipColors)
                    SuggestionChip(onClick = { inputText = "KUMUSTA KA" }, label = { Text("Kumusta") }, colors = chipColors)
                    SuggestionChip(onClick = { inputText = "PAALAM INGAT IYO" }, label = { Text("Paalam") }, colors = chipColors)
                }
                GlossCategory.DAILY -> {
                    SuggestionChip(onClick = { inputText = "GUSTO KAIN AKO" }, label = { Text("Pagkain") }, colors = chipColors)
                    SuggestionChip(onClick = { inputText = "INOM TUBIG KAILANGAN" }, label = { Text("Tubig") }, colors = chipColors)
                    SuggestionChip(onClick = { inputText = "BILI PAGKAIN MAGKANO" }, label = { Text("Bili") }, colors = chipColors)
                    SuggestionChip(onClick = { inputText = "PAHINGA PAGOD AKO" }, label = { Text("Pahinga") }, colors = chipColors)
                }
                GlossCategory.MEDICAL -> {
                    SuggestionChip(onClick = { inputText = "MASAKIT ULO GAMOT" }, label = { Text("Sakit ng Ulo") }, colors = chipColors)
                    SuggestionChip(onClick = { inputText = "MASAKIT TIYAN DOKTOR" }, label = { Text("Sakit ng Tiyan") }, colors = chipColors)
                    SuggestionChip(onClick = { inputText = "NAWAWALA AKO BAHAY" }, label = { Text("Naliligaw") }, colors = chipColors)
                }
            }
        }

        Spacer(Modifier.height(14.dp))

        // Gloss Input Box with instant Clear Trailing Button
        OutlinedTextField(
            value = inputText,
            onValueChange = { inputText = it },
            label = { Text(if (isTagalog) "FSL Sign Gloss Senyas" else "FSL Sign Gloss Tokens") },
            placeholder = { Text(if (isTagalog) "Hal: GUSTO KAIN AKO" else "e.g. GUSTO KAIN AKO") },
            trailingIcon = {
                if (inputText.isNotEmpty()) {
                    IconButton(onClick = { inputText = "" }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Clear Input",
                            tint = textSecondary
                        )
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
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

        Spacer(Modifier.height(20.dp))

        // AI Refined Sentence Card
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = surface),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, border, RoundedCornerShape(20.dp))
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = accent,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = if (isTagalog) "QWEN 2.5 • NATURAL NA SALIN" else "QWEN 2.5 • NATURAL TRANSLATION",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = accent,
                            letterSpacing = 1.sp
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = accent.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = "0.5B Instruct",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = accent,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))

                Text(
                    text = if (translatedText.isNotEmpty()) "\"$translatedText\"" else (if (isTagalog) "Pumili o maglagay ng senyas sa itaas..." else "Select or type sign glosses above..."),
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = textPrimary,
                    lineHeight = 24.sp
                )

                Spacer(Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            val toSpeak = translatedText.ifBlank { inputText }
                            if (toSpeak.isNotBlank()) {
                                sosManager.speak(toSpeak)
                            } else {
                                Toast.makeText(context, if (isTagalog) "Pumili o mag-type muna ng senyas." else "Select or type signs first.", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = primary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = "Speak", tint = Color.White)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = if (isTagalog) "Ipahayag sa Boses" else "Speak Out Loud",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = Color.White
                        )
                    }

                    OutlinedButton(
                        onClick = {
                            val toCopy = translatedText.ifBlank { inputText }
                            if (toCopy.isNotBlank()) {
                                clipboardManager.setText(AnnotatedString(toCopy))
                                Toast.makeText(context, if (isTagalog) "Na-kopya ang salin sa clipboard!" else "Translation copied to clipboard!", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, if (isTagalog) "Walang salin na kokopyahin." else "No translation to copy.", Toast.LENGTH_SHORT).show()
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.height(48.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, border),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = textPrimary)
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = accent, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = if (isTagalog) "Kopyahin" else "Copy",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp,
                            color = textPrimary
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(28.dp))
    }
}
