# YinnMotion

**Code-first Motion & Video Studio for Android**  
*Designed by Yinndev · Ilham*

![YinnMotion logo](branding/yinnmotion-mark.svg)

> **Build motion with code. Create without limits.**

## 🇮🇩 Tentang YinnMotion

**YinnMotion** adalah project editor motion dan video untuk Android dengan pendekatan *code-first*. Fokus utamanya adalah memberi creative control melalui HTML, CSS, dan JavaScript, dengan pengalaman aplikasi yang tetap ringan dan mudah digunakan.

Project ini dikembangkan oleh **Yinndev / Ilham**.

> **Development note:** Repository saat ini berisi fondasi aplikasi Android dan UI awal. YinnMotion belum diklaim sebagai full-featured atau production-ready video editor. Live preview, motion runtime, timeline editing, audio mixing, dan MP4 export harus diselesaikan serta diuji sebelum dinyatakan stabil.

## ✨ Product direction

- **Code-first workflow** — ruang kerja HTML, CSS, dan JavaScript.
- **Motion-oriented editing** — target integrasi GSAP untuk animasi berbasis timeline.
- **Media workflow** — import video, image, dan audio dari penyimpanan perangkat.
- **Native Android foundation** — Kotlin dan Jetpack Compose.
- **Video engine** — AndroidX Media3 sebagai fondasi playback dan export pipeline.
- **Lightweight by design** — menjaga dependency dan ukuran aplikasi tetap efisien.

## 🧩 Technology stack

| Area | Technology |
|---|---|
| Platform | Android |
| Language | Kotlin |
| UI | Jetpack Compose + Material 3 |
| Media foundation | AndroidX Media3 |
| Motion authoring | HTML, CSS, JavaScript |
| Planned animation runtime | GSAP |
| Automation | GitHub Actions |

## 📱 Current implementation

| Component | Status |
|---|---|
| Native app shell & navigation | Implemented |
| Home and project name UI | Implemented |
| HTML / CSS / JavaScript text editor | Initial implementation |
| System media and audio picker | Initial implementation |
| About screen and app identity | Implemented |
| Live code preview | In development |
| GSAP runtime integration | In development |
| Multi-track timeline | Planned |
| Audio controls and mixing | Planned |
| MP4 rendering and export UI | In development |
| Release QA on physical devices | Pending |

Status dibuat transparan agar contributor dan pengguna tahu mana fitur yang sudah tersedia dan mana yang masih dikembangkan.

## 🚀 Build APK with GitHub Actions

Tidak perlu setup Android Studio di perangkat lokal untuk menjalankan cloud build.

1. Buka tab **Actions** pada repository.
2. Pilih workflow **Build YinnMotion APK**.
3. Klik **Run workflow**.
4. Tunggu sampai job berstatus **Success**.
5. Buka hasil workflow dan download artifact **YinnMotion-1.0.0-APK**.

Artifact berisi APK Debug dan Release yang dihasilkan workflow. Release APK dari workflow ini belum ditandatangani dengan release keystore milik publisher; siapkan signing configuration sendiri sebelum distribusi publik.

### Local build

Requirements: JDK 17, Android SDK Platform 35, dan Gradle 8.9.

    gradle --no-daemon assembleDebug
    gradle --no-daemon assembleRelease

Output:
- Debug: app/build/outputs/apk/debug/
- Release: app/build/outputs/apk/release/

## 🗂️ Project structure

    YinnMotion/
    ├── .github/workflows/build.yml
    ├── app/
    │   ├── src/main/
    │   │   ├── java/com/yinndev/yinnmotion/
    │   │   ├── res/drawable/
    │   │   └── AndroidManifest.xml
    │   ├── build.gradle.kts
    │   └── proguard-rules.pro
    ├── branding/yinnmotion-mark.svg
    ├── build.gradle.kts
    ├── gradle.properties
    └── settings.gradle.kts

## 👨‍💻 Developer

| Detail | Information |
|---|---|
| Product | YinnMotion |
| Developer | **Yinndev / Ilham** |
| Package ID | com.yinndev.yinnmotion |
| Current version | 1.0.0 |
| Platform | Android |
| Minimum Android | Android 6.0 (API 23) |
| Repository | [YINNSTORE/YinnMotion](https://github.com/YINNSTORE/YinnMotion) |

## 🛣️ Release principles

YinnMotion 1.0.0 akan dinyatakan stabil setelah core workflow benar-benar berfungsi dan melewati pengujian:

- Code editing dan live preview berjalan konsisten.
- Animasi dapat diputar dengan frame timing yang stabil.
- Media dan audio dapat dikelola tanpa kehilangan akses file.
- MP4 export menghasilkan file valid dan dapat diputar.
- Error handling, lifecycle, dan memory usage diuji pada perangkat Android nyata.
- Debug dan Release build berhasil melalui CI.

**Quality over checklist.** Fitur tidak akan diberi label selesai hanya karena UI-nya sudah tampil.

---

**YinnMotion**  
*Crafted with code by Yinndev · Ilham*