package com.eiyooooo.superwindow.views.cards

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.setPadding
import com.eiyooooo.superwindow.R
import com.eiyooooo.superwindow.databinding.HomeItemContainerBinding
import com.eiyooooo.superwindow.databinding.ItemTitleDetailCardBinding
import com.eiyooooo.superwindow.entities.ShizukuStatus
import com.eiyooooo.superwindow.utils.dp2px
import com.eiyooooo.superwindow.views.MainActivity
import rikka.recyclerview.BaseViewHolder
import rikka.recyclerview.BaseViewHolder.Creator
import rikka.shizuku.Shizuku

class ShizukuInstructionViewHolder(private val binding: ItemTitleDetailCardBinding, root: View) : BaseViewHolder<ShizukuStatus>(root), View.OnClickListener {

    companion object {
        val CREATOR = Creator<ShizukuStatus> { inflater: LayoutInflater, parent: ViewGroup? ->
            val outer = HomeItemContainerBinding.inflate(inflater, parent, false)
            val inner = ItemTitleDetailCardBinding.inflate(inflater, outer.root, true)
            ShizukuInstructionViewHolder(inner, outer.root)
        }
    }

    init {
        root.setOnClickListener(this)
    }

    private inline val root get() = binding.root
    private inline val title get() = binding.title
    private inline val divider get() = binding.divider
    private inline val detail get() = binding.detail

    private var clickMode: Boolean? = null

    override fun onClick(v: View) {
        clickMode?.let {
            if (it) {
                try {
                    val intent = Intent(Intent.ACTION_VIEW)
                    intent.addCategory(Intent.CATEGORY_BROWSABLE)
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    var url = "https://shizuku.rikka.app/guide/setup/"
                    if (title.text.contains("授权")) {
                        url = "https://shizuku.rikka.app/zh-hans/guide/setup/"
                    }
                    intent.setData(Uri.parse(url))
                    context.startActivity(intent)
                } catch (ignored: Exception) {
                    (context as? MainActivity)?.showSnackBar(context.getString(R.string.no_browser))
                }
            } else {
                Shizuku.requestPermission(0)
            }
        }
    }

    override fun onBind() {
        val paddingCollapsed = context.dp2px(12)
        val paddingExpanded = context.dp2px(18)

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S_V2) {
            clickMode = null
            configureCollapsedView(R.string.local_use_not_supported, paddingCollapsed)
            title.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.error, 0, 0, 0)
        } else {
            when (data) {
                ShizukuStatus.HAVE_PERMISSION -> {
                    clickMode = null
                    configureCollapsedView(R.string.Shizuku_connected, paddingCollapsed)
                }

                ShizukuStatus.NO_PERMISSION -> {
                    clickMode = false
                    configureExpandedView(R.string.Shizuku_explanation, R.string.Shizuku_authorization_instruction, paddingExpanded)
                }

                ShizukuStatus.VERSION_NOT_SUPPORT -> {
                    clickMode = true
                    configureExpandedView(R.string.Shizuku_explanation, R.string.Shizuku_need_update, paddingExpanded)
                }

                else -> {
                    clickMode = true
                    configureExpandedView(R.string.Shizuku_explanation, R.string.Shizuku_setup_instruction, paddingExpanded)
                }
            }
            title.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.shizuku, 0, 0, 0)
        }
    }

    private fun configureCollapsedView(titleResId: Int, padding: Int) {
        title.setText(titleResId)
        detail.text = ""
        root.setPadding(padding)
        divider.visibility = View.GONE
        detail.visibility = View.GONE
    }

    private fun configureExpandedView(titleResId: Int, detailResId: Int, padding: Int) {
        title.setText(titleResId)
        detail.setText(detailResId)
        root.setPadding(padding)
        divider.visibility = View.VISIBLE
        detail.visibility = View.VISIBLE
    }
}
