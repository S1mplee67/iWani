package com.projectnexus.iwani.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.projectnexus.iwani.R
import com.projectnexus.iwani.ui.theme.*
import kotlinx.coroutines.delay

enum class SplashStage {
    SEAL,        // Stage 1: ICCT Colleges Foundation Official Seal (fade in & fade out)
    ENGINEERING  // Stage 2: ICCT Engineering Logo with Slice Transition (1992 highlighted)
}

/**
 * Cinematic Two-Stage Intro / Splash Screen for iWani:
 * 1. Stage 1: ICCT Colleges Foundation official seal fades in with golden ambient glow, holds, then fades out.
 * 2. Stage 2: ICCT Computer & Communications Engineering logo enters via a dramatic diagonal slice transition,
 *    snapping together with a golden beam flash to highlight the "1992" foundation, then smoothly opens the app.
 */
@Composable
fun SplashScreen(
    onFinish: () -> Unit
) {
    var stage by remember { mutableStateOf(SplashStage.SEAL) }

    // Stage 1: Seal Animations
    val sealAlpha = remember { Animatable(0f) }
    val sealScale = remember { Animatable(0.85f) }

    // Stage 2: Engineering Slice Transition Animations
    val sliceSlideProgress = remember { Animatable(0f) }
    val sliceFlashAlpha = remember { Animatable(0f) }
    val engContentAlpha = remember { Animatable(0f) }

    // Cinematic orchestrator
    LaunchedEffect(Unit) {
        // --- STAGE 1: ICCT Official Seal ---
        sealAlpha.animateTo(1f, animationSpec = tween(700, easing = FastOutSlowInEasing))
        sealScale.animateTo(1f, animationSpec = tween(700, easing = FastOutSlowInEasing))
        delay(1100) // View the official seal
        // Fade out Seal
        sealAlpha.animateTo(0f, animationSpec = tween(500, easing = FastOutSlowInEasing))
        sealScale.animateTo(1.08f, animationSpec = tween(500, easing = FastOutSlowInEasing))

        // --- STAGE 2: ICCT Engineering 1992 Logo (Slice Transition) ---
        stage = SplashStage.ENGINEERING
        engContentAlpha.snapTo(1f)
        
        // Two slice halves slide into place from opposite diagonal offsets
        sliceSlideProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 800, easing = CubicBezierEasing(0.16f, 1f, 0.3f, 1f))
        )
        // Flash of light along the slice cut line when pieces snap together
        sliceFlashAlpha.snapTo(1f)
        sliceFlashAlpha.animateTo(0f, animationSpec = tween(450, easing = FastOutLinearInEasing))

        delay(1400) // Showcase the 1992 engineering emblem

        // Fade out into main app
        engContentAlpha.animateTo(0f, animationSpec = tween(400, easing = FastOutSlowInEasing))
        delay(50)
        onFinish()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF060A13))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                // Tap anywhere to skip intro immediately
                onFinish()
            },
        contentAlignment = Alignment.Center
    ) {
        // Subtle background radial gradient
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF1E3A8A).copy(alpha = 0.25f),
                            Color(0xFF0F172A).copy(alpha = 0.6f),
                            Color(0xFF060A13)
                        )
                    )
                )
        )

        // Top right Skip Button
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color.White.copy(alpha = 0.08f),
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 44.dp, end = 20.dp)
                .clickable { onFinish() }
        ) {
            Text(
                text = "Skip ›",
                color = TextSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
            )
        }

        // ================= STAGE 1: ICCT OFFICIAL SEAL =================
        if (stage == SplashStage.SEAL) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp)
                    .alpha(sealAlpha.value)
                    .scale(sealScale.value)
            ) {
                // Ambient Glow behind Seal
                Box(
                    modifier = Modifier.size(240.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(230.dp)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        IcctGold.copy(alpha = 0.35f),
                                        IcctBlue.copy(alpha = 0.2f),
                                        Color.Transparent
                                    )
                                ),
                                shape = CircleShape
                            )
                    )
                    Image(
                        painter = painterResource(id = R.drawable.icct_seal),
                        contentDescription = "ICCT Colleges Official Seal",
                        modifier = Modifier
                            .size(220.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Fit
                    )
                }

                Spacer(Modifier.height(28.dp))

                Text(
                    text = "ICCT COLLEGES FOUNDATION INC.",
                    color = Color.White,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.5.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(8.dp))

                Text(
                    text = "A GLOBAL PINOY DISTINCTION",
                    color = IcctGold,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(6.dp))

                Text(
                    text = "Office of Academic Affairs • Institutional Seal",
                    color = TextSecondary,
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center
                )
            }
        }

        // ================= STAGE 2: ICCT ENGINEERING 1992 LOGO (SLICE TRANSITION) =================
        if (stage == SplashStage.ENGINEERING) {
            val progress = sliceSlideProgress.value
            val flashAlpha = sliceFlashAlpha.value

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .alpha(engContentAlpha.value)
            ) {
                // Sliced Emblem Container (250dp x 250dp)
                Box(
                    modifier = Modifier.size(250.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Ambient radial glow behind gear
                    Box(
                        modifier = Modifier
                            .size(240.dp)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        Color(0xFFF59E0B).copy(alpha = 0.35f),
                                        Color(0xFF2563EB).copy(alpha = 0.25f),
                                        Color.Transparent
                                    )
                                ),
                                shape = CircleShape
                            )
                    )

                    // SLICE HALF 1: Top-Left Diagonal Half (Slides in from -TopLeft)
                    val offset1X = ((1f - progress) * -110f).dp
                    val offset1Y = ((1f - progress) * -70f).dp

                    Box(
                        modifier = Modifier
                            .size(230.dp)
                            .offset(x = offset1X, y = offset1Y)
                            .drawWithContent {
                                val w = size.width
                                val h = size.height
                                val diagonalPath = Path().apply {
                                    moveTo(0f, 0f)
                                    lineTo(w, 0f)
                                    lineTo(0f, h)
                                    close()
                                }
                                clipPath(diagonalPath) {
                                    this@drawWithContent.drawContent()
                                }
                            }
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.icct_engineering_1992),
                            contentDescription = "ICCT Engineering 1992 Top Slice",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Fit
                        )
                    }

                    // SLICE HALF 2: Bottom-Right Diagonal Half (Slides in from +BottomRight)
                    val offset2X = ((1f - progress) * 110f).dp
                    val offset2Y = ((1f - progress) * 70f).dp

                    Box(
                        modifier = Modifier
                            .size(230.dp)
                            .offset(x = offset2X, y = offset2Y)
                            .drawWithContent {
                                val w = size.width
                                val h = size.height
                                val diagonalPath = Path().apply {
                                    moveTo(w, 0f)
                                    lineTo(w, h)
                                    lineTo(0f, h)
                                    close()
                                }
                                clipPath(diagonalPath) {
                                    this@drawWithContent.drawContent()
                                }
                            }
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.icct_engineering_1992),
                            contentDescription = "ICCT Engineering 1992 Bottom Slice",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Fit
                        )
                    }

                    // Glowing Slice Cut Flash Line along diagonal when snapping together
                    if (flashAlpha > 0.01f) {
                        androidx.compose.foundation.Canvas(modifier = Modifier.size(230.dp)) {
                            val w = size.width
                            val h = size.height
                            // Draw glowing slash line from (w, 0) to (0, h)
                            drawLine(
                                color = Color.White.copy(alpha = flashAlpha),
                                start = Offset(w, 0f),
                                end = Offset(0f, h),
                                strokeWidth = 8f
                            )
                            drawLine(
                                color = IcctGold.copy(alpha = flashAlpha),
                                start = Offset(w, 0f),
                                end = Offset(0f, h),
                                strokeWidth = 18f
                            )
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))

                // Golden 1992 Foundation Pill
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color(0xFFF59E0B).copy(alpha = 0.16f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF59E0B).copy(alpha = 0.6f)),
                    modifier = Modifier.scale(if (progress > 0.8f) 1f else 0.8f)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "★",
                            color = Color(0xFFFBBF24),
                            fontSize = 11.sp
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = "ESTABLISHED 1992",
                            color = Color(0xFFFDE68A),
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 13.sp,
                            letterSpacing = 2.sp
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = "★",
                            color = Color(0xFFFBBF24),
                            fontSize = 11.sp
                        )
                    }
                }

                Spacer(Modifier.height(14.dp))

                Text(
                    text = "COLLEGE OF COMPUTER STUDIES\n& ENGINEERING",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.2.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp
                )

                Spacer(Modifier.height(6.dp))

                Text(
                    text = "Computer Engineering • Electronics & Communications",
                    color = Color(0xFF93C5FD),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 0.5.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(16.dp))

                Text(
                    text = "Project Nexus • iWani System",
                    color = TextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Normal,
                    letterSpacing = 1.sp
                )
            }
        }
    }
}
