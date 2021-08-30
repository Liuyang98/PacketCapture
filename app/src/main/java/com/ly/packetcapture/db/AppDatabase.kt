package com.ly.packetcapture.db

import androidx.room.Database
import androidx.room.RoomDatabase

/**
 * create by ly on 2021/8/20
 */

@Database(entities = [NetData::class], version = 1 , exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    internal abstract fun getNetDao(): NetDao

}