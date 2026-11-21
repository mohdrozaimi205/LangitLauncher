package com.mohdrozaimi.langitlauncher.ui.components

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.layout
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

/** Wallpaper dan saiz skrin, supaya setiap permukaan kaca boleh kaburkan apa yang ada di belakangnya. */
class GlassContext(val rootSize: IntSize, val wallpaper: @Composable () -> Unit)

val LocalGlass = staticCompositionLocalOf<GlassContext?> { null }

@Composable
fun GlassSurface(
    modifier: Modifier = Modifier,
    shape: Shape,
    tint: Color = Color.White.copy(alpha = 0.18f),
    blur: Dp = 30.dp,
    borderAlpha: Float = 0.30f,
    content: @Composable BoxScope.() -> Unit,
) {
    val glass = LocalGlass.current
    val origin = remember { mutableStateOf(Offset.Zero) }
    Box(
        modifier
            .onGloballyPositioned { origin.value = it.positionInRoot() }
            .clip(shape),
    ) {
        if (glass != null && Build.VERSION.SDK_INT >= 31 && glass.rootSize.width > 0) {
            Box(
                Modifier
                    .matchParentSize()
                    .layout { measurable, constraints ->
                        val placeable = measurable.measure(
                            Constraints.fixed(glass.rootSize.width, glass.rootSize.height),
                        )
                        layout(constraints.maxWidth, constraints.maxHeight) {
                            placeable.place(-origin.value.x.roundToInt(), -origin.value.y.roundToInt())
                        }
                    }
                    .blur(blur),
            ) { glass.wallpaper() }
            Box(Modifier.matchParentSize().background(tint))
        } else {
            Box(Modifier.matchParentSize().background(tint.copy(alpha = (tint.alpha + 0.2f).coerceAtMost(1f))))
        }
        if (borderAlpha > 0f) {
            Box(Modifier.matchParentSize().border(0.6.dp, Color.White.copy(alpha = borderAlpha), shape))
        }
        content()
    }
}
