package com.easycontrol.next.views.cards

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.setPadding
import com.easycontrol.next.R
import com.easycontrol.next.databinding.HomeItemContainerBinding
import com.easycontrol.next.databinding.ItemTitleDetailCardBinding
import com.easycontrol.next.utils.dp2px
import rikka.recyclerview.BaseViewHolder
import rikka.recyclerview.BaseViewHolder.Creator

class IntroductionViewHolder(private val binding: ItemTitleDetailCardBinding, root: View) : BaseViewHolder<Any?>(root), View.OnClickListener {

    companion object {
        val CREATOR = Creator<Any> { inflater: LayoutInflater, parent: ViewGroup? ->
            val outer = HomeItemContainerBinding.inflate(inflater, parent, false)
            val inner = ItemTitleDetailCardBinding.inflate(inflater, outer.root, true)
            IntroductionViewHolder(inner, outer.root)
        }
    }

    init {
        root.setOnClickListener(this)
    }

    private inline val root get() = binding.root
    private inline val title get() = binding.title
    private inline val divider get() = binding.divider
    private inline val detail get() = binding.detail
    private var isExpanded = true

    override fun onClick(v: View) {
        isExpanded = !isExpanded
        if (isExpanded) {
            root.setPadding(context.dp2px(18))
            divider.visibility = View.VISIBLE
            detail.visibility = View.VISIBLE
        } else {
            root.setPadding(context.dp2px(12))
            divider.visibility = View.GONE
            detail.visibility = View.GONE
        }
    }

    override fun onBind() {
        title.setText(R.string.introduction)
        detail.setText(R.string.introduction_detail)
        title.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.help, 0, 0, 0)
    }
}
