package com.eiyooooo.superwindow.adapters

import android.view.View
import androidx.lifecycle.lifecycleScope
import com.eiyooooo.superwindow.entities.WidgetCardData
import com.eiyooooo.superwindow.entities.WindowMode.DUAL
import com.eiyooooo.superwindow.entities.WindowMode.SINGLE
import com.eiyooooo.superwindow.entities.WindowMode.TRIPLE
import com.eiyooooo.superwindow.viewmodels.MainActivityViewModel
import com.eiyooooo.superwindow.views.MainActivity
import com.eiyooooo.superwindow.views.WidgetCardView
import kotlinx.coroutines.launch

class WidgetCardManager(private val mainActivity: MainActivity, private val mainModel: MainActivityViewModel) {

    private val controlPanelWidgetCard = WidgetCardView(mainActivity, WidgetCardData(true, "controlPanel"))
    private val widgetCards: MutableMap<String, WidgetCardView> = mutableMapOf()

    fun init() {
        mainActivity.lifecycleScope.launch {
            controlPanelWidgetCard.setContentView(mainActivity.getControlPanelExpandedView())
        }

//        mainModel.windowMode.observe(mainActivity) {
//            //TODO: 取消windowMode硬依赖，动态更换
//        }
        when (mainModel.windowMode.value!!) {//TODO: 取消windowMode硬依赖
            SINGLE -> {
                mainModel.firstWidgetCardData.observe(mainActivity) {
                    mainActivity.bindingExpanded.widgetContainer.removeAllViews()
                    val showWidget = if (it.isControlPanel) {
                        controlPanelWidgetCard
                    } else if (widgetCards[it.identifier] == null) {
                        val widgetCard = WidgetCardView(mainActivity, it)//TODO: change to TextureView
                        widgetCards[it.identifier] = widgetCard
                        widgetCard
                    } else {
                        widgetCards[it.identifier]!!
                    }
                    showWidget.getControlBar().visibility = View.GONE
                    mainActivity.bindingExpanded.widgetContainer.addView(showWidget.getRootView())
                }
            }

            DUAL -> {

            }

            TRIPLE -> {

            }
        }
    }
//        it.widgetContainer.setTargetView(it.splitHandle)
//        it.leftView.widgetView.setTargetView(it.leftView.controlBar)
//        it.rightView.widgetView.setTargetView(it.rightView.controlBar)
//
//        it.splitHandle.setOnTouchListener(dualSplitHandleListener)
//
//        it.leftView.contentContainer.addView(bindingControlPanel.root)


//    private fun updateDualLayout(newX: Float) {
//        if (newX < 0) return
//
//        val constraintSet = ConstraintSet()
//        constraintSet.clone(bindingExpanded.widgetContainer)
//
//        constraintSet.connect(R.id.split_handle, ConstraintSet.START, R.id.widget_container, ConstraintSet.START, newX.toInt())
//        constraintSet.connect(R.id.split_handle, ConstraintSet.END, R.id.right_view, ConstraintSet.START, 0)
//        constraintSet.connect(R.id.left_view, ConstraintSet.END, R.id.split_handle, ConstraintSet.START, dp2px(4))
//        constraintSet.connect(R.id.right_view, ConstraintSet.START, R.id.split_handle, ConstraintSet.END, dp2px(4))
//
//        constraintSet.applyTo(bindingExpanded.widgetContainer)
//    }
}