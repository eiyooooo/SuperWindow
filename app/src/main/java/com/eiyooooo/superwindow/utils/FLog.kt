package com.eiyooooo.superwindow.utils

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import com.eiyooooo.superwindow.BuildConfig
import com.eiyooooo.superwindow.MyApplication.Companion.appStartTime
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import timber.log.Timber
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object FLog {

    const val PREFIX = "[SuperWindow ${BuildConfig.VERSION_NAME}]-> "
    const val SUFFIX = " <-[SuperWindow ${BuildConfig.VERSION_NAME}]"

    var logFile: File? = null
    private var fLogTree = FLogTree()

    private val logChannel = Channel<String>(Channel.UNLIMITED)
    private val logScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private var writerJob: Job? = null

    fun init(context: Context) {
        val logFiles = context.cacheDir.listFiles { _, name -> name.endsWith(".log") }
        logFiles?.forEach { file ->
            if (file.exists()) {
                file.delete()
            }
        }
        logFile = File(context.cacheDir, "SuperWindow-${BuildConfig.VERSION_NAME}-${appStartTime.time}.log")
    }

    fun makeFLog(logDateFormat: SimpleDateFormat, priority: Int, tag: String?, message: String) {
        if (writerJob?.isActive != true) return

        val timeStamp = logDateFormat.format(Date())
        val priorityStr = when (priority) {
            Log.VERBOSE -> "V"
            Log.DEBUG -> "D"
            Log.INFO -> "I"
            Log.WARN -> "W"
            Log.ERROR -> "E"
            else -> "A"
        }
        val logTag = tag ?: "NoTag"
        val logMessage = "$timeStamp [$priorityStr/$logTag]: $message$SUFFIX\n"

        logScope.launch {
            logChannel.send(logMessage)
        }
    }

    private val writeFLogImmediately: MutableStateFlow<Boolean> by lazy { MutableStateFlow(false) }

    @SuppressLint("LogNotTimber")
    fun startFLog() {
        if (!Timber.forest().contains(fLogTree)) {
            Timber.plant(fLogTree)
        }

        if (writerJob?.isActive == true) return
        writerJob = logScope.launch {
            writeFLogToFile(listOf(""))
            Timber.i("FLog started")
            while (isActive) {
                try {
                    val logBuffer = mutableListOf<String>()
                    var messageCount = 0

                    withTimeoutOrNull(1000L) {
                        for (logMessage in logChannel) {
                            if (writeFLogImmediately.value) break
                            logBuffer.add(logMessage)
                            messageCount++
                            if (messageCount >= 10) break
                        }
                    }

                    if (logBuffer.isNotEmpty()) {
                        writeFLogToFile(logBuffer)
                    }

                    writeFLogImmediately.update { false }
                } catch (e: Exception) {
                    Log.e("FLog", "Error in log writer coroutine", e)
                    delay(1000)
                }
            }
        }
    }

    fun stopFLog() {
        if (Timber.forest().contains(fLogTree)) {
            Timber.uproot(fLogTree)
        }

        writerJob?.cancel()
        writerJob = null
    }

    suspend fun writeLastFLog() {
        writeFLogImmediately.update { true }
        logChannel.send("")
        writeFLogImmediately.first { !it }
    }

    @SuppressLint("LogNotTimber")
    private suspend fun writeFLogToFile(logBuffer: List<String>) {
        withContext(Dispatchers.IO) {
            try {
                FileWriter(logFile, true).use { writer ->
                    for (logMessage in logBuffer) {
                        writer.write(logMessage)
                    }
                }
            } catch (e: Exception) {
                Log.e("FLog", "Error writing log to file", e)
            }
        }
    }

    class FLogTree : Timber.DebugTree() {

        private val logDateFormat = SimpleDateFormat("MM-dd hh:mm:ss.SSS", Locale.getDefault())

        override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
            super.log(priority, tag, PREFIX + message, t)
            makeFLog(logDateFormat, priority, tag, message)
        }
    }
}
