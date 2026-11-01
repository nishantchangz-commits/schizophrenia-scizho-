package com.example.data.repository

import android.util.Log
import com.example.BuildConfig
import com.example.data.api.GeminiApiService
import com.example.data.api.GeminiContent
import com.example.data.api.GeminiPart
import com.example.data.api.GeminiRequest
import com.example.data.api.SystemInstruction
import com.example.data.db.GroundingDao
import com.example.data.model.CrisisResource
import com.example.data.model.GroundingSession
import com.example.data.model.PsychoeducationArticle
import com.example.safety.SafetyGuardrails
import kotlinx.coroutines.flow.Flow

class NeuroRepository(
    private val dao: GroundingDao,
    private val apiService: GeminiApiService = GeminiApiService.create()
) {
    private val TAG = "NeuroRepository"

    val allSessions: Flow<List<GroundingSession>> = dao.getAllSessions()

    suspend fun saveSession(session: GroundingSession): Long {
        return dao.insertSession(session)
    }

    suspend fun deleteSession(id: Long) {
        dao.deleteSession(id)
    }

    /**
     * Sends message to AI or executes neuroscientific safety guardrails.
     * Guaranteed to follow:
     * 1. Never diagnose or prescribe
     * 2. Never validate ungrounded beliefs
     * 3. Always include mandatory disclaimer
     * 4. Immediate crisis override for self-harm/suicidal ideation
     */
    suspend fun processUserPrompt(userInput: String, history: List<Pair<String, Boolean>>): String {
        val trimmed = userInput.trim()

        // 1. Immediate Crisis Override (Rule 4)
        if (SafetyGuardrails.isAcuteCrisis(trimmed)) {
            return "${SafetyGuardrails.CRISIS_OVERRIDE_BLOCK}\n\n${SafetyGuardrails.MANDATORY_DISCLAIMER}"
        }

        // Try Gemini API if API key is configured
        val apiKey = try {
            BuildConfig::class.java.getField("GEMINI_API_KEY").get(null) as? String ?: ""
        } catch (e: Throwable) {
            ""
        }

        if (!apiKey.isNullOrBlank() && apiKey != "MY_GEMINI_API_KEY") {
            try {
                val conversationContents = mutableListOf<GeminiContent>()

                // Add recent history (up to last 6 turns)
                val recentHistory = history.takeLast(6)
                for ((msg, isUser) in recentHistory) {
                    conversationContents.add(
                        GeminiContent(
                            role = if (isUser) "user" else "model",
                            parts = listOf(GeminiPart(text = msg))
                        )
                    )
                }

                // Add current prompt
                conversationContents.add(
                    GeminiContent(
                        role = "user",
                        parts = listOf(GeminiPart(text = trimmed))
                    )
                )

                val request = GeminiRequest(
                    systemInstruction = SystemInstruction(
                        parts = listOf(GeminiPart(text = SafetyGuardrails.SYSTEM_PROMPT))
                    ),
                    contents = conversationContents
                )

                val response = apiService.generateContent(apiKey = apiKey, request = request)
                val reply = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text

                if (!reply.isNullOrBlank()) {
                    var safeReply = reply.trim()
                    // Ensure the disclaimer is explicitly present
                    if (!safeReply.contains("DISCLAIMER: This application is an educational and psychoeducational tool")) {
                        safeReply += "\n\n${SafetyGuardrails.MANDATORY_DISCLAIMER}"
                    }
                    return safeReply
                }
            } catch (e: Exception) {
                Log.w(TAG, "Gemini API call failed, falling back to local neuroscientific engine: ${e.message}")
            }
        }

        // Offline or fallback neuroscientific psychoeducation engine
        return SafetyGuardrails.generateOfflineSafeResponse(trimmed)
    }

    fun getPsychoeducationArticles(): List<PsychoeducationArticle> {
        return listOf(
            PsychoeducationArticle(
                id = "autonomic_system",
                title = "The Autonomic Nervous System",
                subtitle = "Sympathetic Mobilization vs. Parasympathetic Equilibrium",
                readingTimeMinutes = 3,
                category = "Autonomic System",
                summary = "Discover how your sympathetic fight-or-flight system and parasympathetic rest-and-digest branch govern bodily arousal.",
                neurobiologyInsight = "The autonomic nervous system regulates involuntary visceral functions. When threat is perceived, the sympathetic nervous system triggers norepinephrine release from postganglionic sympathetic fibers, accelerating heart rate and dilating bronchioles. Conversely, the parasympathetic system, mediated through the vagus nerve (Cranial Nerve X), secretes acetylcholine to stimulate the cholinergic anti-inflammatory pathway and slow heart rate.",
                practicalTechnique = "Physiological Sigh: Take two quick consecutive inhales through the nose followed by one long, slow sigh out the mouth. Repeat 2 to 3 times to rapidly recruit parasympathetic tone.",
                detailedSections = listOf(
                    "The Sympathetic Branch (Mobilization)" to "Originating in the thoracic and lumbar spinal cord, this division prepares the organism for physical action. Blood is shunted away from the gastrointestinal tract and skin toward skeletal muscle groups.",
                    "The Parasympathetic Branch (Restoration)" to "Originating in the brainstem and sacral spinal cord, this network conserves energy, promotes cellular repair, stabilizes glycemic control, and slows cardiac contraction.",
                    "Heart Rate Variability (HRV)" to "Higher beat-to-beat variability reflects a flexible, resilient autonomic nervous system capable of shifting dynamically between activation and recovery."
                )
            ),
            PsychoeducationArticle(
                id = "amygdala_prefrontal",
                title = "The Amygdala Alarm & Prefrontal Cortex",
                subtitle = "Why Somatosensory Grounding Halts Emotional Escalation",
                readingTimeMinutes = 4,
                category = "Brain Circuits",
                summary = "Understand the neural competition between limbic threat detection and prefrontal executive regulation.",
                neurobiologyInsight = "The basolateral amygdala acts as the brain's rapid threat detector, receiving sensory information via the thalamus along both a 'low road' (subcortical, fast, crude) and a 'high road' (cortical, slower, nuanced). During intense emotional flooding, amygdala hyperactivity inhibits the dorsolateral prefrontal cortex (dlPFC), impairing working memory and reality appraisal.",
                practicalTechnique = "5-4-3-2-1 Somatosensory Anchoring: Engaging 5 sensory channels forces cortical activation, recruiting top-down prefrontal inhibitory projections to down-regulate the amygdala.",
                detailedSections = listOf(
                    "The Thalamo-Amygdala Shortcut" to "Sensory inputs reach the amygdala milliseconds before reaching conscious cortical awareness. This evolutionary survival mechanism explains why physical panic symptoms often precede conscious understanding.",
                    "Top-Down vs. Bottom-Up Regulation" to "Top-down regulation uses cognitive reappraisal from the prefrontal cortex, which can fail under severe stress. Bottom-up regulation leverages bodily sensations and breathing to signal safety directly to the brainstem.",
                    "Breaking the Adrenaline Loop" to "When you acknowledge sensory facts in the room, the hippocampus and prefrontal cortex send inhibitory GABAergic signals to the amygdala, reducing sympathetic outflow."
                )
            ),
            PsychoeducationArticle(
                id = "salience_delusions",
                title = "Salience Network & Intrusive Thoughts",
                subtitle = "How the Brain Determines Meaning and Handles Stress Overload",
                readingTimeMinutes = 4,
                category = "Cognition & Salience",
                summary = "A neuroscientific overview of why high stress can lead to persecutory feelings, ungrounded ideas, and sensory hypervigilance.",
                neurobiologyInsight = "The brain's Salience Network (centered on the anterior insula and dorsal anterior cingulate cortex) acts as a switchboard, determining which internal and external stimuli warrant attention. Under acute neurochemical strain or dopamine dysregulation, neutral stimuli can acquire aberrant salience, creating intense subjective feelings that something menacing or hidden is occurring.",
                practicalTechnique = "Reality Anchoring: Do not fight or debate the thought. Acknowledge: 'My nervous system is in extreme hyper-alert mode and making everything feel dangerous. Let me feel the solid floor under my feet right now.'",
                detailedSections = listOf(
                    "What is Aberrant Salience?" to "In neurobiology, salience is how prominently an object stands out. When hyper-salience occurs, a flickering light or a passing car is mistakenly flagged by the brain as personally significant or threatening.",
                    "Compassionate Emotional Validation" to "The fear, exhaustion, and alarm are 100% physically genuine, even when the thought or premise does not match objective reality. Treating the physical fear with somatic calming helps ease the distress.",
                    "Seeking Clinical Guidance" to "Because persistent persecutory thoughts or perceptual changes can signal metabolic, neurochemical, or psychiatric conditions, compassionate evaluation by a licensed healthcare provider is essential."
                )
            ),
            PsychoeducationArticle(
                id = "neurotransmitters_stress",
                title = "Neurotransmitters & Stress Physiology",
                subtitle = "The Roles of GABA, Serotonin, Dopamine, and Cortisol",
                readingTimeMinutes = 3,
                category = "Sleep & Chemistry",
                summary = "Explore how key biochemical messengers communicate across synapses to modulate emotional reactivity and calm.",
                neurobiologyInsight = "GABA (gamma-aminobutyric acid) is the brain's principal inhibitory neurotransmitter, opening chloride channels to hyperpolarize neurons and prevent over-excitation. Cortisol, released by the adrenal cortex via the HPA axis, mobilizes glucose; however, prolonged elevated cortisol degrades dendritic spines in the hippocampus.",
                practicalTechnique = "Extended Exhalations: Breathing out twice as long as breathing in (e.g., 4s in, 8s out) promotes parasympathetic GABAergic inhibitory transmission.",
                detailedSections = listOf(
                    "GABA: The Neural Brake" to "GABA receptors counteract glutamate excitation. When GABA binds to its receptors, it calms neural firing rates throughout the central nervous system.",
                    "The HPA Axis Cascade" to "Hypothalamus (CRH) -> Pituitary (ACTH) -> Adrenal Glands (Cortisol). This feedback loop is designed for acute stressors, not chronic mental rumination.",
                    "Restoring Homeostasis" to "Consistent sleep cycles, hydration, rhythmic diaphragmatic breathing, and gentle physical movement support natural neurotransmitter synthesis."
                )
            )
        )
    }

    fun getCrisisResources(): List<CrisisResource> {
        return listOf(
            CrisisResource(
                id = "988_lifeline",
                name = "988 Suicide & Crisis Lifeline",
                description = "Free, confidential support available 24/7 for anyone experiencing mental health distress, suicidal crisis, or emotional pain.",
                phoneNumber = "988",
                smsNumber = "988",
                websiteUrl = "https://988lifeline.org",
                availability = "24/7, Free & Confidential",
                region = "US & Canada",
                isEmergencyPriority = true
            ),
            CrisisResource(
                id = "crisis_text_line",
                name = "Crisis Text Line",
                description = "Free, 24/7 text support with trained crisis counselors for any type of emotional or mental crisis.",
                smsNumber = "741741",
                smsKeyword = "HOME",
                websiteUrl = "https://www.crisistextline.org",
                availability = "24/7, Free & Confidential",
                region = "US, UK, Canada",
                isEmergencyPriority = true
            ),
            CrisisResource(
                id = "veterans_crisis",
                name = "Veterans Crisis Line",
                description = "Dedicated support for veterans, service members, and their loved ones. Dial 988 and press 1, or text 838255.",
                phoneNumber = "988",
                smsNumber = "838255",
                websiteUrl = "https://www.veteranscrisisline.net",
                availability = "24/7, Free & Confidential",
                region = "US",
                isEmergencyPriority = false
            ),
            CrisisResource(
                id = "trevor_project",
                name = "The Trevor Project",
                description = "Confidential suicide prevention and crisis intervention services for LGBTQ+ youth.",
                phoneNumber = "18664887386",
                smsNumber = "678678",
                smsKeyword = "START",
                websiteUrl = "https://www.thetrevorproject.org",
                availability = "24/7, Free & Confidential",
                region = "US",
                isEmergencyPriority = false
            ),
            CrisisResource(
                id = "uk_samaritans",
                name = "UK NHS 111 & Samaritans",
                description = "Call 111 for NHS Mental Health triage or 116 123 to speak with Samaritans anytime. Call 999 for emergencies.",
                phoneNumber = "116123",
                websiteUrl = "https://www.samaritans.org",
                availability = "24/7, Free & Confidential",
                region = "UK",
                isEmergencyPriority = false
            ),
            CrisisResource(
                id = "international_helpline",
                name = "International Helplines",
                description = "Comprehensive global directory to locate crisis hotlines, free text lines, and emergency services in your country.",
                websiteUrl = "https://findahelpline.com",
                availability = "Directory Across 100+ Countries",
                region = "International",
                isEmergencyPriority = false
            )
        )
    }
}
