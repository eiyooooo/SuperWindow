package com.eiyooooo.superwindow.wrappers

import android.app.ActivityOptions
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.hardware.display.VirtualDisplay
import android.os.Build
import android.os.SystemClock
import android.view.MotionEvent
import android.view.MotionEvent.PointerCoords
import android.view.MotionEvent.PointerProperties
import android.view.Surface
import androidx.core.view.InputDeviceCompat
import rikka.shizuku.Shizuku
import rikka.shizuku.ShizukuRemoteProcess
import timber.log.Timber

object LocalContent {//TODO

    fun init() {
        ServiceManager.setupManagers()
    }

    fun destroy() {
        ServiceManager.destroy()
    }

    private val virtualDisplayHolder = mutableMapOf<String, VirtualDisplay>()

    fun createContainerForPackage(context: Context? = null, packageName: String, width: Int, height: Int, densityDpi: Int, surface: Surface): Int {
        val vd = DisplayManagerWrapper.createVirtualDisplay(packageName, width, height, densityDpi, surface)
        val displayId = vd.display.displayId
        openApp(context, packageName, displayId = displayId)
        virtualDisplayHolder[packageName] = vd
        return displayId
    }

    fun getPackageInfo(context: Context? = null, packageName: String?, flag: Int): PackageInfo? {
        try {
            if (context != null) {
                val pm = context.packageManager
                val info: PackageInfo? = pm.getPackageInfo(packageName!!, flag)
                info?.let {
                    return it
                }
            }
        } catch (e: Exception) {
            Timber.w("$packageName get package info by context failed", e)
        }
        return IPackageManager.getPackageInfo(packageName, flag, 0)
    }

    private fun getAppMainActivity(context: Context? = null, packageName: String): String {
        if (context != null) {
            val pm = context.packageManager
            val launchIntent = pm.getLaunchIntentForPackage(packageName)
            launchIntent?.component?.className?.let {
                return it
            }
        }
        val mainIntent = Intent(Intent.ACTION_MAIN, null)
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER)
        IPackageManager.queryIntentActivities(mainIntent, null, 0, 0)?.let {
            for (resolveInfo in it) {
                val packageStr = resolveInfo.activityInfo.packageName
                if (packageStr == packageName) {
                    return resolveInfo.activityInfo.name
                }
            }
        }
        return ""
    }

    fun getAppPackages(context: Context? = null): List<String> {
        if (context != null) {
            val pm = context.packageManager
            val packages: MutableList<String> = ArrayList()
            val infos = pm.getInstalledPackages(PackageManager.GET_UNINSTALLED_PACKAGES)
            for (info in infos) {
                packages.add(info.packageName)
            }
            if (packages.isNotEmpty()) {
                return packages
            }
        }
        return IPackageManager.getAllPackages()
    }

    private fun openApp(context: Context? = null, packageName: String, activity: String? = null, displayId: Int): String? {
        val startActivity = activity ?: getAppMainActivity(context, packageName)
        if (context == null) {
            val cmd = if (displayId != 0) "am start --display $displayId -n $packageName/$startActivity" else "am start -n $packageName/$startActivity"
            Timber.d("start activity cmd: $cmd")
            try {
                execReadOutput(cmd)
            } catch (e: Exception) {
                Timber.e(e, "openApp")
                return e.toString()
            }
            return null
        }
        try {
            val intent = Intent()
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
            intent.addFlags(Intent.FLAG_ACTIVITY_LAUNCH_ADJACENT or Intent.FLAG_ACTIVITY_NEW_TASK)
            var options: ActivityOptions? = null
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && displayId != 0) {
                options = ActivityOptions.makeBasic().setLaunchDisplayId(displayId)
            }
            val cName = ComponentName(packageName, startActivity)
            intent.setComponent(cName)
            if (options != null) {
                context.startActivity(intent, options.toBundle())
            } else {
                context.startActivity(intent)
            }
        } catch (e: Exception) {
            Timber.e(e, "openApp")
        }
        return null
    }

    fun injectMotionEvent(motionEvent: MotionEvent, displayId: Int) {
        try {
            val pointerProperties = arrayOfNulls<PointerProperties>(1)
            val properties = PointerProperties()
            properties.id = motionEvent.getPointerId(0)
            properties.toolType = MotionEvent.TOOL_TYPE_FINGER
            pointerProperties[0] = properties

            val pointerCoords = arrayOfNulls<PointerCoords>(1)
            val pointerCoord = PointerCoords()
            pointerCoord.x = motionEvent.x
            pointerCoord.y = motionEvent.y
            pointerCoord.pressure = motionEvent.pressure
            pointerCoord.size = motionEvent.size
            pointerCoord.toolMajor = motionEvent.toolMajor
            pointerCoord.toolMinor = motionEvent.toolMinor
            pointerCoord.touchMajor = motionEvent.touchMajor
            pointerCoord.touchMinor = motionEvent.touchMinor
            pointerCoords[0] = pointerCoord

            val now = SystemClock.uptimeMillis()
            val injectMotionEvent = MotionEvent.obtain(now, now, motionEvent.action, 1, pointerProperties, pointerCoords, 0, 0, 1.0f, 1.0f, 0, 0, 0, 0)
            injectMotionEvent.source = InputDeviceCompat.SOURCE_TOUCHSCREEN
            InputManagerWrapper.setDisplayId(injectMotionEvent, displayId)
            InputManagerWrapper.injectInputEvent(injectMotionEvent, InputManagerWrapper.INJECT_INPUT_EVENT_MODE_ASYNC)
            injectMotionEvent.recycle()
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
