package com.eiyooooo.superwindow.ui.main

import android.graphics.Rect
import android.os.Bundle
import android.view.DragEvent
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.eiyooooo.superwindow.R
import com.eiyooooo.superwindow.databinding.ActivityMainCompactBinding
import com.eiyooooo.superwindow.databinding.ActivityMainExpandedBinding
import com.eiyooooo.superwindow.databinding.ControlPanelCompactBinding
import com.eiyooooo.superwindow.databinding.ControlPanelExpandedBinding
import com.eiyooooo.superwindow.entity.Preferences
import com.eiyooooo.superwindow.entity.SystemServices
import com.eiyooooo.superwindow.ui.bar.BarIconAdapter
import com.eiyooooo.superwindow.ui.bar.BarLayoutManager
import com.eiyooooo.superwindow.ui.contentpanel.LocalContentPanelFragment
import com.eiyooooo.superwindow.ui.controlpanel.ControlPanelAdapter
import com.eiyooooo.superwindow.ui.view.WidgetCardView
import com.eiyooooo.superwindow.ui.widgetcard.WidgetCardData
import com.eiyooooo.superwindow.ui.widgetcard.WidgetCardManager
import com.eiyooooo.superwindow.util.setupWithViewPager
import com.eiyooooo.superwindow.util.startShowElevatedViewAnimation
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    internal val isExpanded by lazy { resources.configuration.screenWidthDp >= 600 }
    private lateinit var bindingCompact: ActivityMainCompactBinding
    internal lateinit var bindingExpanded: ActivityMainExpandedBinding
    private lateinit var bindingControlPanelCompact: ControlPanelCompactBinding
    internal lateinit var bindingControlPanelExpanded: ControlPanelExpandedBinding

    private val mainModel: MainActivityViewModel by viewModels()
    private val controlPanelAdapter: ControlPanelAdapter by lazy { ControlPanelAdapter(this) }
    private val widgetCardManager: WidgetCardManager by lazy { WidgetCardManager(this, mainModel) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        if (isExpanded) {
            bindingExpanded = ActivityMainExpandedBinding.inflate(layoutInflater).also {
                bindingControlPanelExpanded = ControlPanelExpandedBinding.inflate(layoutInflater, null, false)
                it.widgetContainer.addTargetView(it.leftSplitHandle)
                it.widgetContainer.addTargetView(it.rightSplitHandle)
                it.overlay.setOnClickListener { hideElevatedView() }
                it.root.setOnDragListener { _, event ->
                    event.action == DragEvent.ACTION_DRAG_STARTED || event.action == DragEvent.ACTION_DROP
                }
                setContentView(it.root)
                it.root.post { SystemServices.currentDisplay = it.root.display }
                setupBar(it)
            }
            widgetCardManager.init()
            setFullScreen(force = true)
            ViewCompat.setOnApplyWindowInsetsListener(bindingExpanded.root) { view, insets ->
                val bars = insets.getInsets(WindowInsetsCompat.Type.displayCutout())
                view.updatePadding(left = bars.left, right = bars.right)
                WindowInsetsCompat.CONSUMED
            }
            lifecycleScope.launch {
                Preferences.topBottomPaddingFlow.collect {
                    val padding = getResources().displayMetrics.heightPixels * it / 1000
                    bindingExpanded.main.updatePadding(top = padding, bottom = padding)
                }
            }
        } else {
            bindingCompact = ActivityMainCompactBinding.inflate(layoutInflater).also {
                bindingControlPanelCompact = ControlPanelCompactBinding.inflate(layoutInflater, it.widgetContainer, true)
                it.overlay.setOnClickListener { hideElevatedView() }
                setContentView(it.root)
                it.root.post { SystemServices.currentDisplay = it.root.display }
            }
        }

        setupControlPanel()

        mainModel.addShizukuListener()
    }

    override fun onResume() {
        super.onResume()
        mainModel.checkShizukuPermission()
    }

    override fun recreate() {
        if (isExpanded) widgetCardManager.destroy()
        super.recreate()
    }

    override fun onDestroy() {
        if (isExpanded) widgetCardManager.destroy()
        mainModel.removeShizukuListener()
        super.onDestroy()
    }

    private fun setupControlPanel() {
        val lastControlPanelPage = mainModel.currentControlPanelPage
        if (isExpanded) {
            bindingControlPanelExpanded.viewPager.apply {
                adapter = controlPanelAdapter
                offscreenPageLimit = 2
                bindingControlPanelExpanded.bottomNavigation.setupWithViewPager(this) {
                    mainModel.currentControlPanelPage = it
                }
                bindingControlPanelExpanded.navigationRail.setupWithViewPager(this)
                post {
                    setCurrentItem(lastControlPanelPage, false)
                }
            }
            setSupportActionBar(bindingControlPanelExpanded.toolbar)
        } else {
            bindingControlPanelCompact.viewPager.apply {
                adapter = controlPanelAdapter
                offscreenPageLimit = 2
                bindingControlPanelCompact.bottomNavigation.setupWithViewPager(this) {
                    mainModel.currentControlPanelPage = it
                }
                post {
                    setCurrentItem(lastControlPanelPage, false)
                }
            }
            setSupportActionBar(bindingControlPanelCompact.toolbar)
        }
    }

    private fun setupBar(binding: ActivityMainExpandedBinding) {
        val barAdapter = BarIconAdapter()
        binding.bar.apply {
            adapter = barAdapter
            layoutManager = BarLayoutManager(this@MainActivity)
            addItemDecoration(object : RecyclerView.ItemDecoration() {
                val iconWidth = resources.getDimensionPixelSize(R.dimen.bar_icon_size)
                val dividerWidth = resources.getDimensionPixelSize(R.dimen.bar_divider_width)
                val space = resources.getDimensionPixelSize(R.dimen.bar_space)
                override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
                    val itemCount = parent.adapter?.itemCount ?: return
                    val position = parent.getChildAdapterPosition(view)
                    if (position == 0) {
                        val parentWidth = parent.width
                        val itemWidth = if (itemCount == 3) {
                            iconWidth * 2 + space
                        } else {
                            dividerWidth + (itemCount - 2) * (iconWidth + space)
                        }
                        outRect.left = (parentWidth - itemWidth) / 2
                    } else if (position != 1) {
                        outRect.left = space
                    }
                }
            })
        }
        mainModel.widgetCardDataGroup.observe(this) {
            barAdapter.update(it)
        }
    }

    internal fun setFullScreen(fullScreen: Boolean = Preferences.fullScreen, force: Boolean = isExpanded) {
        if (force) {
            WindowCompat.getInsetsController(window, window.decorView).apply {
                if (fullScreen) {
                    systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                    hide(WindowInsetsCompat.Type.systemBars())
                    ViewCompat.setOnApplyWindowInsetsListener(window.decorView) { view, windowInsets ->
                        if (windowInsets.isVisible(WindowInsetsCompat.Type.navigationBars())
                            || windowInsets.isVisible(WindowInsetsCompat.Type.statusBars())
                        ) {
                            hide(WindowInsetsCompat.Type.systemBars())
                        }
                        ViewCompat.onApplyWindowInsets(view, windowInsets)
                    }
                } else {
                    systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_DEFAULT
                    show(WindowInsetsCompat.Type.systemBars())
                    ViewCompat.setOnApplyWindowInsetsListener(window.decorView, null)
                }
            }
        }
    }

    internal fun showSnackBar(text: String) {
        if (isExpanded) {
            val widgetCardDataGroup = mainModel.widgetCardDataGroup.value!!
            if (widgetCardDataGroup.isControlPanelForeground) {
                if (widgetCardDataGroup.foregroundWidgetCardCount == 1) {
                    Snackbar.make(bindingExpanded.root, text, Snackbar.LENGTH_LONG).setAnchorView(bindingExpanded.bar).show()
                } else {
                    Snackbar.make(bindingExpanded.root, text, Snackbar.LENGTH_LONG).setAnchorView(bindingControlPanelExpanded.bottomNavigation).show()
                }
            } else {
                Snackbar.make(bindingExpanded.root, text, Snackbar.LENGTH_LONG).setAnchorView(bindingExpanded.bar).show()
            }
        } else {
            Snackbar.make(bindingCompact.root, text, Snackbar.LENGTH_LONG).setAnchorView(bindingControlPanelCompact.bottomNavigation).show()
        }
    }

    private var touchEventInterceptor: ((MotionEvent) -> Boolean)? = null

    internal fun setTouchEventInterceptor(interceptor: ((MotionEvent) -> Boolean)? = null) {
        touchEventInterceptor = interceptor
    }

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        if (touchEventInterceptor?.invoke(event) == true) {
            return true
        }
        return super.dispatchTouchEvent(event)
    }

    internal fun showElevatedFragment(fragment: Fragment) {
        val overlay = if (isExpanded) bindingExpanded.overlay else bindingCompact.overlay
        val elevatedViewContainer = if (isExpanded) bindingExpanded.elevatedViewContainer else bindingCompact.elevatedViewContainer

        val containerWidth = bindingExpanded.main.width - bindingExpanded.main.paddingLeft - bindingExpanded.main.paddingRight
        val containerHeight = bindingExpanded.main.height - bindingExpanded.main.paddingTop - bindingExpanded.main.paddingBottom

        elevatedViewContainer.layoutParams = elevatedViewContainer.layoutParams.apply {
            width = (containerWidth * 0.8).toInt()
            height = (containerHeight * 0.8).toInt()
        }

        makeCardsBlur(true)
        startShowElevatedViewAnimation(elevatedViewContainer, overlay, true) {
            supportFragmentManager.beginTransaction().replace(R.id.elevated_view_container, fragment).commit()
        }
    }

    internal fun hideElevatedView() {
        val overlay = if (isExpanded) bindingExpanded.overlay else bindingCompact.overlay
        val elevatedViewContainer = if (isExpanded) bindingExpanded.elevatedViewContainer else bindingCompact.elevatedViewContainer

        startShowElevatedViewAnimation(elevatedViewContainer, overlay, false) {
            makeCardsBlur(false)
        }

        supportFragmentManager.findFragmentById(R.id.elevated_view_container)?.let {
            supportFragmentManager.beginTransaction().remove(it).commit()
        }

        elevatedViewContainer.removeAllViews()
    }

    internal fun makeCardsBlur(blur: Boolean) = if (isExpanded) widgetCardManager.makeCardsBlur(blur) else Unit

    internal fun replaceWidgetCard(target: WidgetCardData) {
        if (isExpanded) {
            showElevatedFragment(LocalContentPanelFragment().apply {
                arguments = Bundle().apply {
                    putBoolean("replace", true)
                    putString("targetIdentifier", target.identifier)
                }
            })
        }
    }

    internal fun addWidgetCard(sourceImageView: ImageView, widgetCardData: WidgetCardData) = if (isExpanded) widgetCardManager.addWidgetCard(sourceImageView, widgetCardData) else Unit

    internal fun replaceWidgetCard(targetIdentifier: String, new: WidgetCardData) = if (isExpanded) widgetCardManager.replaceWidgetCard(targetIdentifier, new) else Unit

    internal fun minimizeWidgetCard(target: WidgetCardData) = if (isExpanded) widgetCardManager.minimizeWidgetCard(target) else Unit

    internal fun removeWidgetCard(target: WidgetCardView) = if (isExpanded) widgetCardManager.removeWidgetCard(target) else Unit

    internal fun startWaitDragEvent() = if (isExpanded) widgetCardManager.startWaitDragEvent() else Unit
}
