package com.eiyooooo.superwindow.adapters

import android.content.Context
import android.content.pm.LauncherActivityInfo
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.eiyooooo.superwindow.R
import com.github.promeg.pinyinhelper.Pinyin
import timber.log.Timber

class AllAppsAdapter(
    private val context: Context,
    private val allAppsList: ArrayList<LauncherActivityInfo>,
    private val callback: (String) -> Unit
) : RecyclerView.Adapter<AllAppsAdapter.ViewHolder>() {

    private val packageManager by lazy { context.packageManager }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val icon: ImageView = itemView.findViewById(R.id.icon)
        val appName: TextView = itemView.findViewById(R.id.app_name)
        val click: View = itemView.findViewById(R.id.view_click)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_app, parent, false))

    override fun getItemCount(): Int = allAppsList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val appInfo = allAppsList[position].applicationInfo
        val packageName = appInfo.packageName
        with(holder) {
            try {
                Glide.with(context)
                    .load(appInfo.loadIcon(packageManager))
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(icon)
                appName.text = allAppsList[position].label
                click.setOnClickListener {
                    callback.invoke(packageName)
                }
            } catch (e: Exception) {
                Timber.e(e)
            }
        }
    }

    fun getIndex(str: String?): Int {
        if (str.isNullOrEmpty()) return -1
        for ((index, app) in allAppsList.withIndex()) {
            if (Pinyin.toPinyin(app.label[0])[0] == str[0]) {
                return index
            }
        }
        return -1
    }
}
