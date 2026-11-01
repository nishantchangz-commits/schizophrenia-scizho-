package com.example.data.model

data class PsychoeducationArticle(
    val id: String,
    val title: String,
    val subtitle: String,
    val readingTimeMinutes: Int,
    val category: String, // "Autonomic System", "Brain Circuits", "Cognition & Salience", "Sleep & Chemistry"
    val summary: String,
    val neurobiologyInsight: String,
    val practicalTechnique: String,
    val detailedSections: List<Pair<String, String>>
)

data class CrisisResource(
    val id: String,
    val name: String,
    val description: String,
    val phoneNumber: String? = null,
    val smsNumber: String? = null,
    val smsKeyword: String? = null,
    val websiteUrl: String? = null,
    val availability: String, // "24/7 Free & Confidential"
    val region: String, // "US & Canada", "US, UK, Canada", "Global", "UK"
    val isEmergencyPriority: Boolean = false
)
