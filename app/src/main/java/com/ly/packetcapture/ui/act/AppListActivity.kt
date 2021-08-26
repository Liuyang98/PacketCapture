package com.ly.packetcapture.ui.act

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.ly.packetcapture.databinding.ActivityAppListBinding
import com.ly.packetcapture.ui.adapter.AppListAdapter
import com.ly.packetcapture.ui.vm.AppListViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * 应用选择页
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