package com.easycontrol.next.views

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import com.easycontrol.next.R
import com.easycontrol.next.adapters.LogAdapter
import com.easycontrol.next.databinding.ActivityLogBinding
import com.easycontrol.next.viewmodels.LogActivityViewModel
import kotlinx.coroutines.launch

class LogActivity : AppCompatActivity(), Toolbar.OnMenuItemClickListener {

    private lateinit var mBinding: ActivityLogBinding

    private val mViewModel: LogActivityViewModel by viewModels()
    private val mAdapter = LogAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        mBinding = ActivityLogBinding.inflate(layoutInflater).also {
            setContentView(it.root)
        }
        mBinding.toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
        mBinding.toolbar.inflateMenu(R.menu.log)
        mBinding.toolbar.setOnMenuItemClickListener(this)

        val spinnerAdapter = ArrayAdapter.createFromResource(this, R.array.log_viewer_spinner, R.layout.item_log_level_dropdown)
        spinnerAdapter.setDropDownViewResource(R.layout.item_log_level_dropdown)
        mBinding.spinner.adapter = spinnerAdapter
        mBinding.spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val filter = resources.getStringArray(R.array.log_viewer_spinner)[position]
                mAdapter.filter.filter(filter)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }

        mBinding.list.transcriptMode = ListView.TRANSCRIPT_MODE_NORMAL
        mBinding.list.isStackFromBottom = true
        mBinding.list.adapter = mAdapter

        mViewModel.startReadLog()
        lifecycleScope.launch {
            mViewModel.items.collect {
                mAdapter.updateItems(it)
            }
        }
    }

    override fun onMenuItemClick(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.clear -> {
                mViewModel.clearLog()
                true
            }

            else -> false
        }
    }
}
