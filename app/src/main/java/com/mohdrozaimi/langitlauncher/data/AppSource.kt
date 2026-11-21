package com.mohdrozaimi.langitlauncher.data

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.provider.Settings
import androidx.core.content.ContextCompat
import com.mohdrozaimi.langitlauncher.model.AppEntry
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class AppSource(private val context: Context) {
    private val pm: PackageManager = context.packageManager

    fun loadApps(): List<AppEntry> {
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        val list: List<ResolveInfo> = if (Build.VERSION.SDK_INT >= 33) {
            pm.queryIntentActivities(intent, PackageManager.ResolveInfoFlags.of(0L))
        } else {
            @Suppress("DEPRECATION")
            pm.queryIntentActivities(intent, 0)
        }
        return list.asSequence()
            .filter { it.activityInfo.packageName != context.packageName }
            .map {
                AppEntry(
                    packageName = it.activityInfo.packageName,
                    label = it.loadLabel(pm).toString(),
                    category = it.activityInfo.applicationInfo.category,
                )
            }
            .distinctBy { it.packageName }
            .sortedBy { it.label.lowercase() }
            .toList()
    }

    /** Hantar isyarat setiap kali app dipasang, dibuang atau dikemas kini (dan sekali di awal). */
    val changes: Flow<Unit> = callbackFlow {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(c: Context?, i: Intent?) {
                trySend(Unit)
            }
        }
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_PACKAGE_ADDED)
            addAction(Intent.ACTION_PACKAGE_REMOVED)
            addAction(Intent.ACTION_PACKAGE_CHANGED)
            addAction(Intent.ACTION_PACKAGE_REPLACED)
            addDataScheme("package")
        }
        ContextCompat.registerReceiver(context, receiver, filter, ContextCompat.RECEIVER_EXPORTED)
        trySend(Unit)
        awaitClose { context.unregisterReceiver(receiver) }
    }

    /** App lalai untuk dok: telefon, mesej, pelayar, kamera. */
    fun defaultDock(): List<String> = listOf(
        Intent(Intent.ACTION_DIAL),
        Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:")),
        Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com")),
        Intent(MediaStore.ACTION_IMAGE_CAPTURE),
    ).mapNotNull { resolve(it) }.distinct()

    private fun resolve(intent: Intent): String? {
        val info = if (Build.VERSION.SDK_INT >= 33) {
            pm.resolveActivity(intent, PackageManager.ResolveInfoFlags.of(PackageManager.MATCH_DEFAULT_ONLY.toLong()))
        } else {
            @Suppress("DEPRECATION")
            pm.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY)
        }
        val pkg = info?.activityInfo?.packageName ?: return null
        return if (pkg == "android" || pm.getLaunchIntentForPackage(pkg) == null) null else pkg
    }

    fun launch(pkg: String): Boolean {
        val intent = pm.getLaunchIntentForPackage(pkg) ?: return false
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
        return start(intent)
    }

    fun openAppInfo(pkg: String) = start(
        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:$pkg")).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
    )

    fun uninstall(pkg: String) = start(
        Intent(Intent.ACTION_DELETE, Uri.parse("package:$pkg")).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
    )

    fun webSearch(query: String) = start(
        Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/search?q=" + Uri.encode(query)))
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
    )

    fun start(intent: Intent): Boolean = try {
        context.startActivity(intent)
        true
    } catch (e: Exception) {
        false
    }
}
