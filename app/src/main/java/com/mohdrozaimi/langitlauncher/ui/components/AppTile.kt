package com.mohdrozaimi.langitlauncher.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mohdrozaimi.langitlauncher.data.IconLoader
import com.mohdrozaimi.langitlauncher.domain.formatBadge
import com.mohdrozaimi.langitlauncher.model.AppEntry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.abs

val LabelStyle = TextStyle(
    color = Color.White,
    fontSize = 12.sp,
    fontWeight = FontWeight.Medium,
    textAlign = TextAlign.Center,
    shadow = Shadow(Color.Black.copy(alpha = 0.35f), Offset(0f, 1f), 4f),
)

@Composable
fun AppIconImage(
    pkg: String,
    loader: IconLoader,
    size: Dp,
    colorFilter: ColorFilter?,
    modifier: Modifier = Modifier,
) {
    var bitmap by remember(pkg) { mutableStateOf<ImageBitmap?>(loader.peek(pkg)) }
    LaunchedEffect(pkg) {
        if (bitmap == null) bitmap = withContext(Dispatchers.IO) { loader.load(pkg) }
    }
    val current = bitmap
    if (current != null) {
        Image(
            bitmap = current,
            contentDescription = null,
            colorFilter = colorFilter,
            modifier = modifier.size(size).clip(SquircleShape),
        )
    } else {
        Box(modifier.size(size).clip(SquircleShape).background(Color.White.copy(alpha = 0.3f)))
    }
}

@Composable
fun jiggleRotation(seed: Int): Float {
    val transition = rememberInfiniteTransition(label = "jiggle")
    val dir = if (seed % 2 == 0) 1f else -1f
    val r by transition.animateFloat(
        initialValue = -1.8f * dir,
        targetValue = 1.8f * dir,
        animationSpec = infiniteRepeatable(tween(120 + abs(seed) % 40, easing = LinearEasing), RepeatMode.Reverse),
        label = "rot",
    )
    return r
}

@Composable
fun BadgeDot(text: String, modifier: Modifier = Modifier) {
    Box(
        modifier
            .heightIn(min = 22.dp)
            .widthIn(min = 22.dp)
            .background(Color(0xFFFF3B30), RoundedCornerShape(50))
            .padding(horizontal = 6.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(text, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Medium, maxLines = 1)
    }
}

@Composable
fun MinusButton(modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier
            .size(22.dp)
            .clip(CircleShape)
            .background(Color(0xF0E5E5EA))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Box(Modifier.size(width = 10.dp, height = 2.dp).background(Color.Black.copy(alpha = 0.8f)))
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AppTile(
    app: AppEntry,
    loader: IconLoader,
    iconSize: Dp,
    showLabel: Boolean,
    badge: Int,
    editMode: Boolean,
    colorFilter: ColorFilter?,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier,
    iconModifier: Modifier = Modifier,
    invisible: Boolean = false,
    onRemove: (() -> Unit)? = null,
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val rotation = if (editMode) jiggleRotation(app.packageName.hashCode()) else 0f
    Column(modifier.alpha(if (invisible) 0f else 1f), horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            iconModifier
                .size(iconSize)
                .graphicsLayer { rotationZ = rotation }
                .combinedClickable(
                    interactionSource = interaction,
                    indication = null,
                    onClick = onClick,
                    onLongClick = onLongClick,
                ),
        ) {
            AppIconImage(app.packageName, loader, iconSize, colorFilter, Modifier.alpha(if (pressed) 0.6f else 1f))
            if (!editMode) {
                formatBadge(badge)?.let {
                    BadgeDot(it, Modifier.align(Alignment.TopEnd).offset(x = 8.dp, y = (-6).dp))
                }
            }
            if (editMode && onRemove != null) {
                MinusButton(Modifier.align(Alignment.TopStart).offset(x = (-7).dp, y = (-7).dp), onRemove)
            }
        }
        if (showLabel) {
            Spacer(Modifier.height(5.dp))
            Text(
                app.label,
                style = LabelStyle,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.widthIn(max = iconSize + 22.dp),
            )
        }
    }
}
