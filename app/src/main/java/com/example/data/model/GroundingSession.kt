package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "grounding_sessions")
data class GroundingSession(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val exerciseType: String, // e.g. "5-4-3-2-1 Sensory", "4-4-4-4 Box Breathing", "Somatic Anchor"
    val distressBefore: Int, // 1 to 10
    val distressAfter: Int, // 1 to 10
    val sensations: String, // comma separated (e.g. "racing heart, chest tightness")
    val notes: String = ""
)
