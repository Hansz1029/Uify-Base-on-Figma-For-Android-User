# Integrasi Room Database, Homescreen Dashboard & Topbar Floating Capsule iOS AMOLED

Integrasi Room Database untuk persistensi lokal berkas proyek `.uify` (termasuk metadata layer dan posisi objek), penambahan Homescreen Dashboard interaktif berbasis HTML bawaan pengguna dengan render engine native Android yang optimal, pembaruan **Topbar menjadi Floating Capsule ala iOS AMOLED**, penyembunyian kontrol visual autosave/save agar layar bersih, penggantian logo Figma dengan logo vektor SVG Uinify monokrom putih, serta navigasi mulus dua arah antara Dashboard dan Studio Editor.

## User Review & Critical Decisions

> [!IMPORTANT]
> Konfirmasi keputusan desain dan fitur berdasarkan instruksi Anda:
> 1. **Topbar Floating Capsule iOS AMOLED**: Bilah atas diubah dari header statis menempel menjadi kapsul melayang (*floating pill*) berwarna hitam AMOLED pekat dengan *backdrop-blur*, border kaca halus (*subtle glass border*), dan sudut melengkung kapsul (*rounded-full pill*).
> 2. **Alur Navigasi**: Aplikasi terbuka pertama kali di **Homescreen Dashboard**. Pengguna memilih proyek atau membuat kanvas baru untuk masuk ke **Studio Editor**, dan dapat kembali ke Dashboard kapan saja.
> 3. **Struktur Room Database**: Menyimpan banyak proyek desain lengkap beserta metadata layer, posisi objek (X, Y, W, H, rotasi), konfigurasi kanvas, serta konten penuh berkas `.uify`.
> 4. **Logo Uinify**: Logo Figma di pojok kiri atas diganti dengan **ikon vektor SVG logo Uinify monokrom putih minimalis** (tanpa emote).
> 5. **Pembersihan UI Autosave**: Badge dan tombol autosave disembunyikan dari antarmuka visual; proses auto-save tetap berjalan senyap (*silent background*) ke Room Database pada setiap perubahan input.

---

## 1. Overview & Core Concept

- **Tujuan Utama**: Menghadirkan antarmuka studio desain modern dengan estetika iOS AMOLED yang mewah. Pengguna disambut oleh Homescreen Dashboard untuk memilih proyek dan preset, lalu masuk ke Studio Editor dengan bilah atas berbentuk kapsul melayang (*Floating Capsule*) yang bersih dan futuristik.
- **Penyimpanan Data Andal**: Room SQLite Database menjadi fondasi penyimpanan lokal di Android, menjamin seluruh posisi objek, layer, dan riwayat desain tersimpan aman dalam format `.uify` tanpa risiko hilang saat aplikasi ditutup.

---

## 2. User Experience & Visual Design

### Desain Topbar Floating Capsule iOS AMOLED
```
       ┌────────────────────────────────────────────────────────────────────────┐
       │  [<] [Logo U] Uinify Project  │  [Kanvas] [Aksen] [Impor] [Ekspor]  │  │
       └────────────────────────────────────────────────────────────────────────┘
                       ▲ Floating Capsule (AMOLED Blur & Glass Border)
```
- **Geometri**: Kapsul melayang (*pill shape*) dengan `border-radius: 9999px`, melayang di bagian atas layar dengan *margin* elegan dari tepi status bar.
- **Warna & Efek**: Hitam murni AMOLED (`#000000` / `rgba(10, 10, 12, 0.88)`), efek kaca buram mendalam (*backdrop-filter: blur(28px)*), border tipis 1px `rgba(255, 255, 255, 0.12)`, serta bayangan lembut (*soft ambient drop shadow*).
- **Elemen Isi**:
  - Tombol Kembali ke Dashboard (Ikon SVG)
  - Logo Uinify Monokrom Putih + Judul Proyek
  - Pemilih Mode Kanvas (Infinity / 1:1 / Custom / DPI)
  - Pemilih Aksen Warna Tema
  - Tombol Impor & Ekspor Seluruh Format
  - Tombol Undo & Redo
  - Tombol Layer & Inspector
  - *Autosave & Save badge disembunyikan sesuai permintaan.*

### Alur Navigasi Dua Arah
```
┌─────────────────────────┐       Pilih Proyek / Buat Baru       ┌─────────────────────────┐
│   HOMESCREEN DASHBOARD  │ ───────────────────────────────────> │      STUDIO EDITOR      │
│  (Katalog, Preset, Akun) │ <─────────────────────────────────── │ (Floating Capsule AMOLED│
└─────────────────────────┘       Tombol Back / Logo Menu        └─────────────────────────┘
```

1. **Homescreen Launch**: Pengguna membuka aplikasi dan langsung melihat Dashboard (kartu metrik storage, preset populer seperti iPhone 16 Pro & Android Compact, dan daftar proyek tersimpan dari Room DB).
2. **Buka / Buat Proyek**:
   - Memilih kartu proyek membuka Editor dengan memuat data layer dan objek dari Room DB.
   - Menekan tombol FAB `+` atau preset kanvas membuat proyek baru dan langsung meluncurkan Editor.
3. **Studio Editor**:
   - Menampilkan Topbar Floating Capsule iOS AMOLED di atas kanvas desain tak terbatas.
   - Menekan tombol Back atau navigasi menu membawa pengguna kembali ke Homescreen dengan sinkronisasi instan ke Room Database.

---

## 3. Key Product Decisions & Trade-Offs

- **Room Database vs Plain Files**:
  - *Keputusan*: Menggunakan Room Database (`AppDatabase`, `ProjectEntity`, `ProjectDao`) sebagai penyimpanan utama dengan index timestamp, title, layerCount, canvasWidth, canvasHeight, dan payload JSON `.uify`.
  - *Alasan*: Memungkinkan query cepat untuk menampilkan daftar proyek, pencarian, dan penghitungan jumlah layer/storage di Dashboard tanpa perlu membaca seluruh isi file berat dari disk.
- **Dua Engine WebView Terkoordinasi**:
  - *Keputusan*: Mengelola transisi antara `dashboard.html` dan `editor.html` dalam `MainActivity` menggunakan jembatan native `UinifyNativeBridge`.
  - *Alasan*: Menjaga 100% fitur CSS/JS canggih bawaan Anda (pen bezier, asset icons, text on path, glassmorphism) tetap utuh dengan performa native 60fps berakselerasi GPU.
- **Silent Auto-Save**:
  - *Keputusan*: Menghilangkan indikator teks "Auto-Save" dan tombol manual "Simpan Sekarang" dari antarmuka visual. Debounce 280ms tetap aktif mencatat setiap perubahan ke Room DB.

---

## 4. Technical Architecture & Data Strategy

### Diagram Arsitektur Komponen

```
┌────────────────────────────────────────────────────────────────────────┐
│                          MainActivity (Kotlin)                         │
│  ┌─────────────────────────────┐      ┌─────────────────────────────┐  │
│  │    Dashboard WebView View   │ <──> │      Editor WebView View    │  │
│  │  (iOS Clean Dashboard HTML) │      │  (Floating Capsule AMOLED)  │  │
│  └──────────────┬──────────────┘      └──────────────┬──────────────┘  │
│                 │                                    │                 │
│                 └──────────────┐      ┌──────────────┘                 │
│                                ▼      ▼                                │
│                     ┌────────────────────────────┐                     │
│                     │    UinifyNativeBridge      │                     │
│                     │  (JavascriptInterface)     │                     │
│                     └──────────────┬─────────────┘                     │
└────────────────────────────────────┼───────────────────────────────────┘
                                     ▼
                      ┌────────────────────────────┐
                      │    ProjectRoomRepository   │
                      └──────────────┬─────────────┘
                                     ▼
                      ┌────────────────────────────┐
                      │         ProjectDao         │
                      └──────────────┬─────────────┘
                                     ▼
                      ┌────────────────────────────┐
                      │    Room SQLite Database    │
                      │       (projects.db)        │
                      └────────────────────────────┘
```

### Skema Entity Room Database (`ProjectEntity`)
- `id: String` (Primary Key, misal `proj-1730000000`)
- `title: String` (Nama proyek)
- `lastUpdated: Long` (Timestamp pembaruan terakhir)
- `layerCount: Int` (Jumlah layer/bentuk objek)
- `canvasWidth: Int` & `canvasHeight: Int` (Dimensi kanvas)
- `canvasMode: String` (`infinite` atau `fixed`)
- `presetName: String` (`1:1`, `9:16`, `16:9`, dll.)
- `accentColor: String` (Hex warna aksen yang dipilih)
- `uifyContent: String` (Data lengkap `.uify` format JSON berisi seluruh objek posisi X, Y, W, H, rotasi, efek, stroke, fill)
- `previewSvg: String?` (Vektor preview miniatur)
- `isCurrentActive: Boolean` (Penanda proyek aktif saat ini)
