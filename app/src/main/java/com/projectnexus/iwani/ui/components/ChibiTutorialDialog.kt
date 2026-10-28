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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
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
 * Defines a crystal-clear Cutout Spotlight window over the real underlying app UI.
 * Everything OUTSIDE this rectangle is frosted/darkened out (`ClipOp.Difference`),
 * while everything INSIDE this rectangle shows the live app screen at 100% clarity!
 */
data class SpotlightCutoutSpec(
    val topFraction: Float,
    val bottomFraction: Float,
    val startFraction: Float = 0.035f,
    val endFraction: Float = 0.965f,
    val cornerRadiusDp: Float = 20f
)

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
    val cutout: SpotlightCutoutSpec? = null,
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
 * - Renders directly over the real app screen (no opaque box covering the UI!)
 * - Uses a real Cutout Spotlight (`ClipOp.Difference`) so the surrounding background is darkened/frosted
 *   while the highlighted UI card on the real app screen shines through at 100% clarity
 * - When no specific cutout is active (e.g. self-intro & Finale), blurs the whole background gently
 * - Features 12 normalized chibi sprites (including dedicated Finale "Thank You" poses for Grace, Kian & Shouko)
 * - Equal 1/3 width for all 3 chibis in the Finale so Kian is the exact same size as Grace and Shouko
 */
@Composable
fun ChibiMascotTutorialDialog(
    appLanguage: AppLanguage,
    initialPart: Int = 1,
    onBlurBackgroundChange: (Boolean) -> Unit = {},
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
                titleEn = "Part 1: Welcome & Home Workspace",
                titleTl = "Bahagi 1: Home Screen at Project Nexus",
                subtitleEn = "Meet Grace & Kian, top bar flags, theme wave & bento cards",
                subtitleTl = "Kilalanin sina Grace at Kian, watawat ng EN/TL at bento cards",
                badgeText = "PART 1",
                accentColor = Color(0xFF38BDF8),
                iconRes = R.drawable.chibi_grace
            ),
            WalkthroughPartInfo(
                partNumber = 2,
                titleEn = "Part 2: FSL Camera & Detection Tips",
                titleTl = "Bahagi 2: FSL Kamera at Tips sa Kamay",
                subtitleEn = "Lighting tips, hand distance, stability, mid-word typing & A–Z chart",
                subtitleTl = "Tamang ilaw, distansya ng kamay, pag-edit sa gitna ng salita at A–Z chart",
                badgeText = "PART 2",
                accentColor = Color(0xFF10B981),
                iconRes = R.drawable.chibi_kian_explain
            ),
            WalkthroughPartInfo(
                partNumber = 3,
                titleEn = "Part 3: AI Translate & Quick Words",
                titleTl = "Bahagi 3: AI Pagsasalin at Quick Words",
                subtitleEn = "Frequently used words, Output box, Qwen 2.5 sentences & Voice TTS",
                subtitleTl = "Madalas gamitin na salita, Output box, Qwen 2.5 at Boses",
                badgeText = "PART 3",
                accentColor = Color(0xFFF59E0B),
                iconRes = R.drawable.chibi_grace_point
            ),
            WalkthroughPartInfo(
                partNumber = 4,
                titleEn = "Part 4: Emergency SOS (ft. Shouko)",
                titleTl = "Bahagi 4: Saklolo SOS (kasama si Shouko)",
                subtitleEn = "Special Guest Shouko highlights each 1-tap SOS broadcast card",
                subtitleTl = "Ipapaliwanag ni Shouko ang bawat 1-tap SOS card sa emergency",
                badgeText = "PART 4 • SOS",
                accentColor = Color(0xFFF472B6),
                iconRes = R.drawable.chibi_shouko
            ),
            WalkthroughPartInfo(
                partNumber = 5,
                titleEn = "Part 5: Offline AI & Custom Settings",
                titleTl = "Bahagi 5: Offline AI Model at Settings",
                subtitleEn = "Project Nexus badge, Text Size Scale, Fonts, Sounds & 350 MB AI",
                subtitleTl = "Project Nexus badge, Laki ng Teksto, Font, Tunog at 350 MB AI",
                badgeText = "PART 5",
                accentColor = Color(0xFF818CF8),
                iconRes = R.drawable.chibi_kian_thumbs
            ),
            WalkthroughPartInfo(
                partNumber = 6,
                titleEn = "Part 6: Thank You & Team Inbox",
                titleTl = "Bahagi 6: Pasasalamat at Team Inbox",
                subtitleEn = "Special Thank You from Grace, Shouko & Kian + Team Inbox",
                subtitleTl = "Taos-pusong pasasalamat nina Grace, Shouko at Kian + Team Inbox",
                badgeText = "PART 6",
                accentColor = Color(0xFFFBBF24),
                iconRes = R.drawable.chibi_grace_thankyou
            )
        )
    }

    val allDialogues = remember {
        listOf(
            // ==================== PART 1: WELCOME & HOME WORKSPACE (7 TURNS) ====================
            WalkthroughDialogueLine(
                partNumber = 1,
                speaker = VnSpeaker.GRACE,
                leftSpriteRes = R.drawable.chibi_grace,
                rightSpriteRes = R.drawable.chibi_kian,
                activeSide = "LEFT",
                backgroundRoute = "home",
                cutout = null,
                textEn = "Hi there! My name is Grace, and I'm one of the developers of Project Nexus from ICCT Colleges. Welcome to our official iWani walkthrough!",
                textTl = "Kumusta po! Ang pangalan ko ay Grace, at isa ako sa mga developer ng Project Nexus mula sa ICCT Colleges. Maligayang pagdating sa iWani!"
            ),
            WalkthroughDialogueLine(
                partNumber = 1,
                speaker = VnSpeaker.KIAN,
                leftSpriteRes = R.drawable.chibi_grace,
                rightSpriteRes = R.drawable.chibi_kian_thumbs,
                activeSide = "RIGHT",
                backgroundRoute = "home",
                cutout = null,
                textEn = "And I'm Kian, QA and developer of Project Nexus! Together with our Ascendens team, we built iWani so Deaf and mute Filipinos can communicate naturally with anyone using Filipino Sign Language and offline AI.",
                textTl = "At ako naman si Kian, QA at developer ng Project Nexus! Kasama ang aming Ascendens team, binuo namin ang iWani upang madaling makipag-usap ang mga Bingi at pipi gamit ang Filipino Sign Language at offline AI."
            ),
            WalkthroughDialogueLine(
                partNumber = 1,
                speaker = VnSpeaker.GRACE,
                leftSpriteRes = R.drawable.chibi_grace_point,
                rightSpriteRes = R.drawable.chibi_kian,
                activeSide = "LEFT",
                backgroundRoute = "home",
                cutout = SpotlightCutoutSpec(topFraction = 0.038f, bottomFraction = 0.115f),
                textEn = "First, look at the highlighted Top Bar above! You can tap the USA Flag (EN) or Philippine Flag (TL) anytime to switch the whole app between English and Tagalog, or tap the Sun/Moon button for a smooth wave theme transition.",
                textTl = "Una, tingnan ang naka-highlight na Top Bar sa itaas! Pindutin lang ang watawat ng USA (EN) o Pilipinas (TL) upang magpalit sa English o Tagalog, o ang Sun/Moon icon para sa wave theme transition."
            ),
            WalkthroughDialogueLine(
                partNumber = 1,
                speaker = VnSpeaker.KIAN,
                leftSpriteRes = R.drawable.chibi_grace,
                rightSpriteRes = R.drawable.chibi_kian_explain,
                activeSide = "RIGHT",
                backgroundRoute = "home",
                cutout = SpotlightCutoutSpec(topFraction = 0.19f, bottomFraction = 0.405f),
                textEn = "Right below the greeting is our Hero Showcase! You can tap it to cycle through real-world stories showing how iWani helps in classrooms, hospitals, barangay halls, and daily conversations.",
                textTl = "Sa ibaba naman ng pagbati ay ang ating Hero Showcase! Pwede mo itong pindutin upang makita kung paano nakakatulong ang iWani sa paaralan, ospital, barangay, at araw-araw na usapan."
            ),
            WalkthroughDialogueLine(
                partNumber = 1,
                speaker = VnSpeaker.GRACE,
                leftSpriteRes = R.drawable.chibi_grace_happy,
                rightSpriteRes = R.drawable.chibi_kian_explain,
                activeSide = "LEFT",
                backgroundRoute = "home",
                cutout = SpotlightCutoutSpec(topFraction = 0.41f, bottomFraction = 0.505f),
                textEn = "This banner right here is our Interactive Walkthrough & Team Inbox bar! Whenever you want to replay this guide or send a message to our developers, you can tap it right from the Home screen.",
                textTl = "Ang banner na ito ay ang ating Interactive Walkthrough at Team Inbox bar! Kung gusto mong ulitin ang gabay na ito o magpadala ng mensahe sa aming team, pindutin lang ito sa Home screen."
            ),
            WalkthroughDialogueLine(
                partNumber = 1,
                speaker = VnSpeaker.KIAN,
                leftSpriteRes = R.drawable.chibi_grace_point,
                rightSpriteRes = R.drawable.chibi_kian_thumbs,
                activeSide = "RIGHT",
                backgroundRoute = "home",
                cutout = SpotlightCutoutSpec(topFraction = 0.525f, bottomFraction = 0.72f),
                textEn = "And here is our Core Workspace! The first two cards open the live 3D FSL Camera and the AI Translate screen. Let's head into Part 2 and test the FSL Camera together!",
                textTl = "At narito ang ating Core Workspace! Ang unang dalawang card ay para sa live 3D FSL Kamera at AI Pagsasalin. Dumako na tayo sa Bahagi 2 upang subukan ang FSL Kamera!"
            ),

            // ==================== PART 2: FSL CAMERA & HAND DETECTION TIPS (7 TURNS) ====================
            WalkthroughDialogueLine(
                partNumber = 2,
                speaker = VnSpeaker.KIAN,
                leftSpriteRes = R.drawable.chibi_grace,
                rightSpriteRes = R.drawable.chibi_kian_explain,
                activeSide = "RIGHT",
                backgroundRoute = "camera",
                cutout = SpotlightCutoutSpec(topFraction = 0.115f, bottomFraction = 0.21f),
                textEn = "Welcome to Part 2: the FSL Camera! Look at the top status bar of the camera: the left pill shows live hand scanning status, while the two buttons on the right let you open the A–Z Alphabet Chart or flip between front and back cameras.",
                textTl = "Narito na tayo sa Bahagi 2: ang FSL Kamera! Sa itaas na bahagi, makikita ang status ng pag-scan ng kamay, habang ang dalawang button sa kanan ay para sa A–Z Alphabet Chart at pag-flip ng front o back camera."
            ),
            WalkthroughDialogueLine(
                partNumber = 2,
                speaker = VnSpeaker.GRACE,
                leftSpriteRes = R.drawable.chibi_grace_point,
                rightSpriteRes = R.drawable.chibi_kian_explain,
                activeSide = "LEFT",
                backgroundRoute = "camera",
                cutout = SpotlightCutoutSpec(topFraction = 0.20f, bottomFraction = 0.54f),
                textEn = "Before signing inside the viewfinder, here is Tip #1: make sure your place is well-lit! Good lighting in front of your hand helps MediaPipe track all 21 3D finger joints with 97.4% accuracy.",
                textTl = "Bago mag-senyas sa harap ng kamera, narito ang Tip #1: siguraduhing maliwanag ang iyong lugar! Kapag maliwanag, kitang-kita ng AI ang lahat ng 21 finger joints ng iyong kamay."
            ),
            WalkthroughDialogueLine(
                partNumber = 2,
                speaker = VnSpeaker.KIAN,
                leftSpriteRes = R.drawable.chibi_grace_point,
                rightSpriteRes = R.drawable.chibi_kian_thumbs,
                activeSide = "RIGHT",
                backgroundRoute = "camera",
                cutout = SpotlightCutoutSpec(topFraction = 0.20f, bottomFraction = 0.54f),
                textEn = "And Tip #2: don't place your hand too close to the camera lens! Keep your hand centered about 1 to 2 feet (30 to 50 cm) away from the phone so your entire palm and fingertips stay inside the frame.",
                textTl = "At Tip #2: huwag ilapit nang sobra ang iyong kamay sa lente ng kamera! Panatilihin ang kamay sa gitna na may layong 1 hanggang 2 talampakan upang makita nang buo ang palad at mga daliri."
            ),
            WalkthroughDialogueLine(
                partNumber = 2,
                speaker = VnSpeaker.GRACE,
                leftSpriteRes = R.drawable.chibi_grace_happy,
                rightSpriteRes = R.drawable.chibi_kian,
                activeSide = "LEFT",
                backgroundRoute = "camera",
                cutout = SpotlightCutoutSpec(topFraction = 0.20f, bottomFraction = 0.54f),
                textEn = "Tip #3: once you form a letter from A to Z, hold your hand steady for about one second! That fills the Stability Meter and prevents accidental letters while your fingers are moving between signs.",
                textTl = "Tip #3: kapag naka-porma na ang iyong kamay ng titik A hanggang Z, panatilihing steady ito nang isang segundo! Pupunuan nito ang Stability Meter upang maiwasan ang maling titik habang gumagalaw ang daliri."
            ),
            WalkthroughDialogueLine(
                partNumber = 2,
                speaker = VnSpeaker.KIAN,
                leftSpriteRes = R.drawable.chibi_grace,
                rightSpriteRes = R.drawable.chibi_kian_explain,
                activeSide = "RIGHT",
                backgroundRoute = "camera",
                cutout = SpotlightCutoutSpec(topFraction = 0.58f, bottomFraction = 0.74f),
                textEn = "Now look at the highlighted 'Formed Word' box! What if you spelled 'TUBG' and forgot the letter 'I' in the middle? You don't have to erase the whole word!",
                textTl = "Ngayon, tingnan ang naka-highlight na 'Formed Word' box! Paano kung na-spell mo ay 'TUBG' at nakalimutan ang titik 'I' sa gitna? Hindi mo na kailangang burahin ang buong salita!"
            ),
            WalkthroughDialogueLine(
                partNumber = 2,
                speaker = VnSpeaker.GRACE,
                leftSpriteRes = R.drawable.chibi_grace_point,
                rightSpriteRes = R.drawable.chibi_kian_thumbs,
                activeSide = "LEFT",
                backgroundRoute = "camera",
                cutout = SpotlightCutoutSpec(topFraction = 0.58f, bottomFraction = 0.74f),
                textEn = "Exactly! Just tap directly between the letters inside the box (or use the ◀ and ▶ cursor arrows) to type with your keyboard or sign a new letter right into the middle of the word!",
                textTl = "Tama! Pindutin lang sa pagitan ng mga titik sa loob ng box (o gamitin ang ◀ at ▶ arrows) upang mag-type sa keyboard o magsingit ng bagong senyas sa gitna ng salita!"
            ),

            // ==================== PART 3: AI TRANSLATE & QUICK WORDS (5 TURNS) ====================
            WalkthroughDialogueLine(
                partNumber = 3,
                speaker = VnSpeaker.GRACE,
                leftSpriteRes = R.drawable.chibi_grace_point,
                rightSpriteRes = R.drawable.chibi_kian,
                activeSide = "LEFT",
                backgroundRoute = "translate",
                cutout = SpotlightCutoutSpec(topFraction = 0.195f, bottomFraction = 0.305f),
                textEn = "Welcome to Part 3: Communication & Translation! At the top, we organized everyday situations into quick categories, starting with '★ Frequently Used' right at the front.",
                textTl = "Narito ang Bahagi 3: Komunikasyon at Pagsasalin! Sa itaas, naka-ayos ang mga kategorya simula sa '★ Madalas Gamitin' para sa pinakamabilis na pakikipag-usap."
            ),
            WalkthroughDialogueLine(
                partNumber = 3,
                speaker = VnSpeaker.KIAN,
                leftSpriteRes = R.drawable.chibi_grace,
                rightSpriteRes = R.drawable.chibi_kian_explain,
                activeSide = "RIGHT",
                backgroundRoute = "translate",
                cutout = SpotlightCutoutSpec(topFraction = 0.305f, bottomFraction = 0.525f),
                textEn = "Look at the Quick Words chips highlighted here — Pagkain (Food), Tubig (Water), Bili (Buy), Pahinga (Rest), Tulong (Help), Salamat, Magkano, and Saan ang CR! Tap any chip when you need an instant reply.",
                textTl = "Tingnan ang mga Quick Words chips na naka-highlight dito — Pagkain, Tubig, Bili, Pahinga, Tulong, Salamat, Magkano, at Saan ang CR! Pindutin lang ang alinmang salita kapag kailangan ng agarang sagot."
            ),
            WalkthroughDialogueLine(
                partNumber = 3,
                speaker = VnSpeaker.GRACE,
                leftSpriteRes = R.drawable.chibi_grace_happy,
                rightSpriteRes = R.drawable.chibi_kian_explain,
                activeSide = "LEFT",
                backgroundRoute = "translate",
                cutout = SpotlightCutoutSpec(topFraction = 0.525f, bottomFraction = 0.655f),
                textEn = "Below the chips is the 'Output' box! Whether you signed words from the camera, tapped a Quick Word chip, or typed multiple words like 'MASAKIT ULO GAMOT', they appear right here.",
                textTl = "Sa ibaba ng mga chips ay ang 'Output' box! Mula man sa kamera, Quick Word chip, o tinype na mga salita tulad ng 'MASAKIT ULO GAMOT', dito ito lalabas."
            ),
            WalkthroughDialogueLine(
                partNumber = 3,
                speaker = VnSpeaker.KIAN,
                leftSpriteRes = R.drawable.chibi_grace_point,
                rightSpriteRes = R.drawable.chibi_kian_thumbs,
                activeSide = "RIGHT",
                backgroundRoute = "translate",
                cutout = SpotlightCutoutSpec(topFraction = 0.655f, bottomFraction = 0.875f),
                textEn = "And inside the AI Translation Card below it, our offline Qwen 2.5 engine turns those short words into a complete, polite Tagalog or English sentence! Tap 'Speak Out Loud' to broadcast it in a clear voice.",
                textTl = "At sa AI Translation Card sa ibaba nito, ginagawa itong buo at magalang na pangungusap sa Tagalog o English ng ating offline Qwen 2.5 AI! Pindutin ang 'Iparinig sa Boses' upang iparinig ito."
            ),

            // ==================== PART 4: EMERGENCY SOS — FT. SHOUKO (SPECIAL GUEST) (6 TURNS) ====================
            WalkthroughDialogueLine(
                partNumber = 4,
                speaker = VnSpeaker.GRACE,
                leftSpriteRes = R.drawable.chibi_shouko,
                rightSpriteRes = R.drawable.chibi_grace_happy,
                activeSide = "RIGHT",
                backgroundRoute = "sos",
                cutout = null,
                textEn = "For Part 4, we invited a very special friend to help us explain the Emergency SOS feature! Everyone, please welcome our Special Guest, Shouko!",
                textTl = "Para sa Bahagi 4, nag-imbita kami ng isang espesyal na kaibigan upang ipaliwanag ang Emergency SOS feature! Salubungin natin ang ating Special Guest, si Shouko!"
            ),
            WalkthroughDialogueLine(
                partNumber = 4,
                speaker = VnSpeaker.SHOUKO,
                leftSpriteRes = R.drawable.chibi_shouko_notebook,
                rightSpriteRes = R.drawable.chibi_grace,
                activeSide = "LEFT",
                backgroundRoute = "sos",
                cutout = SpotlightCutoutSpec(topFraction = 0.21f, bottomFraction = 0.342f),
                textEn = "Hello everyone! I'm Shouko. In an emergency, signing or typing can take too much time. Look at the first highlighted card above: 'Emergency SOS (Urgent Help)' — tapping it once immediately speaks out loud to call people nearby for urgent assistance!",
                textTl = "Kumusta po sa inyong lahat! Ako si Shouko. Sa oras ng kagipitan, mahalaga ang bawat segundo. Tingnan ang unang card na naka-highlight sa itaas: 'Emergency SOS (Urgent Help)' — sa isang pindot, malakas na magsasalita ang phone upang humingi ng tulong!"
            ),
            WalkthroughDialogueLine(
                partNumber = 4,
                speaker = VnSpeaker.GRACE,
                leftSpriteRes = R.drawable.chibi_shouko_notebook,
                rightSpriteRes = R.drawable.chibi_grace_point,
                activeSide = "RIGHT",
                backgroundRoute = "sos",
                cutout = SpotlightCutoutSpec(topFraction = 0.342f, bottomFraction = 0.472f),
                textEn = "And look at the second card right below it: 'Medical Emergency / Hospital'! If you feel dizzy, injured, or unwell, one tap announces that you are Deaf and need a doctor or ambulance right away.",
                textTl = "At tingnan ang pangalawang card sa ibaba nito: 'Medical Emergency / Hospital'! Kung ikaw ay nahihilo, nasugatan, o may sakit, ipapahayag agad nito na kailangan mo ng doktor o ambulansya."
            ),
            WalkthroughDialogueLine(
                partNumber = 4,
                speaker = VnSpeaker.SHOUKO,
                leftSpriteRes = R.drawable.chibi_shouko_happy,
                rightSpriteRes = R.drawable.chibi_grace,
                activeSide = "LEFT",
                backgroundRoute = "sos",
                cutout = SpotlightCutoutSpec(topFraction = 0.472f, bottomFraction = 0.605f),
                textEn = "Now look at this third card in the center: 'I Am Lost / Need Directions'! If you ever get lost while commuting or in an unfamiliar place, tapping this card politely asks bystanders to help guide you safely.",
                textTl = "Ngayon ay tingnan ang pangatlong card sa gitna: 'I Am Lost / Need Directions'! Kapag ikaw ay naligaw sa biyahe o sa hindi pamilyar na lugar, magalang itong hihingi ng tulong at direksyon sa mga tao sa paligid."
            ),
            WalkthroughDialogueLine(
                partNumber = 4,
                speaker = VnSpeaker.KIAN,
                leftSpriteRes = R.drawable.chibi_shouko_happy,
                rightSpriteRes = R.drawable.chibi_kian_explain,
                activeSide = "RIGHT",
                backgroundRoute = "sos",
                cutout = SpotlightCutoutSpec(topFraction = 0.605f, bottomFraction = 0.748f),
                textEn = "We also added 'I Am Deaf (Communication Note)' right here! When you tap it, your phone tells the other person: 'Hello, I am deaf and I use sign language. Please write on paper or type on my phone so we can communicate.'",
                textTl = "Idinagdag din namin ang 'I Am Deaf (Communication Note)' dito! Sinasabi nito sa kausap mo: 'Ako po ay bingi at gumagamit ng sign language. Pakisulat po sa papel o mag-type sa aking phone upang tayo ay magkaintindihan.'"
            ),
            WalkthroughDialogueLine(
                partNumber = 4,
                speaker = VnSpeaker.SHOUKO,
                leftSpriteRes = R.drawable.chibi_shouko_notebook,
                rightSpriteRes = R.drawable.chibi_grace_happy,
                activeSide = "LEFT",
                backgroundRoute = "sos",
                cutout = SpotlightCutoutSpec(topFraction = 0.748f, bottomFraction = 0.885f),
                textEn = "And at the bottom is 'Police / Barangay Tanod' for security help! While any SOS card is broadcasting, a glowing audio wave appears at the top — tap the card again or press Stop to silence it anytime.",
                textTl = "At sa ibaba ay ang 'Police / Barangay Tanod' para sa seguridad! Habang nagsasalita ang SOS, may lalabas na audio wave sa itaas — pindutin lang muli ang card o ang Stop button upang ihinto ito."
            ),

            // ==================== PART 5: OFFLINE AI & CUSTOM SETTINGS (4 TURNS) ====================
            WalkthroughDialogueLine(
                partNumber = 5,
                speaker = VnSpeaker.KIAN,
                leftSpriteRes = R.drawable.chibi_grace,
                rightSpriteRes = R.drawable.chibi_kian_thumbs,
                activeSide = "RIGHT",
                backgroundRoute = "settings",
                cutout = SpotlightCutoutSpec(topFraction = 0.115f, bottomFraction = 0.21f),
                textEn = "Welcome to Part 5: Settings & Studio! Look at the metallic 'PROJECT NEXUS' button at the top-right: tapping it opens our About Project Nexus page with the complete Ascendens team roster and ICCT Colleges credits.",
                textTl = "Narito ang Bahagi 5: Mga Setting! Tingnan ang metallic na 'PROJECT NEXUS' button sa kanang itaas: kapag pinindot mo ito, bubukas ang About Project Nexus page kung saan makikita ang buong Ascendens team."
            ),
            WalkthroughDialogueLine(
                partNumber = 5,
                speaker = VnSpeaker.GRACE,
                leftSpriteRes = R.drawable.chibi_grace_point,
                rightSpriteRes = R.drawable.chibi_kian,
                activeSide = "LEFT",
                backgroundRoute = "settings",
                cutout = SpotlightCutoutSpec(topFraction = 0.21f, bottomFraction = 0.495f),
                textEn = "In the 'Appearance & Language' card highlighted here, you can toggle Dark Mode (Midnight Navy) with its portfolio wave transition, or switch between English (US) and Tagalog (PH).",
                textTl = "Sa 'Hitsura at Wika' card na naka-highlight dito, maaari mong i-toggle ang Dark Mode (Midnight Navy) na may wave transition, o magpalit sa pagitan ng English (US) at Tagalog (PH)."
            ),
            WalkthroughDialogueLine(
                partNumber = 5,
                speaker = VnSpeaker.KIAN,
                leftSpriteRes = R.drawable.chibi_grace_point,
                rightSpriteRes = R.drawable.chibi_kian_explain,
                activeSide = "RIGHT",
                backgroundRoute = "settings",
                cutout = SpotlightCutoutSpec(topFraction = 0.50f, bottomFraction = 0.88f),
                textEn = "And inside 'Typography & Font Style', you can switch between 4 custom fonts and drag the 'Text Size Scale' (Laki ng Teksto) slider from 85% to 125% to resize every single text across the app live!",
                textTl = "At sa loob ng 'Estilo at Laki ng Font', maaari kang pumili sa 4 na font at i-slide ang 'Laki ng Teksto' mula 85% hanggang 125% upang palakihin o paliitin ang lahat ng teksto sa buong app!"
            ),
            WalkthroughDialogueLine(
                partNumber = 5,
                speaker = VnSpeaker.GRACE,
                leftSpriteRes = R.drawable.chibi_grace_happy,
                rightSpriteRes = R.drawable.chibi_kian_thumbs,
                activeSide = "LEFT",
                backgroundRoute = "settings",
                cutout = null,
                textEn = "Scroll down in Settings anytime to customize your Button Click Sound (Crystal Chime, Cyber Pulse, Soft Pop, or Tactile Click) and manage 'Extracting AI Model (Qwen 2.5)' for 100% offline translation!",
                textTl = "Mag-scroll lang sa Settings upang pumili ng Tunog ng Button (Crystal Chime, Cyber Pulse, Soft Pop, o Tactile Click) at buksan ang 'Extracting AI Model (Qwen 2.5)' para sa 100% offline na pagsasalin!"
            ),

            // ==================== PART 6: THANK YOU & TEAM INBOX (3 TURNS) ====================
            WalkthroughDialogueLine(
                partNumber = 6,
                speaker = VnSpeaker.GRACE,
                leftSpriteRes = R.drawable.chibi_grace_happy,
                rightSpriteRes = R.drawable.chibi_kian_thumbs,
                activeSide = "LEFT",
                backgroundRoute = "home",
                cutout = null,
                textEn = "You've completed the full iWani walkthrough! Remember, if you ever want to review just one specific section — like Camera tips or Emergency SOS — just tap the 'Part' menu at the top-left to jump straight to that Part.",
                textTl = "Natapos mo na ang buong walkthrough ng iWani! Tandaan, kung gusto mong balikan ang isang partikular na bahagi — tulad ng Camera tips o Emergency SOS — pindutin lang ang 'Bahagi' menu sa kaliwang itaas."
            ),
            WalkthroughDialogueLine(
                partNumber = 6,
                speaker = VnSpeaker.KIAN,
                leftSpriteRes = R.drawable.chibi_grace_happy,
                rightSpriteRes = R.drawable.chibi_kian_thankyou,
                activeSide = "RIGHT",
                backgroundRoute = "home",
                cutout = null,
                textEn = "We poured our hearts into building Project Nexus at ICCT Colleges, and your feedback helps us make iWani even better for the Deaf community!",
                textTl = "Buong puso naming binuo ang Project Nexus sa ICCT Colleges, at malaking tulong ang iyong feedback upang lalo pa naming mapaganda ang iWani para sa komunidad ng mga Bingi!"
            ),
            WalkthroughDialogueLine(
                partNumber = 6,
                speaker = VnSpeaker.ALL_THREE,
                leftSpriteRes = R.drawable.chibi_grace_thankyou,
                centerSpriteRes = R.drawable.chibi_shouko_thankyou,
                rightSpriteRes = R.drawable.chibi_kian_thankyou,
                activeSide = "ALL_THREE",
                backgroundRoute = "home",
                cutout = null,
                textEn = "From Grace, Shouko, Kian, and the entire Project Nexus (Ascendens) team: thank you so much! Tap 'Team Inbox' below to send us your thoughts, or tap 'Finish' to start using iWani!",
                textTl = "Mula kina Grace, Shouko, Kian, at sa buong Project Nexus (Ascendens) team: maraming-maraming salamat po! Pindutin ang 'Team Inbox' sa ibaba upang magpadala ng mensahe, o 'Tapos' upang gamitin na ang iWani!"
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

    // When cutout == null, apply real Gaussian blur to the background Scaffold;
    // when cutout != null, keep the Scaffold sharp so the highlighted card inside the cutout is 100% clear!
    LaunchedEffect(currentLine.cutout) {
        onBlurBackgroundChange(currentLine.cutout == null)
    }
    DisposableEffect(Unit) {
        onDispose {
            onBlurBackgroundChange(false)
        }
    }

    // Typewriter effect state: reveals characters one by one
    var visibleCharCount by remember(currentIndex, isTagalog) { mutableIntStateOf(0) }
    val isTyping = visibleCharCount < fullDialogueText.length

    LaunchedEffect(currentIndex, isTagalog) {
        visibleCharCount = 0
        while (visibleCharCount < fullDialogueText.length) {
            delay(17L)
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
        val hops = listOf(-15f, 0f, -11f, 0f, -6f, 0f)
        for (target in hops) {
            puppetOffset.animateTo(
                targetValue = target,
                animationSpec = tween(durationMillis = 80, easing = FastOutSlowInEasing)
            )
        }
        puppetOffset.snapTo(0f)
    }

    // Subtle pulse for the cutout spotlight border
    val infiniteTransition = rememberInfiniteTransition(label = "CutoutBorderPulse")
    val borderGlowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.55f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(850, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "BorderGlowAlpha"
    )

    fun handleAdvanceOrCompleteTyping() {
        if (isTyping) {
            visibleCharCount = fullDialogueText.length
        } else if (currentIndex < allDialogues.lastIndex) {
            UiSoundManager.playClick(context)
            currentIndex++
        } else {
            UiSoundManager.playSuccess(context)
            onDismiss()
        }
    }

    // Check if the cutout is in the lower half of the screen so the chibis shrink/tuck down out of its way!
    val isLowCutout = currentLine.cutout != null && currentLine.cutout.bottomFraction > 0.56f

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                handleAdvanceOrCompleteTyping()
            }
    ) {
        // ==================== 1. REAL APP CUTOUT SPOTLIGHT MASK ====================
        Canvas(modifier = Modifier.fillMaxSize()) {
            val cutout = currentLine.cutout
            if (cutout == null) {
                // Soft dark glass tint over the blurred background when introducing/thanking
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0x99040814),
                            Color(0xB3060D1F),
                            Color(0xD9050914)
                        )
                    )
                )
            } else {
                val left = size.width * cutout.startFraction
                val top = size.height * cutout.topFraction
                val right = size.width * cutout.endFraction
                val bottom = size.height * cutout.bottomFraction
                val radiusPx = cutout.cornerRadiusDp.dp.toPx()

                val cutoutPath = Path().apply {
                    addRoundRect(
                        RoundRect(
                            rect = Rect(left, top, right, bottom),
                            cornerRadius = CornerRadius(radiusPx, radiusPx)
                        )
                    )
                }

                // Draw the dark frosted mask ONLY OUTSIDE the cutout window!
                // Inside the cutout window is 100% untouched, bright, sharp real app UI!
                clipPath(cutoutPath, clipOp = ClipOp.Difference) {
                    drawRect(color = Color(0xE0040916))

                    // Soft diffused outer halo rings around the cutout so the surrounding edge feels blurred
                    for (ring in 1..4) {
                        val expand = (ring * 7).dp.toPx()
                        drawRoundRect(
                            color = currentLine.speaker.accentColor.copy(alpha = 0.05f * (5 - ring)),
                            topLeft = Offset(left - expand, top - expand),
                            size = Size((right - left) + expand * 2f, (bottom - top) + expand * 2f),
                            cornerRadius = CornerRadius(radiusPx + expand, radiusPx + expand),
                            style = Stroke(width = 6.dp.toPx())
                        )
                    }
                }

                // Crisp glowing frame around the clear cutout window
                drawRoundRect(
                    color = currentLine.speaker.accentColor.copy(alpha = borderGlowAlpha),
                    topLeft = Offset(left, top),
                    size = Size(right - left, bottom - top),
                    cornerRadius = CornerRadius(radiusPx, radiusPx),
                    style = Stroke(width = 2.8.dp.toPx())
                )
            }
        }

        // ==================== 2. TOP BAR: PART SELECTOR (LEFT) + CHAT LOG & SKIP (RIGHT) ====================
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 14.dp, vertical = 8.dp)
                .align(Alignment.TopCenter),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Part Selector Pill (Allows jumping to Part 1..6 anytime!)
            Surface(
                shape = RoundedCornerShape(50),
                color = Color(0xF00B1326),
                border = BorderStroke(1.2.dp, currentPartInfo.accentColor.copy(alpha = 0.85f)),
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
                Surface(
                    shape = RoundedCornerShape(50),
                    color = Color(0xF00B1326),
                    border = BorderStroke(1.dp, Color(0xFF38BDF8).copy(alpha = 0.75f)),
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
                            text = "Log",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFFE2E8F0),
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(50),
                    color = Color(0xF01E293B),
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
            // Character Stage Row right above the dialogue panel
            // Automatically compacts height when highlighting lower-screen cards so the cutout is never blocked!
            val stageHeight = when {
                currentLine.activeSide == "ALL_THREE" -> 195.dp
                isLowCutout -> 112.dp
                else -> 160.dp
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(stageHeight)
                    .padding(horizontal = 8.dp),
                contentAlignment = Alignment.BottomCenter
            ) {
                if (currentLine.activeSide == "ALL_THREE") {
                    // Equal 1/3 weight for Grace, Shouko & Kian so all 3 stand at the EXACT same height and size!
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Box(
                            modifier = Modifier.weight(1f),
                            contentAlignment = Alignment.BottomCenter
                        ) {
                            VnCharacterStageSprite(
                                drawableRes = currentLine.leftSpriteRes ?: R.drawable.chibi_grace_thankyou,
                                isSpeaking = true,
                                puppetOffsetY = puppetOffset.value,
                                glowColor = Color(0xFF38BDF8),
                                spriteHeight = 190.dp
                            )
                        }
                        Box(
                            modifier = Modifier.weight(1f),
                            contentAlignment = Alignment.BottomCenter
                        ) {
                            VnCharacterStageSprite(
                                drawableRes = currentLine.centerSpriteRes ?: R.drawable.chibi_shouko_thankyou,
                                isSpeaking = true,
                                puppetOffsetY = puppetOffset.value * 0.9f,
                                glowColor = Color(0xFFF472B6),
                                spriteHeight = 190.dp
                            )
                        }
                        Box(
                            modifier = Modifier.weight(1f),
                            contentAlignment = Alignment.BottomCenter
                        ) {
                            VnCharacterStageSprite(
                                drawableRes = currentLine.rightSpriteRes ?: R.drawable.chibi_kian_thankyou,
                                isSpeaking = true,
                                puppetOffsetY = puppetOffset.value * 0.95f,
                                glowColor = Color(0xFFF59E0B),
                                spriteHeight = 190.dp
                            )
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
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
                                    glowColor = if (currentLine.speaker == VnSpeaker.SHOUKO) Color(0xFFF472B6) else Color(0xFF38BDF8),
                                    spriteHeight = stageHeight
                                )
                            }
                        }

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
                                    glowColor = if (currentLine.speaker == VnSpeaker.KIAN) Color(0xFFF59E0B) else Color(0xFF38BDF8),
                                    spriteHeight = stageHeight
                                )
                            }
                        }
                    }
                }
            }

            // ==================== BLUE ARCHIVE STYLE DIALOGUE PANEL ====================
            Surface(
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                color = Color(0xF5070E20),
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
                        .padding(horizontal = 18.dp, vertical = 14.dp)
                ) {
                    // Speaker Name + Role Beside It ("Grace  Developer • Project Nexus" / "Shouko  Special Guest")
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
                                fontSize = if (currentLine.speaker == VnSpeaker.ALL_THREE) 18.sp else 21.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White,
                                maxLines = 1,
                                softWrap = false
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = if (isTagalog) currentLine.speaker.roleTl else currentLine.speaker.roleEn,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = currentLine.speaker.accentColor,
                                maxLines = 1,
                                softWrap = false,
                                modifier = Modifier
                                    .padding(bottom = 2.dp)
                                    .autoSlideIfOverflow()
                            )
                        }

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

                    Spacer(Modifier.height(7.dp))

                    // Glowing Horizontal Accent Line under Speaker Name + Role
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

                    Spacer(Modifier.height(10.dp))

                    // Typewriter Animated Dialogue Text Box
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 76.dp, max = 114.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        Text(
                            text = fullDialogueText.take(visibleCharCount),
                            fontSize = 14.5.sp,
                            lineHeight = 21.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFFF1F5F9)
                        )
                    }

                    Spacer(Modifier.height(10.dp))

                    // Bottom Controls Row: [ ◀ Prev ]  [ Tap anywhere ▾ ]  [ Next / Team Inbox + Finish ]
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (currentIndex > 0) {
                            OutlinedButton(
                                onClick = {
                                    UiSoundManager.playClick(context)
                                    currentIndex--
                                },
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, Color(0xFF475569)),
                                contentPadding = PaddingValues(horizontal = 11.dp, vertical = 5.dp),
                                modifier = Modifier.height(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Previous Dialogue",
                                    tint = Color(0xFFE2E8F0),
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    text = if (isTagalog) "Nakaraan" else "Prev",
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFE2E8F0),
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                        } else {
                            Spacer(Modifier.width(68.dp))
                        }

                        Text(
                            text = if (isTyping) {
                                if (isTagalog) "Pindutin para kumpletuhin..." else "Tap to show full text..."
                            } else {
                                if (isTagalog) "Pindutin kahit saan ▾" else "Tap anywhere ▾"
                            },
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = currentLine.speaker.accentColor.copy(alpha = 0.9f),
                            maxLines = 1,
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 6.dp)
                                .autoSlideIfOverflow(),
                            textAlign = TextAlign.Center
                        )

                        if (currentIndex == allDialogues.lastIndex) {
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Button(
                                    onClick = {
                                        UiSoundManager.playSuccess(context)
                                        onOpenFeedbackInbox()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B)),
                                    shape = RoundedCornerShape(12.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 5.dp),
                                    modifier = Modifier.height(36.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Inbox,
                                        contentDescription = "Team Inbox",
                                        tint = Color(0xFF0F172A),
                                        modifier = Modifier.size(14.dp)
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
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 5.dp),
                                    modifier = Modifier.height(36.dp)
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
                                contentPadding = PaddingValues(horizontal = 11.dp, vertical = 5.dp),
                                modifier = Modifier.height(36.dp)
                            ) {
                                Text(
                                    text = if (isTagalog) "Sunod" else "Next",
                                    fontSize = 11.5.sp,
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
                                    modifier = Modifier.size(14.dp)
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
        targetValue = if (isSpeaking) 1f else 0.50f,
        animationSpec = tween(220),
        label = "SpeakerAlpha"
    )
    val targetScale by animateFloatAsState(
        targetValue = if (isSpeaking) 1.02f else 0.92f,
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
                    .size(spriteHeight * 0.78f)
                    .align(Alignment.Center)
            ) {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            glowColor.copy(alpha = 0.30f),
                            glowColor.copy(alpha = 0.06f),
                            Color.Transparent
                        )
                    ),
                    radius = size.minDimension / 2f
                )
            }
        }

        Crossfade(
            targetState = drawableRes,
            animationSpec = tween(200),
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
                            VnSpeaker.ALL_THREE -> R.drawable.chibi_grace_thankyou
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
                                painter = painterResource(id = R.drawable.chibi_grace_thankyou),
                                contentDescription = "Grace",
                                modifier = Modifier.size(44.dp)
                            )
                            Image(
                                painter = painterResource(id = R.drawable.chibi_shouko_thankyou),
                                contentDescription = "Shouko",
                                modifier = Modifier.size(44.dp)
                            )
                            Image(
                                painter = painterResource(id = R.drawable.chibi_kian_thankyou),
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
