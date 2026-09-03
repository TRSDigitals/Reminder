package com.example.data.db

import androidx.room.*
import com.example.data.models.CattleProfile
import kotlinx.coroutines.flow.Flow

@Dao
interface CattleDao {
    @Query("SELECT * FROM cattle_profiles WHERE userId = :userId ORDER BY name ASC")
    fun getAllCattle(userId: String): Flow<List<CattleProfile>>

    @Query("SELECT * FROM cattle_profiles WHERE userId = :userId AND id = :id LIMIT 1")
    suspend fun getCattleById(userId: String, id: String): CattleProfile?

    @Query("SELECT * FROM cattle_profiles WHERE userId = :userId AND name = :name LIMIT 1")
    suspend fun getCattleByName(userId: String, name: String): CattleProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCattle(cattle: CattleProfile)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(cattleList: List<CattleProfile>)

    @Update
    suspend fun updateCattle(cattle: CattleProfile)

    @Query("DELETE FROM cattle_profiles WHERE id = :id AND userId = :userId")
    suspend fun deleteCattleById(id: String, userId: String)

    @Query("DELETE FROM cattle_profiles WHERE userId = :userId")
    suspend fun deleteAllForUser(userId: String)
}
