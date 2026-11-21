package com.mohdrozaimi.langitlauncher.ui.home

import android.Manifest
import android.content.Intent
import android.provider.AlarmClock
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mohdrozaimi.langitlauncher.domain.IconStyle
import com.mohdrozaimi.langitlauncher.domain.LibraryGrouper
import com.mohdrozaimi.langitlauncher.domain.Slot
import com.mohdrozaimi.langitlauncher.domain.iconColorMatrix
import com.mohdrozaimi.langitlauncher.model.AppEntry
import com.mohdrozaimi.langitlauncher.ui.HomeViewModel
import com.mohdrozaimi.langitlauncher.ui.components.AppIconImage
import com.mohdrozaimi.langitlauncher.ui.components.AppTile
import com.mohdrozaimi.langitlauncher.ui.components.GlassButton
import com.mohdrozaimi.langitlauncher.ui.components.GlassContext
import com.mohdrozaimi.langitlauncher.ui.components.GlassSurface
import com.mohdrozaimi.langitlauncher.ui.components.LocalGlass
import com.mohdrozaimi.langitlauncher.ui.components.PageDots
import com.mohdrozaimi.langitlauncher.ui.components.SearchPill
import com.mohdrozaimi.langitlauncher.ui.edit.AppContextMenu
import com.mohdrozaimi.langitlauncher.ui.edit.BadgePromptBanner
import com.mohdrozaimi.langitlauncher.ui.edit.CustomizeSheet
import com.mohdrozaimi.langitlauncher.ui.edit.MenuAction
import com.mohdrozaimi.langitlauncher.ui.edit.MenuIcons
import com.mohdrozaimi.langitlauncher.ui.library.AppLibraryPage
import com.mohdrozaimi.langitlauncher.ui.library.GroupOverlay
import com.mohdrozaimi.langitlauncher.ui.search.SearchOverlay
import com.mohdrozaimi.langitlauncher.ui.wallpaper.Wallpaper
import com.mohdrozaimi.langitlauncher.ui.wallpaper.decodeWallpaper
import com.mohdrozaimi.langitlauncher.ui.widgets.ClockWidget
import com.mohdrozaimi.langitlauncher.ui.widgets.WeatherWidget
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlin.math.max
import kotlin.math.roundToInt

private const val COLUMNS = 4
private const val ROWS = 6
private const val WIDGET_SLOTS = 8

@Stable
private class DragState {
    var pkg by mutableStateOf<String?>(null)
    var from by mutableStateOf<Slot?>(null)
    var pos by mutableStateOf(Offset.Zero)

    fun start(p: String, slot: Slot, at: Offset) {
        pkg = p
        from = slot
        pos = at
    }

    fun clear() {
        pkg = null
        from = null
    }
}

private class MenuTarget(val app: AppEntry, val slot: Slot?, val fromLibrary: Boolean)

@Composable
fun HomeScreen(vm: HomeViewModel) {
    val state by vm.state.collectAsStateWithLifecycle()
    val settings by vm.settings.collectAsStateWithLifecycle()
    val homeTick by vm.homeTick.collectAsStateWithLifecycle()
    val badgeAccess by vm.badgeAccess.collectAsStateWithLifecycle()
    val density = LocalDensity.current

    var editMode by remember { mutableStateOf(false) }
    var searchOpen by remember { mutableStateOf(false) }
    var customizeOpen by remember { mutableStateOf(false) }
    var openGroup by remember { mutableStateOf<String?>(null) }
    var menu by remember { mutableStateOf<MenuTarget?>(null) }
    var rootSize by remember { mutableStateOf(IntSize.Zero) }
    val gridRect = remember { mutableStateOf(Rect.Zero) }
    val dockRect = remember { mutableStateOf(Rect.Zero) }
    val drag = remember { DragState() }

    val iconSize = if (settings.largeIcons) 68.dp else 60.dp
    val showLabels = !settings.largeIcons
    val colorFilter = remember(settings.iconStyle, settings.tint) {
        if (settings.iconStyle == IconStyle.DEFAULT) null
        else ColorFilter.colorMatrix(ColorMatrix(iconColorMatrix(settings.iconStyle, settings.tint)))
    }
    val dim = when (settings.iconStyle) {
        IconStyle.DEFAULT -> 0f
        IconStyle.DARK -> 0.45f
        IconStyle.TINTED -> 0.3f
    }

    val wallpaperImage = produceState<ImageBitmap?>(null, settings.wallpaperPath, rootSize) {
        val path = settings.wallpaperPath
        value = if (path == null || rootSize.width == 0) null
        else withContext(Dispatchers.IO) { decodeWallpaper(path, rootSize.width, rootSize.height) }
    }.value

    val homeCount = max(state.pages.size, 1) + if (editMode) 1 else 0
    val pager = rememberPagerState { homeCount + 1 }
    val libraryIndex = homeCount
    val groups = remember(state.allApps) { LibraryGrouper.group(state.allApps) }

    val locationLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        vm.onLocationResult()
    }
    val wallpaperLauncher = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) vm.setWallpaper(uri)
    }

    LaunchedEffect(settings.locationAsked) {
        delay(800)
        if (!settings.locationAsked && !vm.hasLocationPermission()) {
            locationLauncher.launch(Manifest.permission.ACCESS_COARSE_LOCATION)
        }
    }

    LaunchedEffect(homeTick) {
        if (homeTick > 0) {
            val hadOverlay = searchOpen || customizeOpen || openGroup != null || menu != null
            searchOpen = false
            customizeOpen = false
            openGroup = null
            menu = null
            when {
                editMode -> editMode = false
                !hadOverlay -> pager.animateScrollToPage(0)
            }
        }
    }

    BackHandler(enabled = searchOpen || customizeOpen || openGroup != null || menu != null || editMode) {
        when {
            menu != null -> menu = null
            customizeOpen -> customizeOpen = false
            searchOpen -> searchOpen = false
            openGroup != null -> openGroup = null
            editMode -> editMode = false
        }
    }

    // Seret ke tepi skrin semasa mod edit -> pindah halaman.
    val currentHomeCount by rememberUpdatedState(homeCount)
    LaunchedEffect(drag.pkg) {
        if (drag.pkg == null) return@LaunchedEffect
        val edge = with(density) { 28.dp.toPx() }
        while (true) {
            delay(550)
            val x = drag.pos.x
            val page = pager.currentPage
            if (x < edge && page > 0) pager.animateScrollToPage(page - 1)
            else if (x > rootSize.width - edge && page < currentHomeCount - 1) pager.animateScrollToPage(page + 1)
        }
    }

    val dockCount by rememberUpdatedState(state.dock.size)
    fun drop() {
        val from = drag.from
        val p = drag.pos
        if (from != null) {
            val grid = gridRect.value
            val dock = dockRect.value
            val target: Slot? = when {
                dock.contains(p) -> {
                    val count = max(dockCount + if (from.page == Slot.DOCK) 0 else 1, 1)
                    Slot(Slot.DOCK, ((p.x - dock.left) / (dock.width / count)).toInt())
                }
                grid.contains(p) && pager.currentPage < currentHomeCount -> {
                    val col = ((p.x - grid.left) / (grid.width / COLUMNS)).toInt().coerceIn(0, COLUMNS - 1)
                    val row = ((p.y - grid.top) / (grid.height / ROWS)).toInt().coerceIn(0, ROWS - 1)
                    val page = pager.currentPage
                    val index = row * COLUMNS + col - if (page == 0) WIDGET_SLOTS else 0
                    if (index < 0) null else Slot(page, index)
                }
                else -> null
            }
            if (target != null && target != from) vm.move(from, target)
        }
        drag.clear()
    }

    val tile: @Composable (AppEntry, Slot, Boolean) -> Unit = { app, slot, inDock ->
        val origin = remember { mutableStateOf(Offset.Zero) }
        AppTile(
            app = app,
            loader = vm.icons,
            iconSize = iconSize,
            showLabel = showLabels && !inDock,
            badge = state.badges[app.packageName] ?: 0,
            editMode = editMode,
            colorFilter = colorFilter,
            invisible = drag.pkg == app.packageName,
            onClick = { if (!editMode) vm.launch(app.packageName) },
            onLongClick = { if (!editMode) menu = MenuTarget(app, slot, fromLibrary = false) },
            onRemove = { menu = MenuTarget(app, slot, fromLibrary = false) },
            iconModifier = Modifier
                .onGloballyPositioned { origin.value = it.positionInRoot() }
                .pointerInput(editMode, slot) {
                    if (editMode) {
                        detectDragGestures(
                            onDragStart = { o -> drag.start(app.packageName, slot, origin.value + o) },
                            onDrag = { change, amount ->
                                change.consume()
                                drag.pos += amount
                            },
                            onDragEnd = { drop() },
                            onDragCancel = { drag.clear() },
                        )
                    }
                },
        )
    }

    val wallpaper: @Composable () -> Unit = { Wallpaper(wallpaperImage, dim) }

    Box(Modifier.fillMaxSize().onSizeChanged { rootSize = it }) {
        wallpaper()
        CompositionProviderGlass(rootSize, wallpaper) {
            Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding()) {
                Spacer(Modifier.height(if (editMode) 52.dp else 10.dp))
                HorizontalPager(
                    state = pager,
                    userScrollEnabled = drag.pkg == null,
                    beyondViewportPageCount = 2,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .onGloballyPositioned {
                            val b = it.boundsInRoot()
                            val inset = with(density) { 14.dp.toPx() }
                            gridRect.value = Rect(b.left + inset, b.top, b.right - inset, b.bottom)
                        },
                ) { page ->
                    if (page == libraryIndex) {
                        AppLibraryPage(
                            groups = groups,
                            loader = vm.icons,
                            colorFilter = colorFilter,
                            onLaunch = { vm.launch(it.packageName) },
                            onLongPress = { menu = MenuTarget(it, null, fromLibrary = true) },
                            onOpenSearch = { searchOpen = true },
                            onOpenGroup = { openGroup = it },
                        )
                    } else {
                        HomePage(
                            page = page,
                            apps = state.pages.getOrNull(page).orEmpty(),
                            editMode = editMode,
                            iconStyleDark = settings.iconStyle != IconStyle.DEFAULT,
                            weatherState = state.weather,
                            tile = tile,
                            onLongPressEmpty = { editMode = true },
                            onSwipeDown = { searchOpen = true },
                            onClockClick = { vm.openIntent(Intent(AlarmClock.ACTION_SHOW_ALARMS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) },
                            onWeatherClick = {
                                if (!vm.hasLocationPermission()) locationLauncher.launch(Manifest.permission.ACCESS_COARSE_LOCATION)
                                else vm.onLocationResult()
                            },
                        )
                    }
                }

                if (!badgeAccess && !settings.badgePromptDismissed && !editMode) {
                    BadgePromptBanner(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        onEnable = {
                            vm.openIntent(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                        },
                        onDismiss = { vm.dismissBadgePrompt() },
                    )
                }

                Box(Modifier.fillMaxWidth().padding(vertical = 8.dp), contentAlignment = Alignment.Center) {
                    Crossfade(targetState = pager.isScrollInProgress, label = "pill") { scrolling ->
                        if (scrolling) PageDots(homeCount, pager.currentPage.coerceAtMost(homeCount - 1))
                        else SearchPill(onClick = { searchOpen = true })
                    }
                }

                GlassSurface(
                    modifier = Modifier
                        .padding(start = 10.dp, end = 10.dp, bottom = 8.dp)
                        .fillMaxWidth()
                        .height(if (settings.largeIcons) 100.dp else 92.dp)
                        .onGloballyPositioned { dockRect.value = it.boundsInRoot() },
                    shape = RoundedCornerShape(34.dp),
                    tint = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.2f),
                ) {
                    Row(
                        Modifier.fillMaxSize().padding(horizontal = 12.dp),
                        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        state.dock.forEachIndexed { i, app ->
                            key(app.packageName) { tile(app, Slot(Slot.DOCK, i), true) }
                        }
                    }
                }
            }

            if (editMode) {
                Row(
                    Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 16.dp, vertical = 8.dp),
                ) {
                    GlassButton("Sesuaikan") { customizeOpen = true }
                    Spacer(Modifier.weight(1f))
                    GlassButton("Siap", selected = true) { editMode = false }
                }
            }

            val dragging = drag.pkg
            if (dragging != null) {
                val half = with(density) { iconSize.toPx() / 2 }
                AppIconImage(
                    pkg = dragging,
                    loader = vm.icons,
                    size = iconSize,
                    colorFilter = colorFilter,
                    modifier = Modifier
                        .offset { IntOffset((drag.pos.x - half).roundToInt(), (drag.pos.y - half).roundToInt()) }
                        .graphicsLayer {
                            scaleX = 1.15f
                            scaleY = 1.15f
                        },
                )
            }

            openGroup?.let { name ->
                val apps = groups.firstOrNull { it.first == name }?.second.orEmpty()
                GroupOverlay(
                    name = name,
                    apps = apps,
                    loader = vm.icons,
                    colorFilter = colorFilter,
                    onLaunch = {
                        openGroup = null
                        vm.launch(it.packageName)
                    },
                    onLongPress = { menu = MenuTarget(it, null, fromLibrary = true) },
                    onClose = { openGroup = null },
                )
            }

            if (searchOpen) {
                val suggestions = remember(state.dock, state.pages) {
                    (state.dock + state.pages.firstOrNull().orEmpty()).take(8)
                }
                SearchOverlay(
                    apps = state.allApps,
                    suggestions = suggestions,
                    loader = vm.icons,
                    colorFilter = colorFilter,
                    onLaunch = {
                        searchOpen = false
                        vm.launch(it.packageName)
                    },
                    onWebSearch = {
                        searchOpen = false
                        vm.webSearch(it)
                    },
                    onClose = { searchOpen = false },
                )
            }

            if (customizeOpen) {
                CustomizeSheet(
                    style = settings.iconStyle,
                    tint = settings.tint,
                    large = settings.largeIcons,
                    onStyle = vm::setIconStyle,
                    onTint = vm::setTint,
                    onLarge = vm::setLargeIcons,
                    onPickWallpaper = {
                        wallpaperLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    },
                    onResetWallpaper = vm::resetWallpaper,
                    onDismiss = { customizeOpen = false },
                )
            }

            menu?.let { target ->
                val pkg = target.app.packageName
                val actions = buildList {
                    if (!target.fromLibrary) add(MenuAction("Edit Skrin Utama", MenuIcons.edit) { editMode = true })
                    if (target.fromLibrary && pkg in state.hidden) {
                        add(MenuAction("Tambah ke Skrin Utama", MenuIcons.add) { vm.unhide(pkg) })
                    }
                    add(MenuAction("Info App", MenuIcons.info) { vm.openAppInfo(pkg) })
                    if (!target.fromLibrary) add(MenuAction("Buang dari Skrin Utama", MenuIcons.remove) { vm.hide(pkg) })
                    add(MenuAction("Nyahpasang App", MenuIcons.delete, destructive = true) { vm.uninstall(pkg) })
                }
                AppContextMenu(
                    app = target.app,
                    loader = vm.icons,
                    colorFilter = colorFilter,
                    actions = actions,
                    onDismiss = { menu = null },
                )
            }
        }
    }
}

@Composable
private fun CompositionProviderGlass(rootSize: IntSize, wallpaper: @Composable () -> Unit, content: @Composable () -> Unit) {
    val glass = remember(rootSize, wallpaper) { GlassContext(rootSize, wallpaper) }
    androidx.compose.runtime.CompositionLocalProvider(LocalGlass provides glass) { content() }
}

@Composable
private fun HomePage(
    page: Int,
    apps: List<AppEntry>,
    editMode: Boolean,
    iconStyleDark: Boolean,
    weatherState: com.mohdrozaimi.langitlauncher.domain.WeatherState,
    tile: @Composable (AppEntry, Slot, Boolean) -> Unit,
    onLongPressEmpty: () -> Unit,
    onSwipeDown: () -> Unit,
    onClockClick: () -> Unit,
    onWeatherClick: () -> Unit,
) {
    val density = LocalDensity.current
    BoxWithConstraints(
        Modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp)
            .pointerInput(editMode) {
                detectTapGestures(onLongPress = { if (!editMode) onLongPressEmpty() })
            }
            .pointerInput(editMode) {
                if (!editMode) {
                    var total = 0f
                    val threshold = with(density) { 70.dp.toPx() }
                    detectVerticalDragGestures(
                        onDragStart = { total = 0f },
                        onDragEnd = { if (total > threshold) onSwipeDown() },
                        onVerticalDrag = { _, dy -> total += dy },
                    )
                }
            },
    ) {
        val cellW = maxWidth / COLUMNS
        val cellH = maxHeight / ROWS
        val offset = if (page == 0) WIDGET_SLOTS else 0
        if (page == 0) {
            val widget = min(cellW * 2, cellH * 2) - 26.dp
            val inset = (cellW * 2 - widget) / 2
            ClockWidget(
                size = widget,
                dark = iconStyleDark,
                modifier = Modifier.offset(x = inset, y = 4.dp),
                onClick = onClockClick,
            )
            WeatherWidget(
                size = widget,
                state = weatherState,
                modifier = Modifier.offset(x = cellW * 2 + inset, y = 4.dp),
                onClick = onWeatherClick,
            )
        }
        apps.forEachIndexed { i, app ->
            val pos = i + offset
            if (pos < COLUMNS * ROWS) {
                key(app.packageName) {
                    Box(
                        Modifier
                            .offset(x = cellW * (pos % COLUMNS), y = cellH * (pos / COLUMNS))
                            .size(cellW, cellH),
                        contentAlignment = Alignment.Center,
                    ) { tile(app, Slot(page, i), false) }
                }
            }
        }
    }
}

