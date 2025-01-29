package com.eiyooooo.superwindow.ui.bar.item

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.eiyooooo.superwindow.R
import com.eiyooooo.superwindow.databinding.ItemBarAppIconBinding
import com.eiyooooo.superwindow.ui.contentpanel.LocalContentPanelFragment
import com.eiyooooo.superwindow.ui.main.MainActivity
import com.eiyooooo.superwindow.util.setIconTouchEffect
import rikka.recyclerview.BaseViewHolder
import rikka.recyclerview.BaseViewHolder.Creator

class LocalLauncherViewHolder(private val binding: ItemBarAppIconBinding, root: View) : BaseViewHolder<Any?>(root), View.OnClickListener {

    companion object {
        val CREATOR = Creator<Any> { inflater: LayoutInflater, parent: ViewGroup? ->
            val binding = ItemBarAppIconBinding.inflate(inflater, parent, false)
            LocalLauncherViewHolder(binding, binding.root)
        }
    }

    init {
        root.setIconTouchEffect()
        root.setOnClickListener(this)
    }

    private inline val iconView get() = binding.icon

    override fun onClick(v: View) {
        (context as? MainActivity)?.showElevatedFragment(LocalContentPanelFragment())
    }

    override fun onBind() {
        iconView.setImageResource(R.drawable.rocket_launch_icon)
    }
}
