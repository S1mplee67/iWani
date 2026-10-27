package com.projectnexus.iwani.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
 * Libraries.dev `border-beam` implementation for Jetpack Compose:
 * A smooth traveling light beam around the border of an active card, AI extraction box, or primary CTA.
 */
@Composable
fun BorderBeamBox(
    modifier: Modifier = Modifier,
    active: Boolean = true,
    cornerRadius: Dp = 22.dp,
    borderWidth: Dp = 1.6.dp,
    beamColor: Color = Color(0xFF38BDF8),
    secondaryColor: Color = Color(0xFFF59E0B),
    baseBorderColor: Color = Color(0xFF25334D),
    backgroundColor: Color = Color(0xCC16233B),
    durationMillis: Int = 3400,
    content: @Composable BoxScope.() -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "BorderBeamTransition")
    val angle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
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
                drawContent()
                val strokePx = borderWidth.toPx()
                val crPx = cornerRadius.toPx()
                // Base subtle border
                drawRoundRect(
                    color = baseBorderColor,
                    cornerRadius = CornerRadius(crPx, crPx),
                    style = Stroke(width = strokePx)
                )
                if (active) {
                    // Traveling Sweep Gradient Border Beam
                    rotate(degrees = angle) {
                        drawRoundRect(
                            brush = Brush.sweepGradient(
                                0.0f to Color.Transparent,
                                0.55f to Color.Transparent,
                                0.78f to beamColor.copy(alpha = 0.65f),
                                0.92f to secondaryColor,
                                0.98f to Color.White,
                                1.0f to Color.Transparent
                            ),
                            cornerRadius = CornerRadius(crPx, crPx),
                            style = Stroke(width = strokePx * 1.45f)
                        )
                    }
                }
            },
        content = content
    )
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
            // Wave modulation based on state
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
 */
@Composable
fun MetalBadge(
    text: String,
    modifier: Modifier = Modifier,
    isDark: Boolean = true,
    fontSize: TextUnit = 10.sp
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
    Box(
        modifier = modifier
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
            .padding(horizontal = 10.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = fontSize,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 1.2.sp,
            color = if (isDark) Color(0xFFF8FAFC) else Color(0xFF0F172A)
        )
    }
}
