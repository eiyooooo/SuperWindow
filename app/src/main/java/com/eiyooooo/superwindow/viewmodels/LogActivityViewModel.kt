package com.eiyooooo.superwindow.viewmodels

import android.annotation.SuppressLint
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eiyooooo.superwindow.entities.LogItem
import com.eiyooooo.superwindow.utils.FLog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.sample
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.BufferedReader

@OptIn(FlowPreview::class)
class LogActivityViewModel : ViewModel() {

    private val mItems = MutableStateFlow<List<LogItem>>(emptyList())
    internal val items: Flow<List<LogItem>> = mItems.sample(500L)

    private var mReadJob: Job? = null

    @SuppressLint("LogNotTimber")
    internal fun startReadLog() {
        if (mReadJob?.isActive == true) return
        mReadJob = viewModelScope.launch(Dispatchers.IO) {
            var reader: BufferedReader? = null
            try {
                if (FLog.logFile?.exists() != true) {
                    Timber.e("Log file does not exist ${FLog.logFile?.absolutePath}")
                    return@launch
                }

                reader = FLog.logFile!!.bufferedReader()
                var currentLogEntry = StringBuilder()

                while (isActive) {
                    val line: String? = reader.readLine()
                    line?.let {
                        if (it.endsWith(FLog.SUFFIX)) {
                            currentLogEntry.append(it.substringBefore(FLog.SUFFIX))
                            try {
                                mItems.value += LogItem(currentLogEntry.toString())
                            } catch (t: Throwable) {
                                Log.w("ReadLog", t)
                            }
                            currentLogEntry = StringBuilder()
                        } else {
                            currentLogEntry.append(it).append("\n")
                        }
                    }
                }
            } catch (t: Throwable) {
                Timber.e(t)
            } finally {
                reader?.close()
            }
        }
    }

    internal fun clearLog() {
        mItems.value = emptyList()
    }
}
