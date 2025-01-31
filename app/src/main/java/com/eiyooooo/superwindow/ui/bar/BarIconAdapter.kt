package com.eiyooooo.superwindow.ui.bar

import com.eiyooooo.superwindow.ui.bar.item.AppIconViewHolder
import com.eiyooooo.superwindow.ui.bar.item.ControlPanelViewHolder
import com.eiyooooo.superwindow.ui.bar.item.DividerViewHolder
import com.eiyooooo.superwindow.ui.bar.item.LocalLauncherViewHolder
import com.eiyooooo.superwindow.ui.bar.item.SpaceViewHolder
import com.eiyooooo.superwindow.ui.widgetcard.WidgetCardData
import com.eiyooooo.superwindow.ui.widgetcard.WidgetCardDataGroup
import rikka.recyclerview.IdBasedRecyclerViewAdapter
import rikka.recyclerview.IndexCreatorPool

class BarIconAdapter : IdBasedRecyclerViewAdapter(ArrayList()) {

    companion object {
        private const val ID_SPACE = 0L
        private const val ID_LOCAL_LAUNCHER = 1L
        private const val ID_CONTROL_PANEL = 2L
        private const val ID_DIVIDER_1 = 3L
        private const val ID_APP_ICON = 4L
    }

    init {
        setHasStableIds(true)
    }

    override fun onCreateCreatorPool(): IndexCreatorPool {
        return IndexCreatorPool()
    }

    private val dataList = mutableListOf<WidgetCardData>()

    internal fun update(group: WidgetCardDataGroup) {
        if (itemCount < 3) {
            addItem(SpaceViewHolder.CREATOR, null, ID_SPACE)
            addItem(LocalLauncherViewHolder.CREATOR, null, ID_LOCAL_LAUNCHER)
            addItem(ControlPanelViewHolder.CREATOR, null, ID_CONTROL_PANEL)
            notifyItemRangeInserted(0, 3)
        } else {
            notifyItemChanged(0)
        }

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
                removeItemAt(index + 4)
                notifyItemRemoved(index + 4)

                if (dataList.isEmpty()) {
                    removeItemAt(3)
                    notifyItemRemoved(3)
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
                notifyItemInserted(3)
            }
            addItem(AppIconViewHolder.CREATOR, item, ID_APP_ICON + dataList.size - 1)
            notifyItemInserted(dataList.size + 3)
        }
    }
}
