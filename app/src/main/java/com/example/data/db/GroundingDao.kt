package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.GroundingSession
import kotlinx.coroutines.flow.Flow

@Dao
interface GroundingDao {
    @Query("SELECT * FROM grounding_sessions ORDER BY timestamp DESC")
    fun getAllSessions(): Flow<List<GroundingSession>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: GroundingSession): Long

    @Query("DELETE FROM grounding_sessions WHERE id = :id")
    suspend fun deleteSession(id: Long)

    @Query("SELECT COUNT(*) FROM grounding_sessions")
    fun getSessionCount(): Flow<Int>
}
