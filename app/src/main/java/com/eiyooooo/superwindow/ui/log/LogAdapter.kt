package com.eiyooooo.superwindow.ui.log

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Filter
import android.widget.Filterable
import com.eiyooooo.superwindow.databinding.ItemLogBinding
import java.text.SimpleDateFormat
import java.util.Locale

class LogAdapter : BaseAdapter(), Filterable {

    private val mData = ArrayList<LogItem>()
    private var mFilteredData: ArrayList<LogItem>? = null
    private var mFilter: String? = null

    internal fun updateItems(items: List<LogItem>) {
        synchronized(LogAdapter::class.java) {
            mData.clear()
            mData.addAll(items)
            if (mFilter != null) {
                mFilteredData = ArrayList()
                for (item in mData) {
                    if (item.isNotFiltered(mFilter!!)) {
                        mFilteredData?.add(item)
                    }
                }
            } else {
                mFilteredData = null
            }
            notifyDataSetChanged()
        }
    }

    override fun getCount(): Int {
        return mFilteredData?.size ?: mData.size
    }

    override fun getItem(position: Int): LogItem {
        return mFilteredData?.get(position) ?: mData[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val holder: Holder
        val view: View
        if (convertView == null) {
            val binding = ItemLogBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            holder = Holder(binding)
            view = binding.root
        } else {
            holder = convertView.tag as Holder
            view = convertView
        }
        holder.parse(getItem(position))
        return view
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                synchronized(LogAdapter::class.java) {
                    val results = FilterResults()
                    if (constraint == null) {
                        mFilter = null
                        results.count = mData.size
                        results.values = null
                        return results
                    } else {
                        mFilter = constraint[0].toString()
                    }

                    val filtered = ArrayList<LogItem>()
                    for (item in mData) {
                        if (item.isNotFiltered(mFilter!!)) {
                            filtered.add(item)
                        }
                    }

                    results.values = filtered
                    results.count = filtered.size
                    return results
                }
            }

            @Suppress("UNCHECKED_CAST")
            override fun publishResults(constraint: CharSequence?, results: FilterResults) {
                mFilteredData = results.values as? ArrayList<LogItem>
                notifyDataSetChanged()
            }
        }
    }

    private class Holder(private val mBinding: ItemLogBinding) {
        init {
            mBinding.root.tag = this
        }

        fun parse(data: LogItem) {
            mBinding.time.text = String.format(
                Locale.getDefault(),
                "%s/%s",
                SimpleDateFormat("MM-dd hh:mm:ss.SSS", Locale.getDefault()).format(data.time),
                data.tag
            )
            mBinding.content.text = data.content
            mBinding.tag.text = data.priority
            mBinding.tag.setBackgroundResource(data.getColorRes())
        }
    }
}
