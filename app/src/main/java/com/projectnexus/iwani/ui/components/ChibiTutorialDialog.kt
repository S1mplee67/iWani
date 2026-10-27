package com.projectnexus.iwani.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.projectnexus.iwani.AppLanguage
import com.projectnexus.iwani.R
import com.projectnexus.iwani.ui.sound.UiSoundManager
import com.projectnexus.iwani.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class TutorialSpeaker {
    GRACE_AND_KIAN,
    GRACE,
    KIAN,
    SHOUKO_SOS,
    ALL_THREE_FINALE
}

data class TutorialStepSpec(
    val speaker: TutorialSpeaker,
    val voiceCueCharacter: String,
    val voiceCueType: String,
    val villagerNoiseBadge: String,
    val signActionEn: String,
    val signActionTl: String,
    val stepTitleEn: String,
    val stepTitleTl: String,
    val dialogEn: String,
    val dialogTl: String,
    val howToStepsEn: List<String>,
    val howToStepsTl: List<String>,
    val targetRoute: String? = null,
    val targetRouteLabelEn: String? = null,
    val targetRouteLabelTl: String? = null
)

/**
 * Interactive Chibi Mascot Welcome & Feature Tutorial featuring:
 * - GRACE (Mary Grace Ann • Developer, wearing ICCT Uniform, round glasses, star hairclip & tablet)
 * - KIAN (Kian • QA & Developer, wearing ICCT Uniform, blue ICCT ID lanyard & headphones)
 * - SHOUKO NISHIMIYA (Easter Egg Chibi guest who appears ONLY on the Emergency SOS step & Finale!)
 * Includes Clash-of-Clans villager vocalizations ("Ahh~!", "Hmm-hmm!", "Ooh!", "Umm~!"),
 * persistent Skip button on every step, and a Team Feedback Inbox at the end.
 */
@Composable
fun ChibiMascotTutorialDialog(
    appLanguage: AppLanguage,
    isDarkTheme: Boolean,
    onDismiss: () -> Unit,
    onNavigateToRoute: (String) -> Unit,
    onOpenFeedbackInbox: () -> Unit
) {
    val isTagalog = appLanguage.isTagalog
    val context = LocalContext.current

    val steps = remember {
        listOf(
            // STEP 1: Grace & Kian Self-Introduction as Project Nexus Developers
            TutorialStepSpec(
                speaker = TutorialSpeaker.GRACE_AND_KIAN,
                voiceCueCharacter = "grace",
                voiceCueType = "ahh",
                villagerNoiseBadge = "💬 \"Ahh~! / Hmm-hmm!\"",
                signActionEn = "🤟 Signing FSL: \"Mabuhay & Welcome!\"",
                signActionTl = "🤟 Nag-se-senyas ng FSL: \"Mabuhay at Kumusta!\"",
                stepTitleEn = "Meet Your Project Nexus Developers!",
                stepTitleTl = "Kilalanin ang Developers ng Project Nexus!",
                dialogEn = "Grace: \"Ahh~! Hi there! My name is Grace, and I'm one of the developers of Project Nexus!\"\n\nKian: \"Hmm-hmm! And my name is Kian, also part of Project Nexus and a developer of iWani from ICCT Colleges! Let us guide you through every feature of the app using Filipino Sign Language!\"",
                dialogTl = "Grace: \"Ahh~! Kumusta po! Ang pangalan ko ay Grace, isa sa mga developer ng Project Nexus!\"\n\nKian: \"Hmm-hmm! At ako naman si Kian, bahagi rin ng Project Nexus at developer ng iWani mula sa ICCT Colleges! Samahan niyo kami upang ipakita ang bawat feature at kung paano gamitin ang app!\"",
                howToStepsEn = listOf(
                    "👩‍💻 Grace (DB & Developer): Wears round glasses, star clip & carries her dev tablet.",
                    "👨‍💻 Kian (QA & Developer): Wears his ICCT Colleges ID lanyard & studio headphones.",
                    "🔊 Tap the voice bubble anytime to hear our villager vocal reactions!"
                ),
                howToStepsTl = listOf(
                    "👩‍💻 Grace (DB & Developer): Nakasuot ng salamin, star clip at may hawak na dev tablet.",
                    "👨‍💻 Kian (QA & Developer): Nakasuot ng ICCT ID lanyard at studio headphones.",
                    "🔊 Pindutin ang voice bubble anumang oras para marinig ang aming boses!"
                )
            ),
            // STEP 2: Home Workspace, EN/TL Flag Switcher & Wave Theme (Grace)
            TutorialStepSpec(
                speaker = TutorialSpeaker.GRACE,
                voiceCueCharacter = "grace",
                voiceCueType = "ooh",
                villagerNoiseBadge = "💬 \"Ooh~! Ahh!\"",
                signActionEn = "🤟 Signing FSL: \"Language & Theme\"",
                signActionTl = "🤟 Nag-se-senyas ng FSL: \"Wika at Tema\"",
                stepTitleEn = "1. Top Controls & Bento Workspace",
                stepTitleTl = "1. Top Controls at Bento Workspace",
                dialogEn = "Grace: \"Ooh~! At the top bar, you can switch between English (🇺🇸 EN) and Tagalog (🇵🇭 TL) anytime! Tap the Sun/Moon icon right beside it for a smooth circular wave transition between Dark and Light mode!\"",
                dialogTl = "Grace: \"Ooh~! Sa pinakataas ng screen, pwede mong ilipat ang wika sa English (🇺🇸 EN) o Tagalog (🇵🇭 TL) anumang oras! Pindutin naman ang Sun/Moon icon sa tabi nito para sa wave transition ng Dark at Light mode!\"",
                howToStepsEn = listOf(
                    "🇺🇸 / 🇵🇭 Flag Pill: Instantly translates the entire app between English & Tagalog.",
                    "☀️ / 🌙 Theme Button: Launches a circular wave reveal across the whole screen.",
                    "🍱 2×2 Bento Grid: Quick 1-tap access to Camera, Translate, SOS, and FSL Chart."
                ),
                howToStepsTl = listOf(
                    "🇺🇸 / 🇵🇭 Flag Pill: Agad na isinasalin ang buong app sa English o Tagalog.",
                    "☀️ / 🌙 Theme Button: Nagbibigay ng circular wave transition sa buong screen.",
                    "🍱 2×2 Bento Grid: Mabilis na daan sa Kamera, Salin, SOS, at FSL Tsart."
                )
            ),
            // STEP 3: Live 3D FSL Camera & Typing Between Formed Letters (Kian)
            TutorialStepSpec(
                speaker = TutorialSpeaker.KIAN,
                voiceCueCharacter = "kian",
                voiceCueType = "aha",
                villagerNoiseBadge = "💬 \"Aha~! Hmm!\"",
                signActionEn = "🤟 Signing FSL: \"A – B – C Hand Tracking\"",
                signActionTl = "🤟 Nag-se-senyas ng FSL: \"A – B – C Hand Tracking\"",
                stepTitleEn = "2. FSL Camera & Edit Between Letters",
                stepTitleTl = "2. FSL Kamera at Pag-type sa Pagitan ng Titik",
                dialogEn = "Kian: \"Aha~! In the FSL Camera tab, hold your hand in front of the camera and our 21-joint 3D AI tracks your fingers in real time! Even better: you can tap directly BETWEEN any formed letters (or use ◀ ▶) to type or insert letters anywhere in your word!\"",
                dialogTl = "Kian: \"Aha~! Sa FSL Kamera, itapat lang ang iyong kamay at babasahin ng 3D AI ang iyong senyas! Pwede mo ring pindutin ang PAGITAN ng mga nabuong titik (o gamitin ang ◀ ▶) para mag-type o magsingit ng letra kahit saang bahagi ng salita!\"",
                howToStepsEn = listOf(
                    "✋ Hold a hand sign steady for ~1 second to add the letter at your cursor.",
                    "⌨️ Tap between any letters in the Formed Word box to type or edit mid-word!",
                    "🎥 Tap 'Record to Gallery' to save your live signing video to Movies/iWani.",
                    "🔊 Tap 'Speak' to read your formed word out loud in clear voice."
                ),
                howToStepsTl = listOf(
                    "✋ Panatilihing steady ang kamay nang ~1 segundo para mailagay ang titik.",
                    "⌨️ Pindutin ang pagitan ng mga titik sa Formed Word box para mag-type o mag-edit!",
                    "🎥 Pindutin ang 'I-record sa Gallery' para i-save ang video sa Movies/iWani.",
                    "🔊 Pindutin ang 'Salita / Speak' para iparinig nang malakas ang nabuong salita."
                ),
                targetRoute = "camera",
                targetRouteLabelEn = "Open FSL Camera",
                targetRouteLabelTl = "Buksan ang Kamera"
            ),
            // STEP 4: Full-Portrait Enlarged FSL Alphabet Guide A-Z (Grace)
            TutorialStepSpec(
                speaker = TutorialSpeaker.GRACE,
                voiceCueCharacter = "grace",
                voiceCueType = "hmm",
                villagerNoiseBadge = "💬 \"Hmm~? Ahh!\"",
                signActionEn = "🤟 Signing FSL: \"Learn A to Z Signs\"",
                signActionTl = "🤟 Nag-se-senyas ng FSL: \"Pag-aralan ang A–Z\"",
                stepTitleEn = "3. Full-Portrait FSL Alphabet Chart",
                stepTitleTl = "3. Full-Portrait FSL Alphabet Tsart",
                dialogEn = "Grace: \"Hmm~? Not sure how to sign a letter? Tap the 'FSL Alphabet' tile on Home or the Book icon inside the Camera to open our enlarged full-portrait A–Z guide! You can also pinch or double-tap to zoom in!\"",
                dialogTl = "Grace: \"Hmm~? Hindi sigurado sa posisyon ng daliri? Pindutin ang 'FSL Tsart' sa Home o ang Book icon sa Kamera para buksan ang pinalaking full-portrait A–Z guide! Pwede mo rin itong i-zoom!\"",
                howToStepsEn = listOf(
                    "📖 Full-Portrait Layout: All 26 A–Z hand signs enlarged to fill your screen.",
                    "🔍 Pinch or Double-Tap Zoom: Inspect finger positions closely with ambient glow."
                ),
                howToStepsTl = listOf(
                    "📖 Full-Portrait Layout: Lahat ng 26 A–Z hand signs ay pinalaki sa buong screen.",
                    "🔍 Pinch o Double-Tap Zoom: Tingnan nang malapitan ang bawat posisyon ng kamay."
                )
            ),
            // STEP 5: AI Translate, Quick Words & Output (Kian)
            TutorialStepSpec(
                speaker = TutorialSpeaker.KIAN,
                voiceCueCharacter = "kian",
                voiceCueType = "ooh",
                villagerNoiseBadge = "💬 \"Ooooh! Hmm!\"",
                signActionEn = "🤟 Signing FSL: \"Pagkain • Tubig • Salamat\"",
                signActionTl = "🤟 Nag-se-senyas ng FSL: \"Pagkain • Tubig • Salamat\"",
                stepTitleEn = "4. Translate, Quick Words & Output",
                stepTitleTl = "4. Pagsasalin, Quick Words at Output",
                dialogEn = "Kian: \"Ooooh! In the Translate tab, tap any top category like 'Frequently Used' or 'Daily Needs' and pick quick words like Pagkain, Tubig, Bili, Pahinga, Tulong, or Salamat! Our Qwen 2.5 AI turns them into polite, natural Tagalog or English sentences in the Output box!\"",
                dialogTl = "Kian: \"Ooooh! Sa Salin (Translate) tab, pumili sa mga kategorya gaya ng 'Madalas Gamitin' o 'Araw-araw' at pindutin ang mga quick words tulad ng Pagkain, Tubig, Bili, Pahinga, Tulong, o Salamat! Gagawin itong magalang at buong pangungusap ng AI!\"",
                howToStepsEn = listOf(
                    "⚡ Top Categories: Frequently Used, Daily Needs, Greetings, Emergency, Medical & Campus.",
                    "🍱 Quick Words: Tap Pagkain, Tubig, Bili, Pahinga, Magkano, CR, Bayad po & more!",
                    "🗣️ Speak & Copy: Broadcast the natural sentence aloud or copy it in 1 tap."
                ),
                howToStepsTl = listOf(
                    "⚡ Mga Kategorya: Madalas Gamitin, Araw-araw, Pagbati, Saklolo, Kalusugan at Paaralan.",
                    "🍱 Quick Words: Pindutin ang Pagkain, Tubig, Bili, Pahinga, Magkano, CR, Bayad po at iba pa!",
                    "🗣️ Basahin at Kopyahin: Iparinig sa boses o kopyahin ang pangungusap."
                ),
                targetRoute = "translate",
                targetRouteLabelEn = "Open Translate",
                targetRouteLabelTl = "Buksan ang Salin"
            ),
            // STEP 6: ★ EASTER EGG: SHOUKO NISHIMIYA APPEARS FOR EMERGENCY SOS! ★
            TutorialStepSpec(
                speaker = TutorialSpeaker.SHOUKO_SOS,
                voiceCueCharacter = "shouko",
                voiceCueType = "umm",
                villagerNoiseBadge = "🌸 \"Umm~! Ehehe...\"",
                signActionEn = "🌸 Signing: \"Please Help / Let's Be Friends\"",
                signActionTl = "🌸 Nag-se-senyas: \"Tulong po / Kaibigan\"",
                stepTitleEn = "5. Emergency SOS ★ Special Guest!",
                stepTitleTl = "5. Emergency SOS ★ Espesyal na Bisita!",
                dialogEn = "Shouko Nishimiya: \"Umm~! H-hello... I'm Shouko Nishimiya! When an urgent situation happens and you cannot speak aloud, open the Emergency SOS tab. Just 1 tap broadcasts a loud, clear voice alert for Medical Help, Police/Barangay Tanod, Lost Directions, or Calling Family!\"\n\nGrace: \"Ahh~! Thank you for explaining the SOS feature, Shouko!\"",
                dialogTl = "Shouko Nishimiya: \"Umm~! K-kumusta po... Ako si Shouko Nishimiya! Kapag may biglaang panganib o emergency, buksan lang ang SOS tab. Sa isang pindot, malakas na magsasalita ang phone para humingi ng Doktor, Pulis/Tanod, Direksyon kapag naliligaw, o Tawagan ang Pamilya!\"\n\nGrace: \"Ahh~! Maraming salamat sa pagpapaliwanag ng SOS feature, Shouko!\"",
                howToStepsEn = listOf(
                    "🚨 1-Tap Voice Broadcast: Speaks immediately at full clarity for bystanders to hear.",
                    "🌊 Reactive Voice Glow: Shows a live audio waveform while broadcasting your alert.",
                    "🛑 Instant Stop: Tap the phrase again or the red Stop button anytime to silence."
                ),
                howToStepsTl = listOf(
                    "🚨 1-Tap Voice Broadcast: Agad na magsasalita nang malinaw para marinig ng nasa paligid.",
                    "🌊 Reactive Voice Glow: Nagpapakita ng live audio wave habang nagsasalita.",
                    "🛑 Agarang Paghinto: Pindutin muli ang card o ang Stop button para ihinto."
                ),
                targetRoute = "sos",
                targetRouteLabelEn = "Open Emergency SOS",
                targetRouteLabelTl = "Buksan ang SOS"
            ),
            // STEP 7: AI Model Extractor & Settings Studio (Kian)
            TutorialStepSpec(
                speaker = TutorialSpeaker.KIAN,
                voiceCueCharacter = "kian",
                voiceCueType = "hmm",
                villagerNoiseBadge = "💬 \"Hmm-hmm! Aha!\"",
                signActionEn = "🤟 Signing FSL: \"Settings & AI Engine\"",
                signActionTl = "🤟 Nag-se-senyas ng FSL: \"Setting at AI Model\"",
                stepTitleEn = "6. Extracting AI Model & Settings Studio",
                stepTitleTl = "6. Pag-extract ng AI Model at Mga Setting",
                dialogEn = "Kian: \"Hmm-hmm! Inside the Settings tab, you can change the app's Font Style, adjust Text Size, pick from 4 unique Button Click Sound effects, and open the AI Model Manager where 'Extracting AI Model (~350 MB)' sets up your offline neural engine!\"",
                dialogTl = "Kian: \"Hmm-hmm! Sa loob ng Settings tab, pwede mong palitan ang Font Style ng app, baguhin ang laki ng teksto, pumili ng tunog ng button, at buksan ang AI Model Extractor (~350 MB) para sa 100% offline na pagsasalin!\"",
                howToStepsEn = listOf(
                    "📦 Extracting AI Model: Unpacks ~350 MB Qwen 2.5 weights locally — zero internet needed!",
                    "🔤 Custom Fonts: Switch live between Geometric, Editorial Serif, Cyber Mono & Rounded.",
                    "🎵 Click Sound Engine: Choose Crystal Chime, Cyber Pulse, Soft Pop, or Tactile Click."
                ),
                howToStepsTl = listOf(
                    "📦 Extracting AI Model: Nag-e-extract ng ~350 MB Qwen 2.5 sa storage nang walang internet!",
                    "🔤 Custom Fonts: Pumili sa Geometric, Editorial Serif, Cyber Mono at Rounded.",
                    "🎵 Click Sound Engine: Pumili sa Crystal Chime, Cyber Pulse, Soft Pop, o Tactile Click."
                ),
                targetRoute = "settings",
                targetRouteLabelEn = "Open Settings Studio",
                targetRouteLabelTl = "Buksan ang Settings"
            ),
            // STEP 8: FINALE — All 3 Chibis (Grace, Kian & Shouko) Saying Thank You + Team Inbox Option!
            TutorialStepSpec(
                speaker = TutorialSpeaker.ALL_THREE_FINALE,
                voiceCueCharacter = "all",
                voiceCueType = "yay",
                villagerNoiseBadge = "🎉 \"Ahh~! / Hmm-hmm! / Arigatou & Salamat!\"",
                signActionEn = "🤟🌸 All 3 Signing: \"Maraming Salamat / Thank You!\"",
                signActionTl = "🤟🌸 Silang 3 ay Nag-se-senyas: \"Maraming Salamat Po!\"",
                stepTitleEn = "Thank You from Grace, Kian & Shouko!",
                stepTitleTl = "Maraming Salamat mula kina Grace, Kian at Shouko!",
                dialogEn = "Grace, Kian & Shouko: \"Thank you so much for using iWani by Project Nexus (Ascendens • ICCT Colleges)! We built this app to bridge communication between the Deaf community and hearing majority. If you have any feedback, suggestions, or messages, you can send a message directly to our team's Inbox below!\"",
                dialogTl = "Grace, Kian & Shouko: \"Maraming-maraming salamat po sa paggamit ng iWani mula sa Project Nexus (Ascendens • ICCT Colleges)! Ginawa namin ito upang pag-ugnayin ang komunidad ng mga Bingi at nakakarinig. Kung mayroon po kayong feedback o mensahe, ipadala lang ito sa aming Team Inbox sa ibaba!\"",
                howToStepsEn = listOf(
                    "✉️ Send Feedback to Team Inbox: Tap 'Message Team Inbox' below to send us your thoughts!",
                    "🔄 Replay Anytime: You can reopen this Mascot Tutorial anytime from Home or Settings."
                ),
                howToStepsTl = listOf(
                    "✉️ Magpadala ng Feedback sa Team Inbox: Pindutin ang 'Mag-mensahe sa Team Inbox' sa ibaba!",
                    "🔄 Ulitin Anumang Oras: Maaari mong buksan muli ang gabay na ito sa Home o Settings."
                )
            )
        )
    }

    var currentStepIndex by remember { mutableIntStateOf(0) }
    val currentStep = steps[currentStepIndex]

    // Play the villager voice automatically whenever the user advances to a new dialog step!
    LaunchedEffect(currentStepIndex) {
        UiSoundManager.playVillagerVoice(
            context = context,
            character = currentStep.voiceCueCharacter,
            cue = currentStep.voiceCueType
        )
    }

    // Gentle floating idle bobbing animation for the chibi characters
    val infiniteTransition = rememberInfiniteTransition(label = "ChibiFloat")
    val bobOffset by infiniteTransition.animateFloat(
        initialValue = -4f,
        targetValue = 4f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ChibiBobOffset"
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xE6050914))
                .padding(horizontal = 14.dp, vertical = 18.dp),
            contentAlignment = Alignment.Center
        ) {
            // Non-repetitive Ambient Aurora Glow Backdrop
            AmbientAuroraGlow(
                primaryOrbColor = when (currentStep.speaker) {
                    TutorialSpeaker.SHOUKO_SOS -> Color(0xFFEC4899)
                    TutorialSpeaker.ALL_THREE_FINALE -> Color(0xFFF59E0B)
                    TutorialSpeaker.KIAN -> Color(0xFF38BDF8)
                    else -> Color(0xFF3B82F6)
                },
                secondaryOrbColor = Color(0xFFF59E0B),
                tertiaryOrbColor = Color(0xFF14B8A6),
                alphaMultiplier = 1.2f
            )

            BorderBeamBox(
                active = true,
                cornerRadius = 26.dp,
                beamColor = if (currentStep.speaker == TutorialSpeaker.SHOUKO_SOS) Color(0xFFF472B6) else Color(0xFF38BDF8),
                secondaryColor = Color(0xFFF59E0B),
                baseBorderColor = Color(0xFF253A5E),
                backgroundColor = Color(0xF50B1326),
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // ================= TOP BAR: STEP INDICATOR + PERSISTENT SKIP BUTTON =================
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = Color(0xFF1E293B),
                            border = BorderStroke(1.dp, Color(0xFF334155))
                        ) {
                            Text(
                                text = if (isTagalog)
                                    "GABAY ${currentStepIndex + 1} / ${steps.size}"
                                else
                                    "GUIDE ${currentStepIndex + 1} / ${steps.size}",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 1.sp,
                                color = Color(0xFF93C5FD),
                                maxLines = 1,
                                softWrap = false,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }

                        // Villager Voice Replay Pill ("Ahh~!" / "Hmm!")
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = Color(0xFFF59E0B).copy(alpha = 0.16f),
                            border = BorderStroke(1.dp, Color(0xFFF59E0B).copy(alpha = 0.5f)),
                            modifier = Modifier.interactiveBounceClick {
                                UiSoundManager.playVillagerVoice(
                                    context = context,
                                    character = currentStep.voiceCueCharacter,
                                    cue = currentStep.voiceCueType
                                )
                            }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                    contentDescription = "Voice",
                                    tint = Color(0xFFFBBF24),
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    text = currentStep.villagerNoiseBadge,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFFDE68A),
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                        }

                        // Skip Button on EVERY Dialog Step
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = Color.White.copy(alpha = 0.10f),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.22f)),
                            modifier = Modifier.interactiveBounceClick { onDismiss() }
                        ) {
                            Text(
                                text = if (isTagalog) "Laktawan ✕" else "Skip ✕",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFE2E8F0),
                                maxLines = 1,
                                softWrap = false,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                    }

                    Spacer(Modifier.height(10.dp))

                    // ================= CHIBI STAGE WITH NAME BADGES BELOW/BESIDE THEM =================
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        Color(0xFF162447).copy(alpha = 0.55f),
                                        Color(0xFF0F172A).copy(alpha = 0.85f)
                                    )
                                ),
                                shape = RoundedCornerShape(20.dp)
                            )
                            .border(1.dp, Color(0xFF253B6E), RoundedCornerShape(20.dp))
                            .padding(horizontal = 10.dp, vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            // FSL Sign Gesture Indicator Pill above the Chibis
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = Color(0xFF0EA5E9).copy(alpha = 0.16f),
                                border = BorderStroke(1.dp, Color(0xFF38BDF8).copy(alpha = 0.45f))
                            ) {
                                Text(
                                    text = if (isTagalog) currentStep.signActionTl else currentStep.signActionEn,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF7DD3FC),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                                )
                            }

                            Spacer(Modifier.height(8.dp))

                            // Character Sprites + Name Badges Below Each Character
                            when (currentStep.speaker) {
                                TutorialSpeaker.GRACE_AND_KIAN -> {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceEvenly,
                                        verticalAlignment = Alignment.Bottom
                                    ) {
                                        ChibiCharacterColumn(
                                            drawableRes = R.drawable.chibi_grace,
                                            name = "GRACE",
                                            role = if (isTagalog) "Developer • Project Nexus" else "Developer • Project Nexus",
                                            accessoryTag = "👓 Glasses • ⭐ Clip • 📱 Tablet",
                                            accentColor = Color(0xFF38BDF8),
                                            bobOffset = bobOffset,
                                            onTapVoice = {
                                                UiSoundManager.playVillagerVoice(context, "grace", "ahh")
                                            },
                                            modifier = Modifier.weight(1f)
                                        )
                                        Spacer(Modifier.width(8.dp))
                                        ChibiCharacterColumn(
                                            drawableRes = R.drawable.chibi_kian,
                                            name = "KIAN",
                                            role = if (isTagalog) "Developer • Project Nexus" else "Developer • Project Nexus",
                                            accessoryTag = "🎧 Headphones • 🪪 ICCT ID",
                                            accentColor = Color(0xFFF59E0B),
                                            bobOffset = -bobOffset,
                                            onTapVoice = {
                                                UiSoundManager.playVillagerVoice(context, "kian", "aha")
                                            },
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                }
                                TutorialSpeaker.GRACE -> {
                                    ChibiCharacterColumn(
                                        drawableRes = R.drawable.chibi_grace,
                                        name = "GRACE (Mary Grace Ann)",
                                        role = if (isTagalog) "Project Nexus Developer • ICCT" else "Project Nexus Developer • ICCT",
                                        accessoryTag = "👓 Glasses • ⭐ Star Clip • 📱 Dev Tablet",
                                        accentColor = Color(0xFF38BDF8),
                                        bobOffset = bobOffset,
                                        spriteHeight = 165,
                                        onTapVoice = {
                                            UiSoundManager.playVillagerVoice(context, "grace", currentStep.voiceCueType)
                                        }
                                    )
                                }
                                TutorialSpeaker.KIAN -> {
                                    ChibiCharacterColumn(
                                        drawableRes = R.drawable.chibi_kian,
                                        name = "KIAN (Kian Cagadas)",
                                        role = if (isTagalog) "Project Nexus Developer • ICCT" else "Project Nexus Developer • ICCT",
                                        accessoryTag = "🎧 Studio Headphones • 🪪 ICCT Lanyard",
                                        accentColor = Color(0xFFF59E0B),
                                        bobOffset = bobOffset,
                                        spriteHeight = 165,
                                        onTapVoice = {
                                            UiSoundManager.playVillagerVoice(context, "kian", currentStep.voiceCueType)
                                        }
                                    )
                                }
                                TutorialSpeaker.SHOUKO_SOS -> {
                                    // Easter Egg: Shouko Nishimiya (with Grace beside her)
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceEvenly,
                                        verticalAlignment = Alignment.Bottom
                                    ) {
                                        ChibiCharacterColumn(
                                            drawableRes = R.drawable.chibi_shouko,
                                            name = "SHOUKO NISHIMIYA",
                                            role = if (isTagalog) "★ Easter Egg Guest • SOS Guide" else "★ Easter Egg Guest • SOS Guide",
                                            accessoryTag = "🌸 A Silent Voice • 🦻 FSL Friend",
                                            accentColor = Color(0xFFF472B6),
                                            bobOffset = bobOffset,
                                            spriteHeight = 156,
                                            onTapVoice = {
                                                UiSoundManager.playVillagerVoice(context, "shouko", "umm")
                                            },
                                            modifier = Modifier.weight(1.15f)
                                        )
                                        Spacer(Modifier.width(6.dp))
                                        ChibiCharacterColumn(
                                            drawableRes = R.drawable.chibi_grace,
                                            name = "GRACE",
                                            role = "Developer • Project Nexus",
                                            accessoryTag = "👓 ICCT Uniform",
                                            accentColor = Color(0xFF38BDF8),
                                            bobOffset = -bobOffset,
                                            spriteHeight = 136,
                                            onTapVoice = {
                                                UiSoundManager.playVillagerVoice(context, "grace", "ahh")
                                            },
                                            modifier = Modifier.weight(0.85f)
                                        )
                                    }
                                }
                                TutorialSpeaker.ALL_THREE_FINALE -> {
                                    // Finale: All 3 Chibis Together (Grace, Shouko, Kian)!
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceEvenly,
                                        verticalAlignment = Alignment.Bottom
                                    ) {
                                        ChibiCharacterColumn(
                                            drawableRes = R.drawable.chibi_grace,
                                            name = "GRACE",
                                            role = "Developer",
                                            accessoryTag = "👓 ICCT",
                                            accentColor = Color(0xFF38BDF8),
                                            bobOffset = bobOffset,
                                            spriteHeight = 128,
                                            onTapVoice = {
                                                UiSoundManager.playVillagerVoice(context, "grace", "ahh")
                                            },
                                            modifier = Modifier.weight(1f)
                                        )
                                        Spacer(Modifier.width(4.dp))
                                        ChibiCharacterColumn(
                                            drawableRes = R.drawable.chibi_shouko,
                                            name = "SHOUKO",
                                            role = "★ Guest",
                                            accessoryTag = "🌸 SOS",
                                            accentColor = Color(0xFFF472B6),
                                            bobOffset = -bobOffset,
                                            spriteHeight = 128,
                                            onTapVoice = {
                                                UiSoundManager.playVillagerVoice(context, "shouko", "umm")
                                            },
                                            modifier = Modifier.weight(1f)
                                        )
                                        Spacer(Modifier.width(4.dp))
                                        ChibiCharacterColumn(
                                            drawableRes = R.drawable.chibi_kian,
                                            name = "KIAN",
                                            role = "Developer",
                                            accessoryTag = "🎧 ICCT",
                                            accentColor = Color(0xFFF59E0B),
                                            bobOffset = bobOffset,
                                            spriteHeight = 128,
                                            onTapVoice = {
                                                UiSoundManager.playVillagerVoice(context, "kian", "aha")
                                            },
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    // ================= DIALOG BOX & FEATURE CHECKLIST =================
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = Color(0xFF111C35),
                        border = BorderStroke(
                            1.dp,
                            if (currentStep.speaker == TutorialSpeaker.SHOUKO_SOS)
                                Color(0xFFF472B6).copy(alpha = 0.5f)
                            else
                                Color(0xFF38BDF8).copy(alpha = 0.4f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = if (isTagalog) currentStep.stepTitleTl else currentStep.stepTitleEn,
                                fontWeight = FontWeight.Black,
                                fontSize = 16.sp,
                                color = Color(0xFFFBBF24)
                            )

                            Spacer(Modifier.height(6.dp))

                            Text(
                                text = if (isTagalog) currentStep.dialogTl else currentStep.dialogEn,
                                fontSize = 12.5.sp,
                                color = Color(0xFFF1F5F9),
                                lineHeight = 18.sp
                            )

                            Spacer(Modifier.height(10.dp))
                            HorizontalDivider(color = Color(0xFF1E293B))
                            Spacer(Modifier.height(8.dp))

                            val bullets = if (isTagalog) currentStep.howToStepsTl else currentStep.howToStepsEn
                            bullets.forEach { bullet ->
                                Text(
                                    text = bullet,
                                    fontSize = 11.5.sp,
                                    color = Color(0xFF93C5FD),
                                    lineHeight = 16.sp,
                                    modifier = Modifier.padding(vertical = 2.dp)
                                )
                            }

                            // Optional "Try This Feature" shortcut button on feature steps
                            if (currentStep.targetRoute != null) {
                                Spacer(Modifier.height(8.dp))
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color(0xFF2563EB).copy(alpha = 0.22f),
                                    border = BorderStroke(1.dp, Color(0xFF38BDF8).copy(alpha = 0.55f)),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .interactiveBounceClick {
                                            onDismiss()
                                            onNavigateToRoute(currentStep.targetRoute)
                                        }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                        horizontalArrangement = Arrangement.Center,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.OpenInNew,
                                            contentDescription = null,
                                            tint = Color(0xFF38BDF8),
                                            modifier = Modifier.size(15.dp)
                                        )
                                        Spacer(Modifier.width(6.dp))
                                        Text(
                                            text = if (isTagalog)
                                                (currentStep.targetRouteLabelTl ?: "Subukan Ngayon")
                                            else
                                                (currentStep.targetRouteLabelEn ?: "Try Feature Now"),
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFE0F2FE),
                                            maxLines = 1,
                                            softWrap = false
                                        )
                                    }
                                }
                            }

                            // Finale Step: Prominent "Send Message to Team Inbox" CTA!
                            if (currentStep.speaker == TutorialSpeaker.ALL_THREE_FINALE) {
                                Spacer(Modifier.height(10.dp))
                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = Color(0xFFF59E0B),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .interactiveBounceClick {
                                            onDismiss()
                                            onOpenFeedbackInbox()
                                        }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 11.dp),
                                        horizontalArrangement = Arrangement.Center,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Inbox,
                                            contentDescription = "Inbox",
                                            tint = Color(0xFF0F172A),
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(Modifier.width(8.dp))
                                        Text(
                                            text = if (isTagalog)
                                                "✉️ Magpadala ng Mensahe sa Team Inbox"
                                            else
                                                "✉️ Send Message to Team Inbox",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Black,
                                            color = Color(0xFF0F172A),
                                            maxLines = 1,
                                            softWrap = false
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    // ================= BOTTOM NAVIGATION: BACK / DOTS / NEXT OR FINISH =================
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Back Button
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = if (currentStepIndex > 0) Color(0xFF1E293B) else Color.Transparent,
                            border = BorderStroke(
                                1.dp,
                                if (currentStepIndex > 0) Color(0xFF334155) else Color.Transparent
                            ),
                            modifier = Modifier.interactiveBounceClick(enabled = currentStepIndex > 0) {
                                if (currentStepIndex > 0) currentStepIndex--
                            }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (currentStepIndex > 0) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "Back",
                                        tint = Color.White,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(Modifier.width(4.dp))
                                    Text(
                                        text = if (isTagalog) "Balik" else "Back",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        maxLines = 1,
                                        softWrap = false
                                    )
                                } else {
                                    Spacer(Modifier.width(48.dp))
                                }
                            }
                        }

                        // Step Dots
                        Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                            steps.indices.forEach { idx ->
                                val active = idx == currentStepIndex
                                Box(
                                    modifier = Modifier
                                        .height(6.dp)
                                        .width(if (active) 18.dp else 6.dp)
                                        .background(
                                            color = if (active) Color(0xFFF59E0B) else Color.White.copy(alpha = 0.25f),
                                            shape = CircleShape
                                        )
                                )
                            }
                        }

                        // Next / Finish Button
                        val isLast = currentStepIndex == steps.lastIndex
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = if (isLast) SuccessGreen else Color(0xFF2563EB),
                            modifier = Modifier.interactiveBounceClick {
                                if (!isLast) {
                                    currentStepIndex++
                                } else {
                                    UiSoundManager.playSuccess(context)
                                    onDismiss()
                                }
                            }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (isLast) {
                                        if (isTagalog) "Tapos ✓" else "Finish ✓"
                                    } else {
                                        if (isTagalog) "Sunod" else "Next"
                                    },
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White,
                                    maxLines = 1,
                                    softWrap = false
                                )
                                if (!isLast) {
                                    Spacer(Modifier.width(4.dp))
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                        contentDescription = "Next",
                                        tint = Color.White,
                                        modifier = Modifier.size(15.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ChibiCharacterColumn(
    drawableRes: Int,
    name: String,
    role: String,
    accessoryTag: String,
    accentColor: Color,
    bobOffset: Float,
    spriteHeight: Int = 148,
    onTapVoice: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.interactiveBounceClick { onTapVoice() }
    ) {
        Box(contentAlignment = Alignment.Center) {
            // Soft character aura behind chibi
            Box(
                modifier = Modifier
                    .size((spriteHeight * 0.78f).dp)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                accentColor.copy(alpha = 0.32f),
                                Color.Transparent
                            )
                        ),
                        shape = CircleShape
                    )
            )

            Image(
                painter = painterResource(id = drawableRes),
                contentDescription = name,
                modifier = Modifier
                    .height(spriteHeight.dp)
                    .offset(y = bobOffset.dp),
                contentScale = ContentScale.Fit
            )
        }

        Spacer(Modifier.height(4.dp))

        // Name Tag Pill Directly Below Character
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = accentColor.copy(alpha = 0.20f),
            border = BorderStroke(1.dp, accentColor.copy(alpha = 0.65f))
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = name,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = role,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = accentColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(Modifier.height(2.dp))

        Text(
            text = accessoryTag,
            fontSize = 8.5.sp,
            color = Color(0xFF94A3B8),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * Project Nexus Team Feedback Inbox Modal — allows users to send feedback, appreciation,
 * or bug reports directly to Grace, Kian, and the Project Nexus (Ascendens) development team.
 */
@Composable
fun TeamFeedbackInboxDialog(
    appLanguage: AppLanguage,
    onDismiss: () -> Unit
) {
    val isTagalog = appLanguage.isTagalog
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("iwani_team_inbox", Context.MODE_PRIVATE) }

    var senderName by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(if (isTagalog) "Papuri / Pasasalamat" else "Appreciation") }
    var messageText by remember { mutableStateOf("") }
    var sentHistory by remember {
        mutableStateOf(
            prefs.getStringSet("inbox_messages", emptySet())?.toList()?.sortedDescending() ?: emptyList()
        )
    }

    val categories = if (isTagalog) {
        listOf("Papuri / Pasasalamat", "Suhestiyon", "Ulat ng Bug", "Tanong sa Team")
    } else {
        listOf("Appreciation", "Feature Idea", "Bug Report", "Question")
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xE6050914))
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            AmbientAuroraGlow(
                primaryOrbColor = Color(0xFF38BDF8),
                secondaryOrbColor = Color(0xFFF59E0B),
                tertiaryOrbColor = Color(0xFFEC4899)
            )

            BorderBeamBox(
                active = true,
                cornerRadius = 24.dp,
                beamColor = Color(0xFFF59E0B),
                secondaryColor = Color(0xFF38BDF8),
                baseBorderColor = Color(0xFF334155),
                backgroundColor = Color(0xF50B1326),
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    // Header with all 3 mini chibi avatars
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isTagalog) "Project Nexus • Team Inbox" else "Project Nexus • Team Inbox",
                                fontWeight = FontWeight.Black,
                                fontSize = 19.sp,
                                color = Color(0xFFFBBF24)
                            )
                            Text(
                                text = if (isTagalog)
                                    "Direktang mensahe para kina Grace, Kian at Ascendens Team"
                                else
                                    "Send feedback directly to Grace, Kian & the Ascendens Team",
                                fontSize = 11.5.sp,
                                color = Color(0xFF94A3B8)
                            )
                        }
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                        }
                    }

                    Spacer(Modifier.height(10.dp))

                    // Mini Chibi Trio Greeting Banner
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xFF162447),
                        border = BorderStroke(1.dp, Color(0xFF253B6E)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.chibi_grace),
                                contentDescription = "Grace",
                                modifier = Modifier.size(44.dp)
                            )
                            Image(
                                painter = painterResource(id = R.drawable.chibi_shouko),
                                contentDescription = "Shouko",
                                modifier = Modifier.size(44.dp)
                            )
                            Image(
                                painter = painterResource(id = R.drawable.chibi_kian),
                                contentDescription = "Kian",
                                modifier = Modifier.size(44.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = if (isTagalog)
                                    "\"Salamat po! Binabasa ng aming team ang bawat mensahe sa Inbox!\""
                                else
                                    "\"Thank you! Our developer team reads every message in our Inbox!\"",
                                fontSize = 11.5.sp,
                                color = Color(0xFFE2E8F0),
                                lineHeight = 16.sp,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    // Category Selector Row
                    Text(
                        text = if (isTagalog) "Uri ng Mensahe:" else "Feedback Topic:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF93C5FD)
                    )
                    Spacer(Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        categories.forEach { cat ->
                            val isSel = selectedCategory == cat
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSel) Color(0xFF2563EB) else Color(0xFF1E293B),
                                border = BorderStroke(1.dp, if (isSel) Color(0xFF60A5FA) else Color(0xFF334155)),
                                modifier = Modifier
                                    .weight(1f)
                                    .interactiveBounceClick { selectedCategory = cat }
                            ) {
                                Text(
                                    text = cat,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 7.dp)
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(10.dp))

                    OutlinedTextField(
                        value = senderName,
                        onValueChange = { senderName = it },
                        label = { Text(if (isTagalog) "Iyong Pangalan (Opsyonal)" else "Your Name (Optional)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFF111C35),
                            unfocusedContainerColor = Color(0xFF111C35),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFF38BDF8),
                            unfocusedBorderColor = Color(0xFF334155),
                            focusedLabelColor = Color(0xFF38BDF8),
                            unfocusedLabelColor = Color(0xFF94A3B8)
                        )
                    )

                    Spacer(Modifier.height(10.dp))

                    OutlinedTextField(
                        value = messageText,
                        onValueChange = { messageText = it },
                        label = { Text(if (isTagalog) "Mensahe o Feedback para sa Team" else "Your Message or Feedback for our Team") },
                        placeholder = {
                            Text(
                                if (isTagalog)
                                    "Hal: Maganda ang app! Sana madagdagan pa ng..."
                                else
                                    "e.g. Great FSL app! Here is my feedback for Grace, Kian & team..."
                            )
                        },
                        minLines = 3,
                        maxLines = 5,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFF111C35),
                            unfocusedContainerColor = Color(0xFF111C35),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFFF59E0B),
                            unfocusedBorderColor = Color(0xFF334155),
                            focusedLabelColor = Color(0xFFF59E0B),
                            unfocusedLabelColor = Color(0xFF94A3B8)
                        )
                    )

                    Spacer(Modifier.height(14.dp))

                    // Submit to Team Inbox Button + Email Share Option
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                if (messageText.isBlank()) {
                                    Toast.makeText(
                                        context,
                                        if (isTagalog) "Mag-type muna ng mensahe." else "Please enter a feedback message first.",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                } else {
                                    val stamp = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()).format(Date())
                                    val author = senderName.ifBlank { if (isTagalog) "Bisita" else "Guest User" }
                                    val entry = "[$stamp] ($selectedCategory) $author: ${messageText.trim()}"
                                    val updated = (sentHistory + entry).takeLast(15).toSet()
                                    prefs.edit().putStringSet("inbox_messages", updated).apply()
                                    sentHistory = updated.toList().sortedDescending()
                                    messageText = ""
                                    UiSoundManager.playVillagerVoice(context, "all", "yay")
                                    Toast.makeText(
                                        context,
                                        if (isTagalog) "✓ Naipadala na sa Project Nexus Team Inbox! Salamat po!" else "✓ Sent to Project Nexus Team Inbox! Thank you!",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = "Send",
                                tint = Color(0xFF0F172A),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = if (isTagalog) "Ipadala sa Inbox" else "Send to Team Inbox",
                                fontWeight = FontWeight.Black,
                                fontSize = 12.5.sp,
                                color = Color(0xFF0F172A),
                                maxLines = 1,
                                softWrap = false
                            )
                        }

                        OutlinedButton(
                            onClick = {
                                val body = messageText.ifBlank { "Hi Grace, Kian & Project Nexus Team!" }
                                val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
                                    data = Uri.parse("mailto:projectnexus.icct@gmail.com")
                                    putExtra(Intent.EXTRA_SUBJECT, "iWani Project Nexus Feedback ($selectedCategory)")
                                    putExtra(Intent.EXTRA_TEXT, body)
                                }
                                try {
                                    context.startActivity(Intent.createChooser(emailIntent, "Email Project Nexus Team"))
                                } catch (_: Exception) {
                                    Toast.makeText(context, "projectnexus.icct@gmail.com", Toast.LENGTH_SHORT).show()
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, Color(0xFF38BDF8)),
                            modifier = Modifier.height(48.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Email,
                                contentDescription = "Email",
                                tint = Color(0xFF38BDF8),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = "Email",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFE0F2FE),
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }

                    // Recent Team Inbox Log
                    if (sentHistory.isNotEmpty()) {
                        Spacer(Modifier.height(14.dp))
                        Text(
                            text = if (isTagalog) "Mga Naipadalang Mensahe sa Team Inbox (${sentHistory.size}):" else "Recent Messages in Team Inbox (${sentHistory.size}):",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF34D399)
                        )
                        Spacer(Modifier.height(6.dp))
                        sentHistory.take(4).forEach { msg ->
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFF111C35),
                                border = BorderStroke(1.dp, Color(0xFF1E293B)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp)
                            ) {
                                Text(
                                    text = msg,
                                    fontSize = 11.sp,
                                    color = Color(0xFFCBD5E1),
                                    modifier = Modifier.padding(10.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
