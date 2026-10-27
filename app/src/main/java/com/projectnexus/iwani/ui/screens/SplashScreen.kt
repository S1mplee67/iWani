package com.projectnexus.iwani.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.projectnexus.iwani.R
import com.projectnexus.iwani.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin

private enum class IntroScene {
    APP_LOGO,       // Scene 1: iWani App Icon animated opening -> App Name below -> Fades away everything
    ASCENDENS,      // Scene 2: "created by Ascendens" with kinetic/interactive per-letter animation
    ICCT_SEAL,      // Scene 3: ICCT Logo.png (Official Seal, transparent)
    ENG_SOCIETY,    // Scene 4: ICCT_Engineering_Society_Logo-removebg-preview.png (transparent)
    ENG_1992        // Scene 5: icon.jpg (Computer & Communications Engineering 1992, transparent)
}

/**
 * Multi-Scene Cinematic Studio Intro for iWani:
 * - Scene 1: Animated iris/shockwave opening of the updated PH-flag iWani icon (Blue Hand, Golden Sun, Red Hand),
 *            followed by the "iWani" app name rising below, then everything fades away.
 * - Scene 2: "created by Ascendens" with interactive/kinetic staggered spring typography and a sweeping gold-cyan shimmer.
 * - Scene 3 -> 4 -> 5: 3D Portal Zoom & Luminous Halo transitions through:
 *     1) ICCT Logo.png
 *     2) ICCT_Engineering_Society_Logo-removebg-preview.png
 *     3) icon.jpg (1992 Engineering Emblem, background removed)
 * - Includes persistent "Skip ›" button at top-right.
 */
@Composable
fun SplashScreen(
    onFinish: () -> Unit
) {
    var currentScene by remember { mutableStateOf(IntroScene.APP_LOGO) }
    val scope = rememberCoroutineScope()

    // Continuous ambient animation for glowing aura, orbital rings, and shimmer sweep
    val infiniteTransition = rememberInfiniteTransition(label = "CinematicAmbient")
    val orbitAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(4200, easing = LinearEasing)
        ),
        label = "OrbitAngle"
    )
    val shimmerSweep by infiniteTransition.animateFloat(
        initialValue = -0.4f,
        targetValue = 1.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(1600, easing = LinearEasing)
        ),
        label = "ShimmerSweep"
    )

    // Scene 1 Animatables (App Icon + App Name below -> Fade away everything)
    val iconScale = remember { Animatable(0.25f) }
    val iconAlpha = remember { Animatable(0f) }
    val iconRotation = remember { Animatable(-22f) }
    val ringExpand = remember { Animatable(0f) }
    val nameAlpha = remember { Animatable(0f) }
    val nameOffsetY = remember { Animatable(32f) }
    val nameTracking = remember { Animatable(12f) }

    // Scene 2 Animatables ("created by Ascendens" kinetic typography)
    val createdByAlpha = remember { Animatable(0f) }
    val ascendensProgress = remember { Animatable(0f) }
    val ascendensSceneAlpha = remember { Animatable(1f) }
    val interactivePulse = remember { Animatable(1f) }

    // Scene 3, 4, 5 Shared 3D Portal Transition Animatables
    val badgeScale = remember { Animatable(0.5f) }
    val badgeAlpha = remember { Animatable(0f) }
    val badgeRotationY = remember { Animatable(-65f) }
    val shockwaveProgress = remember { Animatable(0f) }
    val textSlideY = remember { Animatable(24f) }

    // Helper for the 3D Portal Zoom & Shockwave transition between the 3 ICCT logos
    suspend fun playLogoPortalScene() {
        badgeScale.snapTo(0.48f)
        badgeAlpha.snapTo(0f)
        badgeRotationY.snapTo(-60f)
        shockwaveProgress.snapTo(0f)
        textSlideY.snapTo(26f)

        // Enter with 3D perspective flip + shockwave ring expansion
        kotlinx.coroutines.coroutineScope {
            launch {
                shockwaveProgress.animateTo(1f, tween(850, easing = FastOutSlowInEasing))
            }
            launch {
                badgeAlpha.animateTo(1f, tween(420, easing = FastOutSlowInEasing))
            }
            launch {
                badgeScale.animateTo(
                    targetValue = 1f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioLowBouncy,
                        stiffness = Spring.StiffnessMediumLow
                    )
                )
            }
            launch {
                badgeRotationY.animateTo(
                    targetValue = 0f,
                    animationSpec = tween(680, easing = CubicBezierEasing(0.16f, 1f, 0.3f, 1f))
                )
            }
            launch {
                delay(140)
                textSlideY.animateTo(0f, tween(480, easing = FastOutSlowInEasing))
            }
        }

        delay(1050) // Hold for viewing

        // Warp-zoom forward transition out to next logo
        kotlinx.coroutines.coroutineScope {
            launch {
                badgeScale.animateTo(1.48f, tween(380, easing = FastOutLinearInEasing))
            }
            launch {
                badgeRotationY.animateTo(45f, tween(380, easing = FastOutLinearInEasing))
            }
            launch {
                badgeAlpha.animateTo(0f, tween(350, easing = FastOutSlowInEasing))
            }
        }
    }

    // Master Cinematic Timeline
    LaunchedEffect(Unit) {
        // ================= SCENE 1: APP ICON + APP NAME BELOW =================
        currentScene = IntroScene.APP_LOGO
        kotlinx.coroutines.coroutineScope {
            launch { ringExpand.animateTo(1f, tween(900, easing = FastOutSlowInEasing)) }
            launch { iconAlpha.animateTo(1f, tween(450, easing = FastOutSlowInEasing)) }
            launch {
                iconScale.animateTo(
                    targetValue = 1f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                )
            }
            launch {
                iconRotation.animateTo(0f, tween(650, easing = CubicBezierEasing(0.16f, 1f, 0.3f, 1f)))
            }
        }

        // App name rises below the icon
        kotlinx.coroutines.coroutineScope {
            launch { nameAlpha.animateTo(1f, tween(480, easing = FastOutSlowInEasing)) }
            launch { nameOffsetY.animateTo(0f, tween(520, easing = CubicBezierEasing(0.16f, 1f, 0.3f, 1f))) }
            launch { nameTracking.animateTo(3f, tween(600, easing = FastOutSlowInEasing)) }
        }

        delay(950)

        // Fade away everything in Scene 1
        kotlinx.coroutines.coroutineScope {
            launch { iconAlpha.animateTo(0f, tween(450, easing = FastOutSlowInEasing)) }
            launch { nameAlpha.animateTo(0f, tween(420, easing = FastOutSlowInEasing)) }
            launch { iconScale.animateTo(1.18f, tween(450, easing = FastOutSlowInEasing)) }
        }
        delay(120)

        // ================= SCENE 2: "created by Ascendens" =================
        currentScene = IntroScene.ASCENDENS
        ascendensSceneAlpha.snapTo(1f)
        createdByAlpha.animateTo(1f, tween(380, easing = FastOutSlowInEasing))
        // Staggered kinetic letter pop-up across "Ascendens"
        ascendensProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(850, easing = CubicBezierEasing(0.16f, 1f, 0.3f, 1f))
        )
        delay(1150)
        // Dissolve Scene 2
        ascendensSceneAlpha.animateTo(0f, tween(420, easing = FastOutSlowInEasing))
        delay(80)

        // ================= SCENE 3: ICCT Logo.png =================
        currentScene = IntroScene.ICCT_SEAL
        playLogoPortalScene()

        // ================= SCENE 4: ICCT_Engineering_Society_Logo =================
        currentScene = IntroScene.ENG_SOCIETY
        playLogoPortalScene()

        // ================= SCENE 5: icon.jpg (1992 Engineering Logo) =================
        currentScene = IntroScene.ENG_1992
        playLogoPortalScene()

        onFinish()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF050811)),
        contentAlignment = Alignment.Center
    ) {
        // Dynamic Ambient Aura matching each scene's palette
        val auraColors = when (currentScene) {
            IntroScene.APP_LOGO -> listOf(
                Color(0xFF0038A8).copy(alpha = 0.28f),
                Color(0xFFCE1126).copy(alpha = 0.22f),
                Color(0xFF050811)
            )
            IntroScene.ASCENDENS -> listOf(
                Color(0xFFF59E0B).copy(alpha = 0.22f),
                Color(0xFF1D4ED8).copy(alpha = 0.18f),
                Color(0xFF050811)
            )
            IntroScene.ICCT_SEAL -> listOf(
                Color(0xFF1D4ED8).copy(alpha = 0.30f),
                Color(0xFFDC2626).copy(alpha = 0.15f),
                Color(0xFF050811)
            )
            IntroScene.ENG_SOCIETY -> listOf(
                Color(0xFFEA580C).copy(alpha = 0.28f),
                Color(0xFFFACC15).copy(alpha = 0.16f),
                Color(0xFF050811)
            )
            IntroScene.ENG_1992 -> listOf(
                Color(0xFFF59E0B).copy(alpha = 0.30f),
                Color(0xFF16A34A).copy(alpha = 0.16f),
                Color(0xFF050811)
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.radialGradient(colors = auraColors, radius = 900f))
        )

        // Persistent Skip Button at Top-Right
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color.White.copy(alpha = 0.10f),
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 44.dp, end = 20.dp)
                .border(1.dp, Color.White.copy(alpha = 0.18f), RoundedCornerShape(20.dp))
                .clickable { onFinish() }
        ) {
            Text(
                text = "Skip ›",
                color = Color(0xFFE2E8F0),
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
            )
        }

        // =========================================================================
        // SCENE 1: APP ICON WITH ANIMATED OPENING + APP NAME BELOW -> FADES AWAY
        // =========================================================================
        if (currentScene == IntroScene.APP_LOGO) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp)
            ) {
                Box(
                    modifier = Modifier.size(230.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Animated Philippine Flag Tri-Color Opening Rings & Orbital Sparks
                    Canvas(
                        modifier = Modifier
                            .fillMaxSize()
                            .alpha(iconAlpha.value)
                    ) {
                        val center = Offset(size.width / 2f, size.height / 2f)
                        val maxR = size.minDimension / 2f
                        val r = ringExpand.value * maxR

                        // Expanding shockwave ring
                        if (ringExpand.value < 0.99f) {
                            drawCircle(
                                color = Color(0xFFFCD116).copy(alpha = (1f - ringExpand.value) * 0.75f),
                                radius = r * 1.15f,
                                center = center,
                                style = Stroke(width = 6f)
                            )
                        }

                        // Subtle rotating PH-flag tri-color orbital arcs around the transparent icon
                        drawArc(
                            color = Color(0xFF0038A8).copy(alpha = 0.65f),
                            startAngle = orbitAngle,
                            sweepAngle = 90f,
                            useCenter = false,
                            style = Stroke(width = 4f, cap = StrokeCap.Round)
                        )
                        drawArc(
                            color = Color(0xFFFCD116).copy(alpha = 0.75f),
                            startAngle = orbitAngle + 120f,
                            sweepAngle = 90f,
                            useCenter = false,
                            style = Stroke(width = 4f, cap = StrokeCap.Round)
                        )
                        drawArc(
                            color = Color(0xFFCE1126).copy(alpha = 0.75f),
                            startAngle = orbitAngle + 240f,
                            sweepAngle = 90f,
                            useCenter = false,
                            style = Stroke(width = 4f, cap = StrokeCap.Round)
                        )
                    }

                    // Transparent iWani Logo (Blue Left Hand + Golden Sun + Red Right Hand)
                    Image(
                        painter = painterResource(id = R.drawable.logo_iwani),
                        contentDescription = "iWani Official Logo",
                        modifier = Modifier
                            .size(185.dp)
                            .scale(iconScale.value)
                            .alpha(iconAlpha.value)
                            .graphicsLayer {
                                rotationZ = iconRotation.value
                            },
                        contentScale = ContentScale.Fit
                    )
                }

                Spacer(Modifier.height(22.dp))

                // App Name "iWani" animated below the icon
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .alpha(nameAlpha.value)
                        .offset(y = nameOffsetY.value.dp)
                ) {
                    Text(
                        text = "iWani",
                        fontSize = 42.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = nameTracking.value.sp,
                        style = TextStyle(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFF60A5FA),
                                    Color(0xFFFDE047),
                                    Color(0xFFF87171)
                                )
                            ),
                            shadow = Shadow(
                                color = Color(0xFFF59E0B).copy(alpha = 0.45f),
                                offset = Offset(0f, 4f),
                                blurRadius = 18f
                            )
                        )
                    )

                    Spacer(Modifier.height(8.dp))

                    Text(
                        text = "FILIPINO SIGN LANGUAGE • PROJECT NEXUS",
                        color = Color(0xFF94A3B8),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.5.sp
                    )
                }
            }
        }

        // =========================================================================
        // SCENE 2: "created by Ascendens" (KINETIC / INTERACTIVE FONT ANIMATION)
        // =========================================================================
        if (currentScene == IntroScene.ASCENDENS) {
            val word = "Ascendens"
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .alpha(ascendensSceneAlpha.value)
                    .scale(interactivePulse.value)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        // Interactive tap pulse on "Ascendens"
                        scope.launch {
                            interactivePulse.snapTo(1.12f)
                            interactivePulse.animateTo(
                                1f,
                                spring(dampingRatio = Spring.DampingRatioLowBouncy)
                            )
                        }
                    }
            ) {
                Text(
                    text = "CREATED BY",
                    color = Color(0xFF94A3B8),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 6.sp,
                    modifier = Modifier.alpha(createdByAlpha.value)
                )

                Spacer(Modifier.height(14.dp))

                // Per-character staggered 3D spring pop-up with sweeping metallic gold-cyan shimmer
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    word.forEachIndexed { index, char ->
                        val delayFraction = index * 0.065f
                        val rawLocal = ((ascendensProgress.value - delayFraction) / (1f - delayFraction)).coerceIn(0f, 1f)
                        // Overshoot curve for dramatic pop-up
                        val overshoot = if (rawLocal < 0.7f) {
                            (rawLocal / 0.7f) * 1.18f
                        } else {
                            1.18f - ((rawLocal - 0.7f) / 0.3f) * 0.18f
                        }
                        val charY = (1f - rawLocal) * 42f
                        val charRotX = (1f - rawLocal) * -75f

                        Text(
                            text = char.toString(),
                            fontSize = 46.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.5.sp,
                            modifier = Modifier
                                .alpha(rawLocal)
                                .offset(y = charY.dp)
                                .scale(overshoot.coerceAtLeast(0.01f))
                                .graphicsLayer {
                                    rotationX = charRotX
                                },
                            style = TextStyle(
                                brush = Brush.linearGradient(
                                    colors = listOf(
                                        Color(0xFFFDE68A),
                                        Color(0xFFF59E0B),
                                        Color(0xFFFFFFFF),
                                        Color(0xFF38BDF8),
                                        Color(0xFFF59E0B)
                                    ),
                                    start = Offset(shimmerSweep * 500f, 0f),
                                    end = Offset(shimmerSweep * 500f + 260f, 120f)
                                ),
                                shadow = Shadow(
                                    color = Color(0xFFF59E0B).copy(alpha = 0.65f),
                                    offset = Offset(0f, 6f),
                                    blurRadius = 24f
                                )
                            )
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Glowing underline bar that expands with Ascendens
                Box(
                    modifier = Modifier
                        .width((200f * ascendensProgress.value).dp)
                        .height(2.5.dp)
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color(0xFFF59E0B),
                                    Color(0xFF38BDF8),
                                    Color.Transparent
                                )
                            ),
                            shape = RoundedCornerShape(2.dp)
                        )
                )
            }
        }

        // =========================================================================
        // SCENES 3, 4, 5: THE THREE TRANSPARENT INSTITUTIONAL LOGOS WITH PORTAL TRANSITIONS
        // =========================================================================
        if (currentScene == IntroScene.ICCT_SEAL ||
            currentScene == IntroScene.ENG_SOCIETY ||
            currentScene == IntroScene.ENG_1992
        ) {
            val (drawableRes, badgeTag, mainTitle, subTitle, ringColor) = when (currentScene) {
                IntroScene.ICCT_SEAL -> LogoSceneSpec(
                    resId = R.drawable.icct_seal,
                    badgeTag = "OFFICIAL INSTITUTIONAL SEAL",
                    title = "ICCT COLLEGES FOUNDATION INC.",
                    subtitle = "A Global Pinoy Distinction",
                    ringColor = Color(0xFF3B82F6)
                )
                IntroScene.ENG_SOCIETY -> LogoSceneSpec(
                    resId = R.drawable.icct_eng_soc_2002,
                    badgeTag = "★ ESTABLISHED 2002 ★",
                    title = "ICCT ENGINEERING SOCIETY",
                    subtitle = "Leadership • Innovation • Excellence",
                    ringColor = Color(0xFFF97316)
                )
                else -> LogoSceneSpec(
                    resId = R.drawable.icct_engineering_1992,
                    badgeTag = "★ ESTABLISHED 1992 ★",
                    title = "COLLEGE OF COMPUTER STUDIES\n& ENGINEERING",
                    subtitle = "Computer Engineering • Electronics & Communications",
                    ringColor = Color(0xFFF59E0B)
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(28.dp)
                    .alpha(badgeAlpha.value)
            ) {
                Box(
                    modifier = Modifier.size(250.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Luminous Portal Shockwave & Orbital Energy Halo (no square background!)
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val center = Offset(size.width / 2f, size.height / 2f)
                        val maxR = size.minDimension / 2f
                        val sw = shockwaveProgress.value

                        if (sw < 0.99f) {
                            drawCircle(
                                color = ringColor.copy(alpha = (1f - sw) * 0.7f),
                                radius = maxR * (0.55f + sw * 0.65f),
                                center = center,
                                style = Stroke(width = 8f * (1f - sw * 0.5f))
                            )
                        }

                        // Rotating energy arcs framing the transparent emblem
                        drawArc(
                            color = ringColor.copy(alpha = 0.6f),
                            startAngle = orbitAngle,
                            sweepAngle = 110f,
                            useCenter = false,
                            style = Stroke(width = 3.5f, cap = StrokeCap.Round)
                        )
                        drawArc(
                            color = IcctGold.copy(alpha = 0.5f),
                            startAngle = orbitAngle + 180f,
                            sweepAngle = 110f,
                            useCenter = false,
                            style = Stroke(width = 3.5f, cap = StrokeCap.Round)
                        )

                        // 6 orbiting light particles
                        for (i in 0 until 6) {
                            val rad = Math.toRadians((orbitAngle + i * 60f).toDouble())
                            val px = center.x + (maxR * 0.96f) * cos(rad).toFloat()
                            val py = center.y + (maxR * 0.96f) * sin(rad).toFloat()
                            drawCircle(
                                color = Color.White.copy(alpha = 0.75f),
                                radius = 3.5f,
                                center = Offset(px, py)
                            )
                        }
                    }

                    // Transparent Logo with 3D Portal Perspective Rotation & Scale
                    Image(
                        painter = painterResource(id = drawableRes),
                        contentDescription = mainTitle,
                        modifier = Modifier
                            .size(215.dp)
                            .scale(badgeScale.value)
                            .graphicsLayer {
                                rotationY = badgeRotationY.value
                                cameraDistance = 14f * density
                            },
                        contentScale = ContentScale.Fit
                    )
                }

                Spacer(Modifier.height(24.dp))

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.offset(y = textSlideY.value.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = ringColor.copy(alpha = 0.16f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, ringColor.copy(alpha = 0.55f))
                    ) {
                        Text(
                            text = badgeTag,
                            color = Color(0xFFFDE68A),
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 11.sp,
                            letterSpacing = 2.sp,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                        )
                    }

                    Spacer(Modifier.height(14.dp))

                    Text(
                        text = mainTitle,
                        color = Color.White,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.2.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 23.sp
                    )

                    Spacer(Modifier.height(6.dp))

                    Text(
                        text = subTitle,
                        color = Color(0xFF93C5FD),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 0.5.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        // Subtle 5-Stage Cinematic Progress Dots at Bottom
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 36.dp)
        ) {
            IntroScene.values().forEach { scene ->
                val isSelected = scene == currentScene
                Box(
                    modifier = Modifier
                        .height(5.dp)
                        .width(if (isSelected) 22.dp else 6.dp)
                        .background(
                            color = if (isSelected) IcctGold else Color.White.copy(alpha = 0.22f),
                            shape = CircleShape
                        )
                )
            }
        }
    }
}

private data class LogoSceneSpec(
    val resId: Int,
    val badgeTag: String,
    val title: String,
    val subtitle: String,
    val ringColor: Color
)
