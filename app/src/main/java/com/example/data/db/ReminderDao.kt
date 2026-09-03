package com.example.data.db

import androidx.room.*
import com.example.data.models.ReminderItem
import kotlinx.coroutines.flow.Flow

@Dao
interface ReminderDao {
    @Query("SELECT * FROM reminders WHERE userId = :userId AND isActive = 1 ORDER BY targetDate ASC, targetTime ASC")
    fun getAllActiveReminders(userId: String): Flow<List<ReminderItem>>

    @Query("SELECT * FROM reminders WHERE userId = :userId AND targetDate = :date AND isActive = 1 ORDER BY targetTime ASC")
    fun getRemindersForDate(userId: String, date: String): Flow<List<ReminderItem>>

    @Query("SELECT * FROM reminders WHERE userId = :userId AND targetDate LIKE :monthPrefix || '%' AND isActive = 1 ORDER BY targetDate ASC")
    fun getRemindersForMonth(userId: String, monthPrefix: String): Flow<List<ReminderItem>>

    @Query("SELECT * FROM reminders WHERE userId = :userId AND isCompleted = 0 AND isActive = 1 ORDER BY targetDate ASC, targetTime ASC")
    fun getPendingReminders(userId: String): Flow<List<ReminderItem>>

    @Query("SELECT * FROM reminders WHERE userId = :userId AND targetDate = :date AND isCompleted = 0 AND isActive = 1 ORDER BY targetTime ASC")
    fun getPendingRemindersForDate(userId: String, date: String): Flow<List<ReminderItem>>

    @Query("SELECT * FROM reminders WHERE userId = :userId AND id = :id LIMIT 1")
    suspend fun getReminderById(userId: String, id: String): ReminderItem?

    @Query("SELECT * FROM reminders WHERE id = :id LIMIT 1")
    suspend fun getReminderByIdNoUser(id: String): ReminderItem?

    @Query("SELECT * FROM reminders WHERE isActive = 1 AND isCompleted = 0")
    suspend fun getAllPendingAlarms(): List<ReminderItem>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminder(reminder: ReminderItem)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(reminders: List<ReminderItem>)

    @Update
    suspend fun updateReminder(reminder: ReminderItem)

    @Query("UPDATE reminders SET isCompleted = 1, completedAt = :completedAt, updatedAt = :completedAt WHERE id = :id")
    suspend fun markCompleted(id: String, completedAt: Long = System.currentTimeMillis())

    @Query("UPDATE reminders SET advanceNotified = 1 WHERE id = :id")
    suspend fun markAdvanceNotified(id: String)

    @Query("DELETE FROM reminders WHERE id = :id AND userId = :userId")
    suspend fun deleteReminderById(id: String, userId: String)

    @Query("DELETE FROM reminders WHERE userId = :userId")
    suspend fun deleteAllForUser(userId: String)
}
