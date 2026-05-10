# Roadmap Aplikasi Fitness Pribadi (Android, Offline-First)

**Nama produk:** Workout Leveling  
**Distribusi:** APK sideload ke HP pribadi (bukan target Google Play untuk saat ini).  
**Kode sumber:** folder `WorkoutLeveling/` di repo ini — lihat `WorkoutLeveling/README.md`.

Dokumen ini adalah plan step-by-step untuk membuat aplikasi fitness pribadi bertema progres ala game (inspirasi Solo Leveling), fokus aman untuk kondisi overweight, dan bisa dipakai harian tanpa VPS/LLM.

## 0) Tujuan Produk

Tujuan utama:
- Konsisten olahraga 12 minggu.
- Progress terukur (XP, Level, Rank) tanpa overtraining.
- 100% bisa jalan offline di Android.
- Bisa memanfaatkan data dari Garmin lewat Health Connect (jika tersedia).

Non-tujuan (versi awal):
- Tidak ada fitur sosial.
- Tidak ada login akun.
- Tidak ada cloud sync.
- Tidak ada LLM.

---

## 1) Scope MVP (wajib jadi dulu)

Fitur inti MVP:
- Log latihan: jenis latihan, set, rep, beban, durasi.
- Program latihan sederhana: Day A / Day B / Cardio.
- Quest harian dan mingguan.
- XP, level, streak, dan rank personal.
- Riwayat progres (berat badan, sesi latihan, streak).
- Onboarding baseline tubuh **opsional** (berat + lingkar pinggang dll.; bisa lewati / isi nanti) — lihat bagian 1.1.
- **Katalog gerakan Tunturi HG60** (seed / daftar pilihan cepat) — lihat bagian 5.0 agar template tidak berisi gerakan yang tidak ada di mesin.
- Integrasi basic Health Connect (langkah dan menit aktif, opsional di sprint awal).

Definition of Done MVP:
- Bisa dipakai harian minimal 2 minggu tanpa crash.
- Semua data tersimpan lokal (Room).
- User bisa mulai sesi < 10 detik dari home screen.

---

## 1.1) Baseline Tubuh di Awal (disarankan, tidak memblokir latihan)

Prinsip: **boleh mulai latihan dulu**; angka baseline bisa diisi nanti. Tujuannya supaya progres terlihat jelas tanpa obsesi timbang setiap hari.

### Apa yang disarankan disimpan (opsional per item)
- **Berat badan baseline**: 1 kali timbang dengan pola konsisten (misalnya pagi, setelah toilet, sebelum sarapan).
- **Lingkar pinggang** (cm): paling berguna sebagai pelengkap berat; sering berubah lebih dulu dari angka timbangan.
- Opsional: lingkar pinggul, dada, lengan (cm) jika kamu suka data tambahan.
- **Catatan singkat** (opsional): misalnya kondisi tidur, stres, atau alat timbang yang dipakai.

### Frekuensi input setelah baseline
- **Berat & ukuran**: cukup **1× per minggu** (sama hari & kondisi jika memungkinkan). Harian biasanya berisik (air tubuh) dan bisa bikin stres.
- **Metrik utama tetap konsistensi latihan** (jumlah sesi, streak, quest selesai); metrik tubuh adalah pelengkap tren.

### UX onboarding (versi awal app)
- Layar singkat: “**Isi baseline**” atau “**Lewati / isi nanti**” — tidak boleh mengunci `Enter Gate`.
- Reminder lembut mingguan (opsional): “Check-in mingguan: berat & pinggang”.
- Tidak wajib foto; jika nanti ditambah, tetap opsional dan hanya lokal.

### Kenapa ini masuk scope awal
- Untuk kondisi overweight, **kombinasi berat + lingkar pinggang** sering memberi gambaran progres lebih adil daripada berat saja.
- Tetap ringan agar app tidak terasa seperti “buku audit tubuh”.

---

## 2) Stack Teknis

- Bahasa: Kotlin
- UI: Jetpack Compose + Material 3
- Arsitektur: MVVM + Repository
- Database: Room
- Preferences ringan: DataStore
- DI: Hilt (opsional tapi direkomendasikan)
- Background task ringan: WorkManager (opsional)
- Integrasi kesehatan: Health Connect API

---

## 3) Data Model Awal

Entity minimum:
- `WorkoutSession`: id, dateStart, dateEnd, type (A/B/Cardio), notes, effortRpe.
- `ExerciseSet`: id, sessionId, exerciseName, setIndex, reps, weightKg, durationSec.
- `DailyCheckin`: date, bodyWeightKg, sleepHours, sorenessScore, steps.
- `BodyMetricsEntry` (opsional, bisa digabung ke check-in mingguan): date, weightKg nullable, waistCm nullable, hipsCm nullable, chestCm nullable, armCm nullable, notes.
- `Quest`: id, title, type (daily/weekly), targetValue, progressValue, status.
- `PlayerProgress`: date, xpTotal, level, rank, streakDays.
- `ExerciseCatalog` (opsional, sangat membantu): id, displayName, equipmentTag (mis. `HG60_HIGH_PULLEY`), `primaryMuscleTags` / `secondaryMuscleTags` (mis. `back`, `chest` — lihat `ASSETS_AND_PROMPTS.md` bagian K), sortOrder — dipakai untuk quick-pick, filter body part, dan template Day A/B.

Catatan:
- Simpan schema sederhana dulu.
- Hindari model terlalu kompleks di awal.
- Untuk HG60, isi `ExerciseCatalog` sekali (seed) dari katalog di bagian 5.0; user tetap boleh tambah nama latihan bebas saat log.

---

## 4) Rumus XP & Rank (versi aman dan simpel)

### XP Sesi
- Base XP per sesi selesai: `50`
- Bonus durasi: `+1 XP per menit` (maks 45)
- Bonus konsistensi: `+10 XP` jika streak >= 3 hari aktif (tidak harus berturut-turut berat)
- Recovery bonus: `+10 XP` jika ada cooldown/mobility tercatat

Contoh: sesi 35 menit + cooldown + streak aktif  
`50 + 35 + 10 + 10 = 105 XP`

### Level
- Level naik tiap `300 XP` (versi awal linear agar mudah dipahami)

### Rank Personal
- E -> D -> C -> B -> A berdasarkan rata-rata 4 minggu:
  - jumlah sesi per minggu
  - distribusi latihan (strength + cardio + mobility)
  - kepatuhan recovery

---

## 5) Template Program Latihan (khusus alat yang ada)

Per minggu:
- 3x strength (A/B/A lalu minggu depan B/A/B)
- 2x cardio low impact (jalan cepat 25-40 menit)
- 1-2x mobility ringan (10-15 menit)

### 5.0) Katalog Tunturi HG60 — perlu dicatat di app

**Ya, sebaiknya dicatat.** Tujuannya: template latihan, tombol quick-pick saat log, dan progress per gerakan tidak “ngawur” (misalnya mengisi latihan yang tidak bisa dilakukan di mesin).

**Stasiun / fungsi (ringkas, sesuai spesifikasi produk HG60):**
- High pulley (lat bar, ab strap, dll.)
- Chest press multifungsi
- Leg press (curl pad jadi sandaran punggung)
- Leg developer
- Low pulley (bar lurus / cloth handle)
- Biceps pad
- Shoulder press

**Stack beban:** 10 plate × **7 kg** = **70 kg** total (pilih per latihan). Di log app bisa pakai field: **jumlah plate** atau estimasi kg efektif (pilih satu konvensi dan konsisten).

**Bukan bagian HG60 (jangan masukkan sebagai “stasiun mesin”):** middle pulley, squat rack khusus — squat tetap lewat **barbel / bodyweight** di luar konfigurasi stack.

**Contoh gerakan per stasiun (isi seed app; boleh ditambah sesuai manual & kebiasaanmu):**

| Stasiun | Contoh gerakan (nama di app) |
| --- | --- |
| High pulley | Lat pulldown, Triceps pushdown, Face pull (ringan), Kneeling crunch + ab strap |
| Chest press | Chest press (mesin) |
| Shoulder press | Shoulder press (mesin) |
| Leg press | Leg press |
| Leg developer | Leg curl / leg extension (sesuai posisi unit; cek manual untuk nama tepat) |
| Low pulley | Seated row, Standing cable row, Cable curl, Paloff press (anti-rotasi ringan) |
| Biceps pad | Biceps curl (pad + dumbbell atau kabel, tergantung setup) |

**Barbel / dumbbell / bodyweight** tetap entri terpisah (bukan stasiun HG60): goblet squat, RDL, split squat, push-up, plank, jalan cepat, dll.

**Implementasi di MVP:**
- File seed statis (JSON/Kotlin list) atau tabel `ExerciseCatalog` dengan `equipmentTag`.
- Layar log: filter “**Hanya HG60**” / “**Semua**”.
- Revisi daftar sekali setelah kamu baca diagram di **user manual PDF** (nama gerakan disamakan dengan posisi bench yang kamu pakai).

Day A (Tunturi HG60 + finisher bodyweight) — sama dengan template **Day A** di app (`WorkoutCatalog`):
- Chest press (mesin HG60) 3x10-12
- Lat pulldown (high pulley HG60) 3x10-12
- Leg press (HG60) 3x10-12
- Seated cable row (low pulley HG60) 3x10-12
- Incline push-up 3x8-12

Day B (Tunturi HG60 + barbel) — sama dengan template **Day B** di app:
- Shoulder press (mesin HG60) 3x10
- Leg curl (leg developer HG60) 3x10-12
- Triceps pushdown (high pulley HG60) 3x12-15
- Romanian deadlift (barbel) ringan 3x8-10
- Biceps curl (biceps pad HG60) 3x10-12

Gerakan cadangan / variasi HG60 (quick-pick di app): leg extension, face pull, cable crunch, standing cable curl, Paloff press; plus goblet squat, split squat, plank, dead bug, jalan cepat.

Aturan keselamatan:
- Target effort `RPE 6-8` (masih ada sekitar 2–4 rep “sisa” di set berat).
- Jika nyeri sendi tajam, stop gerakan dan ganti variasi.
- Naik beban **bukan tiap sesi**; gunakan aturan di **5.1** di bawah.

### 5.1) Aturan progresi: beban, cardio, bodyweight

Tujuan: progres tanpa memaksakan tiap kali latihan. Cocok untuk kondisi overweight + full body beberapa kali seminggu.

#### Beban (mesin HG60, barbel, dumbbell)

- **Jangan naikkan beban setiap sesi.** Risiko teknik rusak dan sendi overload.
- **Double progression (disarankan):** pilih rentang rep target (misalnya 3×10–12). Jika **semua set** mencapai **atas rentang** dengan form konsisten, minggu berikutnya bisa:
  - naik **satu langkah beban** (di HG60 sering = **+1 plate 7 kg** per stack — loncatan besar; kalau terlalu berat, naikkan **rep** dulu sampai plafon rentang, atau tambah **set** ringan sebelum loncat plate), atau
  - tambah **1–2 rep** di set terakhir saja sambil beban sama.
- **Aturan “2 sesi bagus”:** jika **dua sesi berturut-turut** untuk gerakan yang sama terasa stabil (form sama, tidak ada nyeri sendi tajam), boleh pertimbangkan naik beban kecil atau naik rep/rentang.
- **Frekuensi realistis:** seringnya progres beban **sekitar 1× per minggu per gerakan**, atau lebih lambat — itu normal.
- Tetap di zona **RPE 6–8**; kalau set terakhir sudah “habis”, beban atau volume terlalu agresif.

#### Cardio (jalan cepat, dll.)

- **Tidak perlu meningkat tiap sesi.**
- **Per minggu (pilih satu, jangan semua sekaligus):**
  - tambah **5–10% total durasi** cardio minggu itu, *atau*
  - tambah **satu sesi ringan** ekstra.
- **Per sesi:** jika seminggu penuh terasa mudah, minggu depan bisa tambah **~5 menit** pada **satu** sesi dulu (bukan langsung semua sesi).
- Alternatif tanpa tambah waktu: durasi sama, **kecepatan sedikit lebih cepat** atau **segmen naik tanjakan pendek** — pilih **satu** variabel.
- Dengan **Garmin**: utamakan **denyut + sensasi** (masih bisa ngobrol = zona mudah), bukan “tiap hari harus lebih keras”.

#### Push-up dan latihan berat badan

- **Bukan tiap sesi wajib naik.**
- Urutan progresi yang rapi:
  1. **Sudut:** incline tinggi → incline rendah → lantai.
  2. **Rep per set** naik pelan (misalnya +1 rep **total** per minggu, bukan dipaksakan tiap hari).
  3. **Jumlah set** (misal 2 → 3 set) sebelum rep per set dipaksakan tinggi.
  4. **Tempo** negatif (turun 2–3 detik) untuk menambah sulit tanpa menambah rep.
- **Aturan “2 sesi mudah”:** jika dua sesi berturut-turut variasi yang sama terasa terlalu mudah, baru naik tingkat kesulitan (sudut/rep/set/tempo).

#### Deload (recovery)

- Sekitar **tiap 4–6 minggu** latihan rutin: **satu minggu deload** — turunkan volume **~30–40%** (kurangi set atau durasi cardio), intensitas tetap nyaman. Boleh di app sebagai quest khusus “Recovery Week”.

#### Ringkasan cepat (untuk referensi / logika app nanti)

| Jenis | Kapan naik? |
| --- | --- |
| Beban mesin / barbel / dumbbell | Setelah **2 sesi** form bagus di target rep, atau sekitar **1× per minggu** jika masih aman; **bukan tiap sesi** |
| Cardio | **Bertahap per minggu** (durasi atau intensitas); hindari loncatan tiap sesi |
| Push-up / BW | **Mingguan** atau setelah **2 sesi** terasa mudah pada variasi yang sama |
| Deload | **4–6 minggu** sekali, volume turun ~30–40% |

**Catatan implementasi app (opsional, fase belakang):** layar detail latihan bisa menampilkan **saran teks** berbasis aturan di atas (mis. “dua sesi terakhir stabil → pertimbangkan +1 plate atau +1 rep”), tanpa mengganti penilaian tubuhmu sendiri.

---

## 6) UI Screen Plan

1. `HomeScreen`
   - Tombol `Enter Gate`
   - Quest hari ini
   - Streak, XP, level ringkas

2. `SessionScreen`
   - Pilihan Gate Ringan (12-20m) / Normal (35-45m)
   - Log set/rep/beban cepat
   - Timer istirahat sederhana
   - **Beban default:** untuk tiap gerakan, **isi otomatis dari set terakhir** yang tersimpan di Room (bukan form kosong tiap kali buka sesi). Tombol +/- atau step cepat untuk ubah hari itu.
   - **Gerakan baru** (belum pernah ada histori): minta **satu kali** perkiraan beban aman / plate count, lalu selanjutnya selalu default dari sesi sebelumnya.
   - HG60: opsi input **jumlah plate (×7 kg)** atau **kg perkiraan** — pilih satu konvensi di Settings dan konsisten.

3. `QuestScreen`
   - Daily dan weekly quest
   - Progress bar quest

4. `ProgressScreen`
   - Grafik sesi mingguan
   - Tren berat badan dan (jika ada data) lingkar pinggang
   - Ringkasan rank dan level

5. `SettingsScreen`
   - Satuan (kg/menit)
   - Integrasi Health Connect
   - Export/import backup lokal

### 6.1) Penyimpanan beban & progres (klarifikasi alur)

- **Bukan “login”.** User menekan **Enter Gate** → masuk `SessionScreen`; tidak perlu akun.
- **Sumber kebenaran beban:** riwayat `ExerciseSet` di **Room** (per `exerciseName` atau `exerciseId`). Dari situ app menghitung “**last successful weight / rep**” untuk pre-fill.
- **Local storage:** Room untuk log; **DataStore** opsional untuk preferensi (satuan, konvensi plate vs kg, default rest timer).
- **Naik pelan:** aturan bisnis progresi ada di **5.1**; app bisa menampilkan **hint** (“2 sesi stabil → pertimbangkan +1 plate / +1 rep”), bukan auto-menaikkan beban tanpa konfirmasi user.

---

## 7) Step-by-Step Implementasi (8 Minggu)

## Minggu 1 - Setup Fondasi ✅ (dikerjakan)
- Buat project Android Compose baru → **`WorkoutLeveling/`**
- Setup package structure (`data`, `domain`, `ui`).
- Tambah Room + DataStore + Navigation.
- Buat tema UI dark mode sederhana.

Output minggu 1:
- App bisa run.
- Navigation antar 3 layar dummy (Home, Session placeholder, Progress placeholder).
- Room + entity sesi/set sudah ada; UI log menyusul Minggu 2.

**Lanjut:** Minggu 2 — form log latihan + riwayat sesi.

## Minggu 2 - Core Logging
- Implement `WorkoutSession` dan `ExerciseSet`.
- Buat form log latihan paling minimal.
- Simpan dan tampilkan riwayat sesi.
- Tambah alur singkat **baseline / check-in tubuh** (bisa dilewati): simpan `BodyMetricsEntry` atau field di check-in mingguan.

Output minggu 2:
- Bisa create, view, dan edit sesi latihan.
- Bisa mencatat baseline berat & lingkar pinggang tanpa menghalangi mulai sesi.

## Minggu 3 - Quest + XP
- Implement quest harian/mingguan lokal.
- Implement kalkulasi XP dan level.
- Tampilkan progress di Home.

Output minggu 3:
- Selesai sesi -> XP bertambah -> level update.

## Minggu 4 - Program A/B + Quick Gate
- Tambahkan template latihan Day A/Day B.
- Seed **katalog gerakan HG60** (bagian 5.0) + quick-pick saat log.
- Tambahkan mode Gate Ringan dan Gate Normal.
- Tambahkan check-in effort (RPE) dan nyeri sendi.

Output minggu 4:
- User bisa latihan tanpa isi semua data manual dari nol.
- Daftar latihan selaras dengan stasiun yang benar-benar ada di Tunturi HG60.

## Minggu 5 - Progress Dashboard
- Tambahkan statistik mingguan.
- Grafik sederhana (mis. sesi per minggu, berat badan trend).
- Buat rank personal berbasis konsistensi 4 minggu.

Output minggu 5:
- Dashboard progres bisa dipakai evaluasi mingguan.

## Minggu 6 - Health Connect (Opsional Bertahap)
- Integrasi langkah dan menit aktif.
- Map data ke Cardio XP / quest otomatis.
- Buat fallback jika data HC tidak tersedia.

Output minggu 6:
- Sebagian quest dapat auto-complete dari data health.

## Minggu 7 - Stabilitas & UX
- Poles UI/UX agar input cepat.
- Tambahkan validasi input.
- Tambahkan empty states dan error handling.

Output minggu 7:
- App nyaman dipakai harian.

## Minggu 8 - Rilis Pribadi
- Testing manual 7 hari simulasi.
- Perbaiki bug prioritas tinggi.
- Build APK debug/release untuk HP pribadi.

Output minggu 8:
- APK siap dipakai rutin.

---

## 8) Checklist Testing Minimum

- Buat sesi latihan baru -> data tersimpan.
- Edit/hapus sesi -> statistik ikut berubah.
- Quest harian reset sesuai tanggal.
- XP tidak dobel saat back/refresh.
- App tetap jalan setelah force close.
- Data tetap ada setelah restart HP.

---

## 9) Offline, Cloud, dan VPS

Versi sekarang:
- Offline total: YA
- VPS: TIDAK perlu
- LLM: TIDAK perlu

Future (opsional):
- Cloud backup (Firebase/Supabase) jika butuh sync antar device.

---

## 10) Next Action (langsung eksekusi)

Urutan kerja yang disarankan sekarang:
1. ~~Buat project Android Compose baru.~~ → **Sudah:** `WorkoutLeveling/`
2. ~~Room entities + DAO~~ → **Sudah** (Minggu 1); Minggu 2: wiring UI + insert sesi/set.
3. ~~`HomeScreen` minimal~~ → **Sudah**; kembangkan `SessionScreen` jadi form log.
4. Implement flow `Enter Gate -> Log Sesi -> XP Bertambah` (Minggu 2–3).
5. Pakai 1 minggu, lalu evaluasi friction sebelum tambah fitur baru.

