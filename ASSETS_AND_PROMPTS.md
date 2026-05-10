# Daftar aset + prompt (copy-paste untuk ChatGPT / generator gambar)

Panduan singkat:
- Generate **satu batch dalam satu percakapan** kalau bisa, supaya gaya konsisten.
- Tempel **MASTER STYLE** di awal, lalu untuk tiap gambar tempel **prompt spesifik** di bawahnya (atau gabung jadi satu prompt panjang per gambar).
- Simpan file dengan nama yang disarankan (folder misalnya `app/src/main/res/drawable-xxhdpi/` atau `assets/images/`).
- Resolusi disarankan: **1024×1024** atau **1344×768** (landscape untuk ilustrasi gerakan); untuk ikon **1024×1024** lalu resize ke mdpi/hdpi/xhdpi di Android Studio.

---

## MASTER STYLE (tempel sekali, atau gabung ke setiap prompt di bawah)

```
STYLE LOCK (use for every image in this project):
Flat vector illustration, dark fantasy RPG "gate" mood without any copyrighted characters or logos. Background solid or very dark gradient #0B0F14 to #121A24. Accent colors: electric cyan #22D3EE and soft violet #A78BFA, subtle magical glow only (no busy particles). Thin clean white outline on the subject, minimalist, no text, no watermark, no UI mockup, no brand names, no readable letters. Single stylized human figure, faceless or simple silhouette face, consistent body proportions across all images. Full body or 3/4 view, gym equipment must look realistic and mechanically plausible. High contrast, mobile-app asset, crisp edges, not photorealistic, not 3D render.
```

---

## A) Launcher, splash, home

### A1 — `ic_launcher_foreground.png` (ikon app)

**Prompt (gabungkan dengan MASTER STYLE):**

```
Create an app icon illustration: a minimalist glowing portal / gate symbol, hexagonal or arched frame, cyan and violet inner glow, dark background, centered, lots of padding, works as circular mask crop, no text, vector flat style, STYLE LOCK as above.
```

### A2 — `splash_background.png`

**Prompt:**

```
Abstract dark background for mobile splash screen, subtle vertical light rays through a gate silhouette, colors cyan and violet on near-black, very minimal, no characters, no text, 9:16 aspect ratio, STYLE LOCK as above.
```

### A3 — `home_bg_gate.png` (latar home lembut)

**Prompt:**

```
Dark atmospheric gym void background, faint gate silhouette far away, soft cyan rim light, very subtle grid or floor lines, empty space in center for UI buttons, 9:16, STYLE LOCK as above.
```

### A4 — `illu_gate_enter.png` (tombol / hero "Enter Gate")

**Prompt:**

```
A closed magical gate in a dark training hall, stone or metal frame with cyan cracks of energy, mysterious violet mist at bottom, no people, centered composition, 4:5 aspect ratio, STYLE LOCK as above.
```

### A5 — `illu_gate_clear.png` (setelah sesi / clear)

**Prompt:**

```
The same gate style now fully open with bright cyan light bursting forward, energy particles minimal, triumphant but clean, no people, 4:5 aspect ratio, STYLE LOCK as above.
```

---

## B) Ikon kecil (chip & tab) — bisa 1 batch

**Prompt satu batch (minta 4 ikon terpisah atau 1 sheet lalu split):**

```
Create four separate small icons on transparent background (or dark #0B0F14), 512x512 each, simple glyph style matching STYLE LOCK:
1) XP crystal shard, cyan glow
2) Streak flame or chain link, violet accent
3) Quest scroll or stone tablet
4) Rank chevron badge
No text, thick readable shapes, STYLE LOCK as above.
```

Nama file disarankan:
- `icon_xp.png`
- `icon_streak.png`
- `icon_quest.png`
- `icon_rank.png`

---

## C) Empty states

### C1 — `empty_no_sessions.png`

**Prompt:**

```
Empty state illustration: dark gym with a single closed gate and a folded training mat, lonely but hopeful, small cyan accent, lots of negative space, 4:3, STYLE LOCK as above.
```

### C2 — `empty_no_quests.png`

**Prompt:**

```
Empty state: stone quest board with blank slots, faint violet glow, no text on board, 4:3, STYLE LOCK as above.
```

---

## D) Rank badges (opsional, 5 file)

**Prompt (ulangi ganti {RANK} dan deskripsi warna):**

```
Rank badge icon for mobile app, circular metal emblem, center empty for number overlay, subtle glow, rank theme: {RANK}, no text, 512x512, STYLE LOCK as above.
```

Ganti `{RANK}`:
- `E` — abu-abu dingin, retak kecil
- `D` — hijau mint samar
- `C` — biru cyan
- `B` — violet
- `A` — emas sangat minimal (jangan terlalu bling)

Nama file: `rank_e.png` … `rank_a.png`

---

## E) Ilustrasi gerakan — Tunturi HG60 & latihan kamu

**Untuk setiap baris di bawah:** tempel MASTER STYLE + prompt baris tersebut.

Format file: `ex_<nama>.webp` atau `.png`, rasio **4:3**, subjek di tengah.

| File | Prompt spesifik (setelah MASTER STYLE) |
| --- | --- |
| `ex_hg60_lat_pulldown.png` | `One person doing lat pulldown at a high pulley cable machine, straight bar, seated, elbows tracking down, cable stack visible, side view, 4:3, STYLE LOCK as above.` |
| `ex_hg60_triceps_pushdown.png` | `One person doing triceps pushdown at high pulley, elbows pinned to sides, rope or straight bar attachment, side view, 4:3, STYLE LOCK as above.` |
| `ex_hg60_cable_crunch.png` | `One person kneeling cable crunch using high pulley with rope or strap, hands beside head, abs crunch motion, side view, 4:3, STYLE LOCK as above.` |
| `ex_hg60_face_pull.png` | `One person doing face pull with cable at upper pulley, upper arms horizontal, external rotation emphasis, front 3/4 view, 4:3, STYLE LOCK as above.` |
| `ex_hg60_pallof_press.png` | `One person half-kneeling anti-rotation pallof press with cable from side stack, hands pressing forward, side view, 4:3, STYLE LOCK as above.` |
| `ex_hg60_chest_press.png` | `One person using seated chest press machine, handles at chest level, neutral spine, side view, 4:3, STYLE LOCK as above.` |
| `ex_hg60_shoulder_press.png` | `One person using plate-loaded or selectorized shoulder press machine, elbows bent 90, pressing upward, side view, 4:3, STYLE LOCK as above.` |
| `ex_hg60_leg_press.png` | `One person on leg press machine, feet on platform, knees flexed 90, back on pad, side view, 4:3, STYLE LOCK as above.` |
| `ex_hg60_leg_extension.png` | `One person seated leg extension, shin pad on lower legs, knees bending from 90 to extension, side view, 4:3, STYLE LOCK as above.` |
| `ex_hg60_leg_curl.png` | `One person prone or seated leg curl machine, curling heels toward glutes, hamstring emphasis, side view, 4:3, STYLE LOCK as above.` |
| `ex_hg60_seated_row.png` | `One person seated cable row from low pulley, torso upright, pulling handle to lower ribs, side view, 4:3, STYLE LOCK as above.` |
| `ex_hg60_standing_curl.png` | `One person standing cable curl from low pulley, supinated grip, elbows stable, side view, 4:3, STYLE LOCK as above.` |
| `ex_hg60_biceps_pad_curl.png` | `One person preacher-style biceps curl using padded arm support and dumbbell or cable, upper arm on pad, side view, 4:3, STYLE LOCK as above.` |

---

## F) Barbell, dumbbell, bodyweight, cardio

| File | Prompt spesifik (setelah MASTER STYLE) |
| --- | --- |
| `ex_db_goblet_squat.png` | `One person goblet squat holding one dumbbell vertically at chest, elbows in, depth to parallel, front 3/4 view, 4:3, STYLE LOCK as above.` |
| `ex_bb_romanian_deadlift.png` | `One person Romanian deadlift with barbell, slight knee bend, hips hinged back, neutral spine, bar close to legs, side view, 4:3, STYLE LOCK as above.` |
| `ex_db_shoulder_press.png` | `One person seated dumbbell shoulder press, dumbbells at ear level, pressing up, side view, 4:3, STYLE LOCK as above.` |
| `ex_bw_incline_pushup.png` | `One person incline push-up hands on bench, body straight plank line, side view, 4:3, STYLE LOCK as above.` |
| `ex_bw_pushup.png` | `One person standard push-up, hands under shoulders, side view, 4:3, STYLE LOCK as above.` |
| `ex_bw_squat.png` | `One person bodyweight squat, arms forward for balance, depth comfortable, side view, 4:3, STYLE LOCK as above.` |
| `ex_bw_split_squat.png` | `One person static split squat rear foot elevated optional, front knee tracks over mid-foot, side view, 4:3, STYLE LOCK as above.` |
| `ex_bw_plank.png` | `One person front plank on forearms, straight line head to heels, side view, 4:3, STYLE LOCK as above.` |
| `ex_bw_dead_bug.png` | `One person dead bug core exercise on floor, 90-90 hips and knees, opposite arm and leg extending, top-down angled view, 4:3, STYLE LOCK as above.` |
| `ex_cardio_brisk_walk.png` | `One person brisk walking outdoors silhouette, dynamic simple motion lines minimal, park path, 4:3, STYLE LOCK as above.` |

---

## G) Mode ChatGPT: **satu gambar per request** (disarankan)

Banyak alur image di ChatGPT memang **tidak bisa keluar 23 file sekaligus**. Pakai pola ini:

1. **Pakai satu thread/chat panjang** untuk semua gerakan yang sama “serial”-nya, supaya model bisa mengingat gaya (tidak wajib, tapi membantu).
2. Di **setiap** request gambar baru, tempel lagi **MASTER STYLE** (bagian paling atas file ini). Itu yang paling stabil untuk konsistensi.
3. Lalu tempel **tepat satu** blok dari **G.2** (heading `### 01` … `### 23`). Jangan gabung beberapa heading dalam satu pesan.
4. Opsional: setelah gambar pertama jadi, untuk gambar berikutnya tambahkan kalimat: `Match the character body proportions and line art style of the previous exercise image in this chat.`
5. Kalau MASTER terlalu panjang untukmu, boleh pakai **COMPACT STYLE** (lebih ringkas, risiko beda gaya sedikit lebih besar):

```
COMPACT STYLE (tempel di setiap request, lalu blok G.2):
Flat vector fitness illustration, dark background #0B0F14, cyan #22D3EE and violet #A78BFA accent glow, thin white outline, single faceless human figure, not photorealistic, no text, no watermark, 4:3 aspect ratio, gym equipment plausible.
```

### G.2 Blok siap salin — **satu blok = satu generate**

Aturan salin: untuk tiap nomor, isi pesanmu = **MASTER STYLE (dari atas)** + baris **Filename** + baris **Scene** + (opsional) kalimat match previous image.

#### 01
```
Filename: ex_hg60_lat_pulldown.png
Scene: One person doing lat pulldown at a high pulley cable machine, straight bar, seated, elbows tracking down, cable stack visible, side view, 4:3.
```

#### 02
```
Filename: ex_hg60_triceps_pushdown.png
Scene: One person doing triceps pushdown at high pulley, elbows pinned to sides, rope or straight bar attachment, side view, 4:3.
```

#### 03
```
Filename: ex_hg60_cable_crunch.png
Scene: One person kneeling cable crunch using high pulley with rope or strap, hands beside head, abs crunch motion, side view, 4:3.
```

#### 04
```
Filename: ex_hg60_face_pull.png
Scene: One person doing face pull with cable at upper pulley, upper arms horizontal, external rotation emphasis, front 3/4 view, 4:3.
```

#### 05
```
Filename: ex_hg60_pallof_press.png
Scene: One person half-kneeling anti-rotation pallof press with cable from side stack, hands pressing forward, side view, 4:3.
```

#### 06
```
Filename: ex_hg60_chest_press.png
Scene: One person using seated chest press machine, handles at chest level, neutral spine, side view, 4:3.
```

#### 07
```
Filename: ex_hg60_shoulder_press.png
Scene: One person using selectorized shoulder press machine, elbows bent 90, pressing upward, side view, 4:3.
```

#### 08
```
Filename: ex_hg60_leg_press.png
Scene: One person on leg press machine, feet on platform, knees flexed 90, back on pad, side view, 4:3.
```

#### 09
```
Filename: ex_hg60_leg_extension.png
Scene: One person seated leg extension, shin pad on lower legs, knees bending from 90 to extension, side view, 4:3.
```

#### 10
```
Filename: ex_hg60_leg_curl.png
Scene: One person prone or seated leg curl machine, curling heels toward glutes, hamstring emphasis, side view, 4:3.
```

#### 11
```
Filename: ex_hg60_seated_row.png
Scene: One person seated cable row from low pulley, torso upright, pulling handle to lower ribs, side view, 4:3.
```

#### 12
```
Filename: ex_hg60_standing_curl.png
Scene: One person standing cable curl from low pulley, supinated grip, elbows stable, side view, 4:3.
```

#### 13
```
Filename: ex_hg60_biceps_pad_curl.png
Scene: One person preacher-style biceps curl using padded arm support and dumbbell or cable, upper arm on pad, side view, 4:3.
```

#### 14
```
Filename: ex_db_goblet_squat.png
Scene: One person goblet squat holding one dumbbell vertically at chest, elbows in, depth to parallel, front 3/4 view, 4:3.
```

#### 15
```
Filename: ex_bb_romanian_deadlift.png
Scene: One person Romanian deadlift with barbell, slight knee bend, hips hinged back, neutral spine, bar close to legs, side view, 4:3.
```

#### 16
```
Filename: ex_db_shoulder_press.png
Scene: One person seated dumbbell shoulder press, dumbbells at ear level, pressing up, side view, 4:3.
```

#### 17
```
Filename: ex_bw_incline_pushup.png
Scene: One person incline push-up hands on bench, body straight plank line, side view, 4:3.
```

#### 18
```
Filename: ex_bw_pushup.png
Scene: One person standard push-up, hands under shoulders, side view, 4:3.
```

#### 19
```
Filename: ex_bw_squat.png
Scene: One person bodyweight squat, arms forward for balance, depth comfortable, side view, 4:3.
```

#### 20
```
Filename: ex_bw_split_squat.png
Scene: One person static split squat, rear foot elevated optional, front knee tracks over mid-foot, side view, 4:3.
```

#### 21
```
Filename: ex_bw_plank.png
Scene: One person forearm front plank, straight line head to heels, side view, 4:3.
```

#### 22
```
Filename: ex_bw_dead_bug.png
Scene: One person dead bug core exercise on floor, 90-90 hips and knees, opposite arm and leg extending, top-down angled view, 4:3.
```

#### 23
```
Filename: ex_cardio_brisk_walk.png
Scene: One person brisk walking outdoors, simple park path, minimal motion lines, side view, 4:3.
```

---

## H) Checklist jumlah file

| Kategori | Jumlah |
| --- | ---: |
| A Launcher/home | 5 |
| B Ikon chip | 4 |
| C Empty | 2 |
| D Rank (opsional) | 5 |
| E HG60 | 13 |
| F Free weights / BW / cardio | 10 |
| **Total tanpa rank** | **34** |
| **Total dengan rank** | **39** |

---

## I) Catatan legal & kualitas

- Jangan minta merek, logo gym, atau karakter dari IP orang lain.
- Setelah generate, **periksa anatomi** (lutut, punggung); salah generate bisa menyesatkan untuk latihan.
- Untuk **leg curl vs leg extension**, kalau di rumahmu hanya salah satu yang nyaman, hapus file yang tidak dipakai dari project.

---

## J) Body part / otot: **perlu aset tubuh atau tidak?**

**Tidak wajib.** Cara paling ringan: di database tiap latihan punya **tag otot** (enum/string), lalu di UI tampilkan **chip teks** saja, misalnya `Dada`, `Punggung`, `Kaki`, `Bahu`, `Lengan`, `Core`, `Kardio`. Itu sudah cukup untuk filter, ringkasan mingguan (“minggu ini punggung 2×”), dan quest.

**Kapan aset “tubuh” berguna:** kalau kamu mau layar detail latihan yang kerasa “game” — siluet depan/belakang + area yang bekerja disorot (warna cyan/violet).

### J.1 Dua mode aset (pilih satu)

| Mode | Aset gambar | Di app |
| --- | --- | --- |
| **A — Hanya data (MVP)** | Tidak perlu gambar tubuh | `primaryMuscles` + `secondaryMuscles` (lihat **K**), tampil chip |
| **B — Siluet + sorot di kode** | 2 file: depan + belakang (garis luar tubuh saja) | Di Compose/Android: **overlay** semi-transparan di atas area kasar (persegi/path sederhana) per grup otot. Paling fleksibel, tidak perlu 1 gambar per otot. |
| **C — Banyak gambar** | 1 gambar per kombinasi sorot | Cepat kaku, file banyak, tidak disarankan. |

Disarankan: **Mode A** dulu, lalu tambah **Mode B** kalau kamu sudah bosan chip saja.

### J.2 Prompt siluet tubuh (Mode B, **satu gambar per request**)

Tempel **MASTER STYLE** atau **COMPACT STYLE** dari bagian **G**, lalu tempel salah satu blok:

**`illu_body_front_outline.png`**
```
Filename: illu_body_front_outline.png
Scene: Front view athletic human silhouette for muscle map UI, faceless, neutral standing, arms slightly away from body, simple clean outer contour only, no internal muscle drawing, no labels, no text, dark background #0B0F14, thin white outline, flat vector, full body head to feet, vertical 3:5 aspect ratio, lots of padding for UI overlays.
```

**`illu_body_back_outline.png`**
```
Filename: illu_body_back_outline.png
Scene: Back view same character style as front silhouette, same proportions, arms slightly away, simple outer contour only, no labels, dark background #0B0F14, thin white outline, flat vector, vertical 3:5, padding for overlays.
```

Tips implementasi Mode B:
- Generate **dulu** `illu_body_front_outline`, lalu untuk gambar belakang tambahkan: `Match body proportions and line weight of the previous front silhouette in this chat.`
- Sorotan otot jangan dari AI per zona (susah konsisten); lebih baik **tint di atas layout** (Compose) dengan `Box` + `Modifier.background` atau `Canvas` path kasar.

### J.3 Ikon kecil per grup otot (opsional, ganti chip)

Kalau mau ikon konsisten alih-alih teks saja, generate **satu per satu** (sama seperti gerakan):

| File | Makna |
| --- | --- |
| `muscle_chest.png` | Ikon dada / press |
| `muscle_back.png` | Ikon punggung / tarik |
| `muscle_legs.png` | Ikon kaki |
| `muscle_shoulders.png` | Ikon bahu |
| `muscle_arms.png` | Ikon lengan |
| `muscle_core.png` | Ikon core / perut |
| `muscle_cardio.png` | Ikon jalan/lari |

**Prompt template (ganti {NAME}):**
```
Filename: muscle_{NAME}.png
Scene: Minimal flat glyph icon for fitness UI, single symbol suggesting {NAME} muscle group, cyan and violet accent on dark #0B0F14, no text, 512x512, thick readable shapes, same style as other app icons.
```

---

## K) Daftar tag otot (untuk `ExerciseCatalog` / Room)

Gunakan string stabil (English snake_case atau enum Kotlin) supaya mudah filter:

| `muscle_tag` | Label UI (ID contoh) |
| --- | --- |
| `chest` | Dada |
| `back` | Punggung |
| `legs` | Kaki |
| `shoulders` | Bahu |
| `arms` | Lengan (biceps/triceps) |
| `core` | Core |
| `glutes` | Glute / pinggul (opsional dipisah dari `legs`) |
| `cardio` | Kardio |
| `full_body` | Seluruh tubuh ringan |

Tiap latihan: **1–2 primary**, **0–2 secondary**. Contoh: lat pulldown → primary `back`, secondary `arms`.

---

## L) Update checklist (tambahan body)

| Tambahan | Jumlah |
| --- | ---: |
| Siluet depan + belakang (Mode B) | 2 |
| Ikon grup otot (opsional) | 7 |
| **Tanpa body map (Mode A)** | **0** |
