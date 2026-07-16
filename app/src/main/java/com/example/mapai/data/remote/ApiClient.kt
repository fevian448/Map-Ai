package com.example.mapai.data.remote

import com.example.mapai.data.SettingsStore
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {
    private var retrofit: Retrofit? = null
    private var api: MapAiApi? = null

    fun api(): MapAiApi {
        val base = SettingsStore.get().serverUrl
        if (api == null || retrofit?.baseUrl()?.toString()?.trimEnd('/') != base.trimEnd('/')) {
            retrofit = Retrofit.Builder()
                .baseUrl(if (base.endsWith("/")) base else "$base/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()
            api = retrofit!!.create(MapAiApi::class.java)
        }
        return api!!
    }
}
