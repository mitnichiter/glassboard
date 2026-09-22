package dev.mitnic.lumaglass.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.consume
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import dev.chrisbanes.haze.ExperimentalHazeApi
import dev.chrisbanes.haze.HazeInput
import dev.chrisbanes.haze.glass.GlassStyle
import dev.chrisbanes.haze.glass.hazeGlass
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalHazeApi::class)
@Composable
fun LiquidGlassStage(modifier: Modifier = Modifier, content: @Composable BoxScope.() -> Unit) {
    val palette = LocalGlassPalette.current
    val hazeState = rememberHazeState()
    Box(modifier) {
        Canvas(Modifier.fillMaxSize().hazeSource(hazeState)) {
            drawRect(Brush.linearGradient(listOf(palette.deep, palette.mid, palette.deep)))
            drawCircle(palette.glowA.copy(alpha = .42f), size.minDimension * .52f, Offset(size.width * .16f, 0f))
            drawCircle(palette.glowB.copy(alpha = .32f), size.minDimension * .60f, Offset(size.width * .84f, size.height))
        }
        Box(
            Modifier.fillMaxSize().hazeGlass(
                input = HazeInput.Sources(hazeState),
                style = GlassStyle.regular.then {
                    backgroundColor(palette.deep)
                    tint(Color.White.copy(alpha = .05f))
                    shape(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                },
            ),
        )
        content()
    }
}

@Composable
fun LiquidKeySurface(
    repeat: Boolean,
    emphasized: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    val palette = LocalGlassPalette.current
    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (pressed) .93f else 1f,
        animationSpec = spring(dampingRatio = .58f, stiffness = 560f),
        label = "liquid-key-press",
    )
    val shapeRadius = 13.dp
    Box(
        modifier
            .scale(scale)
            .drawWithCache {
                val radius = shapeRadius.toPx()
                val fill = Brush.linearGradient(
                    colors = if (emphasized) {
                        listOf(palette.keyStrong, palette.keyTint, palette.keyStrong.copy(alpha = .55f))
                    } else {
                        listOf(Color.White.copy(alpha = .20f), palette.keyTint, Color.Black.copy(alpha = .08f))
                    },
                    start = Offset.Zero,
                    end = Offset(size.width, size.height),
                )
                val rim = Brush.linearGradient(
                    listOf(Color.White.copy(alpha = .62f), Color.White.copy(alpha = .10f), palette.glowA.copy(alpha = .32f))
                )
                onDrawBehind {
                    drawRoundRect(Color.Black.copy(alpha = .22f), topLeft = Offset(0f, 2.dp.toPx()), size = Size(size.width, size.height), cornerRadius = androidx.compose.ui.geometry.CornerRadius(radius))
                    drawRoundRect(fill, cornerRadius = androidx.compose.ui.geometry.CornerRadius(radius))
                    drawRoundRect(rim, cornerRadius = androidx.compose.ui.geometry.CornerRadius(radius), style = Stroke(1.dp.toPx()))
                    drawRoundRect(
                        Brush.verticalGradient(listOf(Color.White.copy(alpha = .22f), Color.Transparent)),
                        size = Size(size.width, size.height * .42f),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(radius),
                    )
                }
            }
            .pointerInput(repeat, onClick) {
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    down.consume()
                    pressed = true
                    val job = if (repeat) launch {
                        delay(430)
                        while (true) {
                            onClick()
                            delay(55)
                        }
                    } else null
                    val up = waitForUpOrCancellation()
                    job?.cancel()
                    pressed = false
                    if (up != null) onClick()
                }
            },
        content = content,
    )
}
