package com.workoutleveling.app.domain.catalog

/**
 * Katalog & template diselaraskan dengan **Tunturi Home Gym HG60**:
 * chest press, shoulder press, high/low pulley, leg press, leg developer, biceps pad.
 * Plus barbel/dumbbell/bodyweight yang kamu punya di luar stack.
 *
 * [dos] / [donts] = tips form singkat (bukan pengganti pelatih profesional).
 */
data class CatalogExercise(
    val displayName: String,
    val suggestedReps: String? = null,
    val info: String,
    val dos: List<String> = emptyList(),
    val donts: List<String> = emptyList(),
)

object WorkoutCatalog {

    val dayA: List<CatalogExercise> = listOf(
        CatalogExercise(
            displayName = "Chest press (mesin HG60)",
            suggestedReps = "10",
            info = "Otot: dada, triceps, bahu depan. Memperkuat dorongan horizontal & membantu aktivitas sehari-hari (dorong pintu, angkat barang).",
            dos = listOf(
                "Sandaran punggung menempel stabil; kaki menapak.",
                "Kontrol turun 2–3 detik, dorong tanpa loncat momentum.",
                "Siku ~45° dari tubuh, pergelangan netral.",
            ),
            donts = listOf(
                "Membungkukkan dada dari sandaran.",
                "Mengunci siku kaku atau memaksa rentang jika bahu/siku nyeri.",
                "Melepas napas terus-menerus saat dorong berat.",
            ),
        ),
        CatalogExercise(
            displayName = "Lat pulldown (high pulley HG60)",
            suggestedReps = "10",
            info = "Otot: latissimus, punggung atas. Memperkuat tarikan & postur bahu; membantu menyeimbangkan otot dada.",
            dos = listOf(
                "Dada sedikit angkat, dada ringan ke arah bar.",
                "Tarik ke dada atas / atas dada, siku ke bawah.",
                "Rentang nyaman; jangan paksa bawah jika bahu tidak suka.",
            ),
            donts = listOf(
                "Menarik dengan ayunan tubuh besar.",
                "Menarik ke belakang leher (risiko leher/bahu).",
                "Mengerutkan leher atau menggigit terlalu keras.",
            ),
        ),
        CatalogExercise(
            displayName = "Leg press (HG60)",
            suggestedReps = "10",
            info = "Otot: paha depan, glute, sedikit paha belakang. Beban terkontrol untuk kaki kuat tanpa beban penuh di punggung seperti squat berat.",
            dos = listOf(
                "Telapak kaki rata; lutut mengikuti arah jari kaki.",
                "Turun terkontrol; hentak di bawah yang ringan saja.",
                "Sesuaikan ROM dengan nyaman lutut & pinggul.",
            ),
            donts = listOf(
                "Mengunci lutut keras di atas (hyperextension).",
                "Angkat pinggul dari sandaran (butt wink berlebihan).",
                "Plate terlalu berat sampai form hancur.",
            ),
        ),
        CatalogExercise(
            displayName = "Seated cable row (low pulley HG60)",
            suggestedReps = "10",
            info = "Otot: punggung tengah, bisep, rear delt ringan. Memperkuat postur dada terbuka & tarikan horizontal.",
            dos = listOf(
                "Dada ringan terbuka; bahu turun (jangan naik ke telinga).",
                "Tarik ke arah perut bawah / tulang iga bawah.",
                "Gerakan dari belikat, bukan hanya lengan.",
            ),
            donts = listOf(
                "Membungkuk besar lalu mengayun.",
                "Menarik tinggi ke dada dengan siku terlalu naik.",
                "Memutar punggung untuk cheat rep.",
            ),
        ),
        CatalogExercise(
            displayName = "Incline push-up",
            suggestedReps = "10",
            info = "Otot: dada atas, triceps, core ringan. Variasi lebih ramah sendi daripada push-up lantai penuh.",
            dos = listOf(
                "Badan satu garis lutut–pundak (atau badan penuh jika toes).",
                "Turun dada mendekati permukaan; siku ~45°.",
                "Perut kencang ringan agar pinggul tidak ambruk.",
            ),
            donts = listOf(
                "Pinggul melambung tinggi (puncak gunung).",
                "Kepala menjulur; jaga leher netral.",
                "Rentang terlalu dalam jika pergelangan tangan tidak nyaman.",
            ),
        ),
    )

    val dayB: List<CatalogExercise> = listOf(
        CatalogExercise(
            displayName = "Shoulder press (mesin HG60)",
            suggestedReps = "10",
            info = "Otot: deltoid, triceps. Memperkuat angkat di atas kepala & stabilitas bahu (dengan rentang aman & beban progresif).",
            dos = listOf(
                "Sandaran & kursi stabil; core ringan aktif.",
                "Dorong vertikal nyaman; tidak memaksa di atas kepala jika kaku.",
                "Turun terkontrol; napas teratur.",
            ),
            donts = listOf(
                "Membungkuk atau membusungkan punggung berlebihan.",
                "Arch punggung besar untuk cheat.",
                "Melanjutkan jika nyeri tajam di bahu.",
            ),
        ),
        CatalogExercise(
            displayName = "Leg curl (leg developer HG60)",
            suggestedReps = "10",
            info = "Otot: hamstring. Melengkapi leg press (depan) agar kaki tidak hanya kuat di bagian depan.",
            dos = listOf(
                "Bantal di atas pergelangan kaki sesuai unit.",
                "Gerakkan dari hamstring; jangan lompat pinggul besar.",
                "Rentang penuh nyaman tanpa nyeri lutut belakang.",
            ),
            donts = listOf(
                "Mengangkat pinggul dari bangku untuk cheat.",
                "Memaksa beban dengan ayunan kencang.",
                "Meneruskan jika ada nyeri tajam di lutut.",
            ),
        ),
        CatalogExercise(
            displayName = "Triceps pushdown (high pulley HG60)",
            suggestedReps = "12",
            info = "Otot: triceps. Melengkapi gerakan dorong; membantu stabilitas siku & lengan saat dorong.",
            dos = listOf(
                "Siku tetap dekat sisi tubuh (sedikit ke depan boleh).",
                "Tekan ke bawah dengan menggerakkan siku, bukan bahu.",
                "Berdiri tegak; perut ringan kencang.",
            ),
            donts = listOf(
                "Mengayun bahu & badan untuk rep tambahan.",
                "Membiarkan siku flare lebar tidak terkontrol.",
                "Memaksa lockout jika siku tidak nyaman.",
            ),
        ),
        CatalogExercise(
            displayName = "Romanian deadlift (barbel)",
            suggestedReps = "8",
            info = "Otot: hamstring, glute, punggung bawah (isometrik). Melatih pinggul hinge — penting untuk angkat aman & postur.",
            dos = listOf(
                "Bar dekat kaki; dorong pinggul belakang, lutut sedikit fleksi.",
                "Dada ringan ke atas; punggung netral.",
                "Beban ringan dulu sampai pola hinge konsisten.",
            ),
            donts = listOf(
                "Membulatkan punggung (rounded) saat turun.",
                "Bar menjauh dari kaki (beban ke punggung).",
                "ROM berlebihan jika hamstring kaku — turun sedikit dulu.",
            ),
        ),
        CatalogExercise(
            displayName = "Biceps curl (biceps pad HG60)",
            suggestedReps = "10",
            info = "Otot: biceps, forearm. Isolasi lengan bawah dengan posisi stabil di pad.",
            dos = listOf(
                "Lengan atas tetap di pad; hanya siku yang bergerak.",
                "Turun terkontrol; jangan jatuhkan beban.",
                "Pergelangan netral atau sedikit pronasi sesuai nyaman.",
            ),
            donts = listOf(
                "Mengayun pinggul atau mengangkat siku dari pad.",
                "Memaksa beban dengan memiringkan tubuh.",
                "Hyperextension siku di bawah.",
            ),
        ),
    )

    val cardio: List<CatalogExercise> = listOf(
        CatalogExercise(
            displayName = "Jalan cepat",
            suggestedReps = null,
            info = "Kardio berimpact rendah. Membantu kalori, jantung & kebiasaan bergerak; cocok overweight dengan progresi durasi/intensitas pelan.",
            dos = listOf(
                "Postur tegak; langkah ritmis.",
                "Zona nyaman napas (bisa ngomong kalimat pendek).",
                "Naik durasi/intensitas pelan per minggu.",
            ),
            donts = listOf(
                "Memaksakan pace sampai tidak bisa bicara sama sekali setiap sesi.",
                "Menyiksa sendi: kurangi jika lutut tidak nyaman.",
                "Skip pemanasan & pendinginan total.",
            ),
        ),
    )

    private val extraHg60AndAccessories: List<CatalogExercise> = listOf(
        CatalogExercise(
            displayName = "Leg extension (leg developer HG60)",
            suggestedReps = "10",
            info = "Otot: quadriceps. Alternatif atau tambahan ke leg press untuk kaki depan.",
            dos = listOf(
                "Sandaran punggung stabil; pegangan ringan.",
                "Angkat terkontrol; jeda singkat di atas tanpa mengunci keras.",
                "Rentang nyaman lutut.",
            ),
            donts = listOf(
                "Mengunci lutut keras + beban berat.",
                "Ayunan tubuh untuk angkat beban.",
                "Melanjutkan jika lutut depan nyeri tajam.",
            ),
        ),
        CatalogExercise(
            displayName = "Face pull (high pulley HG60)",
            suggestedReps = "15",
            info = "Otot: rear delt, rotator cuff, punggung atas ringan. Baik untuk keseimbangan bahu & postur dada.",
            dos = listOf(
                "Tarik tali ke arah wajah/dada atas; siku tinggi lebar.",
                "Peras belakang bahu; beban ringan–sedang.",
                "Kontrol eccentrik.",
            ),
            donts = listOf(
                "Menggunakan beban terlalu berat sampai form rusak.",
                "Menarik ke bawah dagu dengan siku rendah.",
                "Membungkuk besar dari inti tubuh.",
            ),
        ),
        CatalogExercise(
            displayName = "Kneeling cable crunch (high pulley HG60)",
            suggestedReps = "12",
            info = "Otot: abs (fleksi). Melengkapi plank; kontrol perut tanpa lompat.",
            dos = listOf(
                "Gerakan dari flexi thoracic; perut yang melengkung.",
                "Napas keluar saat crunch.",
                "Rentang pendek–sedang terkontrol.",
            ),
            donts = listOf(
                "Menarik dengan lengan saja tanpa perut.",
                "Memaksa pinggul maju mundur besar.",
                "Leher ditarik dengan tangan.",
            ),
        ),
        CatalogExercise(
            displayName = "Standing cable curl (low pulley HG60)",
            suggestedReps = "10",
            info = "Otot: biceps. Variasi tanpa biceps pad.",
            dos = listOf(
                "Siku di sisi tubuh; gerakkan hanya siku bawah.",
                "Berdiri stabil; elakan ayunan pinggul.",
                "Turun 2–3 detik.",
            ),
            donts = listOf(
                "Ayunan untuk momentum.",
                "Membawa siku maju besar.",
                "Memutar punggung.",
            ),
        ),
        CatalogExercise(
            displayName = "Paloff press (low pulley HG60)",
            suggestedReps = "10",
            info = "Otot: core anti-rotasi. Memperkuat stabilitas batang tubuh; membantu semua angkat lain.",
            dos = listOf(
                "Berdiri tegak; kabel di samping tubuh.",
                "Tekan lurus ke depan; tahan rotasi.",
                "Bernapas; jangan menahan napas terlalu lama.",
            ),
            donts = listOf(
                "Membiarkan tubuh berputar mengikuti kabel.",
                "Kaki sempit tidak stabil.",
                "Beban besar sampai tubuh miring.",
            ),
        ),
        CatalogExercise(
            displayName = "Goblet squat (dumbbell)",
            suggestedReps = "8",
            info = "Otot: paha, glute, core. Squat dengan beban depan membantu postur; mudah dikontrol untuk pemula.",
            dos = listOf(
                "Dumbbell di dada; siku ke bawah.",
                "Turun duduk antara paha; lutut mengikuti kaki.",
                "Tumit tetap nempel jika memungkinkan.",
            ),
            donts = listOf(
                "Membulatkan punggung bawah berlebihan.",
                "Lutut runtuh ke dalam keras tanpa kontrol.",
                "Memaksa paralel jika sendi tidak siap.",
            ),
        ),
        CatalogExercise(
            displayName = "Split squat (dibantu)",
            suggestedReps = "8",
            info = "Otot: paha & glute per kaki. Melatih keseimbangan & kuat satu kaki dengan beban lebih ringan.",
            dos = listOf(
                "Kaki depan sebagai kerja utama; torso tegak.",
                "Turun vertikal; lutut depan tidak jungkir ke dalam.",
                "Pegangan untuk keseimbangan boleh.",
            ),
            donts = listOf(
                "Lutut depan melampaui jari terlalu jauh tanpa kontrol.",
                "Torso ambruk ke depan.",
                "Melompat atau berpindah kaki tidak stabil.",
            ),
        ),
        CatalogExercise(
            displayName = "Plank (detik di kolom reps)",
            suggestedReps = "30",
            info = "Otot: core stabil. Isometrik; isi kolom reps sebagai detik tahan.",
            dos = listOf(
                "Siku di bawah bahu; badan garis lurus.",
                "Perut & bokong sedikit kencang.",
                "Napas halus; pandangan ke lantai.",
            ),
            donts = listOf(
                "Pinggul naik atau ambruk.",
                "Menahan napas terlalu lama.",
                "Memaksa durasi sampai pinggul sakit.",
            ),
        ),
        CatalogExercise(
            displayName = "Dead bug",
            suggestedReps = "10",
            info = "Otot: core, kontrol pinggul. Gerakan aman untuk memperkuat perut tanpa beban punggung berlebih.",
            dos = listOf(
                "Pinggul menempel lantai; punggung bawah netral ringan.",
                "Gerak lambat; lawan lengan/kaki berlawanan.",
                "Napas stabil.",
            ),
            donts = listOf(
                "Membiarkan punggung melengkung besar dari lantai.",
                "Mempercepat rep sampai tubuh goyang.",
                "Leher tegang menjulur.",
            ),
        ),
    )

    private val allEntries: List<CatalogExercise> =
        dayA + dayB + cardio + extraHg60AndAccessories

    val allNames: List<String> =
        allEntries.map { it.displayName }.distinct().sorted()

    fun suggestionsForQuery(query: String, limit: Int = 12): List<String> {
        val q = query.trim()
        if (q.isEmpty()) return allNames.take(limit)
        return allNames.filter { it.contains(q, ignoreCase = true) }.take(limit)
    }

    fun suggestedRepsForName(name: String): String? =
        allEntries.firstOrNull { it.displayName.equals(name.trim(), ignoreCase = true) }?.suggestedReps

    fun infoForName(name: String): String? =
        entryForName(name)?.info

    fun entryForName(name: String): CatalogExercise? {
        val n = name.trim()
        if (n.isEmpty()) return null
        return allEntries.firstOrNull { it.displayName.equals(n, ignoreCase = true) }
    }
}
