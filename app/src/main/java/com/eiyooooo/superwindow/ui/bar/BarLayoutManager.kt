package com.eiyooooo.superwindow.ui.bar

import android.content.Context
import androidx.recyclerview.widget.LinearLayoutManager

class BarLayoutManager(context: Context) : LinearLayoutManager(context, HORIZONTAL, false) {

    override fun canScrollHorizontally(): Boolean = false

    override fun canScrollVertically(): Boolean = false
}
