package com.eiyooooo.superwindow.ui.main

import android.content.pm.PackageManager
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.eiyooooo.superwindow.contentprovider.LocalContent
import com.eiyooooo.superwindow.ui.controlpanel.HomeData
import com.eiyooooo.superwindow.ui.widgetcard.WidgetCardData
import com.eiyooooo.superwindow.ui.widgetcard.WidgetCardDataGroup
import com.eiyooooo.superwindow.util.BlurUtils
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import rikka.shizuku.Shizuku

class MainActivityViewModel : ViewModel() {

    private val mWidgetCardDataGroup: MutableStateFlow<WidgetCardDataGroup> by lazy { MutableStateFlow(WidgetCardDataGroup(firstWidgetCardData = WidgetCardData(true, "controlPanel"))) }
    val widgetCardDataGroup: LiveData<WidgetCardDataGroup> = mWidgetCardDataGroup.asLiveData()

    fun updateWidgetCardDataGroup(function: (WidgetCardDataGroup) -> WidgetCardDataGroup) {
        mWidgetCardDataGroup.update(function)
    }

    private val mDualSplitHandlePosition: MutableStateFlow<Float> by lazy { MutableStateFlow(-1f) }
    val dualSplitHandlePosition: LiveData<Float> = mDualSplitHandlePosition.asLiveData()

    fun updateDualSplitHandlePosition(position: Float) {
        mDualSplitHandlePosition.update { position }
    }

    private val mShizukuStatus: MutableStateFlow<ShizukuStatus> by lazy { MutableStateFlow(ShizukuStatus.SHIZUKU_NOT_RUNNING) }
    val shizukuStatus: StateFlow<ShizukuStatus> = mShizukuStatus

    private var checkShizukuPermissionJob: Job? = null

    private val binderReceivedListener = Shizuku.OnBinderReceivedListener {
        checkShizukuPermission()
    }

    private val binderDeadListener = Shizuku.OnBinderDeadListener {
        checkShizukuPermission()
    }

    private val requestPermissionResultListener = Shizuku.OnRequestPermissionResultListener { _, grantResult ->
        if (grantResult == PackageManager.PERMISSION_GRANTED) {
            mShizukuStatus.update { ShizukuStatus.HAVE_PERMISSION }
        } else {
            mShizukuStatus.update { ShizukuStatus.NO_PERMISSION }
        }
    }

    fun addShizukuListener() {
        Shizuku.addBinderReceivedListenerSticky(binderReceivedListener)
        Shizuku.addBinderDeadListener(binderDeadListener)
        Shizuku.addRequestPermissionResultListener(requestPermissionResultListener)
    }

    fun removeShizukuListener() {
        Shizuku.removeBinderReceivedListener(binderReceivedListener)
        Shizuku.removeBinderDeadListener(binderDeadListener)
        Shizuku.removeRequestPermissionResultListener(requestPermissionResultListener)
    }

    fun checkShizukuPermission() {
        viewModelScope.launch {
            if (!Shizuku.pingBinder()) {
                mShizukuStatus.update { ShizukuStatus.SHIZUKU_NOT_RUNNING }
                return@launch
            }
            if (Shizuku.isPreV11()) {
                mShizukuStatus.update { ShizukuStatus.VERSION_NOT_SUPPORT }
            }
            if (Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED) {
                mShizukuStatus.update { ShizukuStatus.HAVE_PERMISSION }
            } else {
                mShizukuStatus.update { ShizukuStatus.NO_PERMISSION }
            }
        }
    }

    fun startCheckingShizukuPermission() {
        checkShizukuPermissionJob = viewModelScope.launch {
            while (isActive) {
                delay(1000)
                checkShizukuPermission()
            }
        }
    }

    fun stopCheckingShizukuPermission() {
        checkShizukuPermissionJob?.cancel()
        checkShizukuPermissionJob = null
    }

    var currentControlPanelPage = 0

    val homeData = MediatorLiveData<HomeData>().apply {
        addSource(mShizukuStatus.asLiveData()) {
            value = HomeData(it)
        }
    }

    override fun onCleared() {
        super.onCleared()
        BlurUtils.destroy()
        LocalContent.releaseAllVirtualDisplays()
    }
}
