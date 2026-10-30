package com.example.transitreminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class AlarmToggleReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == ACTION_TOGGLE_ALARM) {
            val prefs = context.getSharedPreferences("prefs", Context.MODE_PRIVATE)
            val currentState = prefs.getBoolean("alarm_active", true)
            val newState = !currentState

            // Simpan status baru
            prefs.edit().putBoolean("alarm_active", newState).apply()

            // Jika dimatikan, hentikan suara alarm yang sedang berbunyi
            if (!newState) {
                TransitAlertManager.stopAlert()
            }

            val destination = intent.getStringExtra(EXTRA_DESTINATION) ?: "Stasiun / Halte Transit"
            val details = intent.getStringExtra(EXTRA_DETAILS) ?: "Memantau perjalanan..."
            val isArrived = intent.getBooleanExtra(EXTRA_IS_ARRIVED, false)

            // Perbarui tampilan kapsul di notifikasi secara instan dengan layout yang sesuai
            if (isArrived) {
                NotificationHelper.showArrivedNotification(context, destination, details)
            } else {
                NotificationHelper.showTransitNotification(context, destination, details)
            }
        }
    }

    companion object {
        const val ACTION_TOGGLE_ALARM = "com.example.transitreminder.ACTION_TOGGLE_ALARM"
        const val EXTRA_DESTINATION = "extra_destination"
        const val EXTRA_DETAILS = "extra_details"
        const val EXTRA_IS_ARRIVED = "extra_is_arrived"
    }
}
