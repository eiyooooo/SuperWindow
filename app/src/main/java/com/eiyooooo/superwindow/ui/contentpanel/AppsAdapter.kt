package com.eiyooooo.superwindow.ui.contentpanel

import android.content.pm.LauncherActivityInfo
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.eiyooooo.superwindow.R
import com.eiyooooo.superwindow.entity.SystemServices
import kotlinx.coroutines.launch
import timber.log.Timber

class AppsAdapter(
    private val appsList: List<LauncherActivityInfo>,
    private val lifecycleScope: LifecycleCoroutineScope,
    private val callback: (String) -> Unit
) : RecyclerView.Adapter<AppsAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val icon: ImageView = itemView.findViewById(R.id.icon)
        val appName: TextView = itemView.findViewById(R.id.app_name)
        val click: View = itemView.findViewById(R.id.view_click)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_app, parent, false))

    override fun getItemCount(): Int = appsList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val activityInfo = appsList[position]
        val applicationInfo = activityInfo.applicationInfo
        lifecycleScope.launch {
            with(holder) {
                try {
                    Glide.with(holder.itemView.context)
                        .load(applicationInfo.loadIcon(SystemServices.packageManager))
                        .transition(DrawableTransitionOptions.withCrossFade())
                        .into(icon)
                    appName.text = activityInfo.label
                    click.setOnClickListener {
                        callback.invoke(applicationInfo.packageName)
                    }
                } catch (e: Exception) {
                    Timber.e(e)
                }
            }
        }
    }
}
