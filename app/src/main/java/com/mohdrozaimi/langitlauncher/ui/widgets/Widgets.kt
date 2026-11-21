package com.mohdrozaimi.langitlauncher.ui.widgets

import android.text.format.DateFormat
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mohdrozaimi.langitlauncher.domain.WeatherParser
import com.mohdrozaimi.langitlauncher.domain.WeatherState
import com.mohdrozaimi.langitlauncher.domain.formatClock
import com.mohdrozaimi.langitlauncher.ui.components.GlassSurface
import com.mohdrozaimi.langitlauncher.ui.components.LabelStyle
import kotlinx.coroutines.delay
import java.time.LocalTime
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

private val WidgetShape = RoundedCornerShape(22.dp)

@Composable
private fun WidgetLabel(text: String) {
    Spacer(Modifier.height(5.dp))
    Text(text, style = LabelStyle)
}

@Composable
fun ClockWidget(size: Dp, dark: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val context = LocalContext.current
    val now = produceState(LocalTime.now()) {
        while (true) {
            value = LocalTime.now()
            delay(60_000L - System.currentTimeMillis() % 60_000L + 50L)
        }
    }.value
    val is24h = DateFormat.is24HourFormat(context)
    val face = if (dark) Color(0xFF1C1C1E) else Color(0xFFF8F8F8)
    val ink = if (dark) Color.White else Color(0xFF111111)
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            Modifier
                .size(size)
                .shadow(10.dp, WidgetShape)
                .clip(WidgetShape)
                .background(face)
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center,
        ) {
            Canvas(Modifier.fillMaxSize().padding(7.dp)) {
                val cx = this.size.width / 2f
                val cy = this.size.height / 2f
                val half = min(cx, cy)
                for (i in 0 until 60) {
                    val a = i / 60.0 * 2 * PI - PI / 2
                    val dx = cos(a).toFloat()
                    val dy = sin(a).toFloat()
                    val edge = min(half / max(abs(dx), abs(dy)), half * 1.2f)
                    val major = i % 5 == 0
                    val len = if (major) half * 0.13f else half * 0.07f
                    drawLine(
                        color = ink.copy(alpha = if (major) 0.85f else 0.4f),
                        start = Offset(cx + dx * edge, cy + dy * edge),
                        end = Offset(cx + dx * (edge - len), cy + dy * (edge - len)),
                        strokeWidth = if (major) 2.dp.toPx() else 1.dp.toPx(),
                    )
                }
            }
            Text(
                formatClock(now, is24h),
                color = ink,
                fontSize = (size.value * 0.25f).sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-1).sp,
            )
        }
        WidgetLabel("Jam")
    }
}

@Composable
fun WeatherWidget(size: Dp, state: WeatherState, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        GlassSurface(
            modifier = Modifier.size(size).clickable(onClick = onClick),
            shape = WidgetShape,
            tint = Color(0xFF9AAFC2).copy(alpha = 0.45f),
        ) {
            Column(Modifier.fillMaxSize().padding(14.dp)) {
                val city = (state as? WeatherState.Ready)?.city?.takeIf { it.isNotBlank() } ?: "Cuaca"
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        city,
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        modifier = Modifier.weight(1f, fill = false),
                    )
                    Spacer(Modifier.width(3.dp))
                    Icon(Icons.Filled.LocationOn, null, tint = Color.White, modifier = Modifier.size(12.dp))
                }
                Text(
                    WeatherParser.tempText(state),
                    color = Color.White,
                    fontSize = (size.value * 0.24f).sp,
                    fontWeight = FontWeight.Light,
                )
                Spacer(Modifier.weight(1f))
                when (state) {
                    is WeatherState.Ready -> {
                        Text(
                            WeatherParser.conditionSymbol(state.info.code),
                            fontSize = 14.sp,
                        )
                        Text(
                            WeatherParser.conditionLabel(state.info.code),
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(
                            "T:${state.info.hiC}° R:${state.info.loC}°",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                    WeatherState.Loading -> Text("Memuatkan…", color = Color.White, fontSize = 13.sp)
                    WeatherState.Unavailable -> Text(
                        "Ketik untuk\naktifkan cuaca",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        }
        WidgetLabel("Cuaca")
    }
}
