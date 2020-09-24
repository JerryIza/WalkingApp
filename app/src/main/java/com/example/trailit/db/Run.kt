package com.example.trailit.db

import android.graphics.Bitmap
import androidx.room.Entity
import androidx.room.PrimaryKey

//entity is just a table in DB, creates columns for each of the variables
@Entity(tableName = "running_table")
data class Run (
    var img: Bitmap? = null,
    var timestamp: Long = 0L,
    var avgSpeedInMPH: Float = 0f,
    var distanceInMeters: Int = 0,
    var timeInMillis: Long = 0L,
    var caloriesBurned: Int = 0
){
   @PrimaryKey(autoGenerate = false)
   var id: Int? = null
}