package com.ly.packetcapture.ui.act

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.ly.packetcapture.databinding.ActivityRecordBinding
import com.ly.packetcapture.ui.adapter.RecordAdapter
import com.ly.packetcapture.ui.vm.RecordViewModel
import com.ly.packetcapture.util.LogUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * 抓包记录页
 * create by ly on 2021/8/23
 */
class RecordActivity : AppCompatActivity() {
    private val vm: RecordViewModel by viewModels()
    private lateinit var bind: ActivityRecordBinding
    val dns by lazy {
        intent.getBooleanExtra(DNS_KEY, false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind = ActivityRecordBinding.inflate(layoutInflater)
        setContentView(bind.root)
        initList()
    }

    private fun initList() {
        bind.rv.layoutManager = LinearLayoutManager(this@RecordActivity)
        lifecycleScope.launch(Dispatchers.Main) {
            vm.getDNSList(dns).flowOn(Dispatchers.IO).collect {
                bind.rv.adapter = RecordAdapter(this@RecordActivity, it)
            }
        }
    }

    companion object {
        const val DNS_KEY = "DNS_KEY"

        fun to(context: Context, dns: Boolean) {
            val intent = Intent(context, RecordActivity::class.java)
            intent.putExtra(DNS_KEY, dns)
            context.startActivity(intent)
        }
    }

}