package com.eiyooooo.superwindow.util

import android.view.InputEvent
import java.lang.reflect.Method

private var setDisplayIdMethod: Method? = null

fun InputEvent.setDisplayId(displayId: Int) {
    setDisplayIdMethod?.invoke(this, displayId) ?: let {
        setDisplayIdMethod = InputEvent::class.java.getMethod("setDisplayId", Int::class.javaPrimitiveType).also {
            it.invoke(this, displayId)
        }
    }
}
