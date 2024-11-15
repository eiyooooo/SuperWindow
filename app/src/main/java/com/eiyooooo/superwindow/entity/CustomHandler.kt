package com.eiyooooo.superwindow.entity

import android.os.Handler
import android.os.HandlerThread

object CustomHandler {

    private val handlerThread by lazy {
        HandlerThread("CustomThread").apply {
            start()
        }
    }

    val customHandler by lazy {
        Handler(handlerThread.looper)
    }
}
