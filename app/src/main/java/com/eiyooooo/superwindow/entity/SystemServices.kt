package com.eiyooooo.superwindow.entity

import android.content.pm.LauncherApps
import android.content.pm.PackageManager
import android.os.Build
import android.os.UserManager
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.core.content.getSystemService
import com.eiyooooo.superwindow.application

object SystemServices {

    val packageManager: PackageManager by lazy { application.packageManager }

    val launcherApps: LauncherApps by lazy { application.getSystemService()!! }

    val userManager: UserManager by lazy { application.getSystemService()!! }

    private val vibrator: Vibrator by lazy { application.getSystemService()!! }

    @Suppress("DEPRECATION")
    fun triggerVibration(ms: Long = 50, amplitude: Int = 100) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val vibrationEffect = VibrationEffect.createOneShot(ms, amplitude)
            vibrator.vibrate(vibrationEffect)
        } else {
            vibrator.vibrate(ms)
        }
    }
}
