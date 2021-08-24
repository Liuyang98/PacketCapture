package com.ly.packetcapture

import android.content.Intent
import android.net.Uri
import android.net.VpnService
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle


import android.view.View
import android.widget.Toast
import androidx.core.content.FileProvider
import com.ly.packetcapture.databinding.ActivityMainBinding
import com.ly.packetcapture.ui.applist.AppListActivity
import com.ly.packetcapture.config.Config
import com.ly.packetcapture.config.MyApplication.Companion.context
import com.ly.packetcapture.db.DatabaseUtil
import com.ly.packetcapture.ui.record.RecordActivity
import com.ly.packetcapture.util.*
import com.ly.packetcapture.vpn.LocalVPNService
import java.io.File
import java.lang.StringBuilder

class MainActivity : AppCompatActivity() {
    private lateinit var bind: ActivityMainBinding


    private val TASK_ID = "TASK_ID"
    private val VPN_REQUEST_CODE = 0x0F

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind = ActivityMainBinding.inflate(layoutInflater)
        setContentView(bind.root)
    }


    private fun startVpn() {
        Config.taskId = SharePrefUtil.getInt(TASK_ID, 0) + 1
        SharePrefUtil.setInt(TASK_ID, Config.taskId)

        val vpnIntent = VpnService.prepare(this)
        if (vpnIntent != null) {
            startActivityForResult(vpnIntent, VPN_REQUEST_CODE)
        } else {
            onActivityResult(VPN_REQUEST_CODE, RESULT_OK, null)
        }
    }

    private fun stopVpn() {
        val intent = Intent(this, LocalVPNService::class.java)
        intent.putExtra("exit", true)
        startService(intent)
    }


    /**
     * 开启VPN
     * @param view
     */
    fun openVpn(view: View?) {
        val running = ServiceUtil.isServiceRunning(LocalVPNService::class.java.name)
        if (running) {
            bind.tvOpenVpn.text = "开启VPN"
            stopVpn()
        } else {
            if (SharePrefUtil.getString(Config.SEL_KEY).isNotEmpty()) {
                bind.tvOpenVpn.text = "VPN运行中"
                startVpn()
            } else {
                Toast.makeText(this, "请先选择要代理的应用", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * 应用选择
     * @param view
     */
    fun selectApp(view: View?) {
        if (bind.tvOpenVpn.text.toString() == "VPN运行中") {
            Toast.makeText(this, "重新启动VPN后生效", Toast.LENGTH_SHORT).show()
        }
        startActivity(Intent(this, AppListActivity::class.java))
    }

    /**
     * DNS信息分享
     *
     * @param view
     */
    fun dnsShare(view: View?) {

        val list = DatabaseUtil.netDao.searchDNSByTaskId(Config.taskId)
        val sb = StringBuilder()
        for(bean in list){
            sb.append(bean.toString()).append("\n\n")
        }


        FileUtil.saveTxt(sb.toString(), this)
        LogUtil.e("地址：：" + Config.CACHE_DIR)
        IntentUtil.shareTxtFile(this)

    }

    /**
     * 查看DNS
     *
     * @param view
     */
    fun dnsSee(view: View?) {
        RecordActivity.to(this,true)
    }

    /**
     * 请求信息分享
     *
     * @param view
     */
    fun ipShare(view: View?) {

        val list = DatabaseUtil.netDao.searchReqByTaskId(Config.taskId)
        val sb = StringBuilder()
        for(bean in list){
            sb.append(bean.toString()).append("\n")
        }

        FileUtil.saveTxt(sb.toString(), this)
        LogUtil.e("请求信息：：" + Config.CACHE_DIR)
        IntentUtil.shareTxtFile(this)

    }

    /**
     * 查看IP
     *
     * @param view
     */
    fun ipSee(view: View?) {
        RecordActivity.to(this,false)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == VPN_REQUEST_CODE && resultCode == RESULT_OK) {
            startService(Intent(this, LocalVPNService::class.java))
            LogUtil.e("startService")
        }
    }


}