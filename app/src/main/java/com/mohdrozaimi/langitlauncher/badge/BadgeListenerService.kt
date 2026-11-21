package com.mohdrozaimi.langitlauncher.badge

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.mohdrozaimi.langitlauncher.domain.BadgeCounter
import com.mohdrozaimi.langitlauncher.domain.NotifInfo

class BadgeListenerService : NotificationListenerService() {

    override fun onListenerConnected() {
        super.onListenerConnected()
        refresh()
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        BadgeStore.update(emptyMap())
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        refresh()
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        refresh()
    }

    private fun refresh() {
        val active: Array<StatusBarNotification> = try {
            activeNotifications ?: return
        } catch (e: Exception) {
            return
        }
        BadgeStore.update(
            BadgeCounter.count(
                active.map {
                    NotifInfo(
                        pkg = it.packageName,
                        isGroupSummary = (it.notification.flags and Notification.FLAG_GROUP_SUMMARY) != 0,
                        isOngoing = it.isOngoing,
                        number = it.notification.number,
                    )
                },
            ),
        )
    }
}
