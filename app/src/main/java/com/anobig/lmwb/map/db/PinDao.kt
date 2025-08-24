package com.anobig.lmwb.map.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface PinDao {
    @Insert
    fun insertAll(vararg pinss: Pin)

    @Query("DELETE FROM pin WHERE uid = :uid")
    fun delete(uid: Int)

    @Query("SELECT * FROM pin")
    fun getPins(): LiveData<List<Pin>>

    @Update
    fun update(pin: Pin)

    @Query("SELECT * FROM pin where uid = :uid")
    fun getPin(uid: Int): List<Pin>
}