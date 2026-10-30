package com.example.transitreminder

import android.app.Notification
import android.content.Context
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import java.util.regex.Pattern

class MapNotificationListener : NotificationListenerService() {

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        if (sbn == null) return

        // Hanya proses notifikasi yang berasal dari Google Maps
        if (sbn.packageName != GOOGLE_MAPS_PACKAGE) return

        val extras = sbn.notification.extras ?: return
        val title = extras.getString(Notification.EXTRA_TITLE) ?: ""
        val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString() ?: ""
        val subText = extras.getCharSequence(Notification.EXTRA_SUB_TEXT)?.toString() ?: ""

        if (title.isBlank() && text.isBlank()) return

        val destination = if (title.isNotBlank()) title else "Tujuan Transit"
        val details = when {
            text.isNotBlank() && subText.isNotBlank() -> "$text • $subText"
            text.isNotBlank() -> text
            else -> subText
        }

        val combinedText = "$title $text $subText"
        val isApproaching = checkIsApproaching(combinedText)

        if (isApproaching) {
            // Tampilan otomatis memanjang mirip Google Maps + alert focus HyperOS
            NotificationHelper.showArrivedNotification(this, destination, details)
            TransitAlertManager.triggerAlertIfEnabled(this)
        } else {
            // Tampilan normal ringkas selama masih dalam perjalanan jauh
            NotificationHelper.showTransitNotification(this, destination, details)
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        if (sbn?.packageName == GOOGLE_MAPS_PACKAGE) {
            NotificationHelper.cancelNotification(this)
        }
    }

    /**
     * Memeriksa kondisi apakah sudah mendekati tujuan berdasarkan opsi pengguna (Jarak vs Waktu).
     */
    private fun checkIsApproaching(text: String): Boolean {
        val prefs = getSharedPreferences("prefs", Context.MODE_PRIVATE)
        val triggerType = prefs.getString("trigger_type", "distance") ?: "distance"
        val lower = text.lowercase()

        return if (triggerType == "distance") {
            // 1. Pemicu Berdasarkan Jarak (Meters)
            val userThresholdMeters = prefs.getInt("threshold_meters", 500)
            val meters = extractMeters(lower)
            meters != null && meters <= userThresholdMeters
        } else {
            // 2. Pemicu Berdasarkan Waktu (Menit)
            val userThresholdMinutes = prefs.getInt("threshold_minutes", 2)
            val minutes = extractMinutes(lower)
            minutes != null && minutes <= userThresholdMinutes
        }
    }

    private fun extractMeters(text: String): Int? {
        val matcher = Pattern.compile("(\\d+)\\s*(?:m|meter)").matcher(text)
        return if (matcher.find()) {
            matcher.group(1)?.toIntOrNull()
        } else {
            null
        }
    }

    private fun extractMinutes(text: String): Int? {
        val matcher = Pattern.compile("(\\d+)\\s*(?:mnt|min|menit)").matcher(text)
        return if (matcher.find()) {
            matcher.group(1)?.toIntOrNull()
        } else if (text.contains("sebentar lagi") || text.contains("arriving")) {
            1
        } else {
            null
        }
    }

    companion object {
        const val GOOGLE_MAPS_PACKAGE = "com.google.android.apps.maps"
    }
}
