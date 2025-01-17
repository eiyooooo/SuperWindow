package com.eiyooooo.superwindow.ui.bar.item

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.eiyooooo.superwindow.R
import com.eiyooooo.superwindow.databinding.ItemBarDrawableBinding
import com.eiyooooo.superwindow.ui.contentpanel.LocalContentPanelFragment
import com.eiyooooo.superwindow.ui.main.MainActivity
import com.eiyooooo.superwindow.util.setIconTouchEffect
import rikka.recyclerview.BaseViewHolder
import rikka.recyclerview.BaseViewHolder.Creator

class LocalLauncherViewHolder(private val binding: ItemBarDrawableBinding, root: View) : BaseViewHolder<Any?>(root), View.OnClickListener {

    companion object {
        val CREATOR = Creator<Any> { inflater: LayoutInflater, parent: ViewGroup? ->
            val binding = ItemBarDrawableBinding.inflate(inflater, parent, false)
            LocalLauncherViewHolder(binding, binding.root)
        }
    }

    init {
        root.setIconTouchEffect()
        root.setOnClickListener(this)
    }

    private inline val root get() = binding.root

    override fun onClick(v: View) {
        (context as? MainActivity)?.showElevatedFragment(LocalContentPanelFragment())
    }

    override fun onBind() {
        root.setImageResource(R.drawable.rocket_launch)
    }
}
