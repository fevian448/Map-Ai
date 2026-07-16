package com.example.mapai.data

import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

object MapRepository {

    private val roadNames = listOf(
        "Jl. Sudirman", "Jl. Thamrin", "Jl. Gatot Subroto", "Jl. Ahmad Yani",
        "Jl. Diponegoro", "Jl. Merdeka", "Jl. Pahlawan", "Jl. Dipati Ukur",
        "Jl. Asia Afrika", "Jl. Hayam Wuruk", "Tol Jagorawi", "Jl. Casablanca"
    )

    private val alertDescriptions = mapOf(
        AlertType.HAZARD to listOf("Ban pecah di bahu jalan", "Objek jatuh di jalur", "Genangan air dalam"),
        AlertType.POLICE to listOf("Patroli polisi terlihat", "Razia di depan", "Pos polisi aktif"),
        AlertType.ACCIDENT to listOf("Kecelakaan ringan 2 mobil", "Tabrakan di persimpangan", "Motor terjatuh"),
        AlertType.ROADWORK to listOf("Perbaikan aspal", "Penutupan 1 lajur", "Galian kabel"),
        AlertType.SPEED_CAM to listOf("Kamera kecepatan aktif", "Speed trap tersembunyi", "Kamera merah"),
        AlertType.TRAFFIC to listOf("Kemacetan parah", "Antrean panjang", "Stop & go")
    )

    private val placeNames = mapOf(
        PlaceCategory.FUEL to listOf("Pertamina", "Shell", "BP AKR", "Vivo"),
        PlaceCategory.FOOD to listOf("Warteg Bahari", "Kopi Kenangan", "McDonald's", "Sate Senayan"),
        PlaceCategory.PARKING to listOf("Parkir Plaza", "Parkir Mall", "Parkir Umum", "Parkir Ruko"),
        PlaceCategory.HOSPITAL to listOf("RS Mata Aini", "RSCM", "RS Siloam", "RS Pertamina"),
        PlaceCategory.ATM to listOf("BCA", "Mandiri", "BRI", "BNI")
    )

    private val fuelPrices = listOf("Rp 10.000/L", "Rp 12.500/L", "Rp 13.900/L", "Rp 14.500/L")

    fun generateAlerts(center: GeoPoint, count: Int = 8): List<TrafficAlert> {
        val types = AlertType.values()
        return List(count) {
            val type = types.random()
            TrafficAlert(
                id = "alert_$it",
                type = type,
                point = randomNearby(center, 0.01),
                description = alertDescriptions[type]!!.random(),
                reporter = listOf("Anda", "Budi", "Sari", "Agus", "WazeUser", "Rina").random(),
                timestamp = System.currentTimeMillis() - Random.nextLong(0, 3_600_000),
                confidence = Random.nextInt(60, 99),
                confirmedBy = Random.nextInt(1, 40)
            )
        }
    }

    fun generateSpeedCameras(center: GeoPoint, count: Int = 4): List<SpeedCamera> {
        return List(count) {
            SpeedCamera(
                id = "cam_$it",
                point = randomNearby(center, 0.008),
                limitKmh = listOf(40, 50, 60, 80, 100).random(),
                direction = listOf("Utara", "Selatan", "Timur", "Barat").random()
            )
        }
    }

    fun generatePlaces(center: GeoPoint, category: PlaceCategory): List<Place> {
        val names = placeNames[category] ?: listOf("Tempat")
        return List(Random.nextInt(5, 9)) {
            val dist = Random.nextDouble(150.0, 5000.0)
            Place(
                id = "${category.key}_$it",
                name = "${names.random()} ${('A'..'Z').random()}",
                category = category,
                point = offsetPoint(center, dist, Random.nextDouble(0.0, 360.0)),
                distanceMeters = dist,
                rating = Random.nextFloat() * 1.5f + 3.5f,
                isOpen = Random.nextBoolean(),
                fuelPrice = if (category == PlaceCategory.FUEL) fuelPrices.random() else null,
                extra = when (category) {
                    PlaceCategory.PARKING -> "${Random.nextInt(2, 8)} rb/jam"
                    PlaceCategory.HOSPITAL -> "IGD 24 jam"
                    PlaceCategory.ATM -> "Tarik & Setor"
                    else -> null
                }
            )
        }.sortedBy { it.distanceMeters }
    }

    fun buildRoute(from: GeoPoint, to: GeoPoint): RouteInfo {
        val segments = 4
        val points = mutableListOf(from)
        val segList = mutableListOf<RouteSegment>()
        val totalDist = haversine(from, to)
        val trafficLevels = listOf(TrafficLevel.FREE, TrafficLevel.SLOW, TrafficLevel.JAM, TrafficLevel.FREE)
        for (i in 0 until segments) {
            val t = (i + 1) / segments.toDouble()
            val lat = from.latitude + (to.latitude - from.latitude) * t
            val lon = from.longitude + (to.longitude - from.longitude) * t
            val p = GeoPoint(lat, lon)
            val segDist = totalDist / segments
            val traffic = trafficLevels[i % trafficLevels.size]
            segList.add(
                RouteSegment(
                    from = points.last(),
                    to = p,
                    distanceMeters = segDist,
                    traffic = traffic,
                    roadName = roadNames.random()
                )
            )
            points.add(p)
        }
        val speedFactor = when {
            segList.any { it.traffic == TrafficLevel.JAM } -> 0.55
            segList.any { it.traffic == TrafficLevel.SLOW } -> 0.78
            else -> 1.0
        }
        val avgSpeedKmh = 45.0
        val freeFlow = totalDist / 1000.0 / avgSpeedKmh * 3600.0
        val duration = freeFlow / speedFactor
        val overall = when (speedFactor) {
            < 0.6 -> TrafficLevel.JAM
            < 0.85 -> TrafficLevel.SLOW
            else -> TrafficLevel.FREE
        }
        return RouteInfo(
            points = points,
            segments = segList,
            totalDistanceMeters = totalDist,
            durationSeconds = duration,
            freeFlowDurationSeconds = freeFlow,
            hasTolls = Random.nextBoolean(),
            overallTraffic = overall
        )
    }

    fun currentWeather(): WeatherInfo {
        val options = listOf(
            Triple("Cerah", "\u2600\uFE0F", "Jalan kering, aman"),
            Triple("Berawan", "\u26C5", "Visibilitas baik"),
            Triple("Hujan Ringan", "\uD83C\uDF27\uFE0F", "Waspada jalan licin"),
            Triple("Hujan Deras", "\uD83C\uDF27\uFE0F", "Bahaya aquaplaning"),
            Triple("Berkabut", "\uD83C\uDF2B\uFE0F", "Nyalakan lampu, jaga jarak")
        )
        val (c, e, risk) = options.random()
        return WeatherInfo(
            condition = c,
            emoji = e,
            temperatureC = Random.nextInt(24, 34),
            windKph = Random.nextInt(2, 25),
            humidity = Random.nextInt(55, 95),
            visibilityKm = if (c.contains("Kabut")) Random.nextDouble(0.3, 2.0) else Random.nextDouble(6.0, 12.0),
            roadRisk = risk
        )
    }

    private fun randomNearby(center: GeoPoint, radius: Double): GeoPoint =
        offsetPoint(center, Random.nextDouble(radius * 1000 * 0.3, radius * 1000), Random.nextDouble(0.0, 360.0))

    private fun offsetPoint(center: GeoPoint, meters: Double, bearingDeg: Double): GeoPoint {
        val earthR = 6371000.0
        val br = Math.toRadians(bearingDeg)
        val dLat = meters * cos(br) / earthR
        val dLon = meters * sin(br) / (earthR * cos(Math.toRadians(center.latitude)))
        return GeoPoint(center.latitude + Math.toDegrees(dLat), center.longitude + Math.toDegrees(dLon))
    }

    fun haversine(a: GeoPoint, b: GeoPoint): Double {
        val r = 6371000.0
        val dLat = Math.toRadians(b.latitude - a.latitude)
        val dLon = Math.toRadians(b.longitude - a.longitude)
        val lat1 = Math.toRadians(a.latitude)
        val lat2 = Math.toRadians(b.latitude)
        val h = sin(dLat / 2).pow(2) + cos(lat1) * cos(lat2) * sin(dLon / 2).pow(2)
        return 2 * r * Math.asin(Math.sqrt(h))
    }
}
