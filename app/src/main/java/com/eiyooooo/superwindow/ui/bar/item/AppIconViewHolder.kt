package com.eiyooooo.superwindow.ui.bar.item

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.eiyooooo.superwindow.databinding.ItemBarAppIconBinding
import com.eiyooooo.superwindow.ui.main.MainActivity
import com.eiyooooo.superwindow.ui.widgetcard.WidgetCardData
import com.eiyooooo.superwindow.util.setIconTouchEffect
import rikka.recyclerview.BaseViewHolder
import rikka.recyclerview.BaseViewHolder.Creator

class AppIconViewHolder(private val binding: ItemBarAppIconBinding, root: View) : BaseViewHolder<WidgetCardData>(root), View.OnLongClickListener {

    companion object {
        val CREATOR = Creator<Any> { inflater: LayoutInflater, parent: ViewGroup? ->
            val binding = ItemBarAppIconBinding.inflate(inflater, parent, false)
            AppIconViewHolder(binding, binding.root)
        }
    }

    init {
        root.setIconTouchEffect()
        root.setOnLongClickListener(this)
    }

    private inline val root get() = binding.root

    override fun onLongClick(v: View): Boolean {
        (context as? MainActivity)?.addWidgetCard(root, data)
        return true
    }

    override fun onBind() {
        root.setImageDrawable(data.icon)
    }
}
