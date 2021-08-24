package com.ly.packetcapture.ui.applist

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.ly.packetcapture.databinding.ActivityAppListBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * create by ly on 2021/6/7
 */
class AppListActivity : AppCompatActivity() {
    private val vm: AppListViewModel by viewModels()
    private lateinit var bind : ActivityAppListBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind = ActivityAppListBinding.inflate(layoutInflater)
        setContentView(bind.root)
        initList()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun initList() {
        lifecycleScope.launch {
            val adapter = AppListAdapter(this@AppListActivity, vm.listNor)
            bind.rv.layoutManager = LinearLayoutManager(this@AppListActivity)
            bind.rv.adapter = adapter
            withContext(Dispatchers.IO) {
                vm.searchLocalAppList()
            }
            adapter.notifyDataSetChanged()
        }
    }

    override fun onDestroy() {
        vm.saveSelInfo()
        super.onDestroy()
    }
}