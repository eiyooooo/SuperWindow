package com.eiyooooo.superwindow.adapters

import android.annotation.SuppressLint
import com.eiyooooo.superwindow.views.cards.IntroductionViewHolder
import rikka.recyclerview.IdBasedRecyclerViewAdapter
import rikka.recyclerview.IndexCreatorPool

class HomeFragmentAdapter : IdBasedRecyclerViewAdapter(ArrayList()) {

    companion object {
        private const val ID_INTRODUCTION = 0L
    }

    init {
        setHasStableIds(true)
    }

    override fun onCreateCreatorPool(): IndexCreatorPool {
        return IndexCreatorPool()
    }

    @SuppressLint("NotifyDataSetChanged")
    internal fun updateData() {
        clear()

        addItem(IntroductionViewHolder.CREATOR, null, ID_INTRODUCTION)

        notifyDataSetChanged()
    }
}
