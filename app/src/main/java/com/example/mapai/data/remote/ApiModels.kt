package com.example.mapai.data.remote

import com.google.gson.annotations.SerializedName

data class ReportDto(
    @SerializedName("id") val id: String,
    @SerializedName("type") val type: String,
    @SerializedName("lat") val lat: Double,
    @SerializedName("lon") val lon: Double,
    @SerializedName("description") val description: String,
    @SerializedName("reporter") val reporter: String,
    @SerializedName("created_at") val createdAt: Long,
    @SerializedName("confirmed") val confirmed: Int,
    @SerializedName("media") val media: String?
)

data class ChatMessage(
    @SerializedName("role") val role: String,
    @SerializedName("content") val content: String
)

data class ChatRequest(
    @SerializedName("messages") val messages: List<ChatMessage>
)

data class ChatResponse(
    @SerializedName("id") val id: String,
    @SerializedName("role") val role: String,
    @SerializedName("content") val content: String
)

data class SosDto(
    @SerializedName("user") val user: String,
    @SerializedName("lat") val lat: Double,
    @SerializedName("lon") val lon: Double,
    @SerializedName("message") val message: String
)

data class NearbyDto(
    @SerializedName("name") val name: String,
    @SerializedName("kind") val kind: String,
    @SerializedName("lat") val lat: Double?,
    @SerializedName("lon") val lon: Double?,
    @SerializedName("meta") val meta: Map<String, String>?
)

data class ContributorDto(
    @SerializedName("name") val name: String,
    @SerializedName("points") val points: Int,
    @SerializedName("reports") val reports: Int
)

data class CctvDto(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("url") val url: String,
    @SerializedName("lat") val lat: Double?,
    @SerializedName("lon") val lon: Double?
)
