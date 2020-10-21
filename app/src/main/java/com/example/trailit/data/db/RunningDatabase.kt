package com.example.trailit.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.trailit.data.converters.Converters
import com.example.trailit.data.entitites.Run

@Database(
    entities = [Run::class],
    version = 1
)
@TypeConverters(Converters::class)
abstract class RunningDatabase : RoomDatabase(){

    abstract fun getRunDao(): RunDAO

}