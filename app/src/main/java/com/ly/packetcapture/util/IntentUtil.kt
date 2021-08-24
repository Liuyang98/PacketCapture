package com.ly.packetcapture.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.ly.packetcapture.config.Config
import java.io.File

/**
 * create by ly on 2021/8/24
 */
object IntentUtil {
    /**
     * 采用系统分享文件方式通过QQ分享文件
     */
    fun shareTxtFile(context: Context) {

        // 通过系统自带分享发送文件
        val intent = Intent(Intent.ACTION_SEND)
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
        intent.type = "text/plain"
        val photoUri: Uri = FileProvider.getUriForFile(context, context.packageName + ".fileprovider", File(Config.CACHE_FILENAME))

        intent.putExtra(Intent.EXTRA_STREAM, photoUri)
        context.startActivity(Intent.createChooser(intent, "分享"))
    }

}