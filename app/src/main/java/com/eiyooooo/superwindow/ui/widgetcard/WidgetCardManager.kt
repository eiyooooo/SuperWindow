package com.eiyooooo.superwindow.ui.widgetcard

import android.os.Build
import android.view.DragEvent
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
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
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.atomic.AtomicReference

class WidgetCardManager(private val mainActivity: MainActivity, private val mainModel: MainActivityViewModel) {

    private lateinit var controlPanelWidgetCard: WidgetCardView
    private val widgetCardMap: MutableMap<String, WidgetCardView> = mutableMapOf()

    private val firstWidgetCard: WidgetCardView?
        get() = mainModel.widgetCardDataGroup.value.getWidgetCard(1)
    private val secondWidgetCard: WidgetCardView?
        get() = mainModel.widgetCardDataGroup.value.getWidgetCard(2)
    private val thirdWidgetCard: WidgetCardView?
        get() = mainModel.widgetCardDataGroup.value.getWidgetCard(3)

    fun init() {
        BlurUtils.init(mainActivity.applicationContext)

        mainActivity.lifecycleScope.launch {
            mainModel.widgetCardDataGroup.collect {
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

                WidgetCardFocusManager.refreshFocusMode()
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
                        Timber.d("remove widget card: ${it.widgetCardData.identifier} because it's not running in VD")
                        it.release()
                        removeWidgetCard(it)
                    }
                }
            }
        }

        val onBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                widgetCardMap[WidgetCardFocusManager.focusing.value]?.injectBackEvent()
            }
        }
        mainActivity.lifecycleScope.launch {
            mainModel.backPressedCallbackIsEnabled.observe(mainActivity) {
                onBackPressedCallback.isEnabled = it
            }
        }
        mainActivity.onBackPressedDispatcher.addCallback(mainActivity, onBackPressedCallback)

        mainActivity.lifecycleScope.launch {
            WidgetCardFocusManager.focusing.combine(WidgetCardFocusManager.focusModeUpdater) { focusingWidgetCard, _ ->
                focusingWidgetCard
            }.collect { identifier ->
                var haveFocus = false
                widgetCardMap.values.forEach {
                    val focus = it.widgetCardData.identifier == identifier
                    it.changeFocusMode(focus)
                    if (focus) {
                        haveFocus = true
                    }
                }
                controlPanelWidgetCard.takeIf { ::controlPanelWidgetCard.isInitialized }?.let {
                    it.changeFocusMode(it.widgetCardData.identifier == identifier)
                }
                mainModel.updateBackPressedCallbackIsEnabled { haveFocus }
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
                it.copy(secondWidgetCardData = WidgetCardData("moe.shizuku.privileged.api", "local", LocalContent.getPackageIcon("moe.shizuku.privileged.api")),
                    thirdWidgetCardData = WidgetCardData("com.eiyooooo.example", "local", LocalContent.getPackageIcon("com.eiyooooo.example")))
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
            if (!::controlPanelWidgetCard.isInitialized) {
                controlPanelWidgetCard = WidgetCardView(mainActivity.bindingControlPanelExpanded.root, widgetCardData).apply {
                    setOnDragListener(dragListener)
                    changeFocusMode(WidgetCardFocusManager.focusing.value == widgetCardData.identifier)
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

    fun replaceWidgetCard(targetIdentifier: String, newWidgetCardData: WidgetCardData) {
        lastWidgetCardDataGroup = null
        mainModel.updateWidgetCardDataGroup { current ->
            when (targetIdentifier) {
                current.firstWidgetCardData.identifier -> {
                    current.copy(firstWidgetCardData = newWidgetCardData, backgroundWidgetCardData = current.backgroundWidgetCardData + current.firstWidgetCardData)
                }

                current.secondWidgetCardData?.identifier -> {
                    current.copy(secondWidgetCardData = newWidgetCardData, backgroundWidgetCardData = current.backgroundWidgetCardData + current.secondWidgetCardData)
                }

                current.thirdWidgetCardData?.identifier -> {
                    current.copy(thirdWidgetCardData = newWidgetCardData, backgroundWidgetCardData = current.backgroundWidgetCardData + current.thirdWidgetCardData)
                }

                else -> current
            }
        }
        WidgetCardFocusManager.updateFocusing { newWidgetCardData.identifier }
    }

    fun minimizeWidgetCard(target: WidgetCardData) {
        mainModel.updateWidgetCardDataGroup { current ->
            when (target.identifier) {
                current.firstWidgetCardData.identifier -> {
                    if (current.secondWidgetCardData != null) {
                        current.copy(
                            firstWidgetCardData = current.secondWidgetCardData,
                            secondWidgetCardData = current.thirdWidgetCardData,
                            thirdWidgetCardData = null,
                            backgroundWidgetCardData = current.backgroundWidgetCardData + target
                        )
                    } else {
                        current.copy(firstWidgetCardData = controlPanelWidgetCard.widgetCardData, backgroundWidgetCardData = current.backgroundWidgetCardData + target)
                    }
                }

                current.secondWidgetCardData?.identifier -> {
                    current.copy(secondWidgetCardData = current.thirdWidgetCardData, thirdWidgetCardData = null, backgroundWidgetCardData = current.backgroundWidgetCardData + target)
                }

                current.thirdWidgetCardData?.identifier -> {
                    current.copy(thirdWidgetCardData = null, backgroundWidgetCardData = current.backgroundWidgetCardData + target)
                }

                else -> current
            }
        }
        WidgetCardFocusManager.updateFocusing { "controlPanel" }
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
        WidgetCardFocusManager.updateFocusing { "controlPanel" }
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

        if (lastGroup != null) {
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

        if (lastGroup == null) {
            mainActivity.bindingExpanded.widgetContainer.postDelayed({
                widgetCards.forEach {
                    it.startCoverTransitAnimation()
                }
            }, 250)
        }
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

    private val dragging = AtomicBoolean(false)
    private val swappingStartTime = AtomicLong(0L)
    private val lastSwappedWidgetCardView = AtomicReference<WidgetCardView?>(null)
    private var waitDragEventRunnable: Runnable? = null

    internal fun startWaitDragEvent() {
        dragging.set(true)
        mainModel.updateWidgetCardDataGroup { if (it.dragging) it else it.copy(dragging = true) }

        waitDragEventRunnable?.let {
            mainActivity.bindingExpanded.widgetContainer.removeCallbacks(it)
        }
        waitDragEventRunnable = Runnable {
            dragging.set(false)
            swappingStartTime.set(0L)
            lastSwappedWidgetCardView.set(null)
            mainModel.updateWidgetCardDataGroup { if (it.dragging) it.copy(dragging = false) else it }
            Timber.d("WidgetCardDataGroup dragging ended by waitDragEventRunnable")
            waitDragEventRunnable = null
        }
        mainActivity.bindingExpanded.widgetContainer.postDelayed(waitDragEventRunnable, 500)
    }

    private val dragListener = View.OnDragListener { view, event ->
        val cardView = view as? WidgetCardView ?: return@OnDragListener false
        val draggingView = event.localState as? WidgetCardView ?: return@OnDragListener false
        when (event.action) {
            DragEvent.ACTION_DRAG_STARTED -> {
                if (event.clipDescription.label.contains("WidgetCardView") && cardView.parent == draggingView.parent) {
                    waitDragEventRunnable?.let {
                        mainActivity.bindingExpanded.widgetContainer.removeCallbacks(it)
                    }
                    waitDragEventRunnable = null
                    true
                } else {
                    false
                }
            }

            DragEvent.ACTION_DRAG_ENTERED -> {
                if (dragging.get() && cardView != draggingView) {
                    if (lastSwappedWidgetCardView.get() != cardView) {
                        swappingStartTime.set(System.currentTimeMillis())
                        lastSwappedWidgetCardView.set(cardView)
                        mainModel.updateWidgetCardDataGroup {
                            it.swap(draggingView.widgetCardData, cardView.widgetCardData)
                        }
                        Timber.d("swap: ${draggingView.widgetCardData.identifier} <-> ${cardView.widgetCardData.identifier} by ACTION_DRAG_ENTERED")
                    } else {
                        val currentTime = System.currentTimeMillis()
                        val lastTime = swappingStartTime.get()
                        if (lastTime != 0L && currentTime - lastTime >= 250 && swappingStartTime.compareAndSet(lastTime, currentTime)) {
                            mainModel.updateWidgetCardDataGroup {
                                it.swap(draggingView.widgetCardData, cardView.widgetCardData)
                            }
                            Timber.d("swap: ${draggingView.widgetCardData.identifier} <-> ${cardView.widgetCardData.identifier} by ACTION_DRAG_ENTERED debounce")
                        }
                    }
                }
                true
            }

            DragEvent.ACTION_DRAG_LOCATION -> {
                if (dragging.get() && cardView != draggingView) {
                    val currentTime = System.currentTimeMillis()
                    val lastTime = swappingStartTime.get()
                    if (lastTime != 0L && currentTime - lastTime >= 250 && swappingStartTime.compareAndSet(lastTime, currentTime)) {
                        lastSwappedWidgetCardView.set(cardView)
                        mainModel.updateWidgetCardDataGroup {
                            it.swap(draggingView.widgetCardData, cardView.widgetCardData)
                        }
                        Timber.d("swap: ${draggingView.widgetCardData.identifier} <-> ${cardView.widgetCardData.identifier} by ACTION_DRAG_LOCATION")
                    }
                }
                true
            }

            DragEvent.ACTION_DROP,
            DragEvent.ACTION_DRAG_ENDED -> {
                if (dragging.get()) {
                    dragging.set(false)
                    swappingStartTime.set(0L)
                    lastSwappedWidgetCardView.set(null)
                    mainActivity.bindingExpanded.widgetContainer.post {
                        mainModel.updateWidgetCardDataGroup { if (it.dragging) it.copy(dragging = false) else it }
                        Timber.d("WidgetCardDataGroup dragging ended by DragEvent")
                    }
                }
                true
            }

            else -> false
        }
    }
}
