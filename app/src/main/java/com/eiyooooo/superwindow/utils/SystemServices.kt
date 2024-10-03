package com.eiyooooo.superwindow.utils

import android.content.pm.LauncherApps
import android.content.pm.PackageManager
import android.os.UserManager
import androidx.core.content.getSystemService
import com.eiyooooo.superwindow.application

val packageManager: PackageManager by lazy { application.packageManager }

val launcherApps: LauncherApps by lazy { application.getSystemService()!! }

val userManager: UserManager by lazy { application.getSystemService()!! }
