# MapsReminder_HyperislandSupport

[![Download APK](https://img.shields.io/badge/Download-TransitReminder.apk-34C759?style=for-the-badge&logo=android)](https://github.com/Dexiusss/MapsReminder_HyperislandSupport/releases/download/v1.0.0/TransitReminder.apk)
[![Release v1.0.0](https://img.shields.io/badge/Release-v1.0.0-blue?style=for-the-badge)](https://github.com/Dexiusss/MapsReminder_HyperislandSupport/releases/tag/v1.0.0)

> 📲 **Download Langsung File APK:**  
> Unduh file siap pasang di HP Anda melalui [**TransitReminder.apk (v1.0.0)**](https://github.com/Dexiusss/MapsReminder_HyperislandSupport/releases/download/v1.0.0/TransitReminder.apk).

---

## Fitur Utama

- **Integrasi Google Maps Otomatis:** Membaca notifikasi rute dan transit dari `com.google.android.apps.maps` secara real-time via `NotificationListenerService` tanpa membebani baterai.
- **Tampilan Clean Tanpa Clutter:** Menghilangkan ikon pin 📍 untuk menghasilkan layout satu baris ultra-minimalis di status bar HyperOS.
- **Tombol Kapsul (*Pill Toggle*) ON/OFF:** Mengaktifkan atau mematikan alarm suara dan getar secara instan langsung dari notifikasi tanpa harus membuka aplikasi.
- **HyperIsland Otomatis Memanjang Saat Dekat:** Begitu sisa jarak atau waktu mendekati tujuan, layout notifikasi otomatis bertransisi ke tampilan memanjang beraksen hijau emerald Google Maps dengan label `TIBA SEBENTAR LAGI`.
- **Fleksibilitas Pemicu Alarm:**
  - **Berdasarkan Jarak:** Mengatur ambang batas pemicu alarm (200m – 2000m).
  - **Berdasarkan Waktu:** Mengatur ambang batas pemicu alarm (1 menit – 10 menit).
- **Karakter Alarm Sistem:** Menggunakan nada notifikasi sistem bawaan perangkat dengan opsi **Repeat** (berulang tiap 3 detik) atau **Just Once** (sekali saja).

---

## Tangkapan Layar / Visualisasi Status

### 1. Di Perjalanan (Normal - Ringkas)
```text
+---------------------------------------------------------------+
|  Halte Juanda                                       [  ON  ]  |
|  3.8 km lagi • ~15 mnt                             (kapsul)   |
+---------------------------------------------------------------+
```

### 2. Saat Sudah Dekat Tujuan (Otomatis Memanjang & Alert)
```text
+---------------------------------------------------------------+
|  [ TIBA SEBENTAR LAGI ]                             [  ON  ]  |
|                                                               |
|  Halte Juanda                                                 |
|  Sisa 150 m • Segera bersiap turun dari kendaraan            |
+---------------------------------------------------------------+
```

---

## Arsitektur Proyek

```
app/
├── src/main/
│   ├── AndroidManifest.xml
│   ├── java/com/example/transitreminder/
│   │   ├── MainActivity.kt            # Layar konfigurasi pemicu & simulasi
│   │   ├── MapNotificationListener.kt # Pembaca Google Maps di latar belakang
│   │   ├── NotificationHelper.kt      # Pembangun notifikasi status bar HyperOS
│   │   ├── AlarmToggleReceiver.kt     # Pengelola tombol kapsul inline ON/OFF
│   │   └── TransitAlertManager.kt     # Pengelola nada notifikasi & getaran
│   └── res/
│       ├── drawable/
│       │   ├── bg_card_arrived.xml
│       │   ├── bg_pill_off.xml
│       │   ├── bg_pill_on.xml
│       │   ├── circle_progress_ring.xml
│       │   └── ic_transit_train.xml
│       └── layout/
│           ├── activity_main.xml
│           ├── notification_transit_arrived.xml
│           └── notification_transit_clean.xml
```

---

## Cara Kompilasi & Build APK

Jalankan perintah berikut di terminal:

```bash
# Windows
gradlew.bat assembleDebug

# Linux / macOS
./gradlew assembleDebug
```

Hasil file APK akan dibuat di:
`app/build/outputs/apk/debug/app-debug.apk`

---

## Izin yang Diperlukan

1. **`POST_NOTIFICATIONS`**: Untuk memunculkan notifikasi navigasi di Android 13+.
2. **`VIBRATE`**: Untuk efek getar pengingat transit saat mendekati tujuan.
3. **`BIND_NOTIFICATION_LISTENER_SERVICE`**: Untuk mendeteksi navigasi aktif dari Google Maps.

---

## Lisensi

Didistribusikan di bawah lisensi MIT.
