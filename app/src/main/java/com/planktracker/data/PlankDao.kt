package com.planktracker.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface PlankDao {

    @Query("SELECT * FROM plank_records ORDER BY date DESC")
    fun getAllRecords(): Flow<List<PlankRecord>>

    @Query("SELECT * FROM plank_records WHERE date = :date LIMIT 1")
    suspend fun getRecordForDate(date: String): PlankRecord?

    @Query("SELECT * FROM plank_records WHERE date = :date LIMIT 1")
    fun observeRecordForDate(date: String): Flow<PlankRecord?>

    @Query("SELECT * FROM plank_records ORDER BY durationSeconds DESC LIMIT 1")
    suspend fun getBestRecord(): PlankRecord?

    @Query("SELECT COUNT(*) FROM plank_records")
    suspend fun getTotalCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(record: PlankRecord)

    @Delete
    suspend fun delete(record: PlankRecord)

    @Query("DELETE FROM plank_records")
    suspend fun deleteAll()
}
