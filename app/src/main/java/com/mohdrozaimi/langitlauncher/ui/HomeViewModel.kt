package com.mohdrozaimi.langitlauncher.ui

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.mohdrozaimi.langitlauncher.badge.BadgeStore
import com.mohdrozaimi.langitlauncher.data.AppSource
import com.mohdrozaimi.langitlauncher.data.IconLoader
import com.mohdrozaimi.langitlauncher.data.LauncherSettings
import com.mohdrozaimi.langitlauncher.data.LayoutStore
import com.mohdrozaimi.langitlauncher.data.WeatherRepository
import com.mohdrozaimi.langitlauncher.domain.HomeLayout
import com.mohdrozaimi.langitlauncher.domain.IconStyle
import com.mohdrozaimi.langitlauncher.domain.LayoutReducer
import com.mohdrozaimi.langitlauncher.domain.Slot
import com.mohdrozaimi.langitlauncher.domain.WeatherState
import com.mohdrozaimi.langitlauncher.model.AppEntry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File

data class HomeUiState(
    val pages: List<List<AppEntry>> = emptyList(),
    val dock: List<AppEntry> = emptyList(),
    val allApps: List<AppEntry> = emptyList(),
    val hidden: Set<String> = emptySet(),
    val badges: Map<String, Int> = emptyMap(),
    val weather: WeatherState = WeatherState.Loading,
)

class HomeViewModel(app: Application) : AndroidViewModel(app) {
    private val source = AppSource(app)
    private val store = LayoutStore(app)
    private val weatherRepo = WeatherRepository(app)
    private val layoutLock = Mutex()

    val icons = IconLoader(app.packageManager, (app.resources.displayMetrics.density * 72).toInt().coerceAtLeast(96))

    private val apps = MutableStateFlow<List<AppEntry>>(emptyList())
    private val weather = MutableStateFlow<WeatherState>(WeatherState.Loading)

    private val _homeTick = MutableStateFlow(0)
    val homeTick: StateFlow<Int> = _homeTick.asStateFlow()

    private val _badgeAccess = MutableStateFlow(BadgeStore.hasAccess(app))
    val badgeAccess: StateFlow<Boolean> = _badgeAccess.asStateFlow()

    val settings: StateFlow<LauncherSettings> =
        store.settings.stateIn(viewModelScope, SharingStarted.Eagerly, LauncherSettings())

    val state: StateFlow<HomeUiState> = combine(apps, store.layout, BadgeStore.counts, weather) { list, layout, badges, w ->
        val byPkg = list.associateBy { it.packageName }
        HomeUiState(
            pages = layout.pages.map { page -> page.mapNotNull { byPkg[it] } }.filter { it.isNotEmpty() },
            dock = layout.dock.mapNotNull { byPkg[it] },
            allApps = list,
            hidden = layout.hidden.toSet(),
            badges = badges,
            weather = w,
        )
    }.stateIn(viewModelScope, SharingStarted.Eagerly, HomeUiState())

    private var weatherJob: Job? = null

    init {
        viewModelScope.launch {
            source.changes.collect { refreshApps() }
        }
        startWeather()
    }

    private suspend fun refreshApps() {
        val list = withContext(Dispatchers.IO) { source.loadApps() }
        val defaults = withContext(Dispatchers.IO) { source.defaultDock() }
        icons.clear()
        apps.value = list
        updateLayout { LayoutReducer.sync(it, list.map { a -> a.packageName }, defaults) }
    }

    private suspend fun updateLayout(change: (HomeLayout) -> HomeLayout) {
        layoutLock.withLock {
            val current = store.currentLayout()
            val next = change(current)
            if (next != current) store.saveLayout(next)
        }
    }

    private fun startWeather() {
        weatherJob?.cancel()
        weatherJob = viewModelScope.launch {
            while (true) {
                weather.value = weatherRepo.fetch()
                delay(30 * 60 * 1000L)
            }
        }
    }

    fun hasLocationPermission() = weatherRepo.hasLocationPermission()

    fun onLocationResult() {
        viewModelScope.launch { store.markLocationAsked() }
        startWeather()
    }

    fun onResume() {
        _badgeAccess.value = BadgeStore.hasAccess(getApplication())
    }

    fun onHomePressed() {
        _homeTick.value += 1
    }

    fun launch(pkg: String) = source.launch(pkg)
    fun openAppInfo(pkg: String) = source.openAppInfo(pkg)
    fun uninstall(pkg: String) = source.uninstall(pkg)
    fun webSearch(q: String) = source.webSearch(q)
    fun openIntent(intent: android.content.Intent) = source.start(intent)

    fun move(from: Slot, to: Slot) {
        viewModelScope.launch { updateLayout { LayoutReducer.move(it, from, to) } }
    }

    fun hide(pkg: String) {
        viewModelScope.launch { updateLayout { LayoutReducer.hide(it, pkg) } }
    }

    fun unhide(pkg: String) {
        viewModelScope.launch { updateLayout { LayoutReducer.unhide(it, pkg) } }
    }

    fun setIconStyle(style: IconStyle) {
        viewModelScope.launch { store.setIconStyle(style) }
    }

    fun setTint(tint: Long) {
        viewModelScope.launch { store.setTint(tint) }
    }

    fun setLargeIcons(large: Boolean) {
        viewModelScope.launch { store.setLargeIcons(large) }
    }

    fun dismissBadgePrompt() {
        viewModelScope.launch { store.dismissBadgePrompt() }
    }

    fun setWallpaper(uri: Uri) {
        viewModelScope.launch {
            val app = getApplication<Application>()
            val path = withContext(Dispatchers.IO) {
                try {
                    app.filesDir.listFiles()?.filter { it.name.startsWith("wallpaper_") }?.forEach { it.delete() }
                    val out = File(app.filesDir, "wallpaper_${System.currentTimeMillis()}.jpg")
                    app.contentResolver.openInputStream(uri)?.use { input ->
                        out.outputStream().use { input.copyTo(it) }
                    } ?: return@withContext null
                    out.absolutePath
                } catch (e: Exception) {
                    null
                }
            }
            if (path != null) store.setWallpaper(path)
        }
    }

    fun resetWallpaper() {
        viewModelScope.launch { store.setWallpaper(null) }
    }
}
