package com.eiyooooo.superwindow.ui.controlpanel.card

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.eiyooooo.superwindow.R
import com.eiyooooo.superwindow.databinding.HomeItemContainerBinding
import com.eiyooooo.superwindow.databinding.ItemTextCardBinding
import rikka.recyclerview.BaseViewHolder
import rikka.recyclerview.BaseViewHolder.Creator

class ExpandModeHintViewHolder(private val binding: ItemTextCardBinding, root: View) : BaseViewHolder<Any?>(root) {

    companion object {
        val CREATOR = Creator<Any> { inflater: LayoutInflater, parent: ViewGroup? ->
            val outer = HomeItemContainerBinding.inflate(inflater, parent, false)
            val inner = ItemTextCardBinding.inflate(inflater, outer.root, true)
            ExpandModeHintViewHolder(inner, outer.root)
        }
    }

    private inline val root get() = binding.root

    override fun onBind() {
        root.setText(R.string.expand_mode_hint)
        root.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.error, 0, 0, 0)
    }
}
