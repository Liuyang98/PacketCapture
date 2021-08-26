package com.ly.packetcapture.util

import android.content.Context
import com.ly.packetcapture.config.Config.CACHE_FILENAME
import android.os.Environment
import android.widget.Toast
import com.ly.packetcapture.config.Config
import java.io.*

/**
 * create by ly on 2021/8/24
 */
object FileUtil {
    fun saveTxt(phoneNumber: String, context: Context): Boolean {
        //sd卡检测
        val sdStatus = Environment.getExternalStorageState()
        if (sdStatus != Environment.MEDIA_MOUNTED) {
            Toast.makeText(context, "SD 卡不可用", Toast.LENGTH_SHORT).show()
            return false
        }

        //检测文件夹是否存在
        val file = File(Config.CACHE_DIR)
        file.exists()
        file.mkdirs()
        val p = CACHE_FILENAME
        var outputStream: FileOutputStream? = null
        try {
            //创建文件，并写入内容
            outputStream = FileOutputStream(File(p))
            outputStream.write(phoneNumber.toByteArray(charset("UTF-8")))
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
            return false
        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.flush()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
                try {
                    outputStream.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
        return true
    }

}