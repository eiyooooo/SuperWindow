package com.eiyooooo.superwindow.adapters

import android.view.View
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.view.animation.PathInterpolatorCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.transition.AutoTransition
import androidx.transition.Transition
import androidx.transition.TransitionManager
import com.eiyooooo.superwindow.R
import com.eiyooooo.superwindow.entities.WidgetCardData
import com.eiyooooo.superwindow.entities.WidgetCardGroup
import com.eiyooooo.superwindow.utils.BlurUtils
import com.eiyooooo.superwindow.viewmodels.MainActivityViewModel
import com.eiyooooo.superwindow.views.MainActivity
import com.eiyooooo.superwindow.views.WidgetCardView
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class WidgetCardManager(private val mainActivity: MainActivity, private val mainModel: MainActivityViewModel) {

    private var forceRefreshUI = true
    private val controlPanelWidgetCard = WidgetCardView(mainActivity, WidgetCardData(true, "controlPanel"))
    private val widgetCards: MutableMap<String, WidgetCardView> = mutableMapOf()

    fun init() {
        forceRefreshUI = true
        BlurUtils.init(mainActivity.applicationContext)

        mainActivity.lifecycleScope.launch {
            controlPanelWidgetCard.setContentView(mainActivity.getControlPanelExpandedView())
        }

        mainModel.widgetCardGroup.observe(mainActivity) {//TODO: handle pendingWidgetCard
            mainModel.dualSplitHandlePosition.removeObserver(dualSplitHandlePositionObserver)
            when (it.foregroundWidgetCardCount) {
                1 -> {
                    showSingleWidgetCard(it)//TODO: not thread safe, could cause ConcurrentModificationException
                    mainActivity.bindingExpanded.widgetContainer.post {//TODO: not thread safe, could cause ConcurrentModificationException
                        onWidgetCardCountChanged(it)
                    }
                }

                2 -> {
                    showDualWidgetCard(it)
                    mainActivity.bindingExpanded.widgetContainer.post {
                        onWidgetCardCountChanged(it)
                        mainModel.dualSplitHandlePosition.observe(mainActivity, dualSplitHandlePositionObserver)
                    }
                }

                3 -> {
                    showTripleWidgetCard(it)
                    mainActivity.bindingExpanded.widgetContainer.post {
                        onWidgetCardCountChanged(it)
                    }
                }
            }
        }

        //TODO: remove this test module
        mainActivity.lifecycleScope.launch {
//            delay(2000)
//            mainModel.lastWidgetCardGroup?.copy(secondWidgetCard = WidgetCardData(false, "test1"), thirdWidgetCard = null)?.let { mainModel.updateWidgetCardGroup(it) }
            while (true) {
                delay(2000)
                mainModel.lastWidgetCardGroup?.copy(secondWidgetCard = WidgetCardData(false, "test1"))?.let { mainModel.updateWidgetCardGroup(it) }
                delay(2000)
                mainModel.lastWidgetCardGroup?.copy(thirdWidgetCard = WidgetCardData(false, "test2"))?.let { mainModel.updateWidgetCardGroup(it) }
                delay(2000)
                mainModel.lastWidgetCardGroup?.copy(thirdWidgetCard = null)?.let { mainModel.updateWidgetCardGroup(it) }
                delay(2000)
                mainModel.lastWidgetCardGroup?.copy(secondWidgetCard = null)?.let { mainModel.updateWidgetCardGroup(it) }
            }
        }
    }

    private var firstWidgetCard: WidgetCardView? = null
    private var secondWidgetCard: WidgetCardView? = null
    private var thirdWidgetCard: WidgetCardView? = null

    private fun showSingleWidgetCard(group: WidgetCardGroup) {
        val firstWidgetCard = getWidgetCard(group.firstWidgetCard)
        this.firstWidgetCard = firstWidgetCard
        this.secondWidgetCard = null
        this.thirdWidgetCard = null
        firstWidgetCard.getControlBar().visibility = View.GONE
        mainActivity.bindingExpanded.firstView.removeAllViews()
        mainActivity.bindingExpanded.secondView.removeAllViews()
        mainActivity.bindingExpanded.thirdView.removeAllViews()
        mainActivity.bindingExpanded.firstView.addView(firstWidgetCard.getRootView())
        mainActivity.bindingExpanded.leftSplitHandle.setWidgetCards()
        mainActivity.bindingExpanded.leftSplitHandle.setOnDragHandle(null)
        mainActivity.bindingExpanded.rightSplitHandle.setWidgetCards()
        mainActivity.bindingExpanded.rightSplitHandle.setOnDragHandle(null)
    }

    private fun showDualWidgetCard(group: WidgetCardGroup) {
        val firstWidgetCard = getWidgetCard(group.firstWidgetCard)
        val secondWidgetCard = getWidgetCard(group.secondWidgetCard!!)
        this.firstWidgetCard = firstWidgetCard
        this.secondWidgetCard = secondWidgetCard
        this.thirdWidgetCard = null
        firstWidgetCard.getControlBar().visibility = View.VISIBLE
        secondWidgetCard.getControlBar().visibility = View.VISIBLE
        mainActivity.bindingExpanded.firstView.removeAllViews()
        mainActivity.bindingExpanded.secondView.removeAllViews()
        mainActivity.bindingExpanded.thirdView.removeAllViews()
        mainActivity.bindingExpanded.firstView.addView(firstWidgetCard.getRootView())
        mainActivity.bindingExpanded.secondView.addView(secondWidgetCard.getRootView())
        mainActivity.bindingExpanded.leftSplitHandle.setWidgetCards(firstWidgetCard, secondWidgetCard)
        mainActivity.bindingExpanded.leftSplitHandle.setOnDragHandle { mainModel.updateDualSplitHandlePosition(it) }
        mainActivity.bindingExpanded.rightSplitHandle.setWidgetCards()
        mainActivity.bindingExpanded.rightSplitHandle.setOnDragHandle(null)
    }

    private fun showTripleWidgetCard(group: WidgetCardGroup) {
        val firstWidgetCard = getWidgetCard(group.firstWidgetCard)
        val secondWidgetCard = getWidgetCard(group.secondWidgetCard!!)
        val thirdWidgetCard = getWidgetCard(group.thirdWidgetCard!!)
        this.firstWidgetCard = firstWidgetCard
        this.secondWidgetCard = secondWidgetCard
        this.thirdWidgetCard = thirdWidgetCard
        firstWidgetCard.getControlBar().visibility = View.VISIBLE
        secondWidgetCard.getControlBar().visibility = View.VISIBLE
        thirdWidgetCard.getControlBar().visibility = View.VISIBLE
        mainActivity.bindingExpanded.firstView.removeAllViews()
        mainActivity.bindingExpanded.secondView.removeAllViews()
        mainActivity.bindingExpanded.thirdView.removeAllViews()
        mainActivity.bindingExpanded.firstView.addView(firstWidgetCard.getRootView())
        mainActivity.bindingExpanded.secondView.addView(secondWidgetCard.getRootView())
        mainActivity.bindingExpanded.thirdView.addView(thirdWidgetCard.getRootView())
        mainActivity.bindingExpanded.leftSplitHandle.setWidgetCards(firstWidgetCard, secondWidgetCard, thirdWidgetCard)
        mainActivity.bindingExpanded.leftSplitHandle.setOnDragHandle(null)//TODO
        mainActivity.bindingExpanded.rightSplitHandle.setWidgetCards(firstWidgetCard, secondWidgetCard, thirdWidgetCard)
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

    private fun onWidgetCardCountChanged(group: WidgetCardGroup) {
        val oldWidgetCardCount = if (forceRefreshUI) -1 else mainModel.lastWidgetCardGroup?.foregroundWidgetCardCount ?: -1
        val newWidgetCardCount = group.foregroundWidgetCardCount
        mainModel.lastWidgetCardGroup = group
        if (newWidgetCardCount == oldWidgetCardCount) return
        forceRefreshUI = false

        constraintSet.clone(mainActivity.bindingExpanded.widgetContainer)

        if (oldWidgetCardCount != -1) {
            firstWidgetCard?.makeBlur()
            secondWidgetCard?.makeBlur()
            thirdWidgetCard?.makeBlur()
            val transition = AutoTransition().apply {
                duration = 250
                interpolator = PathInterpolatorCompat.create(0.25f, 0.1f, 0.25f, 1f)
                addListener(object : Transition.TransitionListener {
                    override fun onTransitionStart(transition: Transition) {
                    }

                    override fun onTransitionEnd(transition: Transition) {
                        firstWidgetCard?.startBlurTransitAnimation()
                        secondWidgetCard?.startBlurTransitAnimation()
                        thirdWidgetCard?.startBlurTransitAnimation()
                    }

                    override fun onTransitionCancel(transition: Transition) {
                        firstWidgetCard?.startBlurTransitAnimation()
                        secondWidgetCard?.startBlurTransitAnimation()
                        thirdWidgetCard?.startBlurTransitAnimation()
                    }

                    override fun onTransitionPause(transition: Transition) {
                    }

                    override fun onTransitionResume(transition: Transition) {
                    }
                })
            }
            TransitionManager.beginDelayedTransition(mainActivity.bindingExpanded.widgetContainer, transition)
        }

        when (newWidgetCardCount) {
            1 -> {
                mainModel.updateDualSplitHandlePosition(-1f)
                constraintSet.constrainPercentWidth(R.id.first_view, 1.0f)
                constraintSet.setVisibility(R.id.second_view, ConstraintSet.GONE)
                constraintSet.setVisibility(R.id.third_view, ConstraintSet.GONE)
                constraintSet.setVisibility(R.id.left_split_handle, ConstraintSet.GONE)
                constraintSet.setVisibility(R.id.right_split_handle, ConstraintSet.GONE)
                if (group.isControlPanelForeground) {
                    mainActivity.bindingControlPanelExpanded.bottomNavigation.visibility = View.GONE
                    mainActivity.bindingControlPanelExpanded.navigationRail.visibility = View.VISIBLE
                }
            }

            2 -> {
                constraintSet.constrainPercentWidth(R.id.first_view, 0.5f)
                constraintSet.constrainPercentWidth(R.id.second_view, 0.5f)
                constraintSet.setVisibility(R.id.second_view, ConstraintSet.VISIBLE)
                constraintSet.setVisibility(R.id.left_split_handle, ConstraintSet.VISIBLE)
                constraintSet.setVisibility(R.id.third_view, ConstraintSet.GONE)
                constraintSet.setVisibility(R.id.right_split_handle, ConstraintSet.GONE)
                if (group.isControlPanelForeground) {
                    mainActivity.bindingControlPanelExpanded.bottomNavigation.visibility = View.VISIBLE
                    mainActivity.bindingControlPanelExpanded.navigationRail.visibility = View.GONE
                }
            }

            3 -> {
                mainModel.updateDualSplitHandlePosition(-1f)
                constraintSet.constrainPercentWidth(R.id.first_view, 0.33f)
                constraintSet.constrainPercentWidth(R.id.second_view, 0.33f)
                constraintSet.constrainPercentWidth(R.id.third_view, 0.33f)
                constraintSet.setVisibility(R.id.second_view, ConstraintSet.VISIBLE)
                constraintSet.setVisibility(R.id.third_view, ConstraintSet.VISIBLE)
                constraintSet.setVisibility(R.id.left_split_handle, ConstraintSet.VISIBLE)
                constraintSet.setVisibility(R.id.right_split_handle, ConstraintSet.VISIBLE)
                if (group.isControlPanelForeground) {
                    mainActivity.bindingControlPanelExpanded.bottomNavigation.visibility = View.VISIBLE
                    mainActivity.bindingControlPanelExpanded.navigationRail.visibility = View.GONE
                }
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
