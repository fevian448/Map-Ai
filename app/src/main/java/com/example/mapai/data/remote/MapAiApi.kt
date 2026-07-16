package com.example.mapai.data.remote

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface MapAiApi {
    @GET("api/reports")
    suspend fun getReports(
        @Query("lat") lat: Double? = null,
        @Query("lon") lon: Double? = null,
        @Query("radius") radius: Double = 5.0
    ): List<ReportDto>

    @POST("api/reports")
    suspend fun postReport(@Body body: Map<String, Any>): ReportDto

    @POST("api/reports/{id}/confirm")
    suspend fun confirmReport(@Path("id") id: String): Map<String, Boolean>

    @POST("api/chat")
    suspend fun chat(@Body body: ChatRequest): ChatResponse

    @POST("api/sos")
    suspend fun sendSos(@Body body: SosDto): Map<String, String>

    @GET("api/nearby")
    suspend fun getNearby(): List<NearbyDto>

    @POST("api/nearby")
    suspend fun postNearby(@Body body: NearbyDto): Map<String, String>

    @GET("api/contributors")
    suspend fun getContributors(): List<ContributorDto>

    @GET("api/cctv")
    suspend fun getCctv(): List<CctvDto>

    @POST("api/cctv")
    suspend fun postCctv(@Body body: CctvDto): Map<String, String>

    @GET("api/places")
    suspend fun getPlaces(
        @Query("lat") lat: Double? = null,
        @Query("lon") lon: Double? = null,
        @Query("radius") radius: Double = 5.0,
        @Query("category") category: String? = null
    ): List<PlaceDto>

    @GET("api/directions")
    suspend fun getDirections(
        @Query("from") from: String,
        @Query("to") to: String,
        @Query("profile") profile: String = "driving"
    ): DirectionsResponse
}
