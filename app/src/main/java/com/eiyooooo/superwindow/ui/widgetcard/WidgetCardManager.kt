package com.eiyooooo.superwindow.ui.widgetcard

import android.os.Build
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.view.animation.PathInterpolatorCompat
import androidx.core.view.children
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

    private lateinit var controlPanelWidgetCard: WidgetCardView
    private val widgetCards: MutableMap<String, WidgetCardView> = mutableMapOf()

    fun init() {
        BlurUtils.init(mainActivity.applicationContext)

        mainModel.widgetCardDataGroup.observe(mainActivity) {
            when (it.foregroundWidgetCardCount) {
                1 -> showSingleWidgetCard(it)
                2 -> showDualWidgetCard(it)
                3 -> showTripleWidgetCard(it)
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
//            while (true) {
//                delay(2000)
//                mainModel.updateWidgetCardDataGroup {
//                    it.copy(secondWidgetCardData = WidgetCardData(false, "test1"))
//                }
//                delay(2000)
//                mainModel.updateWidgetCardDataGroup {
//                    it.copy(thirdWidgetCardData = WidgetCardData(false, "test2"))
//                }
//                delay(2000)
//                mainModel.updateWidgetCardDataGroup {
//                    it.copy(thirdWidgetCardData = null)
//                }
//                delay(2000)
//                mainModel.updateWidgetCardDataGroup {
//                    it.copy(secondWidgetCardData = null)
//                }
//            }
        }
    }

    fun destroy() {
        widgetCards.values.forEach {
            it.release()
        }
        widgetCards.clear()
    }

    private var foregroundWidgetCards: Array<out WidgetCardView>? = null
    private var lastWidgetCardDataGroup: WidgetCardDataGroup? = null

    private fun showSingleWidgetCard(group: WidgetCardDataGroup) {
        val firstWidgetCard = group.getWidgetCard(1)
        firstWidgetCard.setControlBarVisibility(View.GONE)
        refreshWidgetCardContainer(firstWidgetCard)
        mainActivity.bindingExpanded.leftSplitHandle.setWidgetCards()
        mainActivity.bindingExpanded.leftSplitHandle.setOnDragHandle(null)
        mainActivity.bindingExpanded.rightSplitHandle.setWidgetCards()
        mainActivity.bindingExpanded.rightSplitHandle.setOnDragHandle(null)
        onWidgetCardCountChanged(group, firstWidgetCard)
    }

    private fun showDualWidgetCard(group: WidgetCardDataGroup) {
        mainModel.dualSplitHandlePosition.removeObserver(dualSplitHandlePositionObserver)
        val firstWidgetCard = group.getWidgetCard(1)
        val secondWidgetCard = group.getWidgetCard(2)
        firstWidgetCard.setControlBarVisibility(View.VISIBLE)
        secondWidgetCard.setControlBarVisibility(View.VISIBLE)
        refreshWidgetCardContainer(firstWidgetCard, secondWidgetCard)
        mainActivity.bindingExpanded.leftSplitHandle.setWidgetCards(firstWidgetCard, secondWidgetCard)
        mainActivity.bindingExpanded.leftSplitHandle.setOnDragHandle { mainModel.updateDualSplitHandlePosition(it) }
        mainActivity.bindingExpanded.rightSplitHandle.setWidgetCards()
        mainActivity.bindingExpanded.rightSplitHandle.setOnDragHandle(null)
        onWidgetCardCountChanged(group, firstWidgetCard, secondWidgetCard)
        mainModel.dualSplitHandlePosition.observe(mainActivity, dualSplitHandlePositionObserver)
    }

    private fun showTripleWidgetCard(group: WidgetCardDataGroup) {
        val firstWidgetCard = group.getWidgetCard(1)
        val secondWidgetCard = group.getWidgetCard(2)
        val thirdWidgetCard = group.getWidgetCard(3)
        firstWidgetCard.setControlBarVisibility(View.VISIBLE)
        secondWidgetCard.setControlBarVisibility(View.VISIBLE)
        thirdWidgetCard.setControlBarVisibility(View.VISIBLE)
        refreshWidgetCardContainer(firstWidgetCard, secondWidgetCard, thirdWidgetCard)
        mainActivity.bindingExpanded.leftSplitHandle.setWidgetCards(firstWidgetCard, secondWidgetCard, thirdWidgetCard)
        mainActivity.bindingExpanded.leftSplitHandle.setOnDragHandle(null)//TODO
        mainActivity.bindingExpanded.rightSplitHandle.setWidgetCards(firstWidgetCard, secondWidgetCard, thirdWidgetCard)
        mainActivity.bindingExpanded.rightSplitHandle.setOnDragHandle(null)//TODO
        onWidgetCardCountChanged(group, firstWidgetCard, secondWidgetCard, thirdWidgetCard)
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

    private fun refreshWidgetCardContainer(vararg widgetCards: WidgetCardView) {
        mainActivity.bindingExpanded.widgetContainer.children.forEach {
            if (it is WidgetCardView) {
                mainActivity.bindingExpanded.widgetContainer.removeView(it)
            }
        }
        widgetCards.forEach {
            (it.parent as? ViewGroup)?.removeView(it)
            mainActivity.bindingExpanded.widgetContainer.addView(it)
        }
    }

    fun removeWidgetCard(target: WidgetCardView) {
        mainModel.updateWidgetCardDataGroup { current ->
            when (target.widgetCardData.identifier) {
                current.firstWidgetCardData.identifier -> {
                    if (foregroundWidgetCards?.size == 2) {
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

    fun makeCardsBlur(blur: Boolean) {
        if (blur) {
            foregroundWidgetCards?.forEach {
                it.makeBlur()
            }
        } else {
            foregroundWidgetCards?.forEach {
                it.startBlurTransitAnimation()
            }
        }
    }

    private val constraintSet = ConstraintSet()

    private val onWidgetCardCountChangedTransition = AutoTransition().apply {
        duration = 250
        interpolator = PathInterpolatorCompat.create(0.25f, 0.1f, 0.25f, 1f)
        addListener(object : Transition.TransitionListener {
            override fun onTransitionStart(transition: Transition) {
            }

            override fun onTransitionEnd(transition: Transition) {
                foregroundWidgetCards?.forEach {
                    it.startCoverTransitAnimation()
                }
            }

            override fun onTransitionCancel(transition: Transition) {
                foregroundWidgetCards?.forEach {
                    it.startCoverTransitAnimation()
                }
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

    private fun onWidgetCardCountChanged(group: WidgetCardDataGroup, vararg widgetCards: WidgetCardView) {
        val lastGroup = lastWidgetCardDataGroup
        foregroundWidgetCards = widgetCards
        lastWidgetCardDataGroup = group
        if (widgetCards.size == lastGroup?.foregroundWidgetCardCount && group.pendingPosition == 0 && lastGroup.pendingPosition == 0) {
            return
        }

        constraintSet.clone(mainActivity.bindingExpanded.widgetContainer)

        widgetCards.forEach {
            it.makeCover()
        }
        if (lastGroup == null) {
            mainActivity.bindingExpanded.widgetContainer.postDelayed({
                widgetCards.forEach {
                    it.startCoverTransitAnimation()
                }
            }, 250)
        } else {
            TransitionManager.beginDelayedTransition(
                mainActivity.bindingExpanded.widgetContainer,
                if (group.pendingPosition != 0) onWidgetCardCountChangedHasPendingTransition else onWidgetCardCountChangedTransition
            )
        }

        when (widgetCards.size) {
            1 -> {
                mainModel.updateDualSplitHandlePosition(-1f)
                constraintSet.connect(widgetCards[0].id, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START)
                constraintSet.connect(widgetCards[0].id, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END)
                constraintSet.constrainPercentWidth(widgetCards[0].id, 1f)
                constraintSet.setVisibility(R.id.left_split_handle, ConstraintSet.GONE)
                constraintSet.setVisibility(R.id.right_split_handle, ConstraintSet.GONE)
                if (group.isControlPanelForeground) {
                    mainActivity.bindingControlPanelExpanded.bottomNavigation.visibility = View.GONE
                    mainActivity.bindingControlPanelExpanded.navigationRail.visibility = View.VISIBLE
                }
            }

            2 -> {
                constraintSet.connect(widgetCards[0].id, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START)
                constraintSet.connect(widgetCards[0].id, ConstraintSet.END, R.id.left_split_handle, ConstraintSet.START)
                constraintSet.connect(R.id.left_split_handle, ConstraintSet.START, widgetCards[0].id, ConstraintSet.END)
                constraintSet.connect(R.id.left_split_handle, ConstraintSet.END, widgetCards[1].id, ConstraintSet.START)
                constraintSet.connect(widgetCards[1].id, ConstraintSet.START, R.id.left_split_handle, ConstraintSet.END)
                constraintSet.connect(widgetCards[1].id, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END)
                constraintSet.constrainPercentWidth(widgetCards[0].id, 0.5f)
                constraintSet.constrainPercentWidth(widgetCards[1].id, 0.5f)
                constraintSet.setVisibility(R.id.left_split_handle, if (group.pendingPosition != 0) ConstraintSet.INVISIBLE else ConstraintSet.VISIBLE)
                constraintSet.setVisibility(R.id.right_split_handle, ConstraintSet.GONE)
                if (group.isControlPanelForeground) {
                    mainActivity.bindingControlPanelExpanded.bottomNavigation.visibility = View.VISIBLE
                    mainActivity.bindingControlPanelExpanded.navigationRail.visibility = View.GONE
                }
            }

            3 -> {
                mainModel.updateDualSplitHandlePosition(-1f)
                constraintSet.connect(widgetCards[0].id, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START)
                constraintSet.connect(widgetCards[0].id, ConstraintSet.END, R.id.left_split_handle, ConstraintSet.START)
                constraintSet.connect(R.id.left_split_handle, ConstraintSet.START, widgetCards[0].id, ConstraintSet.END)
                constraintSet.connect(R.id.left_split_handle, ConstraintSet.END, widgetCards[1].id, ConstraintSet.START)
                constraintSet.connect(widgetCards[1].id, ConstraintSet.START, R.id.left_split_handle, ConstraintSet.END)
                constraintSet.connect(widgetCards[1].id, ConstraintSet.END, R.id.right_split_handle, ConstraintSet.START)
                constraintSet.connect(R.id.right_split_handle, ConstraintSet.START, widgetCards[1].id, ConstraintSet.END)
                constraintSet.connect(R.id.right_split_handle, ConstraintSet.END, widgetCards[2].id, ConstraintSet.START)
                constraintSet.connect(widgetCards[2].id, ConstraintSet.START, R.id.right_split_handle, ConstraintSet.END)
                constraintSet.connect(widgetCards[2].id, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END)
                constraintSet.constrainPercentWidth(widgetCards[0].id, 0.33f)
                constraintSet.constrainPercentWidth(widgetCards[1].id, 0.33f)
                constraintSet.constrainPercentWidth(widgetCards[2].id, 0.33f)
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
        val firstWidgetCard = foregroundWidgetCards?.getOrNull(0) ?: return@Observer
        val secondWidgetCard = foregroundWidgetCards?.getOrNull(1) ?: return@Observer
        if (it == -1f) return@Observer
        val windowWidth = mainActivity.getResources().displayMetrics.widthPixels
        val newPercent = (it / windowWidth).coerceIn(0.2f, 0.8f)
        constraintSet.clone(mainActivity.bindingExpanded.widgetContainer)
        constraintSet.constrainPercentWidth(firstWidgetCard.id, newPercent)
        constraintSet.constrainPercentWidth(secondWidgetCard.id, 1 - newPercent)
        constraintSet.applyTo(mainActivity.bindingExpanded.widgetContainer)
    }
}
