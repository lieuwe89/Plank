package com.planktracker.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "plank_records")
data class PlankRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val date: String,           // yyyy-MM-dd
    val durationSeconds: Int,   // actual time held
    val targetSeconds: Int,     // target for that day
    val completedAt: Long = System.currentTimeMillis()
) {
    val metTarget: Boolean get() = durationSeconds >= targetSeconds
}
