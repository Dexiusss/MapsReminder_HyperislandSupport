package com.example.transitreminder

import android.content.Context
import android.media.AudioAttributes
import android.media.Ringtone
import android.media.RingtoneManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

object TransitAlertManager {

    private var activeRingtone: Ringtone? = null
    private var isAlertRinging = false
    private val mainHandler = Handler(Looper.getMainLooper())
    private var repeatRunnable: Runnable? = null

    /**
     * Membunyikan nada notifikasi sistem dan getaran.
     * Mendukung mode 'Repeat' (berulang terus) atau 'Just Once' (sekali saja) sesuai pengaturan aplikasi.
     */
    @Synchronized
    fun triggerAlertIfEnabled(context: Context) {
        val prefs = context.getSharedPreferences("prefs", Context.MODE_PRIVATE)
        val isAlarmActive = prefs.getBoolean("alarm_active", true)
        val isRepeatEnabled = prefs.getBoolean("alarm_repeat", false)

        if (!isAlarmActive || isAlertRinging) return

        isAlertRinging = true

        if (isRepeatEnabled) {
            // Mode Berulang (Repeat): Mainkan berulang tiap 3 detik sampai tombol dimatikan
            repeatRunnable = object : Runnable {
                override fun run() {
                    if (!isAlertRinging) return
                    playSystemNotificationSoundAndVibrate(context)
                    mainHandler.postDelayed(this, 3000)
                }
            }
            repeatRunnable?.run()
        } else {
            // Mode Sekali Saja (Just Once)
            playSystemNotificationSoundAndVibrate(context)
        }
    }

    private fun playSystemNotificationSoundAndVibrate(context: Context) {
        // 1. Getar 500ms
        try {
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                vm.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(
                    VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE)
                )
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(500)
            }
        } catch (_: Exception) {}

        // 2. Bunyikan Nada Dering Notifikasi Sistem Bawaan HP
        try {
            val notifUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            activeRingtone = RingtoneManager.getRingtone(context, notifUri)?.apply {
                audioAttributes = AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
                play()
            }
        } catch (_: Exception) {}
    }

    /**
     * Menghentikan suara dan pembatalan looping alarm.
     */
    @Synchronized
    fun stopAlert() {
        isAlertRinging = false
        repeatRunnable?.let { mainHandler.removeCallbacks(it) }
        repeatRunnable = null

        try {
            activeRingtone?.stop()
            activeRingtone = null
        } catch (_: Exception) {}
    }
}
