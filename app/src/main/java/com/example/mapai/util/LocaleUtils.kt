package com.example.mapai.util

import android.content.Context
import android.content.ContextWrapper
import android.content.res.Configuration
import com.example.mapai.data.SettingsStore
import java.util.Locale

private val SUPPORTED = setOf(
    "id", "en", "ms", "zh", "zh-rTW", "ja", "ko", "th", "vi", "hi", "ar",
    "es", "pt", "fr", "de", "it", "ru", "tr", "nl", "pl", "uk", "fa", "he",
    "ta", "bn", "ur", "sw", "fil", "my", "km", "lo", "ne", "si", "am", "cs",
    "sv", "no", "da", "fi", "hu", "ro", "el", "bg", "hr", "sk", "sl", "sr",
    "ca", "eu", "gl", "af", "sq", "az", "ka", "hy", "is", "et", "lv", "lt",
    "mk", "mt", "bs"
)

val ALL_LANGUAGES: List<Pair<String, String>> = listOf(
    "en" to "English",
    "id" to "Bahasa Indonesia",
    "ms" to "Melayu",
    "zh" to "中文 (简体)",
    "zh-rTW" to "中文 (繁體)",
    "ja" to "日本語",
    "ko" to "한국어",
    "th" to "ไทย",
    "vi" to "Tiếng Việt",
    "hi" to "हिन्दी",
    "ta" to "தமிழ்",
    "bn" to "বাংলা",
    "ur" to "اردو",
    "ar" to "العربية",
    "fa" to "فارسی",
    "he" to "עברית",
    "tr" to "Türkçe",
    "es" to "Español",
    "pt" to "Português",
    "fr" to "Français",
    "de" to "Deutsch",
    "it" to "Italiano",
    "ru" to "Русский",
    "uk" to "Українська",
    "nl" to "Nederlands",
    "pl" to "Polski",
    "cs" to "Čeština",
    "sv" to "Svenska",
    "no" to "Norsk",
    "da" to "Dansk",
    "fi" to "Suomi",
    "hu" to "Magyar",
    "ro" to "Română",
    "el" to "Ελληνικά",
    "bg" to "Български",
    "hr" to "Hrvatski",
    "sk" to "Slovenčina",
    "sl" to "Slovenščina",
    "sr" to "Српски",
    "ca" to "Català",
    "eu" to "Euskara",
    "gl" to "Galego",
    "af" to "Afrikaans",
    "sq" to "Shqip",
    "az" to "Azərbaycan",
    "ka" to "ქართული",
    "hy" to "Հայերեն",
    "is" to "Íslenska",
    "et" to "Eesti",
    "lv" to "Latviešu",
    "lt" to "Lietuvių",
    "mk" to "Македонски",
    "mt" to "Malti",
    "bs" to "Bosanski",
    "sw" to "Kiswahili",
    "fil" to "Filipino",
    "my" to "မြန်မာ",
    "km" to "ខ្មែរ",
    "lo" to "ລາວ",
    "ne" to "नेपाली",
    "si" to "සිංහල",
    "am" to "አማርኛ",
    "auto" to "Auto (Ikuti Perangkat)"
)

fun isSupported(code: String): Boolean = code == "auto" || SUPPORTED.contains(code)

fun Context.applyLanguageSetting(): Context {
    return applyLanguage(this, SettingsStore.get().language)
}

fun applyLanguage(context: Context, lang: String): Context {
    val locale = when {
        lang == "auto" -> Locale.getDefault()
        lang.contains("-r") -> {
            val parts = lang.split("-r")
            Locale(parts[0], parts[1])
        }
        lang.contains("-") -> {
            val parts = lang.split("-")
            Locale(parts[0], parts[1])
        }
        else -> Locale(lang)
    }
    Locale.setDefault(locale)
    val config = Configuration(context.resources.configuration)
    config.setLocale(locale)
    return ContextWrapper(context).createConfigurationContext(config)
}
