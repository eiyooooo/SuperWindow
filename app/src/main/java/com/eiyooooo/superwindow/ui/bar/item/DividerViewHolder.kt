package com.eiyooooo.superwindow.ui.bar.item

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.eiyooooo.superwindow.databinding.ItemBarDividerBinding
import rikka.recyclerview.BaseViewHolder
import rikka.recyclerview.BaseViewHolder.Creator

class DividerViewHolder(root: View) : BaseViewHolder<Any?>(root) {

    companion object {
        val CREATOR = Creator<Any> { inflater: LayoutInflater, parent: ViewGroup? ->
            val binding = ItemBarDividerBinding.inflate(inflater, parent, false)
            DividerViewHolder(binding.root)
        }
    }
}
