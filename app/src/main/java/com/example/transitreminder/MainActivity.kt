package com.example.transitreminder

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.button.MaterialButton
import com.google.android.material.button.MaterialButtonToggleGroup
import com.google.android.material.materialswitch.MaterialSwitch
import com.google.android.material.slider.Slider

class MainActivity : AppCompatActivity() {

    private lateinit var tvPermissionStatus: TextView
    private lateinit var btnGrantPermission: MaterialButton
    private lateinit var toggleTriggerType: MaterialButtonToggleGroup
    private lateinit var layoutDistanceSetting: LinearLayout
    private lateinit var layoutTimeSetting: LinearLayout
    private lateinit var sliderDistance: Slider
    private lateinit var sliderTime: Slider
    private lateinit var tvDistanceLabel: TextView
    private lateinit var tvTimeLabel: TextView
    private lateinit var switchRepeatAlarm: MaterialSwitch
    private lateinit var tvRepeatSubtitle: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val prefs = getSharedPreferences("prefs", Context.MODE_PRIVATE)

        // Binding Views
        tvPermissionStatus = findViewById(R.id.tvPermissionStatus)
        btnGrantPermission = findViewById(R.id.btnGrantPermission)
        toggleTriggerType = findViewById(R.id.toggleTriggerType)
        layoutDistanceSetting = findViewById(R.id.layoutDistanceSetting)
        layoutTimeSetting = findViewById(R.id.layoutTimeSetting)
        sliderDistance = findViewById(R.id.sliderDistance)
        sliderTime = findViewById(R.id.sliderTime)
        tvDistanceLabel = findViewById(R.id.tvDistanceLabel)
        tvTimeLabel = findViewById(R.id.tvTimeLabel)
        switchRepeatAlarm = findViewById(R.id.switchRepeatAlarm)
        tvRepeatSubtitle = findViewById(R.id.tvRepeatSubtitle)

        val btnSimulateNormal = findViewById<MaterialButton>(R.id.btnSimulateNormal)
        val btnSimulateArrived = findViewById<MaterialButton>(R.id.btnSimulateArrived)
        val btnStopSimulate = findViewById<MaterialButton>(R.id.btnStopSimulate)

        // 1. Setup Pilihan Mode Trigger (Jarak vs Waktu)
        val savedTriggerType = prefs.getString("trigger_type", "distance") ?: "distance"
        if (savedTriggerType == "distance") {
            toggleTriggerType.check(R.id.btnModeDistance)
            layoutDistanceSetting.visibility = View.VISIBLE
            layoutTimeSetting.visibility = View.GONE
        } else {
            toggleTriggerType.check(R.id.btnModeTime)
            layoutDistanceSetting.visibility = View.GONE
            layoutTimeSetting.visibility = View.VISIBLE
        }

        toggleTriggerType.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                if (checkedId == R.id.btnModeDistance) {
                    layoutDistanceSetting.visibility = View.VISIBLE
                    layoutTimeSetting.visibility = View.GONE
                    prefs.edit().putString("trigger_type", "distance").apply()
                } else if (checkedId == R.id.btnModeTime) {
                    layoutDistanceSetting.visibility = View.GONE
                    layoutTimeSetting.visibility = View.VISIBLE
                    prefs.edit().putString("trigger_type", "time").apply()
                }
            }
        }

        // 2. Setup Slider Jarak
        val savedMeters = prefs.getInt("threshold_meters", 500)
        sliderDistance.value = savedMeters.toFloat()
        tvDistanceLabel.text = "Picu saat sisa jarak: $savedMeters meter"

        sliderDistance.addOnChangeListener { _, value, _ ->
            val meters = value.toInt()
            tvDistanceLabel.text = "Picu saat sisa jarak: $meters meter"
            prefs.edit().putInt("threshold_meters", meters).apply()
        }

        // 3. Setup Slider Waktu
        val savedMinutes = prefs.getInt("threshold_minutes", 2)
        sliderTime.value = savedMinutes.toFloat()
        tvTimeLabel.text = "Picu saat sisa waktu: $savedMinutes menit"

        sliderTime.addOnChangeListener { _, value, _ ->
            val minutes = value.toInt()
            tvTimeLabel.text = "Picu saat sisa waktu: $minutes menit"
            prefs.edit().putInt("threshold_minutes", minutes).apply()
        }

        // 4. Setup Switch Repeat Alarm vs Once
        val isRepeatEnabled = prefs.getBoolean("alarm_repeat", false)
        switchRepeatAlarm.isChecked = isRepeatEnabled
        tvRepeatSubtitle.text = if (isRepeatEnabled) "Berulang tiap 3 detik sampai dimatikan" else "Bunyi 1x saja saat tiba"

        switchRepeatAlarm.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("alarm_repeat", isChecked).apply()
            tvRepeatSubtitle.text = if (isChecked) "Berulang tiap 3 detik sampai dimatikan" else "Bunyi 1x saja saat tiba"
        }

        // 5. Tombol Izin Notifikasi
        btnGrantPermission.setOnClickListener {
            openNotificationListenerSettings()
        }

        // 6. Tombol Uji Coba: Mode Normal di Perjalanan
        btnSimulateNormal.setOnClickListener {
            NotificationHelper.showTransitNotification(
                context = this,
                destination = "Halte Juanda",
                details = "3.8 km lagi • ~15 mnt"
            )
        }

        // 7. Tombol Uji Coba: Mode Sudah Dekat (Memanjang & Alert)
        btnSimulateArrived.setOnClickListener {
            NotificationHelper.showArrivedNotification(
                context = this,
                destination = "Halte Juanda",
                details = "Sisa 150 m • Segera bersiap turun dari kendaraan"
            )
            TransitAlertManager.triggerAlertIfEnabled(this)
        }

        // 8. Tombol Hapus Simulasi
        btnStopSimulate.setOnClickListener {
            NotificationHelper.cancelNotification(this)
        }

        requestNotificationPermission()
    }

    override fun onResume() {
        super.onResume()
        updatePermissionStatus()
    }

    private fun updatePermissionStatus() {
        if (isNotificationListenerEnabled()) {
            tvPermissionStatus.text = "Izin aktif. Siap memantau Google Maps secara otomatis."
            tvPermissionStatus.setTextColor(ContextCompat.getColor(this, R.color.pill_green))
            btnGrantPermission.isEnabled = false
            btnGrantPermission.text = "Izin Sudah Diberikan"
        } else {
            tvPermissionStatus.text = "Izin belum aktif. Diperlukan untuk membaca navigasi Maps."
            tvPermissionStatus.setTextColor(ContextCompat.getColor(this, R.color.text_secondary))
            btnGrantPermission.isEnabled = true
            btnGrantPermission.text = "Buka Pengaturan Izin"
        }
    }

    private fun isNotificationListenerEnabled(): Boolean {
        val cn = ComponentName(this, MapNotificationListener::class.java)
        val flat = Settings.Secure.getString(contentResolver, "enabled_notification_listeners")
        return flat != null && flat.contains(cn.flattenToString())
    }

    private fun openNotificationListenerSettings() {
        try {
            val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
            startActivity(intent)
        } catch (_: Exception) {
            val fallback = Intent(Settings.ACTION_SETTINGS)
            startActivity(fallback)
        }
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    101
                )
            }
        }
    }
}
