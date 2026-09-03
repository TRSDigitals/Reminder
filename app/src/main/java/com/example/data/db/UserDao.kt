package com.example.data.db

import androidx.room.*
import com.example.data.models.UserProfile
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM user_profile LIMIT 1")
    fun getCurrentUserFlow(): Flow<UserProfile?>

    @Query("SELECT * FROM user_profile LIMIT 1")
    suspend fun getCurrentUser(): UserProfile?

    @Query("SELECT * FROM user_profile WHERE phoneNumber = :phone LIMIT 1")
    suspend fun getUserByPhone(phone: String): UserProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserProfile)

    @Update
    suspend fun updateUser(user: UserProfile)

    @Query("DELETE FROM user_profile")
    suspend fun clearUser()
}
