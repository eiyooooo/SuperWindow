package com.eiyooooo.superwindow.adapters

import android.annotation.SuppressLint
import com.eiyooooo.superwindow.entities.HomeData
import com.eiyooooo.superwindow.entities.ShizukuStatus
import com.eiyooooo.superwindow.viewmodels.MainActivityViewModel
import com.eiyooooo.superwindow.views.cards.IntroductionViewHolder
import com.eiyooooo.superwindow.views.cards.ShizukuInstructionViewHolder
import rikka.recyclerview.IdBasedRecyclerViewAdapter
import rikka.recyclerview.IndexCreatorPool

class HomeFragmentAdapter(private val mainModel: MainActivityViewModel) : IdBasedRecyclerViewAdapter(ArrayList()) {

    companion object {
        private const val ID_INTRODUCTION = 0L
        private const val ID_SHIZUKU = 1L
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

        notifyDataSetChanged()
    }
}
