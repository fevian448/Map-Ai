# MapAi — Navigasi Cerdas, Info Lengkap

**MapAi** ialah aplikasi navigasi Android (alternatif Waze) yang dibina dengan **Jetpack Compose + Kotlin** dan backend **Node.js + Express + SQLite**. Projek ini bersifat open-source dan mengutamakan data real-time melalui Socket.IO.

---

## Fitur Utama

| Modul | Fungsi |
|-------|--------|
| **Peta** | Peta OpenStreetMap, carian tempat, tetep destinasi, navigasi dengan route (OSRM / Google Directions API) |
| **Laporan** | Crowd-sourced alerts — polisi, kemacetan, kecelakaan, roadwork, speed cam — dengan sistem confidence & confirm |
| **Jelajah** | Cari tempat berdekatan — BBM, Makanan, Parkir, RS, ATM — dengan rating & jarak |
| **Kemudi** | Speedometer, had laju, cuaca semasa, kamera laju terdekat |
| **Profil** | Profil pengguna & statistik |
| **Chat AI** | Assistant AI tempatan (fallback) — tanya tentang rute, macet, SOS, BBM |
| **SOS/Darurat** | Kirim SMS + lokasi ke contact darurat, panggil, kongsikan lokasi, broadcast ke backend |
| **Carian** | Search places dalam semua kategori |
| **Tetapan** | URL pelayan, bahasa, unit, sumber peta, tema warna, contact darurat |
| **Live View Web** | Paparan peta real-time dari browser (Leaflet + Socket.IO) |

---

## Teknologi

### Android App
- **Kotlin** + **Jetpack Compose** (Material3)
- **osmdroid** — peta OpenStreetMap
- **Retrofit** + **Gson** — REST API client
- **Socket.IO Client** — realtime push
- **Google Play Services Location** — Fused Location Provider
- **Accompanist Permissions** — runtime permissions (Android 13+)
- **Gradle Kotlin DSL** — build system

### Backend
- **Node.js** + **Express** — HTTP API
- **better-sqlite3** — embedded file database (WAL mode)
- **Socket.IO** — realtime push
- **multer** — media uploads
- **OSRM** — open-source routing engine (fallback ke Google Directions API jika API key diset)

---

## Struktur Projek

```
MapAi/
├── app/
│   └── src/main/java/com/example/mapai/
│       ├── data/
│       │   ├── MapRepository.kt          # Data layer (mock + API + routing)
│       │   ├── Models.kt                 # Data models (Parcelable)
│       │   ├── SettingsStore.kt          # SharedPreferences wrapper
│       │   └── remote/
│       │       ├── ApiClient.kt          # Retrofit client
│       │       ├── ApiModels.kt          # DTOs
│       │       ├── MapAiApi.kt           # Retrofit interfaces
│       │       └── SocketManager.kt      # Socket.IO client
│       ├── location/
│       │   └── LocationProvider.kt       # FusedLocationProvider
│       ├── service/
│       │   └── LocationTrackingService.kt # Foreground service placeholder
│       ├── ui/
│       │   ├── MapViewModel.kt           # Shared ViewModel
│       │   ├── map/                      # MapScreen + OsmMapView
│       │   ├── alerts/                   # AlertsScreen + ReportSheet
│       │   ├── chat/                     # ChatScreen + localFallback
│       │   ├── drive/                    # DriveScreen + weather
│       │   ├── explore/                  # ExploreScreen (places)
│       │   ├── profile/                  # ProfileScreen
│       │   ├── search/                   # SearchScreen
│       │   ├── settings/                 # SettingsScreen
│       │   ├── sos/                      # SosScreen
│       │   └── theme/                    # MapAiTheme + colors + typography
│       └── util/
│           ├── DeviceActions.kt          # SMS, call, share location
│           ├── Formatters.kt             # Distance, duration, ETA
│           └── LocaleUtils.kt            # Multi-language support
├── backend/
│   ├── server.js                         # Express + Socket.IO + SQLite
│   ├── package.json
│   ├── Dockerfile
│   ├── README.md
│   └── public/
│       └── index.html                    # Live view web (Leaflet + Socket.IO)
└── gradle/
    └── libs.versions.toml                # Version catalog
```

---

## Backend API

### REST Endpoints
| Method | Endpoint | Fungsi |
|--------|----------|--------|
| `GET` | `/api/reports?lat=&lon=&radius=` | Senarai laporan (filter radius) |
| `POST` | `/api/reports` | Cipta laporan baru |
| `POST` | `/api/reports/:id/confirm` | Konfirmasi laporan |
| `POST` | `/api/chat` | Chat proxy AI (fallback tempatan) |
| `POST` | `/api/sos` | Hantar broadcast SOS |
| `GET` | `/api/sos` | Senarai SOS terkini |
| `GET` | `/api/places?lat=&lon=&radius=&category=` | Senarai places berdekatan |
| `POST` | `/api/places` | Daftar place baru |
| `GET` | `/api/directions?from=&to=&profile=` | Routing (OSRM / Google) |
| `GET` | `/api/nearby` | Senarai peranti berdekatan |
| `POST` | `/api/nearby` | Daftar peranti berdekatan |
| `GET` | `/api/contributors` | Leaderboard penganjur |
| `GET` | `/api/cctv` | Senarai stream CCTV |
| `POST` | `/api/cctv` | Daftar CCTV baru |
| `POST` | `/api/upload` | Muat naik imej/video (max 8MB) |
| `GET` | `/api/feed` | Senarai media feed |
| `GET` | `/media/:filename` | Akses fail yang dimuat naik |
| `GET` | `/api/health` | Semak status backend |

### Socket.IO Events
| Event | Arah | Data |
|-------|------|------|
| `report:new` | Server → Client | Laporan baru |
| `sos:new` | Server → Client | SOS baru |
| `place:new` | Server → Client | Place baru |
| `media:new` | Server → Client | Media baru |
| `ping` / `pong` | Both | Heartbeat |

---

## Cara Menjalankan

### 1. Backend
```bash
cd backend
npm install
npm start
# Backend berjalan di http://localhost:3000
# Live view web: http://localhost:3000/
```

### 2. Android App
Buka projek dalam **Android Studio** ( Arctic Fox atau lebih baru), kemudian:

```bash
# Build debug APK
./gradlew assembleDebug

# Atau jalankan di emulator/device
./gradlew installDebug
```

### 3. Konfigurasi
- Buka **Settings** dalam app
- Tetapkan **URL Server Backend** kepada `http://10.0.2.2:3000` (emulator) atau `http://<IP-anda>:3000` (peranti sebenar)
- Untuk routing Google, set `GOOGLE_MAPS_API_KEY` dalam `gradle.properties` (key ini juga perlu dibenarkan di Google Cloud Console untuk Directions API)

---

## Skrin

| Peta | Laporan | Jelajah | Kemudi | SOS |
|------|---------|---------|--------|-----|
| ![Map](docs/screenshots/map.png) | ![Alerts](docs/screenshots/alerts.png) | ![Explore](docs/screenshots/explore.png) | ![Drive](docs/screenshots/drive.png) | ![SOS](docs/screenshots/sos.png) |

> *Skrin akan ditambah selepas build pertama.*

---

## Sumbangan

1. Fork repositori ini
2. Cipta branch baru (`git checkout -b feature/nama-fitur`)
3. Commit perubahan (`git commit -am 'Tambah fitur X'`)
4. Push ke branch (`git push origin feature/nama-fitur`)
5. Buka **Pull Request**

---

## Lesen

Projek ini menggunakan lesen **MIT**. Lihat fail [LICENSE](LICENSE) untuk maklumat lanjut.

---

## Penafian

- Data routing adalah **simulasi / fallback** jika backend tidak disponibles.
- Chat AI menggunakan **fallback tempatan** secara default — untuk model sebenar, konfigurasikan `LLM_ENDPOINT` dan `LLM_KEY` di `backend/server.js`.
- API key Google Maps yang disimpan dalam repo adalah untuk tujuan development — **jangan gunakan dalam production tanpa restriksi** (API key restriction di Google Cloud Console).

---

Dibina dengan ❤️ untuk komuniti open-source Indonesia.
