package com.eiyooooo.superwindow.utils

import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.preference.Preference
import com.eiyooooo.superwindow.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder

@Suppress("UNCHECKED_CAST")
inline fun <reified T> SharedPreferences.put(key: String, value: T?) {
    edit {
        when (value) {
            null -> putString(key, null)

            is Boolean -> putBoolean(key, value)
            is Int -> putInt(key, value)
            is Long -> putLong(key, value)
            is Float -> putFloat(key, value)

            is String -> putString(key, value)
            is Set<*> -> putStringSet(key, value as Set<String>)
        }
    }
}

inline fun <reified T> SharedPreferences.get(key: String, defaultValue: T): T =
    when (defaultValue) {
        is Boolean -> getBoolean(key, defaultValue) as T
        is Int -> getInt(key, defaultValue) as T
        is Long -> getLong(key, defaultValue) as T
        is Float -> getFloat(key, defaultValue) as T

        else -> error("Type of $defaultValue is not supported")
    }

@Suppress("UNCHECKED_CAST")
inline fun <reified T> SharedPreferences.getNullable(key: String, defaultValue: T?): T? =
    when (defaultValue) {
        null -> getString(key, null) as T?

        is String -> getString(key, defaultValue) as T?
        is Set<*> -> getStringSet(key, defaultValue as Set<String>) as T?

        else -> error("Type of $defaultValue is not supported")
    }

fun Preference.showListPreferenceOnClick(
    setupDialog: MaterialAlertDialogBuilder.() -> Unit,
    items: Array<String>,
    selected: () -> Int,
    onSelected: (Int) -> Unit
) =
    setOnPreferenceClickListener {
        MaterialAlertDialogBuilder(context)
            .setTitle(title)
            .setSingleChoiceItems(items, selected()) { dialog, which ->
                dialog.cancel()
                onSelected(which)
            }
            .setPositiveButton(R.string.cancel, null)
            .apply(setupDialog)
            .show()
        return@setOnPreferenceClickListener true
    }
