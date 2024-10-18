package com.eiyooooo.superwindow.ui.widgetcard

import android.os.Build
import android.view.View
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.view.animation.PathInterpolatorCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.transition.AutoTransition
import androidx.transition.Transition
import androidx.transition.TransitionManager
import com.eiyooooo.superwindow.R
import com.eiyooooo.superwindow.contentprovider.LocalContent
import com.eiyooooo.superwindow.ui.main.MainActivity
import com.eiyooooo.superwindow.ui.main.MainActivityViewModel
import com.eiyooooo.superwindow.ui.main.ShizukuStatus
import com.eiyooooo.superwindow.ui.view.WidgetCardView
import com.eiyooooo.superwindow.util.BlurUtils
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class WidgetCardManager(private val mainActivity: MainActivity, private val mainModel: MainActivityViewModel) {

    private var forceRefreshUI = true
    private lateinit var controlPanelWidgetCard: WidgetCardView
    private val widgetCards: MutableMap<String, WidgetCardView> = mutableMapOf()

    fun init() {
        forceRefreshUI = true
        BlurUtils.init(mainActivity.applicationContext)

        mainModel.widgetCardDataGroup.observe(mainActivity) {//TODO: handle pendingWidgetCard
            mainModel.dualSplitHandlePosition.removeObserver(dualSplitHandlePositionObserver)
            when (it.foregroundWidgetCardCount) {
                1 -> {
                    showSingleWidgetCard(it)
                    onWidgetCardCountChanged(it)
                }

                2 -> {
                    showDualWidgetCard(it)
                    onWidgetCardCountChanged(it)
                    mainModel.dualSplitHandlePosition.observe(mainActivity, dualSplitHandlePositionObserver)
                }

                3 -> {
                    showTripleWidgetCard(it)
                    onWidgetCardCountChanged(it)
                }
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            mainActivity.lifecycleScope.launch {
                mainModel.shizukuStatus.collect {
                    if (it == ShizukuStatus.HAVE_PERMISSION) {
                        LocalContent.init()
                    } else {
                        LocalContent.destroy()
                    }
                }
            }

            mainActivity.lifecycleScope.launch {
                LocalContent.runningTasksInVD.collect { runningTasksInVD ->
                    widgetCards.values.filter { !runningTasksInVD.containsKey(it.displayId) }.forEach { card ->
                        card.release()
                        removeWidgetCard(card)
                    }
                }
            }
        }

        //TODO: remove this test module
        mainActivity.lifecycleScope.launch {
            mainModel.shizukuStatus.filter { it == ShizukuStatus.HAVE_PERMISSION }.first()
            delay(2000)
            mainModel.updateWidgetCardDataGroup {
                it.copy(secondWidgetCardData = WidgetCardData("com.coloros.note", "local"), thirdWidgetCardData = WidgetCardData("com.samsung.android.app.notes", "local"))
            }
        }
    }

    fun destroy() {
        firstWidgetCard?.release()
        secondWidgetCard?.release()
        thirdWidgetCard?.release()
        widgetCards.clear()
    }

    fun makeCardsBlur(blur: Boolean) {
        if (blur) {
            firstWidgetCard?.makeBlur()
            secondWidgetCard?.makeBlur()
            thirdWidgetCard?.makeBlur()
        } else {
            firstWidgetCard?.startBlurTransitAnimation()
            secondWidgetCard?.startBlurTransitAnimation()
            thirdWidgetCard?.startBlurTransitAnimation()
        }
    }

    private var firstWidgetCard: WidgetCardView? = null
    private var secondWidgetCard: WidgetCardView? = null
    private var thirdWidgetCard: WidgetCardView? = null

    private fun showSingleWidgetCard(group: WidgetCardDataGroup) {
        val firstWidgetCard = group.getWidgetCard(1)
        this.firstWidgetCard = firstWidgetCard
        this.secondWidgetCard = null
        this.thirdWidgetCard = null
        firstWidgetCard.setControlBarVisibility(View.GONE)
        mainActivity.bindingExpanded.firstView.removeAllViews()
        mainActivity.bindingExpanded.secondView.removeAllViews()
        mainActivity.bindingExpanded.thirdView.removeAllViews()
        mainActivity.bindingExpanded.firstView.addView(firstWidgetCard.getRootView())
        mainActivity.bindingExpanded.leftSplitHandle.setWidgetCards()
        mainActivity.bindingExpanded.leftSplitHandle.setOnDragHandle(null)
        mainActivity.bindingExpanded.rightSplitHandle.setWidgetCards()
        mainActivity.bindingExpanded.rightSplitHandle.setOnDragHandle(null)
    }

    private fun showDualWidgetCard(group: WidgetCardDataGroup) {
        val firstWidgetCard = group.getWidgetCard(1)
        val secondWidgetCard = group.getWidgetCard(2)
        this.firstWidgetCard = firstWidgetCard
        this.secondWidgetCard = secondWidgetCard
        this.thirdWidgetCard = null
        firstWidgetCard.setControlBarVisibility(View.VISIBLE)
        secondWidgetCard.setControlBarVisibility(View.VISIBLE)
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

    private fun showTripleWidgetCard(group: WidgetCardDataGroup) {
        val firstWidgetCard = group.getWidgetCard(1)
        val secondWidgetCard = group.getWidgetCard(2)
        val thirdWidgetCard = group.getWidgetCard(3)
        this.firstWidgetCard = firstWidgetCard
        this.secondWidgetCard = secondWidgetCard
        this.thirdWidgetCard = thirdWidgetCard
        firstWidgetCard.setControlBarVisibility(View.VISIBLE)
        secondWidgetCard.setControlBarVisibility(View.VISIBLE)
        thirdWidgetCard.setControlBarVisibility(View.VISIBLE)
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

    private fun WidgetCardDataGroup.getWidgetCard(position: Int): WidgetCardView {
        val widgetCard = when (position) {
            1 -> firstWidgetCardData
            2 -> secondWidgetCardData
            else -> thirdWidgetCardData
        }!!
        return if (widgetCard.isControlPanel) {
            if (!this@WidgetCardManager::controlPanelWidgetCard.isInitialized) {
                controlPanelWidgetCard = WidgetCardView(mainActivity.bindingControlPanelExpanded.root, widgetCard)
            }
            controlPanelWidgetCard.updatePosition(position)
        } else if (widgetCards[widgetCard.identifier] == null) {
            val newWidgetCard = WidgetCardView(mainActivity, widgetCardData = widgetCard).updatePosition(position)
            widgetCards[widgetCard.identifier] = newWidgetCard
            newWidgetCard
        } else {
            widgetCards[widgetCard.identifier]!!.updatePosition(position)
        }
    }

    fun removeWidgetCard(target: WidgetCardView) {
        mainModel.updateWidgetCardDataGroup { current ->
            when (target.widgetCardData.identifier) {
                current.firstWidgetCardData.identifier -> {
                    if (secondWidgetCard != null) {
                        current.copy(firstWidgetCardData = current.secondWidgetCardData!!, secondWidgetCardData = current.thirdWidgetCardData, thirdWidgetCardData = null)
                    } else {
                        current.copy(firstWidgetCardData = controlPanelWidgetCard.widgetCardData)
                    }
                }

                current.secondWidgetCardData?.identifier -> {
                    current.copy(secondWidgetCardData = current.thirdWidgetCardData, thirdWidgetCardData = null)
                }

                current.thirdWidgetCardData?.identifier -> {
                    current.copy(thirdWidgetCardData = null)
                }

                else -> {
                    current.copy(backgroundWidgetCardData = current.backgroundWidgetCardData.filter { it.identifier != target.widgetCardData.identifier })
                }
            }
        }
        widgetCards.remove(target.widgetCardData.identifier)
    }

    private val constraintSet = ConstraintSet()

    private val onWidgetCardCountChangedTransition = AutoTransition().apply {
        duration = 250
        interpolator = PathInterpolatorCompat.create(0.25f, 0.1f, 0.25f, 1f)
        addListener(object : Transition.TransitionListener {
            override fun onTransitionStart(transition: Transition) {
            }

            override fun onTransitionEnd(transition: Transition) {
                firstWidgetCard?.startCoverTransitAnimation()
                secondWidgetCard?.startCoverTransitAnimation()
                thirdWidgetCard?.startCoverTransitAnimation()
            }

            override fun onTransitionCancel(transition: Transition) {
                firstWidgetCard?.startCoverTransitAnimation()
                secondWidgetCard?.startCoverTransitAnimation()
                thirdWidgetCard?.startCoverTransitAnimation()
            }

            override fun onTransitionPause(transition: Transition) {
            }

            override fun onTransitionResume(transition: Transition) {
            }
        })
    }

    private val onWidgetCardCountChangedHasPendingTransition = AutoTransition().apply {
        duration = 250
        interpolator = PathInterpolatorCompat.create(0.25f, 0.1f, 0.25f, 1f)
    }

    private fun onWidgetCardCountChanged(group: WidgetCardDataGroup) {
        val lastWidgetCardDataGroup = mainModel.lastWidgetCardDataGroup
        mainModel.lastWidgetCardDataGroup = group
        val oldWidgetCardCount = if (forceRefreshUI) -1 else lastWidgetCardDataGroup?.foregroundWidgetCardCount ?: -1
        val newWidgetCardCount = group.foregroundWidgetCardCount
        if (newWidgetCardCount == oldWidgetCardCount && group.pendingPosition == 0 && lastWidgetCardDataGroup?.pendingPosition == 0) return
        forceRefreshUI = false

        constraintSet.clone(mainActivity.bindingExpanded.widgetContainer)

        firstWidgetCard?.makeCover()
        secondWidgetCard?.makeCover()
        thirdWidgetCard?.makeCover()
        if (oldWidgetCardCount == -1) {
            mainActivity.bindingExpanded.widgetContainer.postDelayed({
                firstWidgetCard?.startCoverTransitAnimation()
                secondWidgetCard?.startCoverTransitAnimation()
                thirdWidgetCard?.startCoverTransitAnimation()
            }, 250)
        } else {
            TransitionManager.beginDelayedTransition(
                mainActivity.bindingExpanded.widgetContainer,
                if (group.pendingPosition != 0) onWidgetCardCountChangedHasPendingTransition else onWidgetCardCountChangedTransition
            )
        }

        when (newWidgetCardCount) {
            1 -> {
                mainModel.updateDualSplitHandlePosition(-1f)
                constraintSet.constrainPercentWidth(R.id.first_view, 1.0f)
                constraintSet.setVisibility(R.id.first_view, ConstraintSet.VISIBLE)
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
                constraintSet.setVisibility(R.id.first_view, ConstraintSet.VISIBLE)//TODO: pending widget card
                constraintSet.setVisibility(R.id.second_view, ConstraintSet.VISIBLE)//TODO: pending widget card
                constraintSet.setVisibility(R.id.third_view, ConstraintSet.GONE)
                constraintSet.setVisibility(R.id.left_split_handle, if (group.pendingPosition != 0) ConstraintSet.INVISIBLE else ConstraintSet.VISIBLE)
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
                constraintSet.setVisibility(R.id.first_view, ConstraintSet.VISIBLE)//TODO: pending widget card
                constraintSet.setVisibility(R.id.second_view, ConstraintSet.VISIBLE)//TODO: pending widget card
                constraintSet.setVisibility(R.id.third_view, ConstraintSet.VISIBLE)//TODO: pending widget card
                constraintSet.setVisibility(R.id.left_split_handle, if (group.pendingPosition != 0) ConstraintSet.INVISIBLE else ConstraintSet.VISIBLE)
                constraintSet.setVisibility(R.id.right_split_handle, if (group.pendingPosition != 0) ConstraintSet.INVISIBLE else ConstraintSet.VISIBLE)
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
