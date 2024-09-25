package com.eiyooooo.superwindow.wrappers

import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.res.AssetManager
import android.content.res.Resources
import android.graphics.drawable.Drawable
import android.hardware.display.VirtualDisplay
import android.view.MotionEvent
import android.view.Surface
import androidx.core.content.res.ResourcesCompat
import rikka.shizuku.Shizuku
import rikka.shizuku.ShizukuRemoteProcess
import timber.log.Timber

object LocalContent {//TODO

    private var init = false//TODO: UI

    fun init() {
        if (init) {
            Timber.d("Managers already init")
            return
        } else {
            init = ServiceManager.setupManagers()
            Timber.d("Managers init: $init")
        }
    }

    fun destroy() {
        if (init) {
            ServiceManager.destroy()
            init = false
        }
    }

    private val virtualDisplayHolder = mutableMapOf<String, VirtualDisplay>()

    fun getVirtualDisplayIdForPackage(packageName: String, width: Int, height: Int, densityDpi: Int, surface: Surface): Int? {
        virtualDisplayHolder[packageName]?.let {
            val displayId = it.display.displayId
            if (true) {//TODO: check if app in this vd
                it.resize(width, height, densityDpi)
                it.surface = surface
                Timber.d("$packageName is already in vd: $displayId")
                return displayId
            } else {
                openApp(displayId, packageName)
                Timber.d("$packageName is opened in vd: $displayId")
            }
        }
        DisplayManagerWrapper.createVirtualDisplay(packageName, width, height, densityDpi, surface)?.let {
            val displayId = it.display.displayId
            Timber.d("Create new vd: $displayId for: $packageName")
            if (openApp(displayId, packageName)) {
                virtualDisplayHolder[packageName] = it
                Timber.d("$packageName is opened in vd: $displayId")
                return displayId
            } else {
                it.release()
            }
        }
        return null
    }

    @Suppress("DEPRECATION")
    fun getPackageIcon(packageName: String): Drawable? {
        return try {
            val applicationInfo = getPackageInfo(packageName)?.applicationInfo ?: throw Exception("applicationInfo == null")
            val assetManager = AssetManager::class.java.getDeclaredConstructor().newInstance().also {
                it.javaClass.getMethod("addAssetPath", String::class.java).invoke(it, applicationInfo.sourceDir)
            } ?: throw Exception("assetManager == null")
            val resources = Resources(assetManager, null, null)
            ResourcesCompat.getDrawable(resources, applicationInfo.icon, null) ?: throw Exception("icon == null")
        } catch (e: Exception) {
            Timber.e(e, "getPackageIcon")
            null
        }
    }

    private fun getPackageInfo(packageName: String, flag: Int = 0): PackageInfo? {
        try {
            FakeContext.get().packageManager?.getPackageInfo(packageName, flag)?.let {
                return it
            }
        } catch (e: Exception) {
            Timber.w(e, "$packageName get package info by context failed")
        }
        return IPackageManager.getPackageInfo(packageName, flag, 0)
    }

    private fun getAppMainActivity(packageName: String): String? {
        try {
            FakeContext.get().packageManager?.getLaunchIntentForPackage(packageName)?.component?.className?.let {
                return it
            }
        } catch (e: Exception) {
            Timber.w(e, "$packageName get launch intent by context failed")
        }
        val intent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        IPackageManager.queryIntentActivities(intent, null, 0, 0)?.let {
            for (resolveInfo in it) {
                val packageStr = resolveInfo.activityInfo.packageName
                if (packageStr == packageName) {
                    return resolveInfo.activityInfo.name
                }
            }
        }
        return null
    }

    @Suppress("DEPRECATION")
    fun getAllPackages(): List<String>? {
        try {
            FakeContext.get().packageManager?.getInstalledPackages(PackageManager.GET_UNINSTALLED_PACKAGES)?.map { it.packageName }?.let {
                if (it.isNotEmpty()) return it
            }
        } catch (e: Exception) {
            Timber.w(e, "getAllPackages by context failed")
        }
        return IPackageManager.getAllPackages()
    }

    private fun openApp(displayId: Int, packageName: String, activity: String? = null): Boolean {
        val startActivity = activity ?: getAppMainActivity(packageName) ?: return false
        val cmd = if (displayId != 0) "am start --display $displayId -n $packageName/$startActivity" else "am start -n $packageName/$startActivity"
        Timber.d("start activity cmd: $cmd")
        try {
            execReadOutput(cmd)
        } catch (e: Exception) {
            Timber.e(e, "openApp")
            return false
        }
        return true
    }

    fun injectMotionEvent(motionEvent: MotionEvent, displayId: Int) {
        try {
            InputManagerWrapper.setDisplayId(motionEvent, displayId)
            InputManagerWrapper.injectInputEvent(motionEvent, InputManagerWrapper.INJECT_INPUT_EVENT_MODE_ASYNC)
        } catch (throwable: Throwable) {
            Timber.e(throwable, "injectMotionEvent failed")
        }
    }

    private fun execReadOutput(command: String): String {
        val process = Shizuku::class.java.getDeclaredMethod(
            "newProcess",
            Array<String>::class.java,
            Array<String>::class.java,
            String::class.java
        ).also { it.isAccessible = true }.invoke(null, arrayOf("sh", "-c", command), null, null)

        if (process is ShizukuRemoteProcess) {
            val result = process.inputStream.bufferedReader().use { it.readText() }
            val exitCode = process.waitFor()
            if (exitCode != 0) {
                throw Exception("Error: Execution failed with exit code: $exitCode")
            }
            return result
        } else {
            throw Exception("Error: Execution failed")
        }
    }
}
