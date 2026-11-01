package com.example.data.model

import java.util.UUID

data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val text: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis(),
    val isCrisisAlert: Boolean = false,
    val isUngroundedGroundingIntervention: Boolean = false
)
