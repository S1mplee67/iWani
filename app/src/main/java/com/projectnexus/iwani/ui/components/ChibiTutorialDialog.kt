package com.projectnexus.iwani.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.FormatListBulleted
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.projectnexus.iwani.AppLanguage
import com.projectnexus.iwani.R
import com.projectnexus.iwani.ui.sound.UiSoundManager
import com.projectnexus.iwani.ui.theme.*
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Active speaker in the Visual Novel / Blue Archive-inspired walkthrough.
 */
enum class VnSpeaker(
    val displayName: String,
    val roleEn: String,
    val roleTl: String,
    val accentColor: Color,
    val voiceId: String,
    val voiceCue: String
) {
    GRACE(
        displayName = "Grace",
        roleEn = "Developer • Project Nexus",
        roleTl = "Developer • Project Nexus",
        accentColor = Color(0xFF38BDF8),
        voiceId = "grace",
        voiceCue = "ahh"
    ),
    KIAN(
        displayName = "Kian",
        roleEn = "QA & Developer • Project Nexus",
        roleTl = "QA & Developer • Project Nexus",
        accentColor = Color(0xFFF59E0B),
        voiceId = "kian",
        voiceCue = "hmm"
    ),
    SHOUKO(
        displayName = "Shouko",
        roleEn = "Special Guest",
        roleTl = "Special Guest",
        accentColor = Color(0xFFF472B6),
        voiceId = "shouko",
        voiceCue = "umm"
    ),
    ALL_THREE(
        displayName = "Grace, Kian & Shouko",
        roleEn = "Project Nexus & Special Guest",
        roleTl = "Project Nexus at Special Guest",
        accentColor = Color(0xFFFBBF24),
        voiceId = "all",
        voiceCue = "yay"
    )
}

/**
 * Where the spotlight focus box points on the blurred background screen.
 */
enum class SpotlightFocusZone {
    TOP_BAR,
    CENTER_HERO,
    CAMERA_VIEWFINDER,
    FORMED_WORD_BOX,
    ALPHABET_CHART_BTN,
    TRANSLATE_OUTPUT_BOX,
    QUICK_WORDS_GRID,
    SOS_CARDS,
    SETTINGS_CARDS,
    NONE
}

/**
 * A single visual-novel dialogue turn inside a Tutorial Part.
 */
data class WalkthroughDialogueLine(
    val partNumber: Int,
    val speaker: VnSpeaker,
    val leftSpriteRes: Int?,
    val rightSpriteRes: Int?,
    val centerSpriteRes: Int? = null,
    val activeSide: String, // "LEFT", "RIGHT", "BOTH", "ALL_THREE"
    val backgroundRoute: String,
    val focusZone: SpotlightFocusZone,
    val focusLabelEn: String,
    val focusLabelTl: String,
    val textEn: String,
    val textTl: String
)

/**
 * Metadata for each selectable Tutorial Part (Parts 1 to 6).
 */
data class WalkthroughPartInfo(
    val partNumber: Int,
    val titleEn: String,
    val titleTl: String,
    val subtitleEn: String,
    val subtitleTl: String,
    val badgeText: String,
    val accentColor: Color,
    val iconRes: Int
)

/**
 * Blue Archive / Visual Novel Style Interactive Walkthrough Overlay.
 * - Organizes the tutorial into 6 selectable Parts (Part 1..Part 6)
 * - Features multi-pose chibi sprites for Grace, Kian, and Shouko (Special Guest)
 * - Puppet up-and-down bounce animation ONLY when a character speaks, then returns to static
 * - Typewriter text animation on every dialogue line (tap once to finish typing, tap again for next line)
 * - Prev button to review previous dialogues + Top-Right Chat Log modal to read dialogue history
 * - Blurred/dimmed background with a glowing Spotlight Focus pointer ("HERE • ...") over the real app UI
 */
@Composable
fun ChibiMascotTutorialDialog(
    appLanguage: AppLanguage,
    isDarkTheme: Boolean = true,
    initialPart: Int = 1,
    onDismiss: () -> Unit,
    onNavigateToRoute: (String) -> Unit,
    onOpenFeedbackInbox: () -> Unit
) {
    val isTagalog = appLanguage.isTagalog
    val context = LocalContext.current

    val partsCatalog = remember {
        listOf(
            WalkthroughPartInfo(
                partNumber = 1,
                titleEn = "Part 1: Welcome & What iWani Is For",
                titleTl = "Bahagi 1: Ano ang iWani at Project Nexus",
                subtitleEn = "Meet Grace & Kian, top bar controls & app mission",
                subtitleTl = "Kilalanin sina Grace at Kian at ang layunin ng app",
                badgeText = "PART 1",
                accentColor = Color(0xFF38BDF8),
                iconRes = R.drawable.chibi_grace
            ),
            WalkthroughPartInfo(
                partNumber = 2,
                titleEn = "Part 2: FSL Camera & Detection Tips",
                titleTl = "Bahagi 2: FSL Kamera at Tips sa Kamay",
                subtitleEn = "Lighting tips, hand distance, mid-word typing & A–Z chart",
                subtitleTl = "Tamang ilaw, distansya ng kamay, pag-edit ng titik at A–Z chart",
                badgeText = "PART 2",
                accentColor = Color(0xFF10B981),
                iconRes = R.drawable.chibi_kian_explain
            ),
            WalkthroughPartInfo(
                partNumber = 3,
                titleEn = "Part 3: AI Translate & Quick Words",
                titleTl = "Bahagi 3: AI Pagsasalin at Quick Words",
                subtitleEn = "Output box, Qwen 2.5 sentences, Quick Words & Voice TTS",
                subtitleTl = "Output box, Qwen 2.5 na pangungusap, Quick Words at Boses",
                badgeText = "PART 3",
                accentColor = Color(0xFFF59E0B),
                iconRes = R.drawable.chibi_grace_point
            ),
            WalkthroughPartInfo(
                partNumber = 4,
                titleEn = "Part 4: Emergency SOS (ft. Shouko)",
                titleTl = "Bahagi 4: Saklolo SOS (kasama si Shouko)",
                subtitleEn = "Special Guest Shouko explains 1-tap loud SOS broadcasts",
                subtitleTl = "Ipapaliwanag ni Shouko ang 1-tap malakas na boses sa emergency",
                badgeText = "PART 4 • SOS",
                accentColor = Color(0xFFF472B6),
                iconRes = R.drawable.chibi_shouko
            ),
            WalkthroughPartInfo(
                partNumber = 5,
                titleEn = "Part 5: Offline AI & Custom Settings",
                titleTl = "Bahagi 5: Offline AI Model at Settings",
                subtitleEn = "Extracting 350 MB AI model, Text Size Scale, Fonts & Sounds",
                subtitleTl = "Pag-extract ng 350 MB AI model, Laki ng Teksto, Font at Tunog",
                badgeText = "PART 5",
                accentColor = Color(0xFF818CF8),
                iconRes = R.drawable.chibi_kian_thumbs
            ),
            WalkthroughPartInfo(
                partNumber = 6,
                titleEn = "Part 6: Thank You & Team Inbox",
                titleTl = "Bahagi 6: Pasasalamat at Team Inbox",
                subtitleEn = "Farewell from Grace, Kian & Shouko + send team feedback",
                subtitleTl = "Pasasalamat nina Grace, Kian at Shouko + magpadala ng feedback",
                badgeText = "PART 6",
                accentColor = Color(0xFFFBBF24),
                iconRes = R.drawable.chibi_grace_happy
            )
        )
    }

    val allDialogues = remember {
        listOf(
            // ==================== PART 1: WELCOME & WHAT IWANI IS FOR ====================
            WalkthroughDialogueLine(
                partNumber = 1,
                speaker = VnSpeaker.GRACE,
                leftSpriteRes = R.drawable.chibi_grace,
                rightSpriteRes = R.drawable.chibi_kian,
                activeSide = "LEFT",
                backgroundRoute = "home",
                focusZone = SpotlightFocusZone.CENTER_HERO,
                focusLabelEn = "HERE • iWani Home Workspace",
                focusLabelTl = "DITO • Home Screen ng iWani",
                textEn = "Hi there! My name is Grace, and I'm one of the developers of Project Nexus from ICCT Colleges. Welcome to iWani!",
                textTl = "Kumusta po! Ang pangalan ko ay Grace, at isa ako sa mga developer ng Project Nexus mula sa ICCT Colleges. Maligayang pagdating sa iWani!"
            ),
            WalkthroughDialogueLine(
                partNumber = 1,
                speaker = VnSpeaker.KIAN,
                leftSpriteRes = R.drawable.chibi_grace,
                rightSpriteRes = R.drawable.chibi_kian_thumbs,
                activeSide = "RIGHT",
                backgroundRoute = "home",
                focusZone = SpotlightFocusZone.CENTER_HERO,
                focusLabelEn = "HERE • Bridging Deaf & Hearing Communities",
                focusLabelTl = "DITO • Tulay sa Komunikasyon ng Bingi at Nakakarinig",
                textEn = "And I'm Kian, also part of Project Nexus and a developer of this app! We built iWani so Deaf and mute Filipinos can communicate effortlessly using Filipino Sign Language (FSL) and offline AI.",
                textTl = "At ako naman si Kian, bahagi rin ng Project Nexus at developer ng app na ito! Binuo namin ang iWani upang madaling makipag-usap ang mga Bingi at pipi gamit ang Filipino Sign Language (FSL) at offline AI."
            ),
            WalkthroughDialogueLine(
                partNumber = 1,
                speaker = VnSpeaker.GRACE,
                leftSpriteRes = R.drawable.chibi_grace_point,
                rightSpriteRes = R.drawable.chibi_kian,
                activeSide = "LEFT",
                backgroundRoute = "home",
                focusZone = SpotlightFocusZone.TOP_BAR,
                focusLabelEn = "HERE • Top Bar: EN/TL Flags & Theme Wave Toggle",
                focusLabelTl = "DITO • Top Bar: Watawat ng EN/TL at Theme Toggle",
                textEn = "Look up at the Top Bar! You can tap the USA Flag (EN) or Philippine Flag (TL) anytime to switch the whole app between English and Tagalog, or tap the Sun/Moon icon for a smooth wave transition.",
                textTl = "Tingnan ang Top Bar sa itaas! Maaari mong pindutin ang watawat ng USA (EN) o Pilipinas (TL) anumang oras upang magpalit sa English o Tagalog, o ang Sun/Moon icon para sa wave theme transition."
            ),

            // ==================== PART 2: FSL CAMERA & HAND DETECTION TIPS ====================
            WalkthroughDialogueLine(
                partNumber = 2,
                speaker = VnSpeaker.KIAN,
                leftSpriteRes = R.drawable.chibi_grace,
                rightSpriteRes = R.drawable.chibi_kian_explain,
                activeSide = "RIGHT",
                backgroundRoute = "camera",
                focusZone = SpotlightFocusZone.CAMERA_VIEWFINDER,
                focusLabelEn = "HERE • Live 26-Letter 3D Hand Tracking Viewfinder",
                focusLabelTl = "DITO • Live 26-Titik 3D Hand Tracking Kamera",
                textEn = "Now we're inside the FSL Camera! Before you start signing, here is our #1 developer tip: make sure your place is well-lit so the camera can clearly see your fingers and knuckles.",
                textTl = "Nandito na tayo sa FSL Kamera! Bago mag-senyas, narito ang aming #1 tip: siguraduhing maliwanag ang iyong paligid upang malinaw na makita ng kamera ang bawat daliri ng iyong kamay."
            ),
            WalkthroughDialogueLine(
                partNumber = 2,
                speaker = VnSpeaker.GRACE,
                leftSpriteRes = R.drawable.chibi_grace_point,
                rightSpriteRes = R.drawable.chibi_kian_explain,
                activeSide = "LEFT",
                backgroundRoute = "camera",
                focusZone = SpotlightFocusZone.CAMERA_VIEWFINDER,
                focusLabelEn = "TIP • Keep Hand 1–2 Feet Away (Not Too Close!)",
                focusLabelTl = "TIP • Huwag Ilapit Nang Sobra ang Kamay sa Kamera!",
                textEn = "That's right! Also, don't place your hand too close to the camera lens. Keep your hand centered inside the frame at a comfortable distance so all 21 hand landmarks stay visible.",
                textTl = "Tama iyan! Huwag din ilapit nang sobra ang iyong kamay sa lente ng kamera. Panatilihin ito sa gitna sa tamang distansya upang makita nang buo ang iyong kamay at mga daliri."
            ),
            WalkthroughDialogueLine(
                partNumber = 2,
                speaker = VnSpeaker.KIAN,
                leftSpriteRes = R.drawable.chibi_grace_point,
                rightSpriteRes = R.drawable.chibi_kian_thumbs,
                activeSide = "RIGHT",
                backgroundRoute = "camera",
                focusZone = SpotlightFocusZone.FORMED_WORD_BOX,
                focusLabelEn = "HERE • Tap Between Letters to Type or Edit Mid-Word!",
                focusLabelTl = "DITO • Pindutin sa Pagitan ng mga Titik para Mag-edit!",
                textEn = "Hold a hand sign steady for a second to lock in the letter! And if you miss a letter in the middle of a word, just tap directly between the letters in the 'Formed Word' box or use the ◀ ▶ arrows to type or delete mid-word!",
                textTl = "Panatilihing steady ang senyas nang isang segundo para ma-lock ang titik! At kung may kulang na titik sa gitna ng salita, pindutin lang sa pagitan ng mga titik sa 'Formed Word' box o gamitin ang ◀ ▶ arrows para mag-type!"
            ),
            WalkthroughDialogueLine(
                partNumber = 2,
                speaker = VnSpeaker.GRACE,
                leftSpriteRes = R.drawable.chibi_grace_happy,
                rightSpriteRes = R.drawable.chibi_kian_thumbs,
                activeSide = "LEFT",
                backgroundRoute = "camera",
                focusZone = SpotlightFocusZone.ALPHABET_CHART_BTN,
                focusLabelEn = "HERE • Full-Portrait A–Z FSL Alphabet Chart",
                focusLabelTl = "DITO • Full-Portrait A–Z FSL Alphabet Chart",
                textEn = "Forgot how a letter looks in Filipino Sign Language? Tap the 'Chart' button on the camera screen (or the FSL Alphabet tile on Home) to open our enlarged Full-Portrait A–Z guide with pinch-to-zoom!",
                textTl = "Nakalimutan kung paano i-senyas ang isang titik? Pindutin ang 'Chart' button sa kamera (o sa Home screen) upang buksan ang malaking Full-Portrait A–Z chart na pwedeng i-zoom!"
            ),

            // ==================== PART 3: AI TRANSLATE & QUICK WORDS ====================
            WalkthroughDialogueLine(
                partNumber = 3,
                speaker = VnSpeaker.GRACE,
                leftSpriteRes = R.drawable.chibi_grace_point,
                rightSpriteRes = R.drawable.chibi_kian,
                activeSide = "LEFT",
                backgroundRoute = "translate",
                focusZone = SpotlightFocusZone.TRANSLATE_OUTPUT_BOX,
                focusLabelEn = "HERE • Output Box & AI Natural Sentence Converter",
                focusLabelTl = "DITO • Output Box at AI Tagabuo ng Pangungusap",
                textEn = "Welcome to Part 3: the Translate screen! When you sign root words like 'TUBIG' or 'PAGKAIN', they appear in the Output box at the top.",
                textTl = "Narito ang Bahagi 3: ang Translate screen! Kapag nag-senyas ka o pumili ng mga salita tulad ng 'TUBIG' o 'PAGKAIN', lalabas ito sa Output box sa itaas."
            ),
            WalkthroughDialogueLine(
                partNumber = 3,
                speaker = VnSpeaker.KIAN,
                leftSpriteRes = R.drawable.chibi_grace,
                rightSpriteRes = R.drawable.chibi_kian_explain,
                activeSide = "RIGHT",
                backgroundRoute = "translate",
                focusZone = SpotlightFocusZone.QUICK_WORDS_GRID,
                focusLabelEn = "HERE • Frequently Used Quick Words & Categories",
                focusLabelTl = "DITO • Madalas Gamitin na Quick Words at Kategorya",
                textEn = "Need to reply fast? Tap any chip under '★ Frequently Used' — like Pagkain, Tubig, Bili, Pahinga, Tulong, Salamat, or Magkano — and our offline Qwen 2.5 AI turns it into a polite Tagalog or English sentence and speaks it out loud!",
                textTl = "Kailangan ng mabilis na sagot? Pumili lang sa '★ Madalas Gamitin' — tulad ng Pagkain, Tubig, Bili, Pahinga, Tulong, Salamat, o Magkano — at gagawin itong magalang na pangungusap ng AI at ipaparinig sa boses!"
            ),

            // ==================== PART 4: EMERGENCY SOS (FT. SHOUKO) ====================
            WalkthroughDialogueLine(
                partNumber = 4,
                speaker = VnSpeaker.SHOUKO,
                leftSpriteRes = R.drawable.chibi_shouko_notebook,
                rightSpriteRes = R.drawable.chibi_grace,
                activeSide = "LEFT",
                backgroundRoute = "sos",
                focusZone = SpotlightFocusZone.SOS_CARDS,
                focusLabelEn = "HERE • 1-Tap Emergency SOS Voice Broadcast Cards",
                focusLabelTl = "DITO • 1-Tap Emergency SOS na Malakas na Boses",
                textEn = "Hello! I'm Shouko. In Part 4, I want to show you the Emergency SOS tab. If you ever face a medical emergency, danger, or get lost, just tap any red SOS card once.",
                textTl = "Kumusta po! Ako si Shouko. Sa Bahagi 4, ipapakita ko sa inyo ang Emergency SOS tab. Kapag may biglaang panganib, sakit, o naliligaw ka, pindutin lang nang isang beses ang alinmang SOS card."
            ),
            WalkthroughDialogueLine(
                partNumber = 4,
                speaker = VnSpeaker.SHOUKO,
                leftSpriteRes = R.drawable.chibi_shouko_happy,
                rightSpriteRes = R.drawable.chibi_grace_happy,
                activeSide = "LEFT",
                backgroundRoute = "sos",
                focusZone = SpotlightFocusZone.SOS_CARDS,
                focusLabelEn = "HERE • Loud Voice Broadcast + Reactive Audio Wave",
                focusLabelTl = "DITO • Agarang Malakas na Boses + Audio Wave",
                textEn = "Your phone will immediately speak out loud in a clear voice to call a Doctor, Police/Barangay Tanod, ask for Directions, or contact your Family — even without internet!",
                textTl = "Agad na magsasalita nang malakas at malinaw ang iyong phone upang humingi ng Doktor, Pulis/Barangay Tanod, magtanong ng Direksyon, o tumawag sa Pamilya — kahit walang internet!"
            ),
            WalkthroughDialogueLine(
                partNumber = 4,
                speaker = VnSpeaker.GRACE,
                leftSpriteRes = R.drawable.chibi_shouko_happy,
                rightSpriteRes = R.drawable.chibi_grace_point,
                activeSide = "RIGHT",
                backgroundRoute = "sos",
                focusZone = SpotlightFocusZone.SOS_CARDS,
                focusLabelEn = "TIP • Tap Card Again or Stop Button to Silence",
                focusLabelTl = "TIP • Pindutin Muli o ang Stop Icon para Ihinto",
                textEn = "Thank you so much for explaining the SOS feature, Shouko! And remember, you can tap the same card again or press the red Stop icon at the top-right to stop the voice broadcast anytime.",
                textTl = "Maraming salamat sa pagpapaliwanag ng SOS feature, Shouko! At tandaan, maaari mong pindutin muli ang card o ang Stop icon sa itaas upang ihinto agad ang boses."
            ),

            // ==================== PART 5: OFFLINE AI & CUSTOM SETTINGS ====================
            WalkthroughDialogueLine(
                partNumber = 5,
                speaker = VnSpeaker.KIAN,
                leftSpriteRes = R.drawable.chibi_grace,
                rightSpriteRes = R.drawable.chibi_kian_explain,
                activeSide = "RIGHT",
                backgroundRoute = "settings",
                focusZone = SpotlightFocusZone.SETTINGS_CARDS,
                focusLabelEn = "HERE • Font Style, Text Size Scale & Sound Studio",
                focusLabelTl = "DITO • Estilo ng Font, Laki ng Teksto at Tunog",
                textEn = "In Part 5, check out the Settings tab! Here you can change the app's Typeface, drag the 'Text Size Scale' (Laki ng Teksto) slider from 85% to 125% to resize all text live, and pick from 4 button click sounds!",
                textTl = "Sa Bahagi 5, narito ang Settings tab! Pwede mong palitan ang Font ng buong app, i-adjust ang 'Laki ng Teksto' mula 85% hanggang 125%, at pumili sa 4 na tunog ng button!"
            ),
            WalkthroughDialogueLine(
                partNumber = 5,
                speaker = VnSpeaker.GRACE,
                leftSpriteRes = R.drawable.chibi_grace_point,
                rightSpriteRes = R.drawable.chibi_kian_thumbs,
                activeSide = "LEFT",
                backgroundRoute = "settings",
                focusZone = SpotlightFocusZone.TOP_BAR,
                focusLabelEn = "HERE • Tap 'PROJECT NEXUS' Badge or AI Model Manager",
                focusLabelTl = "DITO • Pindutin ang 'PROJECT NEXUS' Badge o AI Model",
                textEn = "You can also tap the metallic 'PROJECT NEXUS' badge to view our full Ascendens development team, or open 'Extracting AI Model' to manage your 350 MB offline neural engine!",
                textTl = "Maaari mo ring pindutin ang 'PROJECT NEXUS' badge upang makita ang buong Ascendens team, o buksan ang 'Extracting AI Model' para sa 350 MB offline neural engine!"
            ),

            // ==================== PART 6: THANK YOU & TEAM INBOX ====================
            WalkthroughDialogueLine(
                partNumber = 6,
                speaker = VnSpeaker.ALL_THREE,
                leftSpriteRes = R.drawable.chibi_grace_happy,
                rightSpriteRes = R.drawable.chibi_kian_thumbs,
                centerSpriteRes = R.drawable.chibi_shouko_happy,
                activeSide = "ALL_THREE",
                backgroundRoute = "home",
                focusZone = SpotlightFocusZone.NONE,
                focusLabelEn = "THANK YOU • Project Nexus (Ascendens • ICCT Colleges)",
                focusLabelTl = "MARAMING SALAMAT • Project Nexus (Ascendens • ICCT)",
                textEn = "Thank you so much for exploring iWani with us! You can replay any Part of this walkthrough anytime from Home or Settings. If you have feedback or suggestions for our team, tap 'Team Inbox' below!",
                textTl = "Maraming-maraming salamat po sa pagsama sa amin sa iWani! Maaari mong balikan ang alinmang Bahagi (Part 1–6) sa Home o Settings. Kung may feedback ka para sa aming team, pindutin ang 'Team Inbox' sa ibaba!"
            )
        )
    }

    val startLineIndex = remember(initialPart) {
        allDialogues.indexOfFirst { it.partNumber == initialPart }.coerceAtLeast(0)
    }
    var currentIndex by remember { mutableIntStateOf(startLineIndex) }
    var showPartsSheet by remember { mutableStateOf(false) }
    var showChatLogModal by remember { mutableStateOf(false) }

    val currentLine = allDialogues[currentIndex]
    val currentPartInfo = partsCatalog.firstOrNull { it.partNumber == currentLine.partNumber } ?: partsCatalog.first()
    val fullDialogueText = if (isTagalog) currentLine.textTl else currentLine.textEn

    // Sync background route automatically as the walkthrough progresses through Parts!
    LaunchedEffect(currentLine.backgroundRoute) {
        onNavigateToRoute(currentLine.backgroundRoute)
    }

    // Typewriter effect state: reveals characters one by one
    var visibleCharCount by remember(currentIndex, isTagalog) { mutableIntStateOf(0) }
    val isTyping = visibleCharCount < fullDialogueText.length

    LaunchedEffect(currentIndex, isTagalog) {
        visibleCharCount = 0
        while (visibleCharCount < fullDialogueText.length) {
            delay(18L)
            visibleCharCount = (visibleCharCount + 1).coerceAtMost(fullDialogueText.length)
        }
    }

    // Puppet up-and-down movement ONLY when the dialogue line first starts speaking, then back to static (0f)!
    val puppetOffset = remember { Animatable(0f) }
    LaunchedEffect(currentIndex) {
        UiSoundManager.playVillagerVoice(
            context = context,
            character = currentLine.speaker.voiceId,
            cue = currentLine.speaker.voiceCue
        )
        puppetOffset.snapTo(0f)
        // 3 lively puppet hops while starting to speak, then settle to static 0f
        val hops = listOf(-16f, 0f, -12f, 0f, -7f, 0f)
        for (target in hops) {
            puppetOffset.animateTo(
                targetValue = target,
                animationSpec = tween(durationMillis = 80, easing = FastOutSlowInEasing)
            )
        }
        puppetOffset.snapTo(0f)
    }

    // Pulsing indicator for spotlight focus box & next-triangle
    val infiniteTransition = rememberInfiniteTransition(label = "WalkthroughPulse")
    val spotlightPulse by infiniteTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.03f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "SpotlightPulse"
    )

    fun handleAdvanceOrCompleteTyping() {
        if (isTyping) {
            // First tap completes the typewriter line immediately
            visibleCharCount = fullDialogueText.length
        } else if (currentIndex < allDialogues.lastIndex) {
            UiSoundManager.playClick(context)
            currentIndex++
        } else {
            UiSoundManager.playSuccess(context)
            onDismiss()
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xBF040814),
                            Color(0xCC070E20),
                            Color(0xEB050914)
                        )
                    )
                )
                // Tapping anywhere on the walkthrough advances the dialogue (or finishes typing first)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    handleAdvanceOrCompleteTyping()
                }
        ) {
            // ==================== 1. SPOTLIGHT FOCUS OVERLAY ("HERE • ...") ====================
            if (currentLine.focusZone != SpotlightFocusZone.NONE) {
                SpotlightFocusHighlight(
                    zone = currentLine.focusZone,
                    label = if (isTagalog) currentLine.focusLabelTl else currentLine.focusLabelEn,
                    accentColor = currentLine.speaker.accentColor,
                    pulseScale = spotlightPulse
                )
            }

            // ==================== 2. TOP BAR: PART SELECTOR (LEFT) + CHAT LOG & SKIP (RIGHT) ====================
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 14.dp, vertical = 10.dp)
                    .align(Alignment.TopCenter),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Part Selector Pill (Allows jumping to Part 1..6 anytime!)
                Surface(
                    shape = RoundedCornerShape(50),
                    color = Color(0xE60F172A),
                    border = BorderStroke(1.2.dp, currentPartInfo.accentColor.copy(alpha = 0.8f)),
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .padding(end = 8.dp)
                        .interactiveBounceClick { showPartsSheet = true }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.FormatListBulleted,
                            contentDescription = "Select Tutorial Part",
                            tint = currentPartInfo.accentColor,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = if (isTagalog) currentPartInfo.titleTl else currentPartInfo.titleEn,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            maxLines = 1,
                            softWrap = false,
                            modifier = Modifier
                                .widthIn(max = 175.dp)
                                .autoSlideIfOverflow()
                        )
                        Spacer(Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = null,
                            tint = currentPartInfo.accentColor,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // Top-Right Action Pills: Chat Log + Skip
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Chat Log Button (Top Right)
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = Color(0xE60F172A),
                        border = BorderStroke(1.dp, Color(0xFF38BDF8).copy(alpha = 0.7f)),
                        modifier = Modifier.interactiveBounceClick { showChatLogModal = true }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 11.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.History,
                                contentDescription = "Chat Log",
                                tint = Color(0xFF38BDF8),
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(Modifier.width(5.dp))
                            Text(
                                text = if (isTagalog) "Log" else "Log",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFFE2E8F0),
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }

                    // Skip Button
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = Color(0xE61E293B),
                        border = BorderStroke(1.dp, Color(0xFF475569)),
                        modifier = Modifier.interactiveBounceClick { onDismiss() }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 11.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (isTagalog) "Laktawan" else "Skip",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFE2E8F0),
                                maxLines = 1,
                                softWrap = false
                            )
                            Spacer(Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Skip Walkthrough",
                                tint = Color(0xFF94A3B8),
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }

            // ==================== 3. BOTTOM VISUAL NOVEL STAGE (CHIBIS + BLUE ARCHIVE DIALOGUE BOX) ====================
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
            ) {
                // Character Stage Row (Left / Center / Right Chibis standing right above the dialogue bar)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(215.dp)
                        .padding(horizontal = 12.dp),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    if (currentLine.activeSide == "ALL_THREE") {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            VnCharacterStageSprite(
                                drawableRes = currentLine.leftSpriteRes ?: R.drawable.chibi_grace_happy,
                                isSpeaking = true,
                                puppetOffsetY = puppetOffset.value,
                                glowColor = Color(0xFF38BDF8),
                                spriteHeight = 185.dp
                            )
                            VnCharacterStageSprite(
                                drawableRes = currentLine.centerSpriteRes ?: R.drawable.chibi_shouko_happy,
                                isSpeaking = true,
                                puppetOffsetY = puppetOffset.value * 0.85f,
                                glowColor = Color(0xFFF472B6),
                                spriteHeight = 190.dp
                            )
                            VnCharacterStageSprite(
                                drawableRes = currentLine.rightSpriteRes ?: R.drawable.chibi_kian_thumbs,
                                isSpeaking = true,
                                puppetOffsetY = puppetOffset.value * 0.9f,
                                glowColor = Color(0xFFF59E0B),
                                spriteHeight = 185.dp
                            )
                        }
                    } else {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            // Left Character Slot
                            Box(
                                modifier = Modifier.weight(1f),
                                contentAlignment = Alignment.BottomStart
                            ) {
                                if (currentLine.leftSpriteRes != null) {
                                    val isLeftActive = currentLine.activeSide == "LEFT" || currentLine.activeSide == "BOTH"
                                    VnCharacterStageSprite(
                                        drawableRes = currentLine.leftSpriteRes,
                                        isSpeaking = isLeftActive,
                                        puppetOffsetY = if (isLeftActive) puppetOffset.value else 0f,
                                        glowColor = if (currentLine.leftSpriteRes == R.drawable.chibi_shouko ||
                                            currentLine.leftSpriteRes == R.drawable.chibi_shouko_notebook ||
                                            currentLine.leftSpriteRes == R.drawable.chibi_shouko_happy
                                        ) Color(0xFFF472B6) else Color(0xFF38BDF8),
                                        spriteHeight = 205.dp
                                    )
                                }
                            }

                            // Right Character Slot
                            Box(
                                modifier = Modifier.weight(1f),
                                contentAlignment = Alignment.BottomEnd
                            ) {
                                if (currentLine.rightSpriteRes != null) {
                                    val isRightActive = currentLine.activeSide == "RIGHT" || currentLine.activeSide == "BOTH"
                                    VnCharacterStageSprite(
                                        drawableRes = currentLine.rightSpriteRes,
                                        isSpeaking = isRightActive,
                                        puppetOffsetY = if (isRightActive) puppetOffset.value else 0f,
                                        glowColor = if (currentLine.rightSpriteRes == R.drawable.chibi_kian ||
                                            currentLine.rightSpriteRes == R.drawable.chibi_kian_explain ||
                                            currentLine.rightSpriteRes == R.drawable.chibi_kian_thumbs
                                        ) Color(0xFFF59E0B) else Color(0xFF38BDF8),
                                        spriteHeight = 205.dp
                                    )
                                }
                            }
                        }
                    }
                }

                // ==================== BLUE ARCHIVE STYLE DIALOGUE PANEL ====================
                Surface(
                    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                    color = Color(0xF2081022),
                    border = BorderStroke(
                        width = 1.5.dp,
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                currentLine.speaker.accentColor.copy(alpha = 0.85f),
                                Color(0xFF334155),
                                currentLine.speaker.accentColor.copy(alpha = 0.5f)
                            )
                        )
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 16.dp)
                    ) {
                        // Speaker Name + Role Beside It (Blue Archive Layout: "Grace  Developer • Project Nexus")
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.Bottom,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.Bottom,
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(end = 8.dp)
                            ) {
                                Text(
                                    text = currentLine.speaker.displayName,
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White,
                                    maxLines = 1,
                                    softWrap = false
                                )
                                Spacer(Modifier.width(10.dp))
                                Text(
                                    text = if (isTagalog) currentLine.speaker.roleTl else currentLine.speaker.roleEn,
                                    fontSize = 13.5.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = currentLine.speaker.accentColor,
                                    maxLines = 1,
                                    softWrap = false,
                                    modifier = Modifier
                                        .padding(bottom = 2.dp)
                                        .autoSlideIfOverflow()
                                )
                            }

                            // Part Badge on right of speaker header
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = currentPartInfo.accentColor.copy(alpha = 0.18f),
                                border = BorderStroke(1.dp, currentPartInfo.accentColor.copy(alpha = 0.5f))
                            ) {
                                Text(
                                    text = "${currentPartInfo.badgeText} • ${currentIndex + 1}/${allDialogues.size}",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = currentPartInfo.accentColor,
                                    maxLines = 1,
                                    softWrap = false,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }

                        Spacer(Modifier.height(8.dp))

                        // Sleek Blue Archive-style glowing horizontal divider line under Name + Role
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(2.dp)
                                .background(
                                    Brush.horizontalGradient(
                                        colors = listOf(
                                            currentLine.speaker.accentColor,
                                            currentLine.speaker.accentColor.copy(alpha = 0.35f),
                                            Color.Transparent
                                        )
                                    )
                                )
                        )

                        Spacer(Modifier.height(12.dp))

                        // Typewriter Animated Dialogue Text Box
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 86.dp, max = 130.dp)
                                .verticalScroll(rememberScrollState())
                        ) {
                            Text(
                                text = fullDialogueText.take(visibleCharCount),
                                fontSize = 15.sp,
                                lineHeight = 22.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFFF1F5F9)
                            )
                        }

                        Spacer(Modifier.height(12.dp))

                        // Bottom Navigation Bar inside the VN Panel: [ ◀ Prev ]  [ Tap anywhere to continue ▾ ]  [ Next / Team Inbox ]
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Prev Button
                            if (currentIndex > 0) {
                                OutlinedButton(
                                    onClick = {
                                        UiSoundManager.playClick(context)
                                        currentIndex--
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(1.dp, Color(0xFF475569)),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                    modifier = Modifier.height(38.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "Previous Dialogue",
                                        tint = Color(0xFFE2E8F0),
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(Modifier.width(5.dp))
                                    Text(
                                        text = if (isTagalog) "Nakaraan" else "Prev",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFE2E8F0),
                                        maxLines = 1,
                                        softWrap = false
                                    )
                                }
                            } else {
                                Spacer(Modifier.width(72.dp))
                            }

                            // Center Tap Prompt
                            Text(
                                text = if (isTyping) {
                                    if (isTagalog) "Pindutin para kumpletuhin..." else "Tap to show full text..."
                                } else {
                                    if (isTagalog) "Pindutin kahit saan ▾" else "Tap anywhere to continue ▾"
                                },
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = currentLine.speaker.accentColor.copy(alpha = 0.9f),
                                maxLines = 1,
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(horizontal = 8.dp)
                                    .autoSlideIfOverflow(),
                                textAlign = TextAlign.Center
                            )

                            // Right Action: Next OR Finale Team Inbox + Done
                            if (currentIndex == allDialogues.lastIndex) {
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Button(
                                        onClick = {
                                            UiSoundManager.playSuccess(context)
                                            onOpenFeedbackInbox()
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B)),
                                        shape = RoundedCornerShape(12.dp),
                                        contentPadding = PaddingValues(horizontal = 11.dp, vertical = 6.dp),
                                        modifier = Modifier.height(38.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Inbox,
                                            contentDescription = "Team Inbox",
                                            tint = Color(0xFF0F172A),
                                            modifier = Modifier.size(15.dp)
                                        )
                                        Spacer(Modifier.width(4.dp))
                                        Text(
                                            text = "Team Inbox",
                                            fontSize = 11.5.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Color(0xFF0F172A),
                                            maxLines = 1,
                                            softWrap = false
                                        )
                                    }

                                    Button(
                                        onClick = {
                                            UiSoundManager.playClick(context)
                                            onDismiss()
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                                        shape = RoundedCornerShape(12.dp),
                                        contentPadding = PaddingValues(horizontal = 11.dp, vertical = 6.dp),
                                        modifier = Modifier.height(38.dp)
                                    ) {
                                        Text(
                                            text = if (isTagalog) "Tapos" else "Finish",
                                            fontSize = 11.5.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Color.White,
                                            maxLines = 1,
                                            softWrap = false
                                        )
                                    }
                                }
                            } else {
                                Button(
                                    onClick = { handleAdvanceOrCompleteTyping() },
                                    colors = ButtonDefaults.buttonColors(containerColor = currentLine.speaker.accentColor),
                                    shape = RoundedCornerShape(12.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                    modifier = Modifier.height(38.dp)
                                ) {
                                    Text(
                                        text = if (isTagalog) "Sunod" else "Next",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color(0xFF0F172A),
                                        maxLines = 1,
                                        softWrap = false
                                    )
                                    Spacer(Modifier.width(4.dp))
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                        contentDescription = "Next",
                                        tint = Color(0xFF0F172A),
                                        modifier = Modifier.size(15.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // ==================== 4. PART SELECTOR MODAL (JUMP TO PART 1..6 ANYTIME) ====================
            if (showPartsSheet) {
                WalkthroughPartSelectorModal(
                    parts = partsCatalog,
                    currentPartNumber = currentLine.partNumber,
                    isTagalog = isTagalog,
                    onSelectPart = { selectedPartNum ->
                        UiSoundManager.playClick(context)
                        val targetIdx = allDialogues.indexOfFirst { it.partNumber == selectedPartNum }
                        if (targetIdx >= 0) {
                            currentIndex = targetIdx
                        }
                        showPartsSheet = false
                    },
                    onDismiss = { showPartsSheet = false }
                )
            }

            // ==================== 5. CHAT LOG MODAL (VIEW DIALOGUE HISTORY SO FAR) ====================
            if (showChatLogModal) {
                WalkthroughChatLogModal(
                    dialoguesSoFar = allDialogues.take(currentIndex + 1),
                    isTagalog = isTagalog,
                    onJumpToLine = { lineIdx ->
                        UiSoundManager.playClick(context)
                        currentIndex = lineIdx
                        showChatLogModal = false
                    },
                    onDismiss = { showChatLogModal = false }
                )
            }
        }
    }
}

/**
 * Renders a chibi character sprite on the Visual Novel stage.
 * - Moves up and down like a puppet ONLY when `puppetOffsetY` is non-zero (while speaking), then stays static.
 * - Slightly dims non-speaking characters so the active speaker stands out clearly.
 */
@Composable
private fun VnCharacterStageSprite(
    drawableRes: Int,
    isSpeaking: Boolean,
    puppetOffsetY: Float,
    glowColor: Color,
    spriteHeight: Dp
) {
    val targetAlpha by animateFloatAsState(
        targetValue = if (isSpeaking) 1f else 0.52f,
        animationSpec = tween(240),
        label = "SpeakerAlpha"
    )
    val targetScale by animateFloatAsState(
        targetValue = if (isSpeaking) 1.03f else 0.93f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "SpeakerScale"
    )

    Box(
        contentAlignment = Alignment.BottomCenter,
        modifier = Modifier
            .height(spriteHeight)
            .graphicsLayer {
                alpha = targetAlpha
                scaleX = targetScale
                scaleY = targetScale
                translationY = puppetOffsetY
            }
    ) {
        if (isSpeaking) {
            Canvas(
                modifier = Modifier
                    .size(spriteHeight * 0.82f)
                    .align(Alignment.Center)
            ) {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            glowColor.copy(alpha = 0.34f),
                            glowColor.copy(alpha = 0.08f),
                            Color.Transparent
                        )
                    ),
                    radius = size.minDimension / 2f
                )
            }
        }

        // Crossfade smoothly whenever the character changes pose!
        Crossfade(
            targetState = drawableRes,
            animationSpec = tween(220),
            label = "ChibiPoseCrossfade"
        ) { targetRes ->
            Image(
                painter = painterResource(id = targetRes),
                contentDescription = null,
                modifier = Modifier.height(spriteHeight),
                contentScale = ContentScale.Fit
            )
        }
    }
}

/**
 * Spotlight Focus Box ("HERE • ...") that highlights the relevant UI zone on the blurred background screen.
 */
@Composable
private fun BoxScope.SpotlightFocusHighlight(
    zone: SpotlightFocusZone,
    label: String,
    accentColor: Color,
    pulseScale: Float
) {
    val topPadding: Dp
    val boxHeight: Dp

    when (zone) {
        SpotlightFocusZone.TOP_BAR -> {
            topPadding = 52.dp
            boxHeight = 68.dp
        }
        SpotlightFocusZone.CENTER_HERO -> {
            topPadding = 128.dp
            boxHeight = 170.dp
        }
        SpotlightFocusZone.CAMERA_VIEWFINDER -> {
            topPadding = 122.dp
            boxHeight = 210.dp
        }
        SpotlightFocusZone.FORMED_WORD_BOX -> {
            topPadding = 315.dp
            boxHeight = 105.dp
        }
        SpotlightFocusZone.ALPHABET_CHART_BTN -> {
            topPadding = 118.dp
            boxHeight = 82.dp
        }
        SpotlightFocusZone.TRANSLATE_OUTPUT_BOX -> {
            topPadding = 126.dp
            boxHeight = 140.dp
        }
        SpotlightFocusZone.QUICK_WORDS_GRID -> {
            topPadding = 265.dp
            boxHeight = 145.dp
        }
        SpotlightFocusZone.SOS_CARDS -> {
            topPadding = 155.dp
            boxHeight = 195.dp
        }
        SpotlightFocusZone.SETTINGS_CARDS -> {
            topPadding = 155.dp
            boxHeight = 195.dp
        }
        SpotlightFocusZone.NONE -> return
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = topPadding, start = 18.dp, end = 18.dp)
            .align(Alignment.TopCenter)
            .scale(pulseScale),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Glowing "HERE" Pointer Pill
        Surface(
            shape = RoundedCornerShape(50),
            color = accentColor,
            shadowElevation = 8.dp
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.MyLocation,
                    contentDescription = null,
                    tint = Color(0xFF0F172A),
                    modifier = Modifier.size(15.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = label,
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF0F172A),
                    maxLines = 1,
                    softWrap = false,
                    modifier = Modifier.autoSlideIfOverflow()
                )
            }
        }

        Spacer(Modifier.height(6.dp))

        // Spotlight Frame with BorderBeam around the highlighted app section
        BorderBeamBox(
            active = true,
            cornerRadius = 20.dp,
            beamColor = accentColor,
            secondaryColor = Color.White,
            baseBorderColor = accentColor.copy(alpha = 0.65f),
            backgroundColor = accentColor.copy(alpha = 0.06f),
            modifier = Modifier
                .fillMaxWidth()
                .height(boxHeight)
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "✦",
                    fontSize = 22.sp,
                    color = accentColor.copy(alpha = 0.45f)
                )
            }
        }
    }
}

/**
 * Modal allowing the user to pick any Tutorial Part (Part 1 to Part 6) directly!
 */
@Composable
private fun WalkthroughPartSelectorModal(
    parts: List<WalkthroughPartInfo>,
    currentPartNumber: Int,
    isTagalog: Boolean,
    onSelectPart: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xD9030712))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onDismiss
            )
            .padding(18.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color(0xFF0F172A),
            border = BorderStroke(1.5.dp, Color(0xFF38BDF8).copy(alpha = 0.6f)),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 560.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {}
                )
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isTagalog) "Pumili ng Bahagi ng Tutorial" else "Select Walkthrough Part",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            maxLines = 1,
                            modifier = Modifier.autoSlideIfOverflow()
                        )
                        Text(
                            text = if (isTagalog)
                                "Pumunta agad sa feature na gusto mong aralin"
                            else
                                "Jump directly to any topic you want to review",
                            fontSize = 12.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }
                }

                Spacer(Modifier.height(12.dp))

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    itemsIndexed(parts) { _, part ->
                        val isCurrent = part.partNumber == currentPartNumber
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = if (isCurrent) part.accentColor.copy(alpha = 0.18f) else Color(0xFF1E293B),
                            border = BorderStroke(
                                width = if (isCurrent) 1.5.dp else 1.dp,
                                color = if (isCurrent) part.accentColor else Color(0xFF334155)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .interactiveBounceClick { onSelectPart(part.partNumber) }
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Image(
                                    painter = painterResource(id = part.iconRes),
                                    contentDescription = part.titleEn,
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color(0xFF0F172A))
                                )
                                Spacer(Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = if (isTagalog) part.titleTl else part.titleEn,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 13.5.sp,
                                        color = if (isCurrent) part.accentColor else Color.White,
                                        maxLines = 1,
                                        modifier = Modifier.autoSlideIfOverflow()
                                    )
                                    Spacer(Modifier.height(2.dp))
                                    Text(
                                        text = if (isTagalog) part.subtitleTl else part.subtitleEn,
                                        fontSize = 11.5.sp,
                                        color = Color(0xFFCBD5E1),
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                Spacer(Modifier.width(8.dp))
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = null,
                                    tint = part.accentColor
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Blue Archive-style Chat Log Modal (Top-Right "Log" button) showing all dialogue lines so far.
 */
@Composable
private fun WalkthroughChatLogModal(
    dialoguesSoFar: List<WalkthroughDialogueLine>,
    isTagalog: Boolean,
    onJumpToLine: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xE0030712))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onDismiss
            )
            .padding(18.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color(0xFF0F172A),
            border = BorderStroke(1.5.dp, Color(0xFF38BDF8).copy(alpha = 0.65f)),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 580.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {}
                )
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = null,
                            tint = Color(0xFF38BDF8),
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Column {
                            Text(
                                text = if (isTagalog) "Chat Log (Talaan ng Usapan)" else "Dialogue Chat Log",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )
                            Text(
                                text = if (isTagalog)
                                    "Pindutin ang alinmang linya upang balikan ito"
                                else
                                    "Tap any dialogue entry to jump back to that moment",
                                fontSize = 11.5.sp,
                                color = Color(0xFF94A3B8)
                            )
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close Log", tint = Color.White)
                    }
                }

                Spacer(Modifier.height(12.dp))

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    itemsIndexed(dialoguesSoFar) { idx, line ->
                        val avatarRes = when (line.speaker) {
                            VnSpeaker.GRACE -> line.leftSpriteRes ?: R.drawable.chibi_grace
                            VnSpeaker.KIAN -> line.rightSpriteRes ?: R.drawable.chibi_kian
                            VnSpeaker.SHOUKO -> line.leftSpriteRes ?: R.drawable.chibi_shouko
                            VnSpeaker.ALL_THREE -> R.drawable.chibi_grace_happy
                        }
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = Color(0xFF1E293B),
                            border = BorderStroke(1.dp, line.speaker.accentColor.copy(alpha = 0.45f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .interactiveBounceClick { onJumpToLine(idx) }
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Image(
                                    painter = painterResource(id = avatarRes),
                                    contentDescription = line.speaker.displayName,
                                    modifier = Modifier
                                        .size(46.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF0F172A))
                                        .border(1.dp, line.speaker.accentColor, CircleShape)
                                )
                                Spacer(Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = line.speaker.displayName,
                                            fontWeight = FontWeight.Black,
                                            fontSize = 14.sp,
                                            color = Color.White
                                        )
                                        Spacer(Modifier.width(8.dp))
                                        Text(
                                            text = if (isTagalog) line.speaker.roleTl else line.speaker.roleEn,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = line.speaker.accentColor,
                                            maxLines = 1,
                                            modifier = Modifier.autoSlideIfOverflow()
                                        )
                                    }
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        text = if (isTagalog) line.textTl else line.textEn,
                                        fontSize = 13.sp,
                                        lineHeight = 18.sp,
                                        color = Color(0xFFE2E8F0)
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

/**
 * Project Nexus Team Feedback Inbox Modal — allows users to send feedback, appreciation,
 * or bug reports directly to Grace, Kian, and the Project Nexus (Ascendens) development team.
 * Uses `autoSlideIfOverflow()` on all category chips so long text slides smoothly to the left.
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
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Project Nexus • Team Inbox",
                                fontWeight = FontWeight.Black,
                                fontSize = 19.sp,
                                color = Color(0xFFFBBF24),
                                maxLines = 1,
                                modifier = Modifier.autoSlideIfOverflow()
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
                                painter = painterResource(id = R.drawable.chibi_grace_happy),
                                contentDescription = "Grace",
                                modifier = Modifier.size(44.dp)
                            )
                            Image(
                                painter = painterResource(id = R.drawable.chibi_shouko_happy),
                                contentDescription = "Shouko",
                                modifier = Modifier.size(44.dp)
                            )
                            Image(
                                painter = painterResource(id = R.drawable.chibi_kian_thumbs),
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

                    // Category Selector Row — uses autoSlideIfOverflow() so full text slides smoothly left!
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
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    maxLines = 1,
                                    softWrap = false,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier
                                        .padding(horizontal = 6.dp, vertical = 8.dp)
                                        .autoSlideIfOverflow()
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(10.dp))

                    OutlinedTextField(
                        value = senderName,
                        onValueChange = { senderName = it },
                        label = {
                            Text(
                                if (isTagalog) "Iyong Pangalan (Opsyonal)" else "Your Name (Optional)",
                                fontSize = 12.sp
                            )
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF38BDF8),
                            unfocusedBorderColor = Color(0xFF334155),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedContainerColor = Color(0xFF0F172A),
                            unfocusedContainerColor = Color(0xFF0F172A)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(Modifier.height(8.dp))

                    OutlinedTextField(
                        value = messageText,
                        onValueChange = { messageText = it },
                        label = {
                            Text(
                                if (isTagalog) "Mensahe o Feedback para sa Team" else "Message or Feedback for the Team",
                                fontSize = 12.sp
                            )
                        },
                        minLines = 3,
                        maxLines = 5,
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFF59E0B),
                            unfocusedBorderColor = Color(0xFF334155),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedContainerColor = Color(0xFF0F172A),
                            unfocusedContainerColor = Color(0xFF0F172A)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                if (messageText.isBlank()) {
                                    Toast.makeText(
                                        context,
                                        if (isTagalog) "Pakisulat muna ang iyong mensahe." else "Please enter a message first.",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    return@Button
                                }
                                val timestamp = SimpleDateFormat("MMM dd, hh:mm a", Locale.getDefault()).format(Date())
                                val author = if (senderName.isBlank()) (if (isTagalog) "Bisita" else "Guest User") else senderName.trim()
                                val entry = "[$timestamp] • $selectedCategory — $author: ${messageText.trim()}"
                                val updated = (sentHistory + entry).takeLast(15)
                                prefs.edit().putStringSet("inbox_messages", updated.toSet()).apply()
                                sentHistory = updated.sortedDescending()
                                messageText = ""
                                UiSoundManager.playVillagerVoice(context, "all", "yay")
                                Toast.makeText(
                                    context,
                                    if (isTagalog)
                                        "Naipadala na sa Project Nexus Team Inbox! Maraming salamat po!"
                                    else
                                        "Sent to Project Nexus Team Inbox! Thank you from Grace, Kian & Shouko!",
                                    Toast.LENGTH_LONG
                                ).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B)),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = null,
                                tint = Color(0xFF0F172A),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = if (isTagalog) "Ipadala sa Inbox" else "Send to Team Inbox",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 12.5.sp,
                                color = Color(0xFF0F172A),
                                maxLines = 1,
                                softWrap = false,
                                modifier = Modifier.autoSlideIfOverflow()
                            )
                        }

                        OutlinedButton(
                            onClick = {
                                val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
                                    data = Uri.parse("mailto:projectnexus.icct@gmail.com")
                                    putExtra(Intent.EXTRA_SUBJECT, "[iWani Feedback • $selectedCategory] from ${senderName.ifBlank { "User" }}")
                                    putExtra(Intent.EXTRA_TEXT, messageText)
                                }
                                try {
                                    context.startActivity(Intent.createChooser(emailIntent, "Email Project Nexus Team"))
                                } catch (_: Exception) {
                                    Toast.makeText(context, "projectnexus.icct@gmail.com", Toast.LENGTH_SHORT).show()
                                }
                            },
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.dp, Color(0xFF38BDF8)),
                            modifier = Modifier.height(46.dp)
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
                                color = Color(0xFF38BDF8)
                            )
                        }
                    }

                    if (sentHistory.isNotEmpty()) {
                        Spacer(Modifier.height(14.dp))
                        Text(
                            text = if (isTagalog) "Mga Naipadalang Mensahe sa Team:" else "Recent Messages in Team Inbox:",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF93C5FD)
                        )
                        Spacer(Modifier.height(6.dp))
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            sentHistory.take(4).forEach { msg ->
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = Color(0xFF1E293B),
                                    border = BorderStroke(1.dp, Color(0xFF334155)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = msg,
                                        fontSize = 11.sp,
                                        color = Color(0xFFE2E8F0),
                                        modifier = Modifier.padding(8.dp)
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
