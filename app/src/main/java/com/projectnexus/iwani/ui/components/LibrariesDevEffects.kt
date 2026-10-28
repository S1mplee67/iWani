package com.projectnexus.iwani.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.projectnexus.iwani.R
import com.projectnexus.iwani.ui.sound.UiSoundManager
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Interactive Spring Bounce + Synthesized Sound Click Modifier.
 * Gives every button and card a tactile spring transition and unique sound effect on click.
 */
fun Modifier.interactiveBounceClick(
    enabled: Boolean = true,
    isToggle: Boolean = false,
    onClick: () -> Unit
): Modifier = composed {
    val context = LocalContext.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed && enabled) 0.94f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "InteractiveBounceScale"
    )

    this
        .scale(scale)
        .clickable(
            interactionSource = interactionSource,
            indication = null,
            enabled = enabled
        ) {
            if (isToggle) {
                UiSoundManager.playToggle(context)
            } else {
                UiSoundManager.playClick(context)
            }
            onClick()
        }
}

/**
 * Smoothly slides single-line text to the left ONLY when the text is wider than its box.
 * Prevents truncated "..." text in chips, badges, headers, and buttons.
 */
@OptIn(ExperimentalFoundationApi::class)
fun Modifier.autoSlideIfOverflow(): Modifier = this.basicMarquee(
    iterations = Int.MAX_VALUE,
    repeatDelayMillis = 1200,
    initialDelayMillis = 700,
    velocity = 26.dp
)

/**
 * Libraries.dev `border-beam` implementation for Jetpack Compose:
 * Stationary rounded-rectangle border with an orbiting radial light focal point.
 * Never rotates the rectangle geometry itself, preventing any diagonal lines across cards.
 */
@Composable
fun BorderBeamBox(
    modifier: Modifier = Modifier,
    active: Boolean = true,
    cornerRadius: Dp = 22.dp,
    borderWidth: Dp = 1.8.dp,
    beamColor: Color = Color(0xFF38BDF8),
    secondaryColor: Color = Color(0xFFF59E0B),
    baseBorderColor: Color = Color(0xFF25334D),
    backgroundColor: Color = Color(0xCC16233B),
    durationMillis: Int = 3600,
    content: @Composable BoxScope.() -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "BorderBeamTransition")
    val angle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2f * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = durationMillis, easing = LinearEasing)
        ),
        label = "BorderBeamAngle"
    )

    val shape = RoundedCornerShape(cornerRadius)

    Box(
        modifier = modifier
            .clip(shape)
            .background(backgroundColor, shape)
            .drawWithContent {
                val strokePx = borderWidth.toPx()
                val crPx = cornerRadius.toPx()
                val halfStroke = strokePx / 2f
                val topLeft = Offset(halfStroke, halfStroke)
                val rectSize = androidx.compose.ui.geometry.Size(
                    width = (size.width - strokePx).coerceAtLeast(1f),
                    height = (size.height - strokePx).coerceAtLeast(1f)
                )

                // Soft interior corner glow that follows the beam without any rotating lines
                if (active) {
                    val beamX = size.width * 0.5f + cos(angle) * (size.width * 0.48f)
                    val beamY = size.height * 0.5f + sin(angle) * (size.height * 0.48f)
                    drawRect(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                beamColor.copy(alpha = 0.14f),
                                secondaryColor.copy(alpha = 0.06f),
                                Color.Transparent
                            ),
                            center = Offset(beamX, beamY),
                            radius = size.maxDimension * 0.55f
                        )
                    )
                }

                drawContent()

                // Stationary Base Border
                drawRoundRect(
                    color = baseBorderColor,
                    topLeft = topLeft,
                    size = rectSize,
                    cornerRadius = CornerRadius(crPx, crPx),
                    style = Stroke(width = strokePx)
                )

                if (active) {
                    // Primary traveling light spot along the stationary perimeter
                    val beamX1 = size.width * 0.5f + cos(angle) * (size.width * 0.52f)
                    val beamY1 = size.height * 0.5f + sin(angle) * (size.height * 0.52f)
                    drawRoundRect(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color.White,
                                secondaryColor,
                                beamColor.copy(alpha = 0.75f),
                                Color.Transparent
                            ),
                            center = Offset(beamX1, beamY1),
                            radius = (size.maxDimension * 0.48f).coerceAtLeast(40f)
                        ),
                        topLeft = topLeft,
                        size = rectSize,
                        cornerRadius = CornerRadius(crPx, crPx),
                        style = Stroke(width = strokePx * 1.35f)
                    )

                    // Opposite secondary counter-beam for balanced dual-highlight look
                    val beamX2 = size.width * 0.5f + cos(angle + PI.toFloat()) * (size.width * 0.52f)
                    val beamY2 = size.height * 0.5f + sin(angle + PI.toFloat()) * (size.height * 0.52f)
                    drawRoundRect(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                beamColor.copy(alpha = 0.85f),
                                secondaryColor.copy(alpha = 0.4f),
                                Color.Transparent
                            ),
                            center = Offset(beamX2, beamY2),
                            radius = (size.maxDimension * 0.38f).coerceAtLeast(30f)
                        ),
                        topLeft = topLeft,
                        size = rectSize,
                        cornerRadius = CornerRadius(crPx, crPx),
                        style = Stroke(width = strokePx * 1.15f)
                    )
                }
            },
        content = content
    )
}

/**
 * Libraries.dev `liquid-gooey` inspired Ambient Aurora Glow Background:
 * Smooth, non-repetitive floating luminous nebula orbs for backgrounds and full-screen modals.
 */
@Composable
fun AmbientAuroraGlow(
    modifier: Modifier = Modifier,
    primaryOrbColor: Color = Color(0xFF2563EB),
    secondaryOrbColor: Color = Color(0xFFF59E0B),
    tertiaryOrbColor: Color = Color(0xFF14B8A6),
    alphaMultiplier: Float = 1f
) {
    val infiniteTransition = rememberInfiniteTransition(label = "AuroraGlow")
    val drift by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2f * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(7200, easing = LinearEasing)
        ),
        label = "AuroraDrift"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        val r = size.maxDimension * 0.52f

        val p1 = Offset(
            x = w * (0.22f + 0.14f * cos(drift)),
            y = h * (0.20f + 0.10f * sin(drift))
        )
        val p2 = Offset(
            x = w * (0.78f + 0.14f * sin(drift * 0.8f)),
            y = h * (0.55f + 0.12f * cos(drift * 0.8f))
        )
        val p3 = Offset(
            x = w * (0.35f + 0.15f * sin(drift + 1.5f)),
            y = h * (0.82f + 0.08f * cos(drift + 1.5f))
        )

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    primaryOrbColor.copy(alpha = 0.22f * alphaMultiplier),
                    Color.Transparent
                ),
                center = p1,
                radius = r
            ),
            radius = r,
            center = p1
        )
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    secondaryOrbColor.copy(alpha = 0.18f * alphaMultiplier),
                    Color.Transparent
                ),
                center = p2,
                radius = r * 0.9f
            ),
            radius = r * 0.9f,
            center = p2
        )
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    tertiaryOrbColor.copy(alpha = 0.15f * alphaMultiplier),
                    Color.Transparent
                ),
                center = p3,
                radius = r * 0.85f
            ),
            radius = r * 0.85f,
            center = p3
        )
    }
}

/**
 * Libraries.dev `thinking-orbs` implementation for Jetpack Compose:
 * A 3D Fibonacci-sphere dotted orb rendered on a 2D Canvas that visualizes AI states
 * ("working", "extracting", "listening", "breathing").
 */
@Composable
fun ThinkingOrb(
    modifier: Modifier = Modifier,
    size: Dp = 28.dp,
    state: String = "working",
    primaryColor: Color = Color(0xFF38BDF8),
    accentColor: Color = Color(0xFFF59E0B)
) {
    val infiniteTransition = rememberInfiniteTransition(label = "ThinkingOrbAnim")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2f * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = when (state) {
                    "extracting", "working" -> 2100
                    "listening" -> 1500
                    else -> 3200
                },
                easing = LinearEasing
            )
        ),
        label = "OrbPhase"
    )

    Canvas(modifier = modifier.size(size)) {
        val cx = this.size.width / 2f
        val cy = this.size.height / 2f
        val radius = this.size.minDimension * 0.42f
        val dotCount = 36
        val goldenRatio = (1f + sqrt(5f)) / 2f

        for (i in 0 until dotCount) {
            val yNorm = 1f - (i.toFloat() / (dotCount - 1)) * 2f
            val rSlice = sqrt((1f - yNorm * yNorm).coerceAtLeast(0f))
            val theta = (2f * PI * i / goldenRatio).toFloat() + phase

            val x3d = cos(theta) * rSlice
            val z3d = sin(theta) * rSlice
            val pulse = if (state == "listening") {
                1f + 0.16f * sin(phase * 2f + yNorm * 3f)
            } else {
                1f + 0.08f * cos(phase + i * 0.3f)
            }

            val depth = ((z3d + 1f) / 2f).coerceIn(0.15f, 1f)
            val px = cx + x3d * radius * pulse
            val py = cy + yNorm * radius * pulse
            val dotRadius = (this.size.minDimension * 0.042f) * (0.55f + 0.75f * depth)
            val color = if (i % 3 == 0) accentColor else primaryColor

            drawCircle(
                color = color.copy(alpha = 0.25f + 0.75f * depth),
                radius = dotRadius,
                center = Offset(px, py)
            )
        }
    }
}

/**
 * Libraries.dev `voice-glow` (`VoiceBeam`) implementation for Jetpack Compose:
 * An audio-reactive luminous wave glow along the bottom edge of voice/SOS elements.
 */
@Composable
fun VoiceGlowBar(
    active: Boolean,
    modifier: Modifier = Modifier,
    primaryColor: Color = Color(0xFFEF4444),
    secondaryColor: Color = Color(0xFFF59E0B)
) {
    val infiniteTransition = rememberInfiniteTransition(label = "VoiceGlowAnim")
    val wavePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2f * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(1100, easing = LinearEasing)
        ),
        label = "VoiceWavePhase"
    )

    if (!active) return

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(22.dp)
    ) {
        val w = size.width
        val h = size.height
        val bars = 24
        val step = w / bars
        for (i in 0 until bars) {
            val norm = (i.toFloat() / bars) * PI.toFloat()
            val env = sin(norm)
            val amp = (0.35f + 0.65f * kotlin.math.abs(sin(wavePhase + i * 0.45f))) * env
            val barH = (h * amp).coerceAtLeast(3f)
            val x = i * step + step / 2f
            drawLine(
                brush = Brush.verticalGradient(
                    colors = listOf(secondaryColor, primaryColor)
                ),
                start = Offset(x, h),
                end = Offset(x, h - barH),
                strokeWidth = step * 0.48f,
                cap = StrokeCap.Round
            )
        }
    }
}

/**
 * Libraries.dev `metal-fx` (`MetalBadge`) implementation for Jetpack Compose:
 * Real-time liquid-metal specular badge for highlighting key tags.
 * Enforces single-line non-wrapping text so it never compresses on narrow headers.
 */
@Composable
fun MetalBadge(
    text: String,
    modifier: Modifier = Modifier,
    isDark: Boolean = true,
    fontSize: TextUnit = 10.sp,
    onClick: (() -> Unit)? = null
) {
    val infiniteTransition = rememberInfiniteTransition(label = "MetalSheen")
    val sheen by infiniteTransition.animateFloat(
        initialValue = -100f,
        targetValue = 300f,
        animationSpec = infiniteRepeatable(
            animation = tween(2800, easing = LinearEasing)
        ),
        label = "MetalSheenOffset"
    )

    val shape = RoundedCornerShape(50)
    val clickModifier = if (onClick != null) {
        Modifier.interactiveBounceClick(onClick = onClick)
    } else {
        Modifier
    }

    Box(
        modifier = modifier
            .then(clickModifier)
            .wrapContentWidth(unbounded = false)
            .clip(shape)
            .background(
                brush = Brush.linearGradient(
                    colors = if (isDark) {
                        listOf(
                            Color(0xFF1E293B),
                            Color(0xFF475569),
                            Color(0xFF94A3B8),
                            Color(0xFF334155),
                            Color(0xFF1E293B)
                        )
                    } else {
                        listOf(
                            Color(0xFFE2E8F0),
                            Color(0xFFF8FAFC),
                            Color(0xFFCBD5E1),
                            Color(0xFFE2E8F0)
                        )
                    },
                    start = Offset(sheen, 0f),
                    end = Offset(sheen + 140f, 60f)
                ),
                shape = shape
            )
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.55f),
                        Color(0xFFF59E0B).copy(alpha = 0.45f),
                        Color.White.copy(alpha = 0.25f)
                    )
                ),
                shape = shape
            )
            .padding(horizontal = 10.dp, vertical = 5.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = fontSize,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 1.0.sp,
            maxLines = 1,
            softWrap = false,
            color = if (isDark) Color(0xFFF8FAFC) else Color(0xFF0F172A)
        )
    }
}

/**
 * Enlarged Full-Portrait FSL Alphabet Chart (A-Z) Modal with Ambient Aurora Glow Background
 * and interactive Pinch-to-Zoom / Double-Tap Zoom.
 * Fills the entire portrait screen with crisp, enlarged A-Z hand sign cards.
 */
@Composable
fun FslAlphabetFullModal(
    isTagalog: Boolean,
    onDismiss: () -> Unit
) {
    var scale by remember { mutableFloatStateOf(1f) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF060B18))
        ) {
            // Animated Ambient Aurora & Nebula Glow Background
            AmbientAuroraGlow(
                primaryOrbColor = Color(0xFF38BDF8),
                secondaryOrbColor = Color(0xFFF59E0B),
                tertiaryOrbColor = Color(0xFF6366F1),
                alphaMultiplier = 1.35f
            )

            // Full-Portrait Chart Container filling the entire portrait slide
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 6.dp, vertical = 8.dp)
            ) {
                // Compact Floating Top Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = if (isTagalog) "FSL Alphabet Chart (A–Z)" else "FSL Alphabet Chart (A–Z)",
                            color = Color(0xFFFBBF24),
                            fontWeight = FontWeight.Black,
                            fontSize = 17.sp,
                            maxLines = 1,
                            softWrap = false
                        )
                        Spacer(Modifier.width(8.dp))
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFF38BDF8).copy(alpha = 0.16f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF38BDF8).copy(alpha = 0.45f)),
                            modifier = Modifier.interactiveBounceClick {
                                if (scale > 1.05f) {
                                    scale = 1f
                                    offsetX = 0f
                                    offsetY = 0f
                                } else {
                                    scale = 1.65f
                                }
                            }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ZoomIn,
                                    contentDescription = "Zoom",
                                    tint = Color(0xFF7DD3FC),
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    text = if (scale > 1.05f) "100%" else (if (isTagalog) "I-zoom" else "Zoom"),
                                    color = Color(0xFFE0F2FE),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(36.dp)
                            .background(Color.White.copy(alpha = 0.12f), CircleShape)
                            .border(1.dp, Color.White.copy(alpha = 0.25f), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(Modifier.height(4.dp))

                // Enlarged Full-Portrait Image Box with glowing border & pinch/double-tap zoom
                BorderBeamBox(
                    active = true,
                    cornerRadius = 20.dp,
                    beamColor = Color(0xFF38BDF8),
                    secondaryColor = Color(0xFFF59E0B),
                    baseBorderColor = Color(0xFF1E3A8A),
                    backgroundColor = Color(0xDD080F20),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .pointerInput(Unit) {
                                detectTapGestures(
                                    onDoubleTap = {
                                        if (scale > 1.05f) {
                                            scale = 1f
                                            offsetX = 0f
                                            offsetY = 0f
                                        } else {
                                            scale = 1.75f
                                        }
                                    }
                                )
                            }
                            .pointerInput(Unit) {
                                detectTransformGestures { _, pan, zoom, _ ->
                                    val newScale = (scale * zoom).coerceIn(1f, 3f)
                                    scale = newScale
                                    if (newScale > 1.02f) {
                                        offsetX = (offsetX + pan.x).coerceIn(-600f, 600f)
                                        offsetY = (offsetY + pan.y).coerceIn(-900f, 900f)
                                    } else {
                                        offsetX = 0f
                                        offsetY = 0f
                                    }
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        // Ambient radial glow behind the chart
                        AmbientAuroraGlow(
                            primaryOrbColor = Color(0xFF2563EB),
                            secondaryOrbColor = Color(0xFFF59E0B),
                            tertiaryOrbColor = Color(0xFF10B981),
                            alphaMultiplier = 0.9f
                        )

                        Image(
                            painter = painterResource(id = R.drawable.alfabet_fsl_full),
                            contentDescription = "FSL Alphabet Chart Full Portrait",
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(4.dp)
                                .graphicsLayer {
                                    scaleX = scale
                                    scaleY = scale
                                    translationX = offsetX
                                    translationY = offsetY
                                },
                            contentScale = ContentScale.FillBounds
                        )
                    }
                }
            }
        }
    }
}
