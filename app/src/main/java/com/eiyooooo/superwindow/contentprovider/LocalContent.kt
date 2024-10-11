package com.eiyooooo.superwindow.contentprovider

import android.annotation.SuppressLint
import android.app.ActivityManager.RunningTaskInfo
import android.app.TaskStackListener
import android.content.Intent
import android.content.pm.LauncherActivityInfo
import android.content.pm.LauncherApps
import android.content.pm.PackageInfo
import android.content.res.AssetManager
import android.content.res.Resources
import android.graphics.drawable.Drawable
import android.hardware.display.VirtualDisplay
import android.os.Build
import android.os.Handler
import android.os.UserHandle
import android.view.MotionEvent
import android.view.Surface
import androidx.annotation.RequiresApi
import androidx.core.content.res.ResourcesCompat
import com.eiyooooo.superwindow.application
import com.eiyooooo.superwindow.entity.SystemServices
import com.eiyooooo.superwindow.wrapper.DisplayManagerWrapper
import com.eiyooooo.superwindow.wrapper.IPackageManager
import com.eiyooooo.superwindow.wrapper.ServiceManager
import com.github.promeg.pinyinhelper.Pinyin
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import rikka.shizuku.Shizuku
import rikka.shizuku.ShizukuRemoteProcess
import timber.log.Timber

object LocalContent {//TODO

    private val ioScope = CoroutineScope(Dispatchers.IO)

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
                ioScope.launch {
                    initAppsMap()
                }
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

    @SuppressLint("NewApi")
    private val runningTaskStackListener = object : TaskStackListener() {
        override fun onTaskDisplayChanged(taskId: Int, newDisplayId: Int) {
            mRunningTasksInVD.update {
                if (virtualDisplayHolder.containsKey(newDisplayId)) {
                    Timber.d("Task: $taskId is moved to vd: $newDisplayId")
                    it.apply { get(newDisplayId)?.add(taskId) ?: put(newDisplayId, mutableSetOf(taskId)) }
                } else {
                    it.filter { (displayId, tasks) ->
                        if (tasks.remove(taskId)) {
                            Timber.d("Task: $taskId is removed from vd: $displayId")
                        }
                        tasks.isNotEmpty()
                    }.toMutableMap()
                }
            }
        }

        override fun onTaskRemovalStarted(taskInfo: RunningTaskInfo) {
            mRunningTasksInVD.update {
                it.filter { (displayId, tasks) ->
                    tasks.remove(taskInfo.taskId)
                    if (tasks.remove(taskInfo.taskId)) {
                        Timber.d("Task: $taskInfo.taskId is removed from vd: $displayId")
                    }
                    tasks.isNotEmpty()
                }.toMutableMap()
            }
        }
    }

    private val packageContainer = mutableMapOf<String, Int>()
    private val virtualDisplayHolder = mutableMapOf<Int, VirtualDisplay>()
    private val mRunningTasksInVD: MutableStateFlow<MutableMap<Int, MutableSet<Int>>> by lazy { MutableStateFlow(mutableMapOf()) }
    val runningTasksInVD: StateFlow<Map<Int, Set<Int>>> = mRunningTasksInVD

    fun getVirtualDisplayIdForPackage(packageName: String, width: Int, height: Int, densityDpi: Int, surface: Surface): Int? {
        virtualDisplayHolder[packageContainer[packageName]]?.let { vd ->
            val displayId = vd.display.displayId
            vd.resize(width, height, densityDpi)
            vd.surface = surface
            Timber.d("Resize vd: $displayId for: $packageName, width: $width, height: $height, densityDpi: $densityDpi")
            return displayId
        }
        DisplayManagerWrapper.createVirtualDisplay(packageName, width, height, densityDpi, surface)?.let {
            val displayId = it.display.displayId
            Timber.d("Create new vd: $displayId for: $packageName, width: $width, height: $height, densityDpi: $densityDpi")
            if (showAPP(packageName, displayId)) {
                packageContainer[packageName] = displayId
                virtualDisplayHolder[displayId] = it
                Timber.d("show app: $packageName successfully in vd: $displayId")
                return displayId
            } else {
                Timber.d("Failed to show app: $packageName, releasing vd: $displayId")
                it.release()
            }
        }
        return null
    }

    fun releaseAllVirtualDisplays() {
        virtualDisplayHolder.values.forEach {
            it.release()
        }
        virtualDisplayHolder.clear()
        packageContainer.clear()
    }

    @SuppressLint("NewApi")
    private fun showAPP(packageName: String, displayId: Int): Boolean {
        ServiceManager.getActivityTaskManager()?.getAllRootTaskInfos()?.let {
            for (taskInfo in it) {
                if (packageName == (taskInfo.baseIntent.component?.packageName ?: taskInfo.baseActivity?.packageName)) {
                    Timber.d("Try move $packageName to vd: $displayId")
                    val cmd = "am display move-stack ${taskInfo.taskId} $displayId"
                    try {
                        execReadOutput(cmd)
                        Timber.d("Move stack success, cmd: $cmd")
                    } catch (t: Throwable) {
                        Timber.e(t, "Move stack failed, cmd: $cmd")
                    }
                    return true
                }
            }
        }
        Timber.d("Try open $packageName in vd: $displayId")
        val startActivity = getAppMainActivity(packageName) ?: return false
        val cmd = "am start --display $displayId -n $packageName/$startActivity"
        try {
            execReadOutput(cmd)
            Timber.d("Start activity success, cmd: $cmd")
            return true
        } catch (t: Throwable) {
            Timber.e(t, "Start activity failed, cmd: $cmd")
            return false
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
            SystemServices.packageManager.getPackageInfo(packageName, flag)?.let {
                return it
            }
        } catch (t: Throwable) {
            Timber.w(t, "$packageName get package info by context failed")
        }
        return IPackageManager.getPackageInfo(packageName, flag, 0)
    }

    private fun getAppMainActivity(packageName: String): String? {
        try {
            SystemServices.packageManager.getLaunchIntentForPackage(packageName)?.component?.className?.let {
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
        Timber.e("$packageName get launch intent failed")
        return null
    }

    private val appsMap: MutableMap<String, LauncherActivityInfo> = mutableMapOf()
    private val appsCallback = object : LauncherApps.Callback() {
        override fun onPackageRemoved(packageName: String?, user: UserHandle?) {
            packageName?.let {
                appsMap.remove(it)
            }
        }

        override fun onPackageAdded(packageName: String?, user: UserHandle?) {
            packageName?.let {
                updateCachedApp(it, user)
            }
        }

        override fun onPackageChanged(packageName: String?, user: UserHandle?) {
            packageName?.let {
                updateCachedApp(it, user)
            }
        }

        override fun onPackagesAvailable(packageNames: Array<out String>?, user: UserHandle?, replacing: Boolean) {
            packageNames?.forEach {
                updateCachedApp(it, user)
            }
        }

        override fun onPackagesUnavailable(packageNames: Array<out String>?, user: UserHandle?, replacing: Boolean) {
            packageNames?.forEach {
                appsMap.remove(it)
            }
        }

        private fun updateCachedApp(packageName: String, user: UserHandle?) {
            val currentPackageName = application.packageName
            SystemServices.launcherApps.getActivityList(packageName, user)
                .filter { it.applicationInfo.packageName != currentPackageName }
                .forEach { appsMap[it.applicationInfo.packageName] = it }
        }
    }

    private suspend fun initAppsMap() {
        if (appsMap.isEmpty()) {
            try {
                withContext(Dispatchers.IO) {
                    val currentPackageName = application.packageName
                    SystemServices.userManager.userProfiles.flatMap { SystemServices.launcherApps.getActivityList(null, it) }
                        .filter { it.applicationInfo.packageName != currentPackageName }
                        .forEach { appsMap[it.applicationInfo.packageName] = it }
                    SystemServices.launcherApps.unregisterCallback(appsCallback)
                    SystemServices.launcherApps.registerCallback(appsCallback, Handler(application.mainLooper))
                }
            } catch (e: Exception) {
                Timber.e(e, "initAppsList failed")
                SystemServices.launcherApps.unregisterCallback(appsCallback)
            }
        }
    }

    suspend fun getAppsList(): List<LauncherActivityInfo> {
        return withContext(Dispatchers.IO) {
            initAppsMap()
            appsMap.values.sortedBy { Pinyin.toPinyin(it.label[0]) }
        }
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
                throw Exception("Error: Execution failed with exit code: $exitCode, output: $result")
            }
            return result
        } else {
            throw Exception("Error: Execution failed")
        }
    }
}
