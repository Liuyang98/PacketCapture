package com.ly.packetcapture.util

import com.ly.packetcapture.config.MyApplication
import android.preference.PreferenceManager

/**
 * Created by yangl.liu on 2017/5/19.
 */
/**
 * 选项设置，可以设置和获取的数据类型有：String、int、boolean
 */
object SharePrefUtil {
    private val mPref  = PreferenceManager.getDefaultSharedPreferences(MyApplication.context)
    private val editor = mPref.edit()

    /**
     * 获得参数：String
     *
     * @param name
     * @return String
     */
    fun getString(name: String?): String {
        return mPref.getString(name, "")!!
    }

    /**
     * 设置参数：String
     *
     * @param name
     * @param value
     */
    fun setString(name: String?, value: String?) {
        editor.putString(name, value)
        editor.commit()
    }

    /**
     * 获得参数：int
     *
     * @param name
     * @return int
     */
    fun getInt(name: String, value: Int = 0): Int {
        return mPref.getInt(name, value)
    }

    /**
     * 设置参数：int
     *
     * @param name
     * @param value
     */
    fun setInt(name: String?, value: Int) {
        editor.putInt(name, value)
        editor.commit()
        return
    }

    /**
     * 设置参数：boolean
     *
     * @param name
     * @param value
     */
    fun setBool(name: String?, value: Boolean) {
        editor.putBoolean(name, value)
        editor.commit()
    }

    /**
     * 获得参数：boolean
     *
     * @param name
     * @return
     */
    fun getBool(name: String?): Boolean {
        return mPref.getBoolean(name, false)
    }

    /**
     * 设置参数：long
     *
     * @param name
     * @param value
     */
    fun setLong(name: String?, value: Long) {
        editor.putLong(name, value)
        editor.commit()
    }

    /**
     * 获得参数：long
     *
     * @param name
     * @return
     */
    fun getLong(name: String?): Long {
        return mPref.getLong(name, 0L)
    }

    /**
     * 清除配置文件内容
     */
    fun clear() {
        editor.clear()
        editor.commit()
    }

    /**
     * 移除某个key值已经对应的值
     *
     * @param key
     */
    fun remove(key: String?) {
        editor.remove(key)
        editor.commit()
    }

}