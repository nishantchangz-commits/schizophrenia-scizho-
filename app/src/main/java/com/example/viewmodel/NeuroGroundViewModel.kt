package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.AppDatabase
import com.example.data.model.ChatMessage
import com.example.data.model.CrisisResource
import com.example.data.model.GroundingSession
import com.example.data.model.PsychoeducationArticle
import com.example.data.repository.NeuroRepository
import com.example.safety.SafetyGuardrails
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

enum class BreathingPhase(val label: String, val instruction: String, val seconds: Int) {
    INHALE("Inhale", "Fill lungs gently through your nose", 4),
    HOLD_IN("Hold", "Rest comfortably with full lungs", 4),
    EXHALE("Exhale", "Release smoothly and slowly through mouth", 4),
    HOLD_OUT("Hold", "Rest calmly in the quiet pause", 4)
}

class NeuroGroundViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: NeuroRepository

    init {
        val db = AppDatabase.getDatabase(application)
        repository = NeuroRepository(db.groundingDao())
    }

    // --- Chat & Dialogue State ---
    private val _messages = MutableStateFlow<List<ChatMessage>>(
        listOf(
            ChatMessage(
                text = "Welcome to NeuroGround. I am your neuroscience psychoeducational guide and reality-anchoring companion. How is your nervous system feeling right now?\n\n${SafetyGuardrails.MANDATORY_DISCLAIMER}",
                isUser = false
            )
        )
    )
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating.asStateFlow()

    private val _showCrisisModal = MutableStateFlow(false)
    val showCrisisModal: StateFlow<Boolean> = _showCrisisModal.asStateFlow()

    fun openCrisisModal() {
        _showCrisisModal.value = true
    }

    fun closeCrisisModal() {
        _showCrisisModal.value = false
    }

    fun sendMessage(input: String) {
        val trimmed = input.trim()
        if (trimmed.isBlank()) return

        val userMsg = ChatMessage(text = trimmed, isUser = true)
        _messages.value = _messages.value + userMsg

        // Immediate Client-Side Crisis Check (Rule 4)
        if (SafetyGuardrails.isAcuteCrisis(trimmed)) {
            _showCrisisModal.value = true
            val crisisResponse = ChatMessage(
                text = "${SafetyGuardrails.CRISIS_OVERRIDE_BLOCK}\n\n${SafetyGuardrails.MANDATORY_DISCLAIMER}",
                isUser = false,
                isCrisisAlert = true
            )
            _messages.value = _messages.value + crisisResponse
            return
        }

        // Check ungrounded belief flag for UI highlighting
        val isUngrounded = SafetyGuardrails.isUngroundedBelief(trimmed)

        viewModelScope.launch {
            _isGenerating.value = true
            try {
                val history = _messages.value.map { it.text to it.isUser }
                val responseText = repository.processUserPrompt(trimmed, history)

                _messages.value = _messages.value + ChatMessage(
                    text = responseText,
                    isUser = false,
                    isUngroundedGroundingIntervention = isUngrounded
                )
            } catch (e: Exception) {
                _messages.value = _messages.value + ChatMessage(
                    text = SafetyGuardrails.generateOfflineSafeResponse(trimmed),
                    isUser = false
                )
            } finally {
                _isGenerating.value = false
            }
        }
    }

    fun clearChat() {
        _messages.value = listOf(
            ChatMessage(
                text = "Hello! I am here to share neuroscientific psychoeducation, somatosensory grounding, and stress-regulation strategies. What would you like to explore today?\n\n${SafetyGuardrails.MANDATORY_DISCLAIMER}",
                isUser = false
            )
        )
    }

    // --- Box Breathing State ---
    private val _breathingActive = MutableStateFlow(false)
    val breathingActive: StateFlow<Boolean> = _breathingActive.asStateFlow()

    private val _breathingPhase = MutableStateFlow(BreathingPhase.INHALE)
    val breathingPhase: StateFlow<BreathingPhase> = _breathingPhase.asStateFlow()

    private val _phaseProgress = MutableStateFlow(0f) // 0.0 to 1.0
    val phaseProgress: StateFlow<Float> = _phaseProgress.asStateFlow()

    private val _secondsRemainingInPhase = MutableStateFlow(4)
    val secondsRemainingInPhase: StateFlow<Int> = _secondsRemainingInPhase.asStateFlow()

    private val _cycleCount = MutableStateFlow(0)
    val cycleCount: StateFlow<Int> = _cycleCount.asStateFlow()

    private var breathingJob: Job? = null

    fun toggleBreathing() {
        if (_breathingActive.value) {
            stopBreathing()
        } else {
            startBreathing()
        }
    }

    fun startBreathing() {
        _breathingActive.value = true
        _breathingPhase.value = BreathingPhase.INHALE
        _cycleCount.value = 0
        breathingJob?.cancel()
        breathingJob = viewModelScope.launch {
            val phases = BreathingPhase.values()
            var phaseIndex = 0
            while (isActive && _breathingActive.value) {
                val current = phases[phaseIndex]
                _breathingPhase.value = current
                val totalMs = current.seconds * 1000L
                val stepMs = 50L
                var elapsedMs = 0L

                while (elapsedMs < totalMs && isActive && _breathingActive.value) {
                    delay(stepMs)
                    elapsedMs += stepMs
                    _phaseProgress.value = (elapsedMs.toFloat() / totalMs).coerceIn(0f, 1f)
                    val secLeft = current.seconds - (elapsedMs / 1000).toInt()
                    _secondsRemainingInPhase.value = secLeft.coerceAtLeast(1)
                }

                phaseIndex = (phaseIndex + 1) % phases.size
                if (phaseIndex == 0) {
                    _cycleCount.value = _cycleCount.value + 1
                }
            }
        }
    }

    fun stopBreathing() {
        _breathingActive.value = false
        breathingJob?.cancel()
        breathingJob = null
        _phaseProgress.value = 0f
    }

    // --- 5-4-3-2-1 Sensory Grounding State ---
    private val _sensoryStep = MutableStateFlow(0) // 0: See, 1: Touch, 2: Hear, 3: Smell, 4: Taste
    val sensoryStep: StateFlow<Int> = _sensoryStep.asStateFlow()

    private val _sensoryLogs = MutableStateFlow<Map<Int, List<String>>>(emptyMap())
    val sensoryLogs: StateFlow<Map<Int, List<String>>> = _sensoryLogs.asStateFlow()

    fun nextSensoryStep() {
        if (_sensoryStep.value < 4) {
            _sensoryStep.value = _sensoryStep.value + 1
        }
    }

    fun previousSensoryStep() {
        if (_sensoryStep.value > 0) {
            _sensoryStep.value = _sensoryStep.value - 1
        }
    }

    fun resetSensoryTool() {
        _sensoryStep.value = 0
        _sensoryLogs.value = emptyMap()
    }

    fun addSensoryItem(step: Int, item: String) {
        val current = _sensoryLogs.value.toMutableMap()
        val list = (current[step] ?: emptyList()) + item
        current[step] = list
        _sensoryLogs.value = current
    }

    // --- Journal & Somatic Check-In State ---
    val pastSessions: StateFlow<List<GroundingSession>> = repository.allSessions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _distressBefore = MutableStateFlow(7)
    val distressBefore: StateFlow<Int> = _distressBefore.asStateFlow()

    private val _distressAfter = MutableStateFlow(3)
    val distressAfter: StateFlow<Int> = _distressAfter.asStateFlow()

    private val _selectedSensations = MutableStateFlow<Set<String>>(emptySet())
    val selectedSensations: StateFlow<Set<String>> = _selectedSensations.asStateFlow()

    private val _journalNotes = MutableStateFlow("")
    val journalNotes: StateFlow<String> = _journalNotes.asStateFlow()

    private val _sessionSaveSuccess = MutableStateFlow(false)
    val sessionSaveSuccess: StateFlow<Boolean> = _sessionSaveSuccess.asStateFlow()

    fun setDistressBefore(score: Int) {
        _distressBefore.value = score
    }

    fun setDistressAfter(score: Int) {
        _distressAfter.value = score
    }

    fun toggleSensation(sensation: String) {
        val current = _selectedSensations.value.toMutableSet()
        if (current.contains(sensation)) {
            current.remove(sensation)
        } else {
            current.add(sensation)
        }
        _selectedSensations.value = current
    }

    fun setJournalNotes(notes: String) {
        _journalNotes.value = notes
    }

    fun saveCheckIn(exerciseType: String = "Grounding Check-In") {
        viewModelScope.launch {
            val session = GroundingSession(
                exerciseType = exerciseType,
                distressBefore = _distressBefore.value,
                distressAfter = _distressAfter.value,
                sensations = _selectedSensations.value.joinToString(", "),
                notes = _journalNotes.value
            )
            repository.saveSession(session)
            _sessionSaveSuccess.value = true
            delay(2000)
            _sessionSaveSuccess.value = false
            _journalNotes.value = ""
            _selectedSensations.value = emptySet()
        }
    }

    fun deleteSession(id: Long) {
        viewModelScope.launch {
            repository.deleteSession(id)
        }
    }

    // --- Psychoeducation Articles ---
    val articles: List<PsychoeducationArticle> = repository.getPsychoeducationArticles()

    private val _selectedArticle = MutableStateFlow<PsychoeducationArticle?>(null)
    val selectedArticle: StateFlow<PsychoeducationArticle?> = _selectedArticle.asStateFlow()

    fun selectArticle(article: PsychoeducationArticle?) {
        _selectedArticle.value = article
    }

    // --- Crisis Resources ---
    val crisisResources: List<CrisisResource> = repository.getCrisisResources()

    override fun onCleared() {
        super.onCleared()
        stopBreathing()
    }
}
