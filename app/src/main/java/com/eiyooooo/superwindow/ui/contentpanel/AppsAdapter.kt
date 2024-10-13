package com.eiyooooo.superwindow.ui.contentpanel

import android.content.pm.LauncherActivityInfo
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.eiyooooo.superwindow.databinding.ItemAppBinding
import com.github.promeg.pinyinhelper.Pinyin
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AppsAdapter(
    private val appsList: List<Pair<LauncherActivityInfo, Drawable>>,
    private val scope: CoroutineScope,
    private val selectingLetter: StateFlow<Char?>,
    private val callback: (String) -> Unit
) : RecyclerView.Adapter<AppsAdapter.ViewHolder>() {

    inner class ViewHolder(binding: ItemAppBinding) : RecyclerView.ViewHolder(binding.root) {
        val icon: ImageView = binding.icon
        val appName: TextView = binding.appName
        val click: View = binding.viewClick

        fun bindAlphaTransition(letter: Char?, selectedLetter: Char?) {
            val targetAlpha = if (selectedLetter == null || letter == selectedLetter) 1f else 0.25f
            if (itemView.alpha != targetAlpha) {
                itemView.animate().alpha(targetAlpha).setDuration(300).start()
            }
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
            click.setOnClickListener {
                callback.invoke(applicationInfo.packageName)
            }
            scope.launch {
                selectingLetter.collect {
                    bindAlphaTransition(letter, it)
                }
            }
        }
    }
}
