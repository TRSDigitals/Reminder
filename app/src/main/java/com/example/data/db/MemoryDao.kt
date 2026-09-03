package com.example.data.db

import androidx.room.*
import com.example.data.models.MemoryRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface MemoryDao {
    @Query("SELECT * FROM memory_records WHERE userId = :userId ORDER BY timestamp DESC")
    fun getAllRecords(userId: String): Flow<List<MemoryRecord>>

    @Query("SELECT * FROM memory_records WHERE userId = :userId AND category = :category ORDER BY timestamp DESC")
    fun getRecordsByCategory(userId: String, category: String): Flow<List<MemoryRecord>>

    @Query("SELECT * FROM memory_records WHERE userId = :userId AND date = :date ORDER BY time ASC")
    fun getRecordsByDate(userId: String, date: String): Flow<List<MemoryRecord>>

    @Query("SELECT * FROM memory_records WHERE userId = :userId AND date LIKE :monthPrefix || '%' ORDER BY date ASC, time ASC")
    fun getRecordsByMonth(userId: String, monthPrefix: String): Flow<List<MemoryRecord>>

    @Query("SELECT * FROM memory_records WHERE userId = :userId AND (text LIKE '%' || :query || '%' OR relatedAnimal LIKE '%' || :query || '%' OR category LIKE '%' || :query || '%') ORDER BY timestamp DESC")
    fun searchRecords(userId: String, query: String): Flow<List<MemoryRecord>>

    @Query("SELECT * FROM memory_records WHERE userId = :userId AND relatedAnimal = :animalName ORDER BY timestamp DESC")
    fun getRecordsForAnimal(userId: String, animalName: String): Flow<List<MemoryRecord>>

    @Query("SELECT * FROM memory_records WHERE userId = :userId AND relatedPlot = :plotName ORDER BY timestamp DESC")
    fun getRecordsForPlot(userId: String, plotName: String): Flow<List<MemoryRecord>>

    @Query("SELECT * FROM memory_records WHERE userId = :userId AND syncStatus = 'PENDING_SYNC'")
    suspend fun getPendingSyncRecords(userId: String): List<MemoryRecord>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: MemoryRecord)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(records: List<MemoryRecord>)

    @Update
    suspend fun updateRecord(record: MemoryRecord)

    @Query("DELETE FROM memory_records WHERE id = :id AND userId = :userId")
    suspend fun deleteRecordById(id: String, userId: String)

    @Query("DELETE FROM memory_records WHERE userId = :userId")
    suspend fun deleteAllForUser(userId: String)
}
