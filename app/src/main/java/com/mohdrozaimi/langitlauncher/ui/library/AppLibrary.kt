package com.mohdrozaimi.langitlauncher.ui.library

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mohdrozaimi.langitlauncher.data.IconLoader
import com.mohdrozaimi.langitlauncher.model.AppEntry
import com.mohdrozaimi.langitlauncher.ui.components.AppIconImage
import com.mohdrozaimi.langitlauncher.ui.components.AppTile
import com.mohdrozaimi.langitlauncher.ui.components.GlassSurface
import com.mohdrozaimi.langitlauncher.ui.components.LabelStyle
import com.mohdrozaimi.langitlauncher.ui.components.SearchPill

@Composable
fun AppLibraryPage(
    groups: List<Pair<String, List<AppEntry>>>,
    loader: IconLoader,
    colorFilter: ColorFilter?,
    onLaunch: (AppEntry) -> Unit,
    onLongPress: (AppEntry) -> Unit,
    onOpenSearch: () -> Unit,
    onOpenGroup: (String) -> Unit,
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 22.dp, end = 22.dp, top = 8.dp, bottom = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item(span = { GridItemSpan(2) }) {
            SearchPill(Modifier.fillMaxWidth(), text = "Pustaka Apl", onClick = onOpenSearch)
        }
        items(groups, key = { it.first }) { (name, apps) ->
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                GlassSurface(
                    modifier = Modifier.fillMaxWidth().aspectRatio(1f),
                    shape = RoundedCornerShape(26.dp),
                    tint = Color.White.copy(alpha = 0.16f),
                ) {
                    BoxWithConstraints(Modifier.fillMaxSize().padding(13.dp)) {
                        val gap = 12.dp
                        val cell = (maxWidth - gap) / 2
                        Column(verticalArrangement = Arrangement.spacedBy(gap)) {
                            for (row in 0..1) {
                                Row(horizontalArrangement = Arrangement.spacedBy(gap)) {
                                    for (col in 0..1) {
                                        LibraryCell(row * 2 + col, apps, cell, loader, colorFilter, onLaunch, onLongPress) {
                                            onOpenGroup(name)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                Spacer(Modifier.height(6.dp))
                Text(name, style = LabelStyle, maxLines = 1)
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun LibraryCell(
    position: Int,
    apps: List<AppEntry>,
    cell: Dp,
    loader: IconLoader,
    colorFilter: ColorFilter?,
    onLaunch: (AppEntry) -> Unit,
    onLongPress: (AppEntry) -> Unit,
    onMore: () -> Unit,
) {
    val showMini = position == 3 && apps.size > 4
    when {
        showMini -> {
            val mini = (cell - 4.dp) / 2
            Column(
                Modifier.size(cell).clickable(onClick = onMore),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                for (r in 0..1) {
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        for (c in 0..1) {
                            val app = apps.getOrNull(3 + r * 2 + c)
                            if (app != null) AppIconImage(app.packageName, loader, mini, colorFilter) else Spacer(Modifier.size(mini))
                        }
                    }
                }
            }
        }
        position < apps.size -> {
            val app = apps[position]
            Box(
                Modifier.size(cell).combinedClickable(onClick = { onLaunch(app) }, onLongClick = { onLongPress(app) }),
            ) {
                AppIconImage(app.packageName, loader, cell, colorFilter)
            }
        }
        else -> Spacer(Modifier.size(cell))
    }
}

@Composable
fun GroupOverlay(
    name: String,
    apps: List<AppEntry>,
    loader: IconLoader,
    colorFilter: ColorFilter?,
    onLaunch: (AppEntry) -> Unit,
    onLongPress: (AppEntry) -> Unit,
    onClose: () -> Unit,
) {
    GlassSurface(Modifier.fillMaxSize().clickable(onClick = onClose), shape = RectangleShape, tint = Color.Black.copy(alpha = 0.4f), borderAlpha = 0f) {
        Column(
            Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().padding(20.dp),
        ) {
            Text(name, color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(20.dp))
            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                verticalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                items(apps, key = { it.packageName }) { app ->
                    AppTile(
                        app = app,
                        loader = loader,
                        iconSize = 58.dp,
                        showLabel = true,
                        badge = 0,
                        editMode = false,
                        colorFilter = colorFilter,
                        onClick = { onLaunch(app) },
                        onLongClick = { onLongPress(app) },
                    )
                }
            }
        }
    }
}
