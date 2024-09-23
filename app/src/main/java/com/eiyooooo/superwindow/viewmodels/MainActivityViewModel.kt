package com.eiyooooo.superwindow.viewmodels

import android.content.pm.PackageManager
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.eiyooooo.superwindow.entities.HomeData
import com.eiyooooo.superwindow.entities.ShizukuStatus
import com.eiyooooo.superwindow.entities.WidgetCardData
import com.eiyooooo.superwindow.entities.WidgetCardGroup
import com.eiyooooo.superwindow.utils.BlurUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import rikka.shizuku.Shizuku

class MainActivityViewModel : ViewModel() {

    private val mWidgetCardGroup: MutableStateFlow<WidgetCardGroup> by lazy { MutableStateFlow(WidgetCardGroup(firstWidgetCard = WidgetCardData(true, "controlPanel"))) }
    val widgetCardGroup: LiveData<WidgetCardGroup> = mWidgetCardGroup.asLiveData()

    fun updateWidgetCardGroup(widgetCardGroup: WidgetCardGroup) {
        mWidgetCardGroup.update { widgetCardGroup }
    }

    var lastWidgetCardGroup: WidgetCardGroup? = null

    private val mShizukuStatus: MutableStateFlow<ShizukuStatus> by lazy { MutableStateFlow(ShizukuStatus.SHIZUKU_NOT_RUNNING) }

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
        viewModelScope.launch(Dispatchers.IO) {
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
        checkShizukuPermissionJob = viewModelScope.launch(Dispatchers.IO) {
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

    val homeData = MediatorLiveData<HomeData>().apply {
        addSource(mShizukuStatus.asLiveData()) {
            value = HomeData(it)
        }
    }

    override fun onCleared() {
        super.onCleared()
        BlurUtils.destroy()
    }
}
