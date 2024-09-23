package com.eiyooooo.superwindow.adapters

import android.view.View
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.view.animation.PathInterpolatorCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.transition.ChangeBounds
import androidx.transition.TransitionManager
import com.eiyooooo.superwindow.R
import com.eiyooooo.superwindow.entities.WidgetCardData
import com.eiyooooo.superwindow.entities.WidgetCardGroup
import com.eiyooooo.superwindow.viewmodels.MainActivityViewModel
import com.eiyooooo.superwindow.views.MainActivity
import com.eiyooooo.superwindow.views.WidgetCardView
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class WidgetCardManager(private val mainActivity: MainActivity, private val mainModel: MainActivityViewModel) {

    private val controlPanelWidgetCard = WidgetCardView(mainActivity, WidgetCardData(true, "controlPanel"))
    private val widgetCards: MutableMap<String, WidgetCardView> = mutableMapOf()

    fun init() {
        mainActivity.lifecycleScope.launch {
            controlPanelWidgetCard.setContentView(mainActivity.getControlPanelExpandedView())
        }

        mainModel.widgetCardGroup.observe(mainActivity) {//TODO: handle pendingWidgetCard
            mainModel.dualSplitHandlePosition.removeObserver(dualSplitHandlePositionObserver)

            when (it.foregroundWidgetCardCount) {
                1 -> {
                    widgetCardCountChanged(1)
                    showSingleWidgetCard(it)
                }

                2 -> {
                    widgetCardCountChanged(2)
                    showDualWidgetCard(it)
                    mainModel.dualSplitHandlePosition.observe(mainActivity, dualSplitHandlePositionObserver)
                }

                3 -> {
                    widgetCardCountChanged(3)
                    showTripleWidgetCard(it)
                }
            }

            mainModel.lastWidgetCardGroup = it
        }

        //TODO: remove this test module
        mainActivity.lifecycleScope.launch {
            delay(2000)
            mainModel.lastWidgetCardGroup?.copy(secondWidgetCard = WidgetCardData(false, "test1"), thirdWidgetCard = null)?.let { mainModel.updateWidgetCardGroup(it) }
        }
    }

    private fun showSingleWidgetCard(group: WidgetCardGroup) {
        val showWidgetCard = getWidgetCard(group.firstWidgetCard)
        showWidgetCard.getControlBar().visibility = View.GONE
        mainActivity.bindingExpanded.firstView.removeAllViews()
        mainActivity.bindingExpanded.secondView.removeAllViews()
        mainActivity.bindingExpanded.thirdView.removeAllViews()
        mainActivity.bindingExpanded.firstView.addView(showWidgetCard.getRootView())
        mainActivity.bindingExpanded.leftSplitHandle.setWidgetCards()
        mainActivity.bindingExpanded.leftSplitHandle.setOnDragHandle(null)
        mainActivity.bindingExpanded.rightSplitHandle.setWidgetCards()
        mainActivity.bindingExpanded.rightSplitHandle.setOnDragHandle(null)
    }

    private fun showDualWidgetCard(group: WidgetCardGroup) {
        val showFirstWidgetCard = getWidgetCard(group.firstWidgetCard)
        val showSecondWidgetCard = getWidgetCard(group.secondWidgetCard!!)
        showFirstWidgetCard.getControlBar().visibility = View.VISIBLE
        showSecondWidgetCard.getControlBar().visibility = View.VISIBLE
        mainActivity.bindingExpanded.firstView.removeAllViews()
        mainActivity.bindingExpanded.secondView.removeAllViews()
        mainActivity.bindingExpanded.thirdView.removeAllViews()
        mainActivity.bindingExpanded.firstView.addView(showFirstWidgetCard.getRootView())
        mainActivity.bindingExpanded.secondView.addView(showSecondWidgetCard.getRootView())
        mainActivity.bindingExpanded.leftSplitHandle.setWidgetCards(showFirstWidgetCard, showSecondWidgetCard)
        mainActivity.bindingExpanded.leftSplitHandle.setOnDragHandle { mainModel.updateDualSplitHandlePosition(it) }
        mainActivity.bindingExpanded.rightSplitHandle.setWidgetCards()
        mainActivity.bindingExpanded.rightSplitHandle.setOnDragHandle(null)
    }

    private fun showTripleWidgetCard(group: WidgetCardGroup) {
        val showFirstWidgetCard = getWidgetCard(group.firstWidgetCard)
        val showSecondWidgetCard = getWidgetCard(group.secondWidgetCard!!)
        val showThirdWidgetCard = getWidgetCard(group.thirdWidgetCard!!)
        showFirstWidgetCard.getControlBar().visibility = View.VISIBLE
        showSecondWidgetCard.getControlBar().visibility = View.VISIBLE
        showThirdWidgetCard.getControlBar().visibility = View.VISIBLE
        mainActivity.bindingExpanded.firstView.removeAllViews()
        mainActivity.bindingExpanded.secondView.removeAllViews()
        mainActivity.bindingExpanded.thirdView.removeAllViews()
        mainActivity.bindingExpanded.firstView.addView(showFirstWidgetCard.getRootView())
        mainActivity.bindingExpanded.secondView.addView(showSecondWidgetCard.getRootView())
        mainActivity.bindingExpanded.thirdView.addView(showThirdWidgetCard.getRootView())
        mainActivity.bindingExpanded.leftSplitHandle.setWidgetCards(showFirstWidgetCard, showSecondWidgetCard, showThirdWidgetCard)
        mainActivity.bindingExpanded.leftSplitHandle.setOnDragHandle(null)//TODO
        mainActivity.bindingExpanded.rightSplitHandle.setWidgetCards(showFirstWidgetCard, showSecondWidgetCard, showThirdWidgetCard)
        mainActivity.bindingExpanded.rightSplitHandle.setOnDragHandle(null)//TODO
    }

    private fun getWidgetCard(widgetCard: WidgetCardData): WidgetCardView {
        return if (widgetCard.isControlPanel) {
            controlPanelWidgetCard
        } else if (widgetCards[widgetCard.identifier] == null) {
            val newWidgetCard = WidgetCardView(mainActivity, widgetCard)//TODO: change to TextureView
            widgetCards[widgetCard.identifier] = newWidgetCard
            newWidgetCard
        } else {
            widgetCards[widgetCard.identifier]!!
        }
    }

    private val constraintSet = ConstraintSet()

    private fun widgetCardCountChanged(viewCount: Int) {
        constraintSet.clone(mainActivity.bindingExpanded.widgetContainer)

        val transition = ChangeBounds().apply {
            duration = 250
            interpolator = PathInterpolatorCompat.create(0.25f, 0.1f, 0.25f, 1f)
        }
        TransitionManager.beginDelayedTransition(mainActivity.bindingExpanded.widgetContainer, transition)

        when (viewCount) {
            1 -> {
                constraintSet.constrainPercentWidth(R.id.first_view, 1.0f)
                constraintSet.setVisibility(R.id.second_view, ConstraintSet.GONE)
                constraintSet.setVisibility(R.id.third_view, ConstraintSet.GONE)
                constraintSet.setVisibility(R.id.left_split_handle, ConstraintSet.GONE)
                constraintSet.setVisibility(R.id.right_split_handle, ConstraintSet.GONE)
            }

            2 -> {
                constraintSet.constrainPercentWidth(R.id.first_view, 0.5f)
                constraintSet.constrainPercentWidth(R.id.second_view, 0.5f)
                constraintSet.setVisibility(R.id.second_view, ConstraintSet.VISIBLE)
                constraintSet.setVisibility(R.id.left_split_handle, ConstraintSet.VISIBLE)
                constraintSet.setVisibility(R.id.third_view, ConstraintSet.GONE)
                constraintSet.setVisibility(R.id.right_split_handle, ConstraintSet.GONE)
            }

            3 -> {
                constraintSet.constrainPercentWidth(R.id.first_view, 0.33f)
                constraintSet.constrainPercentWidth(R.id.second_view, 0.33f)
                constraintSet.constrainPercentWidth(R.id.third_view, 0.33f)
                constraintSet.setVisibility(R.id.second_view, ConstraintSet.VISIBLE)
                constraintSet.setVisibility(R.id.third_view, ConstraintSet.VISIBLE)
                constraintSet.setVisibility(R.id.left_split_handle, ConstraintSet.VISIBLE)
                constraintSet.setVisibility(R.id.right_split_handle, ConstraintSet.VISIBLE)
            }
        }

        constraintSet.applyTo(mainActivity.bindingExpanded.widgetContainer)
    }

    private val dualSplitHandlePositionObserver = Observer<Float> {
        if (it == -1f) return@Observer
        val windowWidth = mainActivity.getResources().displayMetrics.widthPixels
        val newPercent = (it / windowWidth).coerceIn(0.2f, 0.8f)
        constraintSet.clone(mainActivity.bindingExpanded.widgetContainer)
        constraintSet.constrainPercentWidth(R.id.first_view, newPercent)
        constraintSet.constrainPercentWidth(R.id.second_view, 1 - newPercent)
        constraintSet.applyTo(mainActivity.bindingExpanded.widgetContainer)
    }
}
