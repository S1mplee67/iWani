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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
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

private data class Atom3D(
    val x: Float,
    val y: Float,
    val z: Float,
    val radiusScale: Float,
    val colorOffset: Float
)

/**
 * 3D Rotating Ball-and-Stick Molecular Structure Icon (for MediaPipe FSL 3D):
 * 5 well-spaced 3D atom spheres connected by long, clearly visible metallic sticks.
 * Rotates smoothly to the right with exact 2*PI periodicity (zero loop flinch) and color-changing glossy balls.
 */
@Composable
fun MolecularStructureOrb(
    modifier: Modifier = Modifier,
    size: Dp = 40.dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "MolecularStructureAnim")
    val rotPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2f * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2800, easing = LinearEasing)
        ),
        label = "MolRotPhase"
    )
    val colorPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3600, easing = LinearEasing)
        ),
        label = "MolColorPhase"
    )

    // 5 widely spaced 3D atoms so the long connecting sticks in between are always clearly visible
    val atoms = remember {
        listOf(
            Atom3D(0.0f, 0.04f, 0.0f, 1.05f, 0.0f),     // 0: Center Hub Atom
            Atom3D(0.0f, -0.78f, 0.18f, 0.96f, 1.2f),   // 1: Top Atom
            Atom3D(-0.76f, 0.44f, 0.42f, 0.98f, 2.4f),  // 2: Bottom-Left Front Atom
            Atom3D(0.72f, 0.48f, -0.44f, 0.98f, 3.6f),  // 3: Bottom-Right Back Atom
            Atom3D(0.68f, -0.30f, 0.54f, 0.92f, 4.8f)   // 4: Upper-Right Front Atom
        )
    }

    val bonds = remember {
        listOf(
            0 to 1,
            0 to 2,
            0 to 3,
            0 to 4,
            1 to 4
        )
    }

    val palette = remember {
        listOf(
            Color(0xFFEF4444), // Crimson Red
            Color(0xFF0EA5E9), // Sky Cyan-Blue
            Color(0xFF10B981), // Emerald Green
            Color(0xFFF59E0B), // Amber Gold
            Color(0xFFA855F7), // Vivid Violet
            Color(0xFFF8FAFC)  // Pearl White
        )
    }

    Canvas(modifier = modifier.size(size)) {
        val cx = this.size.width * 0.5f
        val cy = this.size.height * 0.52f
        val scale = this.size.minDimension * 0.42f

        // Smooth rightward 3D rotation with exact 1x harmonic tilt (f(0) == f(2*PI), zero loop flinch!)
        val angleY = -rotPhase
        val cosY = cos(angleY)
        val sinY = sin(angleY)
        val tiltX = 0.28f + 0.14f * sin(rotPhase)
        val cosX = cos(tiltX)
        val sinX = sin(tiltX)

        // Project all 3D atoms
        val projected = atoms.mapIndexed { idx, a ->
            val x1 = a.x * cosY - a.z * sinY
            val z1 = a.x * sinY + a.z * cosY
            val y2 = a.y * cosX - z1 * sinX
            val z2 = a.y * sinX + z1 * cosX
            val persp = 1f / (1f - z2 * 0.22f)
            val px = cx + x1 * scale * persp
            val py = cy + y2 * scale * persp
            Triple(idx, Offset(px, py), z2)
        }

        // 1. Draw Long 3D Cylindrical Sticks sorted back-to-front
        val sortedBonds = bonds.sortedBy { (i, j) ->
            (projected[i].third + projected[j].third) * 0.5f
        }
        val stickWidth = (this.size.minDimension * 0.062f).coerceAtLeast(2.2f)
        for ((i, j) in sortedBonds) {
            val p1 = projected[i].second
            val p2 = projected[j].second
            val avgZ = (projected[i].third + projected[j].third) * 0.5f
            val depthAlpha = ((avgZ + 1f) * 0.5f).coerceIn(0.55f, 1f)

            // Metallic grey cylinder base
            drawLine(
                color = Color(0xFF64748B).copy(alpha = depthAlpha),
                start = p1,
                end = p2,
                strokeWidth = stickWidth,
                cap = StrokeCap.Round
            )
            // Bright silver specular highlight down the middle of the stick
            drawLine(
                color = Color(0xFFE2E8F0).copy(alpha = depthAlpha * 0.9f),
                start = p1,
                end = p2,
                strokeWidth = stickWidth * 0.38f,
                cap = StrokeCap.Round
            )
        }

        // 2. Draw 3D Glossy Color-Changing Atom Balls sorted back-to-front
        val sortedAtoms = projected.sortedBy { it.third }
        for ((idx, pos, z) in sortedAtoms) {
            val atom = atoms[idx]
            val depthScale = 0.84f + 0.24f * ((z + 1f) * 0.5f).coerceIn(0f, 1f)
            val ballRadius = (this.size.minDimension * 0.082f) * atom.radiusScale * depthScale

            // Smooth color transition across palette
            val cPos = (colorPhase + atom.colorOffset) % palette.size
            val cIdx1 = cPos.toInt() % palette.size
            val cIdx2 = (cIdx1 + 1) % palette.size
            val cFrac = cPos - cPos.toInt()
            val baseColor = lerp(palette[cIdx1], palette[cIdx2], cFrac)
            val darkEdge = lerp(baseColor, Color(0xFF090D16), 0.62f)
            val highlightSpot = Offset(pos.x - ballRadius * 0.32f, pos.y - ballRadius * 0.32f)

            // Subtle outer glow
            drawCircle(
                color = baseColor.copy(alpha = 0.20f),
                radius = ballRadius * 1.22f,
                center = pos
            )
            // 3D Shaded Sphere Body
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.White,
                        lerp(baseColor, Color.White, 0.35f),
                        baseColor,
                        darkEdge
                    ),
                    center = highlightSpot,
                    radius = ballRadius * 1.35f
                ),
                radius = ballRadius,
                center = pos
            )
            // Crisp glossy specular reflection dot
            drawCircle(
                color = Color.White.copy(alpha = 0.88f),
                radius = ballRadius * 0.22f,
                center = highlightSpot
            )
        }
    }
}

private data class BrainNode3D(
    val x: Float,
    val y: Float,
    val z: Float
)

/**
 * 3D Rotating Cyber-Circuit Brain Icon with Moving Synaptic Lights (for Qwen2.5-0.5B):
 * Preserves an unmistakable side-profile Brain silhouette (Frontal, Parietal dome, Occipital,
 * Cerebellum & vertical Brainstem tracks) while rotating briskly to the right in 3D with
 * bright synaptic light packets racing along crisp PCB circuit pathways.
 */
@Composable
fun NeuralCircuitBrainOrb(
    modifier: Modifier = Modifier,
    size: Dp = 40.dp,
    isExtracting: Boolean = false
) {
    val infiniteTransition = rememberInfiniteTransition(label = "NeuralCircuitBrainAnim")
    val rotAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2f * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = if (isExtracting) 1400 else 2100, easing = LinearEasing)
        ),
        label = "BrainRotAngle"
    )
    val lightPulsePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = if (isExtracting) 800 else 1250, easing = LinearEasing)
        ),
        label = "BrainLightPulse"
    )

    // Unmistakable Human Brain Side-Profile Outline (0..11) + Internal Circuit Hubs (12..16) + Brainstem (17..19)
    val baseProfile = remember {
        listOf(
            // Outer Brain Cortex Silhouette (0..11)
            Offset(-0.82f, 0.04f),  // 0: Frontal pole tip (left)
            Offset(-0.76f, -0.32f), // 1: Frontal curve
            Offset(-0.48f, -0.64f), // 2: Superior frontal gyrus
            Offset(-0.08f, -0.78f), // 3: Parietal top crown
            Offset(0.36f, -0.72f),  // 4: Parietal-occipital dome
            Offset(0.72f, -0.44f),  // 5: Occipital upper lobe
            Offset(0.84f, -0.06f),  // 6: Occipital back tip (right)
            Offset(0.70f, 0.24f),   // 7: Cerebellum upper notch
            Offset(0.46f, 0.44f),   // 8: Cerebellum lower bulge
            Offset(0.14f, 0.34f),   // 9: Brainstem junction
            Offset(-0.28f, 0.36f),  // 10: Temporal lobe bottom
            Offset(-0.64f, 0.24f),  // 11: Orbital frontal base
            // Clean Internal PCB Circuit Hubs (12..16)
            Offset(-0.42f, -0.22f), // 12: Frontal PCB hub
            Offset(-0.04f, -0.38f), // 13: Motor-parietal PCB hub
            Offset(0.38f, -0.26f),  // 14: Occipital PCB hub
            Offset(-0.18f, 0.04f),  // 15: Temporal PCB hub
            Offset(0.28f, 0.08f),   // 16: Deep cerebellar hub
            // Vertical Brainstem Data Bus Tracks (17..19)
            Offset(-0.04f, 0.86f),  // 17: Brainstem bus Left
            Offset(0.10f, 0.92f),   // 18: Brainstem bus Center
            Offset(0.24f, 0.86f)    // 19: Brainstem bus Right
        )
    }

    val cortexLoopIndices = remember {
        listOf(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 0)
    }

    val pcbTraces = remember {
        listOf(
            // Crisp PCB pathways from brainstem up through the cortex
            17 to 15, 18 to 9, 19 to 16,
            15 to 12, 12 to 1, 12 to 2,
            15 to 13, 13 to 3, 13 to 4,
            9 to 16, 16 to 14, 14 to 5, 14 to 6,
            15 to 10, 12 to 0, 16 to 8
        )
    }

    Canvas(modifier = modifier.size(size)) {
        val cx = this.size.width * 0.5f
        val cy = this.size.height * 0.48f
        val rScale = this.size.minDimension * 0.44f

        // Deep electric-blue neural aura behind the brain
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0xFF0284C7).copy(alpha = 0.36f),
                    Color(0xFF1E3A8A).copy(alpha = 0.14f),
                    Color.Transparent
                ),
                center = Offset(cx, cy),
                radius = rScale * 1.22f
            ),
            radius = rScale * 1.22f,
            center = Offset(cx, cy)
        )

        // Brisk rightward 3D rotation that preserves the recognizable brain profile at all angles
        // Exact 1x integer harmonics so loop start and loop end match with 0 flinch
        val yaw = -0.44f * sin(rotAngle)
        val pitch = 0.16f * cos(rotAngle)
        val cosY = cos(yaw)
        val sinY = sin(yaw)
        val cosX = cos(pitch)
        val sinX = sin(pitch)

        // Two 3D hemispheres shifting in depth as the brain rotates to the right
        val hemiZOffset = 0.26f * cos(rotAngle)
        val hemiXShift = 0.10f * sin(rotAngle)

        fun projectPoint(pt: Offset, zBase: Float, xShift: Float): Pair<Offset, Float> {
            val nx = pt.x + xShift
            val x1 = nx * cosY - zBase * sinY
            val z1 = nx * sinY + zBase * cosY
            val y2 = pt.y * cosX - z1 * sinX
            val z2 = pt.y * sinX + z1 * cosX
            val persp = 1f / (1f - z2 * 0.24f)
            return Pair(Offset(cx + x1 * rScale * persp, cy + y2 * rScale * persp), z2)
        }

        val backHemi = baseProfile.map { projectPoint(it, -hemiZOffset, -hemiXShift) }
        val frontHemi = baseProfile.map { projectPoint(it, hemiZOffset, hemiXShift) }

        // 1. Draw Back Hemisphere Brain Silhouette Outline (3D depth layer)
        val backOutlinePath = Path().apply {
            cortexLoopIndices.forEachIndexed { i, idx ->
                val p = backHemi[idx].first
                if (i == 0) moveTo(p.x, p.y) else lineTo(p.x, p.y)
            }
        }
        drawPath(
            path = backOutlinePath,
            color = Color(0xFF0369A1).copy(alpha = 0.55f),
            style = Stroke(width = (this.size.minDimension * 0.032f).coerceAtLeast(1.2f), cap = StrokeCap.Round)
        )

        // 2. Draw 3D Rotating Longitudinal Energy Ribbons sweeping to the right across the brain dome
        for (band in 0 until 3) {
            val bandPhase = (rotAngle / (2f * PI).toFloat() + band / 3f) % 1f
            // Sweeps from left (-0.65) to right (+0.65) across the brain cortex
            val bandX = -0.62f + bandPhase * 1.24f
            val bandAlpha = sin(bandPhase * PI.toFloat()).coerceIn(0f, 1f)
            val topY = -0.74f * sqrt((1f - (bandX * bandX) / 0.72f).coerceAtLeast(0.08f))
            val botY = 0.30f
            val pTop = projectPoint(Offset(bandX, topY), 0.18f * sin(bandPhase * PI.toFloat()), 0f).first
            val pMid = projectPoint(Offset(bandX + 0.10f, (topY + botY) * 0.5f), 0.26f, 0f).first
            val pBot = projectPoint(Offset(bandX, botY), 0.18f * sin(bandPhase * PI.toFloat()), 0f).first

            val ribbonPath = Path().apply {
                moveTo(pTop.x, pTop.y)
                quadraticTo(pMid.x, pMid.y, pBot.x, pBot.y)
            }
            drawPath(
                path = ribbonPath,
                color = Color(0xFF38BDF8).copy(alpha = 0.45f * bandAlpha),
                style = Stroke(width = (this.size.minDimension * 0.028f).coerceAtLeast(1.1f), cap = StrokeCap.Round)
            )
        }

        // 3. Draw Crisp Front Hemisphere Brain Cortex Silhouette Outline
        val frontOutlinePath = Path().apply {
            cortexLoopIndices.forEachIndexed { i, idx ->
                val p = frontHemi[idx].first
                if (i == 0) moveTo(p.x, p.y) else lineTo(p.x, p.y)
            }
        }
        drawPath(
            path = frontOutlinePath,
            color = Color(0xFF38BDF8).copy(alpha = 0.95f),
            style = Stroke(width = (this.size.minDimension * 0.044f).coerceAtLeast(1.7f), cap = StrokeCap.Round)
        )

        // 4. Draw Internal PCB Circuit Traces + Fast Moving Synaptic Lights
        val traceStroke = (this.size.minDimension * 0.030f).coerceAtLeast(1.2f)
        pcbTraces.forEachIndexed { idx, (a, b) ->
            val p1 = frontHemi[a].first
            val p2 = frontHemi[b].first
            val elbow = Offset(p1.x + (p2.x - p1.x) * 0.55f, p2.y)

            drawLine(
                color = Color(0xFF0EA5E9).copy(alpha = 0.82f),
                start = p1,
                end = elbow,
                strokeWidth = traceStroke,
                cap = StrokeCap.Round
            )
            drawLine(
                color = Color(0xFF0EA5E9).copy(alpha = 0.82f),
                start = elbow,
                end = p2,
                strokeWidth = traceStroke,
                cap = StrokeCap.Round
            )

            // Moving Synaptic Light Pulse along the PCB trace
            val t = (lightPulsePhase + (idx * 0.15f)) % 1f
            val lightPos = if (t < 0.5f) {
                val segT = t * 2f
                Offset(p1.x + (elbow.x - p1.x) * segT, p1.y + (elbow.y - p1.y) * segT)
            } else {
                val segT = (t - 0.5f) * 2f
                Offset(elbow.x + (p2.x - elbow.x) * segT, elbow.y + (p2.y - elbow.y) * segT)
            }
            val sparkR = (this.size.minDimension * 0.036f).coerceAtLeast(1.4f)
            val glowColor = if (idx % 4 == 0) Color(0xFFFDE047) else Color(0xFF00FFFF)

            drawCircle(
                color = glowColor.copy(alpha = 0.55f),
                radius = sparkR * 1.7f,
                center = lightPos
            )
            drawCircle(
                color = Color.White,
                radius = sparkR * 0.68f,
                center = lightPos
            )
        }

        // 5. Crisp, compact PCB Solder-Pad Nodes (small enough that they never blur the brain silhouette)
        val nodeR = (this.size.minDimension * 0.024f).coerceAtLeast(1.0f)
        frontHemi.forEachIndexed { idx, (pos, _) ->
            val pulse = 0.5f + 0.5f * sin((lightPulsePhase * 2f * PI + idx * 0.6f).toFloat())
            drawCircle(
                color = Color(0xFF38BDF8).copy(alpha = 0.45f + 0.4f * pulse),
                radius = nodeR * 1.45f,
                center = pos
            )
            drawCircle(
                color = Color.White.copy(alpha = 0.85f),
                radius = nodeR * 0.72f,
                center = pos
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
            color = if (isDark) Color(0xFFF8FAFC) else Color(0xFF0F172A),
            modifier = Modifier.autoSlideIfOverflow()
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
