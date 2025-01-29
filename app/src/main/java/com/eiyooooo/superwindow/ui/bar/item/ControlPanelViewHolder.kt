package com.eiyooooo.superwindow.ui.bar.item

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.eiyooooo.superwindow.R
import com.eiyooooo.superwindow.databinding.ItemBarDrawableBinding
import com.eiyooooo.superwindow.ui.main.MainActivity
import com.eiyooooo.superwindow.ui.widgetcard.controlPanelWidgetCardData
import com.eiyooooo.superwindow.util.setIconTouchEffect
import rikka.recyclerview.BaseViewHolder
import rikka.recyclerview.BaseViewHolder.Creator

class ControlPanelViewHolder(private val binding: ItemBarDrawableBinding, root: View) : BaseViewHolder<Any?>(root), View.OnLongClickListener {

    companion object {
        val CREATOR = Creator<Any> { inflater: LayoutInflater, parent: ViewGroup? ->
            val binding = ItemBarDrawableBinding.inflate(inflater, parent, false)
            ControlPanelViewHolder(binding, binding.root)
        }
    }

    init {
        root.setIconTouchEffect()
        root.setOnLongClickListener(this)
    }

    private inline val root get() = binding.root

    override fun onLongClick(v: View): Boolean {
        (context as? MainActivity)?.addWidgetCard(root, controlPanelWidgetCardData)
        return true
    }

    override fun onBind() {
        root.setImageResource(R.drawable.home)
    }
}
