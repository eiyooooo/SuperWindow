package com.eiyooooo.superwindow.adapters

import com.eiyooooo.superwindow.viewmodels.MainActivityViewModel
import com.eiyooooo.superwindow.views.MainActivity

class WidgetCardManager(private val mainActivity: MainActivity, private val mainModel: MainActivityViewModel) {

//    fun a() {
//        it.widgetContainer.setTargetView(it.splitHandle)
//        it.leftView.widgetView.setTargetView(it.leftView.controlBar)
//        it.rightView.widgetView.setTargetView(it.rightView.controlBar)
//
//        it.splitHandle.setOnTouchListener(dualSplitHandleListener)
//
//        it.leftView.contentContainer.addView(bindingControlPanel.root)
//
//        mainModel.dualSplitHandlePosition.observe(this) {
//            updateDualLayout(it)
//        }
//    }
//
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