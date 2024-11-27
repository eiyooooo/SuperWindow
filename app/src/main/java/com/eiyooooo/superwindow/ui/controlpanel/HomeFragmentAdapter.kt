package com.eiyooooo.superwindow.ui.controlpanel

import android.annotation.SuppressLint
import com.eiyooooo.superwindow.ui.controlpanel.card.ExpandModeHintViewHolder
import com.eiyooooo.superwindow.ui.controlpanel.card.IntroductionViewHolder
import com.eiyooooo.superwindow.ui.controlpanel.card.LocalContentViewHolder
import com.eiyooooo.superwindow.ui.controlpanel.card.ShizukuInstructionViewHolder
import com.eiyooooo.superwindow.ui.main.MainActivity
import com.eiyooooo.superwindow.ui.main.MainActivityViewModel
import com.eiyooooo.superwindow.ui.main.ShizukuStatus
import rikka.recyclerview.IdBasedRecyclerViewAdapter
import rikka.recyclerview.IndexCreatorPool

class HomeFragmentAdapter(private val mainActivity: MainActivity, private val mainModel: MainActivityViewModel) : IdBasedRecyclerViewAdapter(ArrayList()) {

    companion object {
        private const val ID_INTRODUCTION = 0L
        private const val ID_SHIZUKU = 1L
        private const val ID_EXPAND_MODE_HINT = 2L
        private const val ID_LOCAL_CONTENT = 3L
    }

    init {
        setHasStableIds(true)
    }

    override fun onCreateCreatorPool(): IndexCreatorPool {
        return IndexCreatorPool()
    }

    @SuppressLint("NotifyDataSetChanged")
    internal fun updateData(homeData: HomeData) {
        clear()

        addItem(IntroductionViewHolder.CREATOR, null, ID_INTRODUCTION)

        homeData.shizukuStatus.let {
            addItem(ShizukuInstructionViewHolder.CREATOR, it, ID_SHIZUKU)
            mainModel.stopCheckingShizukuPermission()
            if (it == ShizukuStatus.NO_PERMISSION) {
                mainModel.startCheckingShizukuPermission()
            }
        }
        if (!mainActivity.isExpanded) {
            addItem(ExpandModeHintViewHolder.CREATOR, null, ID_EXPAND_MODE_HINT)
        }
        if (mainActivity.isExpanded && homeData.localContentReady) {
            addItem(LocalContentViewHolder.CREATOR, null, ID_LOCAL_CONTENT)
        }

        notifyDataSetChanged()
    }
}
