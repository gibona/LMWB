package com.anobig.lmwb.map.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [Pin::class], version = 1)
abstract class MapDb : RoomDatabase() {
    abstract fun pinDao(): PinDao
}