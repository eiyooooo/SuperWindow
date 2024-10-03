package com.eiyooooo.superwindow.views.dialogs

import android.app.Dialog
import android.content.pm.LauncherActivityInfo
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.eiyooooo.superwindow.R
import com.eiyooooo.superwindow.adapters.AllAppsAdapter
import com.eiyooooo.superwindow.databinding.LocalContentPanelBinding
import com.eiyooooo.superwindow.utils.dp2px
import com.eiyooooo.superwindow.utils.launcherApps
import com.eiyooooo.superwindow.utils.userManager
import com.github.promeg.pinyinhelper.Pinyin
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.util.Collections

class LocalContentPanelDialog : AppCompatDialogFragment() {

    private lateinit var binding: LocalContentPanelBinding

    private var allAppsList = ArrayList<LauncherActivityInfo>()
    private var appsPinyinMap = HashMap<String, String>()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = LocalContentPanelBinding.inflate(LayoutInflater.from(context), null, false)
        return MaterialAlertDialogBuilder(requireContext(), theme)
            .setTitle(getString(R.string.local_content_panel))
            .setView(binding.root)
            .create()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View = binding.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val context = context ?: return

        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                allAppsList.clear()
                userManager.userProfiles.forEach {
                    allAppsList.addAll(launcherApps.getActivityList(null, it))
                }

                allAppsList.forEach {
                    appsPinyinMap[it.label.toString()] = Pinyin.toPinyin(it.label[0])
                }

                Collections.sort(allAppsList, PinyinComparable())
            }

            val adapter = AllAppsAdapter(context, allAppsList) {
                // TODO: handle chose app
                Timber.d(it)
                dismiss()
            }

            val recyclerViewWidth = binding.recyclerView.width
            val spanCount = (recyclerViewWidth / context.dp2px(100)).coerceAtLeast(3)

            binding.recyclerView.layoutManager = GridLayoutManager(context, spanCount)
            binding.recyclerView.adapter = adapter

            binding.waveSideBarView.setLetterChangeListener { letter ->
                val pos = adapter.getIndex(letter)
                if (pos != -1) {
                    binding.recyclerView.scrollToPosition(pos)
                    val layoutManager = binding.recyclerView.layoutManager as GridLayoutManager
                    layoutManager.scrollToPositionWithOffset(pos, 0)
                }
            }
        }
    }

    inner class PinyinComparable : Comparator<LauncherActivityInfo> {
        override fun compare(o1: LauncherActivityInfo?, o2: LauncherActivityInfo?): Int {
            if (o1 == null || o2 == null) return 0

            val pinyin1 = appsPinyinMap[o1.label]
            val pinyin2 = appsPinyinMap[o2.label]

            return when {
                pinyin1 == null || pinyin2 == null -> 0
                else -> pinyin1.compareTo(pinyin2)
            }
        }
    }
}
