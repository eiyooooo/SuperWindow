package com.eiyooooo.superwindow.ui.bar

import com.eiyooooo.superwindow.ui.bar.item.AppIconViewHolder
import com.eiyooooo.superwindow.ui.bar.item.DividerViewHolder
import com.eiyooooo.superwindow.ui.bar.item.LocalLauncherViewHolder
import com.eiyooooo.superwindow.ui.widgetcard.WidgetCardData
import com.eiyooooo.superwindow.ui.widgetcard.WidgetCardDataGroup
import rikka.recyclerview.IdBasedRecyclerViewAdapter
import rikka.recyclerview.IndexCreatorPool

class BarIconAdapter : IdBasedRecyclerViewAdapter(ArrayList()) {

    companion object {
        private const val ID_LOCAL_LAUNCHER = 0L
        private const val ID_DIVIDER_1 = 1L
        private const val ID_APP_ICON = 2L
    }

    init {
        setHasStableIds(true)
    }

    override fun onCreateCreatorPool(): IndexCreatorPool {
        return IndexCreatorPool()
    }

    private val dataList = mutableListOf<WidgetCardData>()

    internal fun update(group: WidgetCardDataGroup) {
        if (itemCount == 0 || getItemId(0) != ID_LOCAL_LAUNCHER) {
            addItem(LocalLauncherViewHolder.CREATOR, null, ID_LOCAL_LAUNCHER)
            notifyItemInserted(0)
        } else {
            notifyItemChanged(0)
        }
        //TODO: add control panel

        val newList = mutableListOf<WidgetCardData>().apply {
            add(group.firstWidgetCardData)
            group.secondWidgetCardData?.let { add(it) }
            group.thirdWidgetCardData?.let { add(it) }
            addAll(group.backgroundWidgetCardData)
        }.filter { it.icon != null && it.identifierValidated }

        val itemsToRemove = dataList.filterNot { oldItem ->
            newList.any { it.identifier == oldItem.identifier }
        }
        itemsToRemove.forEach { item ->
            val index = dataList.indexOf(item)
            if (index != -1) {
                dataList.removeAt(index)
                removeItemAt(index + 2)
                notifyItemRemoved(index + 2)

                if (dataList.isEmpty()) {
                    removeItemAt(1)
                    notifyItemRemoved(1)
                }
            }
        }

        val itemsToAdd = newList.filterNot { newItem ->
            dataList.any { it.identifier == newItem.identifier }
        }
        itemsToAdd.forEach { item ->
            dataList.add(item)
            if (dataList.size == 1) {
                addItem(DividerViewHolder.CREATOR, null, ID_DIVIDER_1)
                notifyItemInserted(1)
            }
            addItem(AppIconViewHolder.CREATOR, item, ID_APP_ICON + dataList.size - 1)
            notifyItemInserted(dataList.size + 1)
        }
    }
}
