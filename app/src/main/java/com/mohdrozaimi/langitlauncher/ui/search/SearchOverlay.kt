package com.mohdrozaimi.langitlauncher.ui.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mohdrozaimi.langitlauncher.data.IconLoader
import com.mohdrozaimi.langitlauncher.domain.SearchFilter
import com.mohdrozaimi.langitlauncher.model.AppEntry
import com.mohdrozaimi.langitlauncher.ui.components.AppTile
import com.mohdrozaimi.langitlauncher.ui.components.GlassSurface

@Composable
fun SearchOverlay(
    apps: List<AppEntry>,
    suggestions: List<AppEntry>,
    loader: IconLoader,
    colorFilter: ColorFilter?,
    onLaunch: (AppEntry) -> Unit,
    onWebSearch: (String) -> Unit,
    onClose: () -> Unit,
) {
    var query by remember { mutableStateOf("") }
    val focus = remember { FocusRequester() }
    LaunchedEffect(Unit) { focus.requestFocus() }
    val results = remember(query, apps) { SearchFilter.filter(apps, query) }
    val shown = if (query.isBlank()) suggestions else results

    GlassSurface(Modifier.fillMaxSize(), shape = RectangleShape, tint = Color.Black.copy(alpha = 0.38f), borderAlpha = 0f) {
        Column(
            Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
        ) {
            Text(
                if (query.isBlank()) "Cadangan" else "Apl",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(start = 4.dp, top = 8.dp, bottom = 10.dp),
            )
            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                modifier = Modifier.weight(1f).fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(18.dp),
            ) {
                items(shown, key = { it.packageName }) { app ->
                    AppTile(
                        app = app,
                        loader = loader,
                        iconSize = 56.dp,
                        showLabel = true,
                        badge = 0,
                        editMode = false,
                        colorFilter = colorFilter,
                        onClick = { onLaunch(app) },
                        onLongClick = {},
                    )
                }
            }
            if (query.isNotBlank()) {
                GlassSurface(
                    modifier = Modifier.fillMaxWidth().height(48.dp).clickable { onWebSearch(query) },
                    shape = RoundedCornerShape(14.dp),
                    tint = Color.White.copy(alpha = 0.16f),
                ) {
                    Text(
                        "Cari “$query” di web",
                        color = Color.White,
                        fontSize = 15.sp,
                        maxLines = 1,
                        modifier = Modifier.align(Alignment.CenterStart).padding(horizontal = 16.dp),
                    )
                }
                Spacer(Modifier.height(10.dp))
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                GlassSurface(
                    modifier = Modifier.weight(1f).height(44.dp),
                    shape = RoundedCornerShape(14.dp),
                    tint = Color.White.copy(alpha = 0.2f),
                ) {
                    Row(
                        Modifier.fillMaxSize().padding(horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(Icons.Filled.Search, null, tint = Color.White.copy(alpha = 0.8f), modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Box(Modifier.weight(1f)) {
                            if (query.isEmpty()) {
                                Text("Cari", color = Color.White.copy(alpha = 0.6f), fontSize = 17.sp)
                            }
                            BasicTextField(
                                value = query,
                                onValueChange = { query = it },
                                singleLine = true,
                                textStyle = TextStyle(color = Color.White, fontSize = 17.sp),
                                cursorBrush = SolidColor(Color.White),
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                                keyboardActions = KeyboardActions(
                                    onSearch = {
                                        val first = results.firstOrNull()
                                        if (first != null) onLaunch(first) else if (query.isNotBlank()) onWebSearch(query)
                                    },
                                ),
                                modifier = Modifier.fillMaxWidth().focusRequester(focus),
                            )
                        }
                    }
                }
                Text(
                    "Batal",
                    color = Color.White,
                    fontSize = 17.sp,
                    modifier = Modifier.clickable(onClick = onClose).padding(start = 12.dp, top = 8.dp, bottom = 8.dp),
                )
            }
        }
    }
}
