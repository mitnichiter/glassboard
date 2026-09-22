package dev.mitnic.lumaglass.data

import android.content.Context

enum class GlassTheme { AURORA, MIDNIGHT, PEARL }
enum class OneHandedMode { OFF, LEFT, RIGHT }

data class KeyboardPrefs(
    val theme: GlassTheme = GlassTheme.AURORA,
    val haptics: Boolean = true,
    val sound: Boolean = false,
    val suggestions: Boolean = true,
    val numberRow: Boolean = false,
    val oneHanded: OneHandedMode = OneHandedMode.OFF,
    val keyHeight: Int = 48,
    val incognito: Boolean = false,
)

class KeyboardPreferences(context: Context) {
    private val store = context.getSharedPreferences("lumaglass_preferences", Context.MODE_PRIVATE)

    fun load(): KeyboardPrefs = KeyboardPrefs(
        theme = runCatching { GlassTheme.valueOf(store.getString("theme", null) ?: "AURORA") }
            .getOrDefault(GlassTheme.AURORA),
        haptics = store.getBoolean("haptics", true),
        sound = store.getBoolean("sound", false),
        suggestions = store.getBoolean("suggestions", true),
        numberRow = store.getBoolean("number_row", false),
        oneHanded = runCatching {
            OneHandedMode.valueOf(store.getString("one_handed", null) ?: "OFF")
        }.getOrDefault(OneHandedMode.OFF),
        keyHeight = store.getInt("key_height", 48),
        incognito = store.getBoolean("incognito", false),
    )

    fun save(value: KeyboardPrefs) {
        store.edit()
            .putString("theme", value.theme.name)
            .putBoolean("haptics", value.haptics)
            .putBoolean("sound", value.sound)
            .putBoolean("suggestions", value.suggestions)
            .putBoolean("number_row", value.numberRow)
            .putString("one_handed", value.oneHanded.name)
            .putInt("key_height", value.keyHeight)
            .putBoolean("incognito", value.incognito)
            .apply()
    }
}
