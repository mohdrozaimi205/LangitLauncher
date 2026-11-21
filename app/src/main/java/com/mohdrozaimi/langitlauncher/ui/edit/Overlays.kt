package com.mohdrozaimi.langitlauncher.ui.edit

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mohdrozaimi.langitlauncher.data.IconLoader
import com.mohdrozaimi.langitlauncher.domain.IconStyle
import com.mohdrozaimi.langitlauncher.model.AppEntry
import com.mohdrozaimi.langitlauncher.ui.components.AppIconImage
import com.mohdrozaimi.langitlauncher.ui.components.GlassButton
import com.mohdrozaimi.langitlauncher.ui.components.GlassSurface

/** Lapisan gelap yang tutup bila diketik di luar. */
@Composable
fun Scrim(onDismiss: () -> Unit, content: @Composable BoxScope.() -> Unit) {
    Box(
        Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.35f))
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onDismiss),
    ) { content() }
}

class MenuAction(val label: String, val icon: ImageVector, val destructive: Boolean = false, val run: () -> Unit)

@Composable
fun AppContextMenu(
    app: AppEntry,
    loader: IconLoader,
    colorFilter: ColorFilter?,
    actions: List<MenuAction>,
    onDismiss: () -> Unit,
) {
    Scrim(onDismiss) {
        Column(
            Modifier.align(Alignment.Center).width(260.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            AppIconImage(app.packageName, loader, 72.dp, colorFilter)
            Spacer(Modifier.height(8.dp))
            Text(app.label, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(14.dp))
            GlassSurface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                tint = Color(0xFFF2F2F7).copy(alpha = 0.82f),
                borderAlpha = 0f,
            ) {
                Column {
                    actions.forEachIndexed { i, action ->
                        if (i > 0) HorizontalDivider(color = Color.Black.copy(alpha = 0.1f), thickness = 0.5.dp)
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onDismiss()
                                    action.run()
                                }
                                .padding(horizontal = 16.dp, vertical = 13.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            val color = if (action.destructive) Color(0xFFFF3B30) else Color.Black
                            Text(action.label, color = color, fontSize = 16.sp, modifier = Modifier.weight(1f))
                            Icon(action.icon, null, tint = color, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        }
    }
}

object MenuIcons {
    val edit = Icons.Filled.Edit
    val info = Icons.Filled.Info
    val remove = Icons.Filled.Clear
    val delete = Icons.Filled.Delete
    val add = Icons.Filled.Add
}

val TintChoices = listOf(0xFFFF9F0AL, 0xFF30D158L, 0xFF0A84FFL, 0xFFBF5AF2L, 0xFFFF375FL, 0xFFFFD60AL)

@Composable
fun CustomizeSheet(
    style: IconStyle,
    tint: Long,
    large: Boolean,
    onStyle: (IconStyle) -> Unit,
    onTint: (Long) -> Unit,
    onLarge: (Boolean) -> Unit,
    onPickWallpaper: () -> Unit,
    onResetWallpaper: () -> Unit,
    onDismiss: () -> Unit,
) {
    Scrim(onDismiss) {
        GlassSurface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {},
            shape = RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp),
            tint = Color.Black.copy(alpha = 0.35f),
        ) {
            Column(
                Modifier.navigationBarsPadding().padding(22.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Text("Sesuaikan", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
                SectionLabel("Saiz ikon")
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    GlassButton("Kecil", selected = !large) { onLarge(false) }
                    GlassButton("Besar", selected = large) { onLarge(true) }
                }
                SectionLabel("Gaya")
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    GlassButton("Lalai", selected = style == IconStyle.DEFAULT) { onStyle(IconStyle.DEFAULT) }
                    GlassButton("Gelap", selected = style == IconStyle.DARK) { onStyle(IconStyle.DARK) }
                    GlassButton("Warna", selected = style == IconStyle.TINTED) { onStyle(IconStyle.TINTED) }
                }
                if (style == IconStyle.TINTED) {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        TintChoices.forEach { c ->
                            Box(
                                Modifier
                                    .size(32.dp)
                                    .background(Color(c), CircleShape)
                                    .border(if (c == tint) 3.dp else 0.dp, Color.White, CircleShape)
                                    .clickable { onTint(c) },
                            )
                        }
                    }
                }
                SectionLabel("Wallpaper")
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    GlassButton("Pilih Gambar", onClick = onPickWallpaper)
                    GlassButton("Gelombang Asal", onClick = onResetWallpaper)
                }
                Spacer(Modifier.height(4.dp))
            }
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(text, color = Color.White.copy(alpha = 0.7f), fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
}

@Composable
fun BadgePromptBanner(modifier: Modifier = Modifier, onEnable: () -> Unit, onDismiss: () -> Unit) {
    GlassSurface(modifier = modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp), tint = Color.Black.copy(alpha = 0.25f)) {
        Row(Modifier.padding(start = 16.dp, end = 8.dp, top = 8.dp, bottom = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(
                "Hidupkan badge notifikasi pada ikon",
                color = Color.White,
                fontSize = 14.sp,
                modifier = Modifier.weight(1f),
            )
            GlassButton("Hidupkan", selected = true, onClick = onEnable)
            Icon(
                Icons.Filled.Clear,
                contentDescription = "Tutup",
                tint = Color.White,
                modifier = Modifier.padding(start = 6.dp).size(22.dp).clickable(onClick = onDismiss),
            )
        }
    }
}
