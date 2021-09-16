package com.ly.packetcapture.vpn.service

import android.content.Intent
import android.net.VpnService
import android.os.ParcelFileDescriptor
import com.ly.packetcapture.R
import com.ly.packetcapture.vpn.bio.BioUdpHandler
import com.ly.packetcapture.vpn.bio.NioSingleThreadTcpHandler
import com.ly.packetcapture.config.Config
import com.ly.packetcapture.config.Config.dns
import com.ly.packetcapture.vpn.protocol.tcpip.Packet
import com.ly.packetcapture.util.LogUtil
import com.ly.packetcapture.util.SharePrefUtil
import java.io.*
import java.lang.Exception
import java.nio.ByteBuffer
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.BlockingQueue
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.system.exitProcess

class LocalVPNService : VpnService() {
    //设备到网络UDP队列
    private var deviceToNetworkUDPQueue: BlockingQueue<Packet>? = null
    //设备到网络TCP队列
    private var deviceToNetworkTCPQueue: BlockingQueue<Packet>? = null
    //网络到设备的队列
    private var networkToDeviceQueue: BlockingQueue<ByteBuffer>? = null
    private lateinit var executorService: ExecutorService
    override fun onCreate() {
        super.onCreate()
        setupVPN()
        deviceToNetworkUDPQueue = ArrayBlockingQueue(1000)
        deviceToNetworkTCPQueue = ArrayBlockingQueue(1000)
        networkToDeviceQueue = ArrayBlockingQueue(1000)
        executorService = Executors.newFixedThreadPool(10)
        executorService.submit(BioUdpHandler(deviceToNetworkUDPQueue!!, networkToDeviceQueue!!, this))
        executorService.submit(NioSingleThreadTcpHandler(deviceToNetworkTCPQueue!!, networkToDeviceQueue!!, this))

        deviceToNetworkUDPQueue!!
        deviceToNetworkTCPQueue!!
        networkToDeviceQueue!!
        vpnInterface!!

        executorService.submit(VPNRunnable(vpnInterface!!.fileDescriptor, deviceToNetworkUDPQueue!!, deviceToNetworkTCPQueue!!, networkToDeviceQueue!!))
    }

    /**
     * 设置VPNService属性并获取对象
     */
    private fun setupVPN() {
        try {
            if (vpnInterface == null) {
                val builder = Builder()
                builder.addAddress(VPN_ADDRESS, 32)
                builder.addRoute(VPN_ROUTE, 0)
                builder.addDnsServer(dns)
                val selList = SharePrefUtil.getString(Config.SEL_KEY)
                if (selList.isNotEmpty()) {
                    val appList = selList.split(",").toTypedArray()
                    for (app in appList) {
                        LogUtil.e("拦截的APP:$app")
                        builder.addAllowedApplication(app)
                    }
                }
                builder.addAllowedApplication("com.android.systemui")
                vpnInterface = builder.setSession(getString(R.string.app_name)).establish()
            }
        } catch (e: Exception) {
            LogUtil.e("设置VPNService失败 : $e")
            exitProcess(0)
        }
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        //判断是否要关闭
        if (intent.getBooleanExtra("exit", false)) {
            executorService.shutdownNow()
            cleanup()
            stopSelf()
        }
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        executorService.shutdownNow()
        cleanup()
    }

    private fun cleanup() {
        deviceToNetworkTCPQueue = null
        deviceToNetworkUDPQueue = null
        networkToDeviceQueue = null
        vpnInterface?.let {
            closeResources(it)
            vpnInterface = null
        }
    }

    /**
     * 回写网卡线程
     */

    companion object {
        private const val VPN_ADDRESS = "10.0.0.2" // Only IPv4 support for now
        private const val VPN_ROUTE = "0.0.0.0" // Intercept everything
        var vpnInterface: ParcelFileDescriptor? = null

        // TODO: Move this to a "utils" class for reuse
        private fun closeResources(vararg resources: Closeable) {
            for (resource in resources) {
                try {
                    resource.close()
                } catch (e: Exception) {
                    // Ignore
                }
            }
        }
    }

}