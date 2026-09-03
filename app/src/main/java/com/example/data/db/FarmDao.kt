package com.example.data.db

import androidx.room.*
import com.example.data.models.FarmPlot
import kotlinx.coroutines.flow.Flow

@Dao
interface FarmDao {
    @Query("SELECT * FROM farm_plots WHERE userId = :userId ORDER BY name ASC")
    fun getAllPlots(userId: String): Flow<List<FarmPlot>>

    @Query("SELECT * FROM farm_plots WHERE userId = :userId AND id = :id LIMIT 1")
    suspend fun getPlotById(userId: String, id: String): FarmPlot?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlot(plot: FarmPlot)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(plots: List<FarmPlot>)

    @Update
    suspend fun updatePlot(plot: FarmPlot)

    @Query("DELETE FROM farm_plots WHERE id = :id AND userId = :userId")
    suspend fun deletePlotById(id: String, userId: String)

    @Query("DELETE FROM farm_plots WHERE userId = :userId")
    suspend fun deleteAllForUser(userId: String)
}
