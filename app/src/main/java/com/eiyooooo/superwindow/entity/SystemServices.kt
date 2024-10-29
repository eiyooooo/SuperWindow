package com.eiyooooo.superwindow.entity

import android.content.pm.LauncherApps
import android.content.pm.PackageManager
import android.os.UserManager
import android.view.Display
import androidx.core.content.getSystemService
import com.eiyooooo.superwindow.application

object SystemServices {

    val packageManager: PackageManager by lazy { application.packageManager }

    val launcherApps: LauncherApps by lazy { application.getSystemService()!! }

    val userManager: UserManager by lazy { application.getSystemService()!! }

    var currentDisplay: Display? = null
}
