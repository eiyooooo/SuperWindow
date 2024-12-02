package com.eiyooooo.superwindow.ui.contentpanel

import android.annotation.SuppressLint
import android.content.pm.LauncherActivityInfo
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.eiyooooo.superwindow.databinding.ItemAppBinding
import com.eiyooooo.superwindow.ui.widgetcard.WidgetCardData
import com.github.promeg.pinyinhelper.Pinyin
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AppsAdapter(
    private val appsList: List<Pair<LauncherActivityInfo, Drawable>>,
    private val scope: CoroutineScope,
    private val selectingLetter: StateFlow<Char?>,
    private val supportLongClick: Boolean,
    private val callback: (Boolean, WidgetCardData) -> Unit
) : RecyclerView.Adapter<AppsAdapter.ViewHolder>() {

    inner class ViewHolder(binding: ItemAppBinding) : RecyclerView.ViewHolder(binding.root) {
        val icon: ImageView = binding.icon
        val appName: TextView = binding.appName
        val root: View = binding.root

        fun updateItemAlphaForSelection(letter: Char?, selectedLetter: Char?) {
            val targetAlpha = if (selectedLetter == null || letter == selectedLetter) 1f else 0.1f
            if (itemView.alpha != targetAlpha) {
                itemView.animate().alpha(targetAlpha).setDuration(250).start()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(ItemAppBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun getItemCount(): Int = appsList.size

    @SuppressLint("ClickableViewAccessibility")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val (activityInfo, drawable) = appsList[position]
        val applicationInfo = activityInfo.applicationInfo
        val letter = Pinyin.toPinyin(activityInfo.label[0]).firstOrNull()?.uppercaseChar()?.takeIf { it.isLetter() } ?: '#'
        with(holder) {
            icon.setImageDrawable(drawable)
            appName.text = activityInfo.label
            root.setOnTouchListener { _, event ->
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        icon.animate()
                            .scaleX(0.75f)
                            .scaleY(0.75f)
                            .setInterpolator(AccelerateDecelerateInterpolator())
                            .setDuration(150L)
                            .start()
                    }

                    MotionEvent.ACTION_UP,
                    MotionEvent.ACTION_CANCEL -> {
                        icon.animate()
                            .scaleX(1f)
                            .scaleY(1f)
                            .setInterpolator(AccelerateDecelerateInterpolator())
                            .setDuration(150L)
                            .start()
                    }
                }
                false
            }
            root.setOnClickListener {
                callback.invoke(false, WidgetCardData(applicationInfo.packageName, "local", drawable))
            }
            if (supportLongClick) {
                root.setOnLongClickListener {
                    callback.invoke(true, WidgetCardData(applicationInfo.packageName, "local", drawable))
                    true
                }
            }
            scope.launch {
                selectingLetter.collect {
                    updateItemAlphaForSelection(letter, it)
                }
            }
        }
    }
}
