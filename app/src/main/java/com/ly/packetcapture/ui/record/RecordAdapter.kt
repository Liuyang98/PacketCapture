package com.ly.packetcapture.ui.record

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ly.packetcapture.databinding.ActivityAppListItemBinding
import com.ly.packetcapture.databinding.ActivityRecordItemBinding
import com.ly.packetcapture.db.NetData

/**
 * create by ly on 2021/6/7
 */
open class RecordAdapter(private var mContext: Context, private var mDatas: List<NetData>) : RecyclerView.Adapter<RecordAdapter.RecordHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecordHolder {
        val bind = ActivityRecordItemBinding.inflate( LayoutInflater.from(mContext), parent, false)
        return RecordHolder(bind)
    }

    override fun onBindViewHolder(holder: RecordHolder, position: Int) {
        holder.bind.tv.text = mDatas[position].toString()
    }

    override fun getItemCount() = mDatas.size

    inner class RecordHolder(val bind:  ActivityRecordItemBinding) : RecyclerView.ViewHolder(bind.root) {

    }
}