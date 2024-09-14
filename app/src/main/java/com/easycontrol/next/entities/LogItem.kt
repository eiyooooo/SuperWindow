package com.easycontrol.next.entities

import androidx.annotation.ColorRes
import com.easycontrol.next.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.regex.Matcher
import java.util.regex.Pattern

class LogItem(line: String) {

    companion object {
        private const val PRIORITY_VERBOSE = "V"
        private const val PRIORITY_DEBUG = "D"
        private const val PRIORITY_INFO = "I"
        private const val PRIORITY_WARNING = "W"
        private const val PRIORITY_ERROR = "E"
        private const val PRIORITY_ASSERT = "A"

        private val sLogcatPattern: Pattern = Pattern.compile(
            """(\d{2}-\d{2} \d{2}:\d{2}:\d{2}\.\d{3}) \[([VDIWEA])/(.+?)]: (.*?)(?=\n\d{2}-\d{2} \d{2}:\d{2}:\d{2}\.\d{3} \[|$)""",
            Pattern.DOTALL
        )

        private val LOG_COLORS = mapOf(
            PRIORITY_VERBOSE to R.color.log_verbose,
            PRIORITY_DEBUG to R.color.log_debug,
            PRIORITY_INFO to R.color.log_info,
            PRIORITY_WARNING to R.color.log_warning,
            PRIORITY_ERROR to R.color.log_error,
            PRIORITY_ASSERT to R.color.log_assert
        )

        private val SUPPORTED_FILTERS = listOf(
            PRIORITY_VERBOSE,
            PRIORITY_DEBUG,
            PRIORITY_INFO,
            PRIORITY_WARNING,
            PRIORITY_ERROR,
            PRIORITY_ASSERT
        )
    }

    internal var time: Date
    internal var priority: String
    internal var tag: String
    internal var content: String

    init {
        val matcher: Matcher = sLogcatPattern.matcher(line)
        if (!matcher.find()) {
            throw IllegalStateException("logcat pattern not match: $line")
        }

        val timeText = matcher.group(1) ?: error("timeText error")
        val priorityText = matcher.group(2) ?: error("priorityText error")
        val tagText = matcher.group(3) ?: error("tagText error")
        val contentText = matcher.group(4) ?: error("contentText error")

        time = SimpleDateFormat("MM-dd hh:mm:ss.SSS", Locale.getDefault()).parse(timeText) ?: error("parse timeText error")
        priority = priorityText
        tag = tagText
        content = contentText
    }

    @ColorRes
    internal fun getColorRes(): Int {
        return LOG_COLORS[priority] ?: error("Color not found for priority: $priority")
    }

    internal fun isNotFiltered(filter: String): Boolean {
        return SUPPORTED_FILTERS.indexOf(priority) >= SUPPORTED_FILTERS.indexOf(filter)
    }
}
