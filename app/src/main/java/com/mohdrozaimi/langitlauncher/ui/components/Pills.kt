package com.mohdrozaimi.langitlauncher.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SearchPill(modifier: Modifier = Modifier, text: String = "Cari", onClick: () -> Unit) {
    GlassSurface(
        modifier = modifier.height(30.dp).clickable(onClick = onClick),
        shape = RoundedCornerShape(50),
        tint = Color.White.copy(alpha = 0.22f),
    ) {
        Row(
            Modifier.align(Alignment.Center).padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Filled.Search, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
            Spacer(Modifier.width(5.dp))
            Text(text, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
fun PageDots(count: Int, current: Int, modifier: Modifier = Modifier) {
    GlassSurface(
        modifier = modifier.height(30.dp),
        shape = RoundedCornerShape(50),
        tint = Color.White.copy(alpha = 0.22f),
    ) {
        Row(
            Modifier.align(Alignment.Center).padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            repeat(count) { i ->
                Box(
                    Modifier
                        .size(7.dp)
                        .background(if (i == current) Color.White else Color.White.copy(alpha = 0.4f), CircleShape),
                )
            }
        }
    }
}

@Composable
fun GlassButton(
    text: String,
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    onClick: () -> Unit,
) {
    GlassSurface(
        modifier = modifier.height(34.dp).clickable(onClick = onClick),
        shape = RoundedCornerShape(50),
        tint = if (selected) Color.White.copy(alpha = 0.92f) else Color.White.copy(alpha = 0.22f),
    ) {
        Text(
            text,
            color = if (selected) Color.Black else Color.White,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.align(Alignment.Center).padding(horizontal = 16.dp),
        )
    }
}
