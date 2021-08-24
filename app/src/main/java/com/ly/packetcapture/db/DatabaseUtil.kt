package com.ly.packetcapture.db

import androidx.room.Room
import com.ly.packetcapture.config.MyApplication
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

/**
 * create by ly on 2021/8/20
 */
object DatabaseUtil {
    val netDao by lazy {
        Room.databaseBuilder(MyApplication.context, AppDatabase::class.java, "database-net")
            .fallbackToDestructiveMigration()
            .allowMainThreadQueries()
            .build().getNetDao()
    }


    fun insert(netData: NetData){
        GlobalScope.launch(Dispatchers.IO) {
            netDao.insert(netData)
        }
    }
}
