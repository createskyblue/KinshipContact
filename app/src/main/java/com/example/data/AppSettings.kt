package com.example.data

import android.content.Context
import android.content.SharedPreferences

class AppSettings(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("family_contacts_prefs", Context.MODE_PRIVATE)

    var gridColumns: Int
        get() = prefs.getInt("grid_columns", 2)
        set(value) = prefs.edit().putInt("grid_columns", value).apply()

    var fontSize: String
        get() = prefs.getString("font_size", "large") ?: "large"
        set(value) = prefs.edit().putString("font_size", value).apply()

    var fontColor: String
        get() = prefs.getString("font_color", "dark") ?: "dark"
        set(value) = prefs.edit().putString("font_color", value).apply()

    var dialScheme: String
        get() = prefs.getString("dial_scheme", "CALL") ?: "CALL"
        set(value) = prefs.edit().putString("dial_scheme", value).apply()

    var hideSettingsButton: Boolean
        get() = prefs.getBoolean("hide_settings_button", false)
        set(value) = prefs.edit().putBoolean("hide_settings_button", value).apply()

    var adminPassword: String
        get() = prefs.getString("admin_password", "123") ?: "123"
        set(value) = prefs.edit().putString("admin_password", value).apply()

    var isPermissionGuided: Boolean
        get() = prefs.getBoolean("permission_guided", false)
        set(value) = prefs.edit().putBoolean("permission_guided", value).apply()
}
