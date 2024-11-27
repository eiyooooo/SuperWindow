package com.eiyooooo.superwindow.ui.contentpanel

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.eiyooooo.superwindow.content.LocalContent
import com.eiyooooo.superwindow.databinding.LocalContentPanelBinding
import com.eiyooooo.superwindow.ui.main.MainActivity
import com.github.promeg.pinyinhelper.Pinyin
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber

class LocalContentPanelFragment : Fragment() {

    private lateinit var binding: LocalContentPanelBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = LocalContentPanelBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val replace = arguments?.getBoolean("replace") ?: false
        val targetIdentifier = arguments?.getString("targetIdentifier")
        val supportLongClick = !replace

        binding.waveSideBarView.visibility = View.GONE
        binding.nestedScrollView.visibility = View.GONE
        binding.progressBar.visibility = View.VISIBLE

        lifecycleScope.launch {
            val appsList = withContext(Dispatchers.Default) {
                LocalContent.getAppsList()
            }

            val spanCount = (resources.configuration.screenWidthDp * 0.8 / 80).toInt().coerceAtMost(4)
            val layoutManager = GridLayoutManager(view.context, spanCount)
            binding.recyclerView.setItemViewCacheSize(appsList.size)
            binding.recyclerView.layoutManager = layoutManager

            val adapter = AppsAdapter(appsList, lifecycleScope, binding.waveSideBarView.selectingLetter, supportLongClick) { longClick, data ->
                if (replace) {
                    targetIdentifier?.let {
                        (context as? MainActivity)?.replaceWidgetCard(it, data)
                    }
                    (context as? MainActivity)?.hideElevatedView()
                } else {
                    // TODO: handle chose app
                    Timber.d("longClick: $longClick, data: $data")
                }
            }
            binding.recyclerView.adapter = adapter
            binding.recyclerView.post {
                binding.waveSideBarView.visibility = View.VISIBLE
                binding.nestedScrollView.visibility = View.VISIBLE
                binding.progressBar.visibility = View.GONE
            }

            val pinyinCache = mutableMapOf<Char, Int>()
            binding.waveSideBarView.setLetterChangeListener { letter ->
                val position = pinyinCache[letter] ?: let {
                    var foundPosition = -1
                    for ((index, app) in appsList.withIndex()) {
                        val pinyinFirstLetter = Pinyin.toPinyin(app.first.label[0]).firstOrNull()
                        if (letter == '#') {
                            if (pinyinFirstLetter?.isLetter() != true) {
                                foundPosition = index
                                break
                            }
                        } else if (letter == pinyinFirstLetter?.uppercaseChar()) {
                            foundPosition = index
                            break
                        }
                    }
                    if (foundPosition >= 0) {
                        pinyinCache[letter] = foundPosition
                        foundPosition
                    } else {
                        return@setLetterChangeListener
                    }
                }
                layoutManager.findViewByPosition(position)?.let {
                    binding.nestedScrollView.smoothScrollTo(0, it.top)
                }
            }
        }
    }
}
