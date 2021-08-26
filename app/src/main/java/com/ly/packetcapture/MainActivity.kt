package com.ly.packetcapture


import android.content.Intent
import android.net.VpnService
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.ly.packetcapture.databinding.ActivityMainBinding
import com.ly.packetcapture.ui.act.AppListActivity
import com.ly.packetcapture.ui.act.RecordActivity
import com.ly.packetcapture.ui.vm.MainViewModel
import com.ly.packetcapture.util.*
import com.ly.packetcapture.vpn.service.LocalVPNService

class MainActivity : AppCompatActivity() {
    private val VPN_REQUEST_CODE = 0x0F
    private lateinit var bind: ActivityMainBinding
    private val vm: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind = ActivityMainBinding.inflate(layoutInflater)
        setContentView(bind.root)
    }

    /**
     * 启动VPN
     */
    private fun startVpn() {
        vm.startDataSave()
        val vpnIntent = VpnService.prepare(this)
        if (vpnIntent != null) {
            startActivityForResult(vpnIntent, VPN_REQUEST_CODE)
        } else {
            onActivityResult(VPN_REQUEST_CODE, RESULT_OK, null)
        }
    }

    /**
     * 关闭VPN
     */
    private fun stopVpn() {
        val intent = Intent(this, LocalVPNService::class.java)
        intent.putExtra("exit", true)
        startService(intent)
    }

    /**
     * 开启VPN
     */
    fun openVpn(view: View?) {
        val running = ServiceUtil.isServiceRunning(LocalVPNService::class.java.name)
        if (running) {
            bind.tvOpenVpn.text = getString(R.string.vpn_start)
            stopVpn()
        } else {
            if (vm.hasSelectApp()) {
                bind.tvOpenVpn.text = getString(R.string.vpn_running)
                startVpn()
            } else {
                Toast.makeText(this, getString(R.string.no_select_app), Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * 应用选择
     * @param view
     */
    fun selectApp(view: View?) {
        if (ServiceUtil.isServiceRunning(LocalVPNService::class.java.name)) {
            Toast.makeText(this, getString(R.string.change_select_in_running), Toast.LENGTH_SHORT).show()
        }
        startActivity(Intent(this, AppListActivity::class.java))
    }

    /**
     * DNS查看
     *
     * @param view
     */
    fun dnsSee(view: View?) {
        RecordActivity.to(this, true)
    }

    /**
     * DNS信息分享
     *
     * @param view
     */
    fun dnsShare(view: View?) {
        vm.dealShareDnsData(this)
        IntentUtil.shareTxtFile(this)
    }

    /**
     * 请求信息查看
     *
     * @param view
     */
    fun reqSee(view: View?) {
        RecordActivity.to(this, false)
    }

    /**
     * 请求信息分享
     *
     * @param view
     */
    fun reqShare(view: View?) {
        vm.dealShareReqData(this)
        IntentUtil.shareTxtFile(this)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == VPN_REQUEST_CODE && resultCode == RESULT_OK) {
            startService(Intent(this, LocalVPNService::class.java))
        }
    }

}