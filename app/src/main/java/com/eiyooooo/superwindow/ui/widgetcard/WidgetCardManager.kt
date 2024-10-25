package com.eiyooooo.superwindow.ui.widgetcard

import android.os.Build
import android.view.DragEvent
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
import timber.log.Timber
import java.util.concurrent.atomic.AtomicBoolean

class WidgetCardManager(private val mainActivity: MainActivity, private val mainModel: MainActivityViewModel) {

    private lateinit var controlPanelWidgetCard: WidgetCardView
    private val widgetCardMap: MutableMap<String, WidgetCardView> = mutableMapOf()

    private val firstWidgetCard: WidgetCardView?
        get() = mainModel.widgetCardDataGroup.value!!.getWidgetCard(1)
    private val secondWidgetCard: WidgetCardView?
        get() = mainModel.widgetCardDataGroup.value!!.getWidgetCard(2)
    private val thirdWidgetCard: WidgetCardView?
        get() = mainModel.widgetCardDataGroup.value!!.getWidgetCard(3)

    fun init() {
        BlurUtils.init(mainActivity.applicationContext)

        mainModel.widgetCardDataGroup.observe(mainActivity) {
            mainModel.dualSplitHandlePosition.removeObserver(dualSplitHandlePositionObserver)

            val first = it.getWidgetCard(1)
            val second = it.getWidgetCard(2)
            val third = it.getWidgetCard(3)

            when {
                third != null && second != null && first != null -> {
                    showTripleWidgetCard(it, first, second, third)
                }

                second != null && first != null -> {
                    showDualWidgetCard(it, first, second)
                }

                first != null -> {
                    showSingleWidgetCard(it, first)
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
                    widgetCardMap.values.filter { !runningTasksInVD.containsKey(it.displayId) }.forEach {
                        it.release()
                        removeWidgetCard(it)
                    }
                }
            }
        }

        //TODO: remove this test module
        test()
    }

    private fun test() {
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
        widgetCardMap.values.forEach {
            it.release()
        }
        widgetCardMap.clear()
    }

    private fun showSingleWidgetCard(group: WidgetCardDataGroup, first: WidgetCardView) {
        if (group.isControlPanelForeground) {
            mainActivity.bindingControlPanelExpanded.bottomNavigation.visibility = View.GONE
            mainActivity.bindingControlPanelExpanded.navigationRail.visibility = View.VISIBLE
        }

        refreshWidgetCardContainer(first)

        mainActivity.bindingExpanded.leftSplitHandle.setWidgetCards()
        mainActivity.bindingExpanded.leftSplitHandle.setOnDragHandle(null)
        mainActivity.bindingExpanded.rightSplitHandle.setWidgetCards()
        mainActivity.bindingExpanded.rightSplitHandle.setOnDragHandle(null)

        constrainWidgetCard(group, first)
    }

    private fun showDualWidgetCard(group: WidgetCardDataGroup, first: WidgetCardView, second: WidgetCardView) {
        if (group.isControlPanelForeground) {
            mainActivity.bindingControlPanelExpanded.bottomNavigation.visibility = View.VISIBLE
            mainActivity.bindingControlPanelExpanded.navigationRail.visibility = View.GONE
        }

        refreshWidgetCardContainer(first, second)

        mainActivity.bindingExpanded.leftSplitHandle.setWidgetCards(first, second)
        mainActivity.bindingExpanded.leftSplitHandle.setOnDragHandle { mainModel.updateDualSplitHandlePosition(it) }
        mainActivity.bindingExpanded.rightSplitHandle.setWidgetCards()
        mainActivity.bindingExpanded.rightSplitHandle.setOnDragHandle(null)

        constrainWidgetCard(group, first, second)

        mainModel.dualSplitHandlePosition.observe(mainActivity, dualSplitHandlePositionObserver)
    }

    private fun showTripleWidgetCard(group: WidgetCardDataGroup, first: WidgetCardView, second: WidgetCardView, third: WidgetCardView) {
        if (group.isControlPanelForeground) {
            mainActivity.bindingControlPanelExpanded.bottomNavigation.visibility = View.VISIBLE
            mainActivity.bindingControlPanelExpanded.navigationRail.visibility = View.GONE
        }

        refreshWidgetCardContainer(first, second, third)

        mainActivity.bindingExpanded.leftSplitHandle.setWidgetCards(first, second, third)
        mainActivity.bindingExpanded.leftSplitHandle.setOnDragHandle(null)//TODO
        mainActivity.bindingExpanded.rightSplitHandle.setWidgetCards(first, second, third)
        mainActivity.bindingExpanded.rightSplitHandle.setOnDragHandle(null)//TODO

        constrainWidgetCard(group, first, second, third)
    }

    private fun WidgetCardDataGroup.getWidgetCard(position: Int): WidgetCardView? {
        val widgetCardData = when (position) {
            1 -> firstWidgetCardData
            2 -> secondWidgetCardData
            else -> thirdWidgetCardData
        } ?: return null
        return if (widgetCardData.isControlPanel) {
            if (!this@WidgetCardManager::controlPanelWidgetCard.isInitialized) {
                controlPanelWidgetCard = WidgetCardView(mainActivity.bindingControlPanelExpanded.root, widgetCardData).apply {
                    setOnDragListener(dragListener)
                }
            }
            controlPanelWidgetCard
        } else widgetCardMap[widgetCardData.identifier] ?: let {
            val newWidgetCardView = WidgetCardView(mainActivity, widgetCardData = widgetCardData).apply {
                setOnDragListener(dragListener)
            }
            widgetCardMap[widgetCardData.identifier] = newWidgetCardView
            newWidgetCardView
        }
    }

    private fun refreshWidgetCardContainer(vararg widgetCards: WidgetCardView) {
        widgetCards.forEach {
            it.visibility = View.GONE
        }
        val widgetCardSet = widgetCards.toSet()
        mainActivity.bindingExpanded.widgetContainer.children.filterIsInstance<WidgetCardView>().toList().forEach {
            if (!widgetCardSet.contains(it)) {
                it.visibility = View.GONE
                mainActivity.bindingExpanded.widgetContainer.removeView(it)
            } else {
                it.visibility = View.VISIBLE
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
                    if (current.secondWidgetCardData != null) {
                        current.copy(firstWidgetCardData = current.secondWidgetCardData, secondWidgetCardData = current.thirdWidgetCardData, thirdWidgetCardData = null)
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
        widgetCardMap.remove(target.widgetCardData.identifier)
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

    private val constraintSet = ConstraintSet()
    private var lastWidgetCardDataGroup: WidgetCardDataGroup? = null

    private fun constrainWidgetCard(group: WidgetCardDataGroup, vararg widgetCards: WidgetCardView) {
        val lastGroup = lastWidgetCardDataGroup
        lastWidgetCardDataGroup = group
        if (group.foregroundWidgetCardCount == lastGroup?.foregroundWidgetCardCount && !group.dragging && !lastGroup.dragging) {
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
                if (group.dragging) cardDraggingTransition else cardChangedTransition
            )
        }

        when (widgetCards.size) {
            1 -> {
                mainModel.updateDualSplitHandlePosition(-1f)
                constraintSet.connect(widgetCards[0].id, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START)
                constraintSet.connect(widgetCards[0].id, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END)
                constraintSet.constrainPercentWidth(widgetCards[0].id, 1f)
                constraintSet.setVisibility(widgetCards[0].id, ConstraintSet.VISIBLE)
                constraintSet.setVisibility(R.id.left_split_handle, ConstraintSet.GONE)
                constraintSet.setVisibility(R.id.right_split_handle, ConstraintSet.GONE)
                widgetCards[0].setControlBarVisibility(View.GONE)
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
                constraintSet.setVisibility(widgetCards[0].id, ConstraintSet.VISIBLE)
                constraintSet.setVisibility(widgetCards[1].id, ConstraintSet.VISIBLE)
                widgetCards[0].setControlBarVisibility(if (group.dragging) View.GONE else View.VISIBLE)
                widgetCards[1].setControlBarVisibility(if (group.dragging) View.GONE else View.VISIBLE)
                constraintSet.setVisibility(R.id.left_split_handle, if (group.dragging) ConstraintSet.INVISIBLE else ConstraintSet.VISIBLE)
                constraintSet.setVisibility(R.id.right_split_handle, ConstraintSet.GONE)
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
                constraintSet.setVisibility(widgetCards[0].id, ConstraintSet.VISIBLE)
                constraintSet.setVisibility(widgetCards[1].id, ConstraintSet.VISIBLE)
                constraintSet.setVisibility(widgetCards[2].id, ConstraintSet.VISIBLE)
                widgetCards[0].setControlBarVisibility(if (group.dragging) View.GONE else View.VISIBLE)
                widgetCards[1].setControlBarVisibility(if (group.dragging) View.GONE else View.VISIBLE)
                widgetCards[2].setControlBarVisibility(if (group.dragging) View.GONE else View.VISIBLE)
                constraintSet.setVisibility(R.id.left_split_handle, if (group.dragging) ConstraintSet.INVISIBLE else ConstraintSet.VISIBLE)
                constraintSet.setVisibility(R.id.right_split_handle, if (group.dragging) ConstraintSet.INVISIBLE else ConstraintSet.VISIBLE)
            }
        }

        constraintSet.applyTo(mainActivity.bindingExpanded.widgetContainer)
    }

    private val cardChangedTransition = AutoTransition().apply {
        duration = 250
        interpolator = PathInterpolatorCompat.create(0.25f, 0.1f, 0.25f, 1f)
        addListener(object : Transition.TransitionListener {
            override fun onTransitionStart(transition: Transition) {
            }

            override fun onTransitionEnd(transition: Transition) {
                firstWidgetCard?.run {
                    if (dragging.get()) onDragEnded()
                    startCoverTransitAnimation()
                }
                secondWidgetCard?.run {
                    if (dragging.get()) onDragEnded()
                    startCoverTransitAnimation()
                }
                thirdWidgetCard?.run {
                    if (dragging.get()) onDragEnded()
                    startCoverTransitAnimation()
                }
            }

            override fun onTransitionCancel(transition: Transition) {
                firstWidgetCard?.run {
                    if (dragging.get()) onDragEnded()
                    startCoverTransitAnimation()
                }
                secondWidgetCard?.run {
                    if (dragging.get()) onDragEnded()
                    startCoverTransitAnimation()
                }
                thirdWidgetCard?.run {
                    if (dragging.get()) onDragEnded()
                    startCoverTransitAnimation()
                }
            }

            override fun onTransitionPause(transition: Transition) {
            }

            override fun onTransitionResume(transition: Transition) {
            }
        })
    }

    private val cardDraggingTransition = AutoTransition().apply {
        duration = 250
        interpolator = PathInterpolatorCompat.create(0.25f, 0.1f, 0.25f, 1f)
        addListener(object : Transition.TransitionListener {
            override fun onTransitionStart(transition: Transition) {
                swapping.set(true)
            }

            override fun onTransitionEnd(transition: Transition) {
                swapping.set(false)
            }

            override fun onTransitionCancel(transition: Transition) {
                swapping.set(false)
            }

            override fun onTransitionPause(transition: Transition) {
            }

            override fun onTransitionResume(transition: Transition) {
            }
        })
    }

    private val dualSplitHandlePositionObserver = Observer<Float> {
        val first = firstWidgetCard ?: return@Observer
        val second = secondWidgetCard ?: return@Observer
        if (it == -1f) return@Observer
        val windowWidth = mainActivity.getResources().displayMetrics.widthPixels
        val newPercent = (it / windowWidth).coerceIn(0.2f, 0.8f)
        constraintSet.clone(mainActivity.bindingExpanded.widgetContainer)
        constraintSet.constrainPercentWidth(first.id, newPercent)
        constraintSet.constrainPercentWidth(second.id, 1 - newPercent)
        constraintSet.applyTo(mainActivity.bindingExpanded.widgetContainer)
    }

    val dragging = AtomicBoolean(false)
    private val swapping = AtomicBoolean(false)

    private val dragListener = View.OnDragListener { view, event ->
        val cardView = view as? WidgetCardView ?: return@OnDragListener false
        val draggingView = event.localState as? WidgetCardView ?: return@OnDragListener false
        when (event.action) {
            DragEvent.ACTION_DRAG_STARTED -> {
                event.clipDescription.label.contains("WidgetCardView") && cardView.parent == draggingView.parent
            }

            DragEvent.ACTION_DRAG_ENTERED -> {
                if (dragging.get() && cardView != draggingView) {
                    swapping.set(true)
                    mainModel.updateWidgetCardDataGroup {
                        it.swap(draggingView.widgetCardData, cardView.widgetCardData)
                    }
                    Timber.d("ACTION_DRAG_ENTERED, swap: ${draggingView.widgetCardData.identifier} <-> ${cardView.widgetCardData.identifier}")
                }
                true
            }

            DragEvent.ACTION_DRAG_LOCATION -> {
                if (dragging.get() && !swapping.get() && cardView != draggingView) {
                    swapping.set(true)
                    mainModel.updateWidgetCardDataGroup {
                        it.swap(draggingView.widgetCardData, cardView.widgetCardData)
                    }
                    Timber.d("ACTION_DRAG_LOCATION, swap: ${draggingView.widgetCardData.identifier} <-> ${cardView.widgetCardData.identifier}")
                }
                true
            }

            DragEvent.ACTION_DROP -> {
                if (dragging.get()) {
                    dragging.set(false)
                    swapping.set(false)
                    mainModel.updateWidgetCardDataGroup { it.copy(dragging = false) }
                    Timber.d("ACTION_DROP, WidgetCardDataGroup dragging ended")
                }
                true
            }

            DragEvent.ACTION_DRAG_ENDED -> {
                if (dragging.get()) {
                    dragging.set(false)
                    swapping.set(false)
                    mainActivity.bindingExpanded.widgetContainer.post {
                        mainModel.updateWidgetCardDataGroup { it.copy(dragging = false) }
                        Timber.d("ACTION_DRAG_ENDED, WidgetCardDataGroup dragging ended")
                    }
                }
                true
            }

            else -> false
        }
    }
}
