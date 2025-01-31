package com.eiyooooo.superwindow.ui.contentpanel

import android.content.pm.LauncherActivityInfo
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewPropertyAnimator
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.eiyooooo.superwindow.databinding.ItemAppBinding
import com.eiyooooo.superwindow.ui.widgetcard.WidgetCardData
import com.eiyooooo.superwindow.util.setIconTouchEffect
import com.github.promeg.pinyinhelper.Pinyin
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AppsAdapter(
    private val appsList: List<Pair<LauncherActivityInfo, Drawable>>,
    private val scope: CoroutineScope,
    private val selectingLetter: StateFlow<Char?>,
    private val supportLongClick: Boolean,
    private val callback: (ImageView, Boolean, WidgetCardData) -> Unit
) : RecyclerView.Adapter<AppsAdapter.ViewHolder>() {

    inner class ViewHolder(binding: ItemAppBinding) : RecyclerView.ViewHolder(binding.root) {
        val iconContainer: View = binding.iconContainer
        val icon: ImageView = binding.icon
        val appName: TextView = binding.appName
        val root: View = binding.root

        private var animator: ViewPropertyAnimator? = null

        fun updateItemAlphaForSelection(letter: Char?, selectedLetter: Char?) {
            val targetAlpha = if (selectedLetter == null || letter == selectedLetter) 1f else 0.1f
            animator?.cancel()
            animator = if (itemView.alpha != targetAlpha) {
                itemView.animate().alpha(targetAlpha).setDuration(250).apply {
                    start()
                }
            } else null
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(ItemAppBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun getItemCount(): Int = appsList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val (activityInfo, drawable) = appsList[position]
        val applicationInfo = activityInfo.applicationInfo
        val letter = Pinyin.toPinyin(activityInfo.label[0]).firstOrNull()?.uppercaseChar()?.takeIf { it.isLetter() } ?: '#'
        with(holder) {
            icon.setImageDrawable(drawable)
            appName.text = activityInfo.label
            root.setIconTouchEffect(0.75f, 100L, iconContainer)
            root.setOnClickListener {
                callback.invoke(icon, false, WidgetCardData(applicationInfo.packageName, "local", drawable))
            }
            if (supportLongClick) {
                root.setOnLongClickListener {
                    callback.invoke(icon, true, WidgetCardData(applicationInfo.packageName, "local", drawable))
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
