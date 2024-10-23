package com.eiyooooo.superwindow.entity

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.PreferenceManager
import com.eiyooooo.superwindow.BuildConfig
import com.eiyooooo.superwindow.util.get
import com.eiyooooo.superwindow.util.put
import com.fredporciuncula.flow.preferences.FlowSharedPreferences

object Preferences {

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var flowSharedPreferences: FlowSharedPreferences

    fun init(context: Context) {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        this.sharedPreferences = sharedPreferences
        this.flowSharedPreferences = FlowSharedPreferences(sharedPreferences)

        if (!sharedPreferences.contains("others.enable_log")) {
            enableLog = BuildConfig.DEBUG
        }
    }

    var darkTheme
        get() = sharedPreferences.get("appearance.dark_theme", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        set(value) = sharedPreferences.put("appearance.dark_theme", value)

    val darkThemeFlow
        get() = flowSharedPreferences.getInt("appearance.dark_theme", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM).asFlow()

    var systemColor
        get() = sharedPreferences.get("appearance.system_color", true)
        set(value) = sharedPreferences.put("appearance.system_color", value)

    var fullScreen
        get() = sharedPreferences.get("appearance.full_screen", true)
        set(value) = sharedPreferences.put("appearance.full_screen", value)

    var topBottomPadding
        get() = sharedPreferences.get("appearance.top_bottom_padding", 0)
        set(value) = sharedPreferences.put("appearance.top_bottom_padding", value)

    val topBottomPaddingFlow
        get() = flowSharedPreferences.getInt("appearance.top_bottom_padding", 0).asFlow()

    var enableLog
        get() = sharedPreferences.get("others.enable_log", BuildConfig.DEBUG)
        set(value) = sharedPreferences.put("others.enable_log", value)
}
