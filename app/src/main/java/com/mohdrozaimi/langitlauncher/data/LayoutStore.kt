package com.mohdrozaimi.langitlauncher.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.mohdrozaimi.langitlauncher.domain.HomeLayout
import com.mohdrozaimi.langitlauncher.domain.IconStyle
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json

private val Context.langitStore: DataStore<Preferences> by preferencesDataStore(name = "langit")

data class LauncherSettings(
    val wallpaperPath: String? = null,
    val iconStyle: IconStyle = IconStyle.DEFAULT,
    val tint: Long = 0xFFFF9F0AL,
    val largeIcons: Boolean = false,
    val badgePromptDismissed: Boolean = false,
    val locationAsked: Boolean = false,
)

class LayoutStore(context: Context) {
    private val store = context.applicationContext.langitStore
    private val json = Json { ignoreUnknownKeys = true }

    private object Keys {
        val layout = stringPreferencesKey("layout_json")
        val wallpaper = stringPreferencesKey("wallpaper_path")
        val iconStyle = stringPreferencesKey("icon_style")
        val tint = longPreferencesKey("tint")
        val large = booleanPreferencesKey("large_icons")
        val badgeDismissed = booleanPreferencesKey("badge_prompt_dismissed")
        val locAsked = booleanPreferencesKey("location_asked")
    }

    val layout: Flow<HomeLayout> = store.data.map { prefs ->
        prefs[Keys.layout]?.let { runCatching { json.decodeFromString(HomeLayout.serializer(), it) }.getOrNull() }
            ?: HomeLayout()
    }

    val settings: Flow<LauncherSettings> = store.data.map { p ->
        LauncherSettings(
            wallpaperPath = p[Keys.wallpaper],
            iconStyle = p[Keys.iconStyle]?.let { s -> IconStyle.entries.firstOrNull { it.name == s } } ?: IconStyle.DEFAULT,
            tint = p[Keys.tint] ?: 0xFFFF9F0AL,
            largeIcons = p[Keys.large] ?: false,
            badgePromptDismissed = p[Keys.badgeDismissed] ?: false,
            locationAsked = p[Keys.locAsked] ?: false,
        )
    }

    suspend fun currentLayout(): HomeLayout = layout.first()

    suspend fun saveLayout(layout: HomeLayout) {
        store.edit { it[Keys.layout] = json.encodeToString(HomeLayout.serializer(), layout) }
    }

    suspend fun setWallpaper(path: String?) {
        store.edit { if (path == null) it.remove(Keys.wallpaper) else it[Keys.wallpaper] = path }
    }

    suspend fun setIconStyle(style: IconStyle) = store.edit { it[Keys.iconStyle] = style.name }
    suspend fun setTint(tint: Long) = store.edit { it[Keys.tint] = tint }
    suspend fun setLargeIcons(large: Boolean) = store.edit { it[Keys.large] = large }
    suspend fun dismissBadgePrompt() = store.edit { it[Keys.badgeDismissed] = true }
    suspend fun markLocationAsked() = store.edit { it[Keys.locAsked] = true }
}
