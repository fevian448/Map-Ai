package com.example.mapai.util

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.net.wifi.WifiManager
import android.os.Build
import android.provider.Settings
import android.telephony.TelephonyManager
import androidx.core.content.ContextCompat

object DeviceActions {

    fun call(context: Context, phone: String) {
        val intent = Intent(Intent.ACTION_CALL, Uri.parse("tel:$phone"))
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE)
            == PackageManager.PERMISSION_GRANTED
        ) {
            context.startActivity(intent)
        } else {
            val i = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone"))
            context.startActivity(i)
        }
    }

    fun dial(context: Context, phone: String) {
        context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone")))
    }

    fun sendSms(context: Context, phone: String, body: String) {
        val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:$phone")).apply {
            putExtra("sms_body", body)
        }
        context.startActivity(intent)
    }

    fun shareLocation(context: Context, lat: Double, lon: Double, label: String = "MapAi") {
        val uri = Uri.parse("geo:$lat,$lon?q=$lat,$lon($label)")
        val intent = Intent(Intent.ACTION_VIEW, uri)
        context.startActivity(intent)
    }

    fun openUrl(context: Context, url: String) {
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    }

    fun networkInfo(context: Context): Map<String, String> {
        val result = mutableMapOf<String, String>()
        val tm = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        val wifi = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        result["imei"] = try { if (Build.VERSION.SDK_INT >= 29) "restricted" else tm.deviceId ?: "n/a" } catch (_: Exception) { "n/a" }
        result["carrier"] = try { tm.networkOperatorName ?: "n/a" } catch (_: Exception) { "n/a" }
        result["phoneType"] = when (tm.phoneType) {
            TelephonyManager.PHONE_TYPE_GSM -> "GSM"
            TelephonyManager.PHONE_TYPE_CDMA -> "CDMA"
            else -> "Unknown"
        }
        result["wifiOn"] = wifi.isWifiEnabled.toString()
        result["ipLocal"] = try {
            val ip = wifi.connectionInfo.ipAddress
            "${ip and 0xff}.${ip shr 8 and 0xff}.${ip shr 16 and 0xff}.${ip shr 24 and 0xff}"
        } catch (_: Exception) { "n/a" }
        result["androidId"] = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
        return result
    }
}
