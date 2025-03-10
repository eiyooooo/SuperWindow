package com.eiyooooo.superwindow.ui.controlpanel

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.eiyooooo.superwindow.BuildConfig
import com.eiyooooo.superwindow.R
import com.eiyooooo.superwindow.entity.Preferences
import com.eiyooooo.superwindow.ui.log.LogActivity
import com.eiyooooo.superwindow.ui.main.MainActivity
import com.eiyooooo.superwindow.util.FLog
import com.eiyooooo.superwindow.util.showListPreferenceOnClick
import com.eiyooooo.superwindow.util.showSliderPreferenceOnClick
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber

class SettingsFragment : PreferenceFragmentCompat() {

    private var systemColorPreference: Preference? = null
    private var darkThemePreference: Preference? = null
    private var fullScreenPreference: Preference? = null
    private var topBottomPaddingPreference: Preference? = null
    private var enableLogPreference: Preference? = null
    private var logViewerPreference: Preference? = null
    private var exportLogPreference: Preference? = null
    private var licensePreference: Preference? = null

    private var exportLogJob: Job? = null

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        val context = requireContext()
        val activity = requireActivity() as MainActivity

        setPreferencesFromResource(R.xml.settings, rootKey)

        systemColorPreference = findPreference("appearance.system_color")
        darkThemePreference = findPreference("appearance.dark_theme")
        fullScreenPreference = findPreference("appearance.full_screen")
        topBottomPaddingPreference = findPreference("appearance.top_bottom_padding")
        enableLogPreference = findPreference("others.enable_log")
        logViewerPreference = findPreference("others.log_viewer")
        exportLogPreference = findPreference("others.export_log")
        licensePreference = findPreference("others.license")

        systemColorPreference?.apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                setOnPreferenceChangeListener { _, _ ->
                    activity.recreate()
                    true
                }
            } else {
                isVisible = false
            }
        }
        darkThemePreference?.apply {
            val darkThemeOptions = arrayOf(getString(R.string.follow_system), getString(R.string.always_off), getString(R.string.always_on))

            this.showListPreferenceOnClick(
                setupDialog = { setIcon(R.drawable.settings_night_sight) },
                items = darkThemeOptions,
                selected = { Preferences.darkTheme.coerceAtLeast(0) },
                onSelected = {
                    Preferences.darkTheme = if (it == 0) AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM else it
                    AppCompatDelegate.setDefaultNightMode(Preferences.darkTheme)
                    activity.recreate()
                },
                onShowDialog = { activity.makeCardsBlur(true) },
                onDismissDialog = { activity.makeCardsBlur(false) }
            )

            lifecycleScope.launch {
                Preferences.darkThemeFlow.collect {
                    summary = darkThemeOptions.getOrNull(it) ?: getString(R.string.follow_system)
                }
            }
        }
        fullScreenPreference?.apply {
            setOnPreferenceChangeListener { _, newValue ->
                activity.setFullScreen(newValue as Boolean)
                true
            }
        }
        topBottomPaddingPreference?.apply {
            this.showSliderPreferenceOnClick(
                setupDialog = { setIcon(R.drawable.padding) },
                initialValue = { Preferences.topBottomPadding.toFloat() },
                valueRange = 0f to 250f,
                step = 1f,
                labelFormatter = { "${it / 10}%" },
                onValueChanged = { Preferences.topBottomPadding = it.toInt() },
                onShowDialog = { activity.makeCardsBlur(true) },
                onDismissDialog = { activity.makeCardsBlur(false) }
            )
        }

        enableLogPreference?.setOnPreferenceChangeListener { _, newValue ->
            if (newValue as Boolean) {
                logViewerPreference?.isVisible = true
                exportLogPreference?.isVisible = true
                FLog.startFLog()
            } else {
                logViewerPreference?.isVisible = false
                exportLogPreference?.isVisible = false
                FLog.stopFLog()
            }
            true
        }
        logViewerPreference?.apply {
            isVisible = Preferences.enableLog
            setOnPreferenceClickListener {
                context.startActivity(Intent(context, LogActivity::class.java))
                true
            }
        }
        exportLogPreference?.apply {
            isVisible = Preferences.enableLog
            setOnPreferenceClickListener {
                if (exportLogJob?.isActive == true) return@setOnPreferenceClickListener true
                exportLogJob = lifecycleScope.launch(Dispatchers.IO) {
                    try {
                        if (FLog.logFile?.exists() == true) {
                            activity.showSnackBar(getString(R.string.exporting_log))
                            FLog.writeLastFLog()
                            context.startActivity(
                                Intent.createChooser(
                                    Intent(Intent.ACTION_SEND)
                                        .setType("text/plain")
                                        .setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                        .putExtra(
                                            Intent.EXTRA_STREAM,
                                            FileProvider.getUriForFile(context, "${BuildConfig.APPLICATION_ID}.log", FLog.logFile!!)
                                        ),
                                    context.getString(androidx.appcompat.R.string.abc_shareactionprovider_share_with)
                                )
                            )
                            delay(500)
                            activity.showSnackBar(getString(R.string.export_log_success))
                            return@launch
                        }
                    } catch (t: Throwable) {
                        Timber.e(t, "Export log failed")
                    }
                    activity.showSnackBar(getString(R.string.export_log_fail))
                }
                true
            }
        }
        licensePreference?.setOnPreferenceClickListener {
            startActivity(Intent(context, OssLicensesMenuActivity::class.java))
            true
        }
    }
}
