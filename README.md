# 🎨 Uify — Base on Figma for Android

<p align="center">
  <img src="https://img.shields.io/badge/Platform-Android-3DDC84?style=for-the-badge&logo=android&logoColor=white" alt="Android Platform" />
  <img src="https://img.shields.io/badge/Language-Kotlin-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white" alt="Kotlin" />
  <img src="https://img.shields.io/badge/Design-Figma-F24E1E?style=for-the-badge&logo=figma&logoColor=white" alt="Figma" />
  <img src="https://img.shields.io/badge/Build-Gradle%20KTS-02303A?style=for-the-badge&logo=gradle&logoColor=white" alt="Gradle KTS" />
</p>

---

## 📖 Tentang Proyek

**Uify** adalah implementasi antarmuka pengguna (UI/UX) modern untuk Android yang diadaptasi langsung dari desain Figma. Proyek ini berfokus pada transisi desain yang presisi (*pixel-perfect*), komponen interaktif yang mulus, serta otomasi pembuatan aset untuk mempercepat alur kerja pengembangan UI Android.

---

## ✨ Fitur Utama

- **Figma to Android Layouts**: Implementasi komponen antarmuka yang presisi mengacu pada standar *design token* Figma.
- **Interactive Dashboard & Editor**: Antarmuka dashboard dan editor visual yang responsif untuk berbagai ukuran layar Android.
- **Capsule Swipe Component**: Komponen interaksi gestur *swipe* berbentuk kapsul yang intuitif.
- **Asset & Layout Automation**: Dilengkapi dengan sekumpulan script Python untuk memproses, memperbarui, dan men-generate aset UI secara otomatis.

---

## 📁 Struktur Repositori

```text
├── app/                        # Modul utama aplikasi Android (Kotlin / Jetpack Compose / XML)
├── gradle/                     # Konfigurasi Gradle wrapper
├── .aistudio/                  # Metadata & artifact project
├── create_dashboard_asset.py   # Script otomasi pembuatan aset Dashboard
├── create_editor_assets.py     # Script otomasi pembuatan aset Editor
├── create_full_editor.py       # Script pembentukan komponen Editor lengkap
├── fix_ghost_panel.py          # Script patch/perbaikan bug layer Ghost Panel
├── update_capsule_swipe.py     # Script pembaruan logika gestur Capsule Swipe
├── update_editor_capsule.py    # Script integrasi komponen Capsule ke dalam Editor
├── build.gradle.kts            # Konfigurasi root build Gradle (Kotlin DSL)
├── settings.gradle.kts         # Konfigurasi repositori dan modul Gradle
└── README.md                   # Dokumentasi proyek
