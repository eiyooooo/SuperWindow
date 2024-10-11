package com.eiyooooo.superwindow.ui.contentpanel

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.eiyooooo.superwindow.contentprovider.LocalContent
import com.eiyooooo.superwindow.databinding.LocalContentPanelBinding
import com.github.promeg.pinyinhelper.Pinyin
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber

class LocalContentPanel : Fragment() {

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

        lifecycleScope.launch {
            val appsList = withContext(Dispatchers.IO) {
                LocalContent.getAppsList()
            }

            val spanCount = if (resources.configuration.screenWidthDp < 600) 3 else 4
            val layoutManager = GridLayoutManager(view.context, spanCount)
            binding.recyclerView.setItemViewCacheSize(appsList.size)
            binding.recyclerView.layoutManager = layoutManager

            val adapter = AppsAdapter(appsList, lifecycleScope) {
                // TODO: handle chose app
                Timber.d(it)
            }
            binding.recyclerView.adapter = adapter

            val pinyinCache = mutableMapOf<Char, Int>()
            binding.waveSideBarView.setLetterChangeListener { letter ->
                val position = pinyinCache[letter] ?: let {
                    val foundPosition = appsList.binarySearch { app ->
                        Pinyin.toPinyin(app.label[0])[0].compareTo(letter)
                    }
                    if (foundPosition >= 0) {
                        pinyinCache[letter] = foundPosition
                        foundPosition
                    } else {
                        return@setLetterChangeListener
                    }
                }
                binding.recyclerView.scrollToPosition(position)
                layoutManager.scrollToPositionWithOffset(position, 0)
            }
        }
    }
}
