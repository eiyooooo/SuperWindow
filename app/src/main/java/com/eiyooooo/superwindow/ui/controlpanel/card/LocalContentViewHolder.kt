package com.eiyooooo.superwindow.ui.controlpanel.card

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.eiyooooo.superwindow.R
import com.eiyooooo.superwindow.databinding.HomeItemContainerBinding
import com.eiyooooo.superwindow.databinding.ItemTextCardBinding
import com.eiyooooo.superwindow.ui.contentpanel.LocalContentPanelFragment
import com.eiyooooo.superwindow.ui.main.MainActivity
import rikka.recyclerview.BaseViewHolder
import rikka.recyclerview.BaseViewHolder.Creator

class LocalContentViewHolder(private val binding: ItemTextCardBinding, root: View) : BaseViewHolder<Any?>(root), View.OnClickListener {

    companion object {
        val CREATOR = Creator<Any> { inflater: LayoutInflater, parent: ViewGroup? ->
            val outer = HomeItemContainerBinding.inflate(inflater, parent, false)
            val inner = ItemTextCardBinding.inflate(inflater, outer.root, true)
            LocalContentViewHolder(inner, outer.root)
        }
    }

    init {
        root.setOnClickListener(this)
    }

    private inline val root get() = binding.root

    override fun onClick(v: View) {
        (context as MainActivity).showElevatedFragment(LocalContentPanelFragment())
    }

    override fun onBind() {
        root.setText(R.string.local_app_launcher)
        root.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.rocket_launch, 0, 0, 0)
    }
}
