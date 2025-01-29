package com.eiyooooo.superwindow.ui.bar.item

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.eiyooooo.superwindow.R
import com.eiyooooo.superwindow.databinding.ItemBarAppIconBinding
import com.eiyooooo.superwindow.ui.main.MainActivity
import com.eiyooooo.superwindow.ui.widgetcard.controlPanelWidgetCardData
import com.eiyooooo.superwindow.util.setIconTouchEffect
import rikka.recyclerview.BaseViewHolder
import rikka.recyclerview.BaseViewHolder.Creator

class ControlPanelViewHolder(private val binding: ItemBarAppIconBinding, root: View) : BaseViewHolder<Any?>(root), View.OnLongClickListener {

    companion object {
        val CREATOR = Creator<Any> { inflater: LayoutInflater, parent: ViewGroup? ->
            val binding = ItemBarAppIconBinding.inflate(inflater, parent, false)
            ControlPanelViewHolder(binding, binding.root)
        }
    }

    init {
        root.setIconTouchEffect()
        root.setOnLongClickListener(this)
    }

    private inline val iconView get() = binding.icon

    override fun onLongClick(v: View): Boolean {
        (context as? MainActivity)?.addWidgetCard(iconView, controlPanelWidgetCardData)
        return true
    }

    override fun onBind() {
        iconView.setImageResource(R.drawable.home_icon)
    }
}
