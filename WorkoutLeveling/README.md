# Workout Leveling

APK pribadi (sideload), offline-first — lihat rencana di `../ROADMAP_SOLO_FITNESS_APP.md`.

## Buka di Android Studio

1. **File → Open** → pilih folder `WorkoutLeveling` (bukan root `SOLOLEVEL` saja, kecuali kamu import modul).
2. Tunggu **Gradle Sync**; pastikan JDK **17** (Android Studio → Settings → Build → Gradle).
3. Jalankan di emulator atau perangkat: **Run** ▶.

## Minggu 1–2 (status)

- Jetpack Compose + Material 3 + tema gelap (gate).
- Navigation: Home → Session, Home → Progress, Home → Baseline (opsional).
- Room v5: tabel `exercise_catalog` + `pain_reports`; migrasi `1 → 2 → 3 → 4 → 5`.
- XP sesi: 50 + menit latihan (maks 45) + bonus streak (≥3 hari), level tiap 300 XP (lihat `ProgressRules`).
- Quest harian otomatis: “Selesaikan 1 sesi hari ini”.
- Quest mingguan (3× sesi), grafik 14 hari di Progress (jumlah sesi + volume rep×kg + menit cardio), readiness check (tidur/energi/pegal), saran progression otomatis, flag nyeri tajam per gerakan (warning jika berulang), **katalog custom** (tambah/edit/hapus + filter HG60), **bonus XP mobility/cooldown** (checkbox saat simpan), **rank** dari ringkasan rolling ~4 minggu (bukan hanya level), check-in tubuh berkala + cut trend ringkas, backup DB (ekspor + impor tervalidasi + **safety backup otomatis sebelum impor**), Health Connect, pengingat Senin (WorkManager).
- UX kecil: kartu tips pertama di Home (bisa dismiss), plus unit test dasar untuk `ProgressRules` dan validator header backup.
- Android 13+: izin **notifikasi** diminta **sekali** saat pertama buka (opsional; pengingat tetap dijadwalkan).
- **compileSdk 36** (syarat `androidx.health.connect:connect-client`); `targetSdk` tetap 35.
- DataStore: `UserPreferences` (flag baseline selesai / dilewati).

### Gradle / JDK

File `gradle/gradle-daemon-jvm.properties` (vendor JetBrains) **dihapus** agar build bisa pakai JDK biasa (`JAVA_HOME` / JDK di Android Studio). Jika Studio membuat ulang file itu dan build error, hapus lagi atau sesuaikan vendor/versi JDK.

## Gradle / Android Studio: error `Unknown command-line option '--jvm-vendor'`

Itu muncul jika **Gradle terlalu lama** dibanding Android Studio (Studio memanggil `updateDaemonJvm --jvm-vendor=…`).  
Project ini memakai **Gradle 8.11.1** + **AGP 8.9.2** supaya selaras.

Setelah pull/sync: **File → Sync Project with Gradle Files**.

Di **Settings → Build Tools → Gradle → Gradle JDK**, pilih **jbr-21 Embedded** (atau JDK 17+) bawaan Android Studio.

## Build APK debug dari terminal

```bash
cd WorkoutLeveling
./gradlew :app:assembleDebug
```

Output biasanya: `app/build/outputs/apk/debug/app-debug.apk`  
*(Perlu JDK 17 di `JAVA_HOME`.)*
