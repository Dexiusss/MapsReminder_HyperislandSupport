package com.example.transitreminder

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat

object NotificationHelper {

    private const val CHANNEL_ID = "transit_clean_channel"
    private const val NOTIFICATION_ID = 2001

    /**
     * Tampilan Normal di Perjalanan (Kapsul Clean Satu Baris).
     */
    fun showTransitNotification(context: Context, destination: String, details: String) {
        buildAndNotify(context, destination, details, isArrived = false)
    }

    /**
     * Tampilan Saat Sudah Dekat (Otomatis Memanjang Mirip Google Maps + Alert Focus HyperOS).
     */
    fun showArrivedNotification(context: Context, destination: String, details: String) {
        buildAndNotify(context, destination, details, isArrived = true)
    }

    private fun buildAndNotify(
        context: Context,
        destination: String,
        details: String,
        isArrived: Boolean
    ) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        createChannel(notificationManager)

        val prefs = context.getSharedPreferences("prefs", Context.MODE_PRIVATE)
        val isAlarmActive = prefs.getBoolean("alarm_active", true)

        // PendingIntent untuk Tombol Kapsul ON/OFF
        val toggleIntent = Intent(context, AlarmToggleReceiver::class.java).apply {
            action = AlarmToggleReceiver.ACTION_TOGGLE_ALARM
            putExtra(AlarmToggleReceiver.EXTRA_DESTINATION, destination)
            putExtra(AlarmToggleReceiver.EXTRA_DETAILS, details)
            putExtra(AlarmToggleReceiver.EXTRA_IS_ARRIVED, isArrived)
        }

        val togglePendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            toggleIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Pilih Layout: Jika sudah dekat, gunakan layout memanjang (notification_transit_arrived)
        val layoutRes = if (isArrived) {
            R.layout.notification_transit_arrived
        } else {
            R.layout.notification_transit_clean
        }

        val remoteViews = RemoteViews(context.packageName, layoutRes).apply {
            setTextViewText(R.id.tvDestination, destination)
            setTextViewText(R.id.tvDetails, details)

            if (isAlarmActive) {
                setTextViewText(R.id.btnPillToggle, "ON")
                setInt(R.id.btnPillToggle, "setBackgroundResource", R.drawable.bg_pill_on)
            } else {
                setTextViewText(R.id.btnPillToggle, "OFF")
                setInt(R.id.btnPillToggle, "setBackgroundResource", R.drawable.bg_pill_off)
            }

            setOnClickPendingIntent(R.id.btnPillToggle, togglePendingIntent)
        }

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_transit_train)
            .setStyle(NotificationCompat.DecoratedCustomViewStyle())
            .setCustomContentView(remoteViews)
            .setOngoing(true)
            .setCategory(NotificationCompat.CATEGORY_NAVIGATION)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)

        if (isArrived) {
            // Memberikan prioritas MAX dan alert agar HyperOS otomatis memanjangkan status bar / heads-up banner
            builder.setPriority(NotificationCompat.PRIORITY_MAX)
            builder.setDefaults(NotificationCompat.DEFAULT_VIBRATE)
        } else {
            builder.setPriority(NotificationCompat.PRIORITY_HIGH)
        }

        notificationManager.notify(NOTIFICATION_ID, builder.build())
    }

    /**
     * Menghapus notifikasi saat rute selesai atau dibatalkan.
     */
    fun cancelNotification(context: Context) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(NOTIFICATION_ID)
        TransitAlertManager.stopAlert()
    }

    private fun createChannel(manager: NotificationManager) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Transit Reminder",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifikasi status transit dan rute navigasi Google Maps"
                setShowBadge(false)
            }
            manager.createNotificationChannel(channel)
        }
    }
}
