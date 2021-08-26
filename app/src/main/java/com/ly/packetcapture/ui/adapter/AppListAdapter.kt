package com.ly.packetcapture.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ly.packetcapture.ui.adapter.AppListAdapter.AppListViewHolder
import com.ly.packetcapture.databinding.ActivityAppListItemBinding
import com.ly.packetcapture.bean.AppListBean

/**
 * create by ly on 2021/6/7
 */
open class AppListAdapter(private var mContext: Context, private var mDatas: List<AppListBean>) : RecyclerView.Adapter<AppListViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppListViewHolder {
        val bind = ActivityAppListItemBinding.inflate( LayoutInflater.from(mContext), parent, false)
        return AppListViewHolder(bind)
    }

    override fun onBindViewHolder(holder: AppListViewHolder, position: Int) {
        val bean = mDatas[position]
        holder.bean = bean
        holder.bind.tv.text = bean.appLabel
        holder.bind.iv.setImageDrawable(bean.appIcon)
        holder.bind.sw.isChecked = bean.selected
    }

    override fun getItemCount() = mDatas.size

    inner class AppListViewHolder(val bind:  ActivityAppListItemBinding) : RecyclerView.ViewHolder(bind.root) {
        lateinit var bean: AppListBean

        init {
            bind.sw.setOnCheckedChangeListener { _, isChecked -> bean.selected = isChecked }
        }
    }
}