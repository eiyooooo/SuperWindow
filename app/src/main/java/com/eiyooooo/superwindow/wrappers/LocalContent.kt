package com.eiyooooo.superwindow.wrappers

import android.annotation.SuppressLint
import android.app.ActivityManager.RunningTaskInfo
import android.app.TaskStackListener
import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.res.AssetManager
import android.content.res.Resources
import android.graphics.drawable.Drawable
import android.hardware.display.VirtualDisplay
import android.os.Build
import android.view.MotionEvent
import android.view.Surface
import androidx.annotation.RequiresApi
import androidx.core.content.res.ResourcesCompat
import com.eiyooooo.superwindow.entities.RunningTask
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import rikka.shizuku.Shizuku
import rikka.shizuku.ShizukuRemoteProcess
import timber.log.Timber

object LocalContent {//TODO

    private var init = false//TODO: UI

    @RequiresApi(Build.VERSION_CODES.S_V2)
    fun init() {
        if (init) {
            Timber.d("Managers already init")
            return
        } else {
            init = try {
                ServiceManager.setupManagers()
                ServiceManager.getActivityTaskManager().registerTaskStackListener(runningTaskStackListener)
                true
            } catch (t: Throwable) {
                Timber.e(t, "Managers init failed")
                ServiceManager.destroy()
                false
            }
            Timber.d("Managers init: $init")
        }
    }

    fun destroy() {
        if (init) {
            ServiceManager.destroy()
            init = false
        }
    }

    private val runningTaskList: MutableStateFlow<MutableList<RunningTask>> by lazy { MutableStateFlow(mutableListOf()) }

    @SuppressLint("NewApi")
    private val runningTaskStackListener = object : TaskStackListener() {
        override fun onTaskCreated(taskId: Int, componentName: ComponentName) {
            Timber.d("onTaskCreated -> taskId: $taskId, packageName: ${componentName.packageName}")
            runningTaskList.update { it.apply { add(RunningTask(taskId, componentName.packageName)) } }
        }

        override fun onTaskRemovalStarted(taskInfo: RunningTaskInfo) {
            Timber.d("onTaskRemovalStarted -> taskId: ${taskInfo.taskId}, packageName: ${taskInfo.baseIntent.component?.packageName}")
            runningTaskList.update { list -> list.apply { removeIf { it.taskId == taskInfo.taskId } } }
        }

        override fun onTaskDisplayChanged(taskId: Int, newDisplayId: Int) {
            Timber.d("onTaskDisplayChanged -> taskId: $taskId, newDisplayId: $newDisplayId")
            runningTaskList.update { list -> list.apply { find { it.taskId == taskId }?.let { task -> task.displayId = newDisplayId } } }
        }
    }

    private val virtualDisplayHolder = mutableMapOf<String, VirtualDisplay>()//TODO: need destroy

    fun getVirtualDisplayIdForPackage(packageName: String, width: Int, height: Int, densityDpi: Int, surface: Surface): Int? {
        virtualDisplayHolder[packageName]?.let { vd ->
            val displayId = vd.display.displayId
            vd.resize(width, height, densityDpi)
            vd.surface = surface
            Timber.d("Resize vd: $displayId for: $packageName, width: $width, height: $height, densityDpi: $densityDpi")
            showAPP(packageName, displayId)//TODO: handle failure
            return displayId
        }
        DisplayManagerWrapper.createVirtualDisplay(packageName, width, height, densityDpi, surface)?.let {
            val displayId = it.display.displayId
            Timber.d("Create new vd: $displayId for: $packageName, width: $width, height: $height, densityDpi: $densityDpi")
            if (showAPP(packageName, displayId)) {
                virtualDisplayHolder[packageName] = it
                Timber.d("$packageName is opened in vd: $displayId")
                return displayId
            } else {
                it.release()
            }
        }
        return null
    }

    private fun showAPP(packageName: String, displayId: Int): Boolean {
        return runningTaskList.value.run {
            find { it.packageName == packageName }?.let {
                if (it.displayId == displayId) {
                    Timber.d("$packageName is already in vd: $displayId")
                    true
                } else {
                    Timber.d("Try move $packageName to vd: $displayId")
                    val cmd = "am display move-stack ${it.taskId} $displayId"
                    try {
                        execReadOutput(cmd)
                        Timber.d("Move stack success, cmd: $cmd")
                        true
                    } catch (t: Throwable) {
                        Timber.e(t, "Move stack failed, cmd: $cmd")
                        false
                    }
                }
            } ?: let {
                Timber.d("Try open $packageName in vd: $displayId")
                val startActivity = getAppMainActivity(packageName) ?: return false
                val cmd = "am start --display $displayId -n $packageName/$startActivity"
                try {
                    execReadOutput(cmd)
                    Timber.d("Start activity success, cmd: $cmd")
                    true
                } catch (t: Throwable) {
                    Timber.e(t, "Start activity failed, cmd: $cmd")
                    false
                }
            }
        }
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
        } catch (t: Throwable) {
            Timber.e(t, "getPackageIcon")
            null
        }
    }

    private fun getPackageInfo(packageName: String, flag: Int = 0): PackageInfo? {
        try {
            FakeContext.get().packageManager?.getPackageInfo(packageName, flag)?.let {
                return it
            }
        } catch (t: Throwable) {
            Timber.w(t, "$packageName get package info by context failed")
        }
        return IPackageManager.getPackageInfo(packageName, flag, 0)
    }

    private fun getAppMainActivity(packageName: String): String? {
        try {
            FakeContext.get().packageManager?.getLaunchIntentForPackage(packageName)?.component?.className?.let {
                return it
            }
        } catch (t: Throwable) {
            Timber.w(t, "$packageName get launch intent by context failed")
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
        } catch (t: Throwable) {
            Timber.w(t, "getAllPackages by context failed")
        }
        return IPackageManager.getAllPackages()
    }

    fun injectMotionEvent(motionEvent: MotionEvent, displayId: Int) {
        try {
            ServiceManager.getSetDisplayIdMethod().invoke(motionEvent, displayId)
            ServiceManager.getInputManager().injectInputEvent(motionEvent, 0)
        } catch (t: Throwable) {
            Timber.e(t, "injectMotionEvent failed")
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
