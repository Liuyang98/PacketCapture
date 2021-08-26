package com.ly.packetcapture.ui.act

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.ly.packetcapture.databinding.ActivityRecordBinding
import com.ly.packetcapture.ui.adapter.RecordAdapter
import com.ly.packetcapture.ui.vm.RecordViewModel

/**
 * 抓包记录页
 * create by ly on 2021/8/23
 */
class RecordActivity : AppCompatActivity() {
    private val vm: RecordViewModel by viewModels()
    private lateinit var bind: ActivityRecordBinding
    val dns by lazy {
        intent.getBooleanExtra(DNS_KEY,false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind = ActivityRecordBinding.inflate(layoutInflater)
        setContentView(bind.root)
        initList()
    }

    private fun initList() {
        bind.rv.layoutManager = LinearLayoutManager(this@RecordActivity)
        bind.rv.adapter = RecordAdapter(this@RecordActivity, vm.getDNSList(dns))
    }

    companion object{
       const val DNS_KEY = "DNS_KEY"

        fun to (context: Context, dns:Boolean){
            val intent = Intent(context, RecordActivity::class.java)
            intent.putExtra(DNS_KEY,dns)
            context.startActivity(intent)
        }
    }

}