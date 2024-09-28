package com.eiyooooo.superwindow.entities

data class RunningTask(
    val taskId: Int,
    val packageName: String,
    var displayId: Int = -1
)
