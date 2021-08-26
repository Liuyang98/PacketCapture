package com.ly.packetcapture.util

import android.text.TextUtils
import android.util.Log
import com.ly.packetcapture.BuildConfig

/**
 * create by ly on 2021/5/17
 */
/**
 * LogUtils工具说明:
 * 1 只输出等级大于等于LEVEL的日志 所以在开发和产品发布后通过修改LEVEL来选择性输出日志.
 * 当LEVEL=NOTHING则屏蔽了所有的日志.
 * 2 v,d,i,w,e均对应两个方法. 若不设置TAG或者TAG为空则为设置默认TAG
 */
object LogUtil {
    const val VERBOSE = 1
    const val DEBUG = 2
    const val INFO = 3
    const val WARN = 4
    const val ERROR = 5
    const val NOTHING = 6
    val LEVEL = if (BuildConfig.DEBUG) VERBOSE else NOTHING
    const val SEPARATOR = ","
    fun v(message: String?) {
        if (LEVEL <= VERBOSE) {
            val stackTraceElement = Thread.currentThread()
                .stackTrace[3]
            val tag = getDefaultTag(stackTraceElement)
            if (!TextUtils.isEmpty(message)) Log.v(tag, message!!)
        }
    }

    fun v(tag: String?, message: String?) {
        var tag = tag
        if (LEVEL <= VERBOSE) {
            if (TextUtils.isEmpty(tag)) {
                val stackTraceElement = Thread.currentThread()
                    .stackTrace[3]
                tag = getDefaultTag(stackTraceElement)
            }
            if (!TextUtils.isEmpty(message)) Log.v(tag, message!!)
        }
    }

    fun d(message: String?) {
        if (LEVEL <= DEBUG) {
            val stackTraceElement = Thread.currentThread()
                .stackTrace[3]
            val tag = getDefaultTag(stackTraceElement)
            if (!TextUtils.isEmpty(message)) Log.d(tag, message!!)
        }
    }

    @JvmStatic
    fun d(tag: String?, message: String?) {
        var tag = tag
        if (LEVEL <= DEBUG) {
            if (TextUtils.isEmpty(tag)) {
                val stackTraceElement = Thread.currentThread()
                    .stackTrace[3]
                tag = getDefaultTag(stackTraceElement)
            }
            if (!TextUtils.isEmpty(message)) Log.d(tag, message!!)
        }
    }

    fun i(message: String?) {
        if (LEVEL <= INFO) {
            val stackTraceElement = Thread.currentThread()
                .stackTrace[3]
            val tag = getDefaultTag(stackTraceElement)
            if (!TextUtils.isEmpty(message)) Log.i(tag, message!!)
        }
    }

    @JvmStatic
    fun i(tag: String?, message: String?) {
        var tag = tag
        if (LEVEL <= INFO) {
            if (TextUtils.isEmpty(tag)) {
                val stackTraceElement = Thread.currentThread()
                    .stackTrace[3]
                tag = getDefaultTag(stackTraceElement)
            }
            if (!TextUtils.isEmpty(message)) Log.i(tag, message!!)
        }
    }

    fun w(message: String?) {
        if (LEVEL <= WARN) {
            val stackTraceElement = Thread.currentThread()
                .stackTrace[3]
            val tag = getDefaultTag(stackTraceElement)
            if (!TextUtils.isEmpty(message)) Log.w(tag, message!!)
        }
    }

    @JvmStatic
    fun w(tag: String?, message: String?) {
        var tag = tag
        if (LEVEL <= WARN) {
            if (TextUtils.isEmpty(tag)) {
                val stackTraceElement = Thread.currentThread()
                    .stackTrace[3]
                tag = getDefaultTag(stackTraceElement)
            }
            if (!TextUtils.isEmpty(message)) Log.w(tag, message!!)
        }
    }

    fun e(message: String?) {
        if (LEVEL <= ERROR) {
            val stackTraceElement = Thread.currentThread()
                .stackTrace[3]
            val tag = getDefaultTag(stackTraceElement)
            if (!TextUtils.isEmpty(message)) Log.e(tag, message!!)
        }
    }

    fun e(tag: String?, message: String?) {
        var tag = tag
        if (LEVEL <= ERROR) {
            if (TextUtils.isEmpty(tag)) {
                val stackTraceElement = Thread.currentThread()
                    .stackTrace[3]
                tag = getDefaultTag(stackTraceElement)
            }
            if (!TextUtils.isEmpty(message)) Log.e(tag, message!!)
        }
    }

    fun e(e: Throwable) {
        if (LEVEL <= ERROR) {
            val stackTraceElement = Thread.currentThread()
                .stackTrace[3]
            val tag = getDefaultTag(stackTraceElement)
            Log.e(tag, e.message, e)
        }
    }

    @JvmStatic
    fun e(tag: String?, e: Throwable) {
        var tag = tag
        if (LEVEL <= ERROR) {
            if (TextUtils.isEmpty(tag)) {
                val stackTraceElement = Thread.currentThread()
                    .stackTrace[3]
                tag = getDefaultTag(stackTraceElement)
            }
            Log.e(tag, e.message, e)
        }
    }

    /**
     * 获取默认的TAG名称. 比如在MainActivity.java中调用了日志输出. 则TAG为MainActivity
     */
    private fun getDefaultTag(stackTraceElement: StackTraceElement): String {
        val fileName = stackTraceElement.fileName
        val stringArray = fileName.split("\\.").toTypedArray()
        return stringArray[0]
    }

}