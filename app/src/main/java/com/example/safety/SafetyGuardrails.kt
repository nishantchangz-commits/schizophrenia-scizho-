package com.example.safety

object SafetyGuardrails {

    const val MANDATORY_DISCLAIMER =
        "DISCLAIMER: This application is an educational and psychoeducational tool powered by AI. It is NOT a clinical diagnostic instrument, prescriber of treatment, or substitute for evaluation by a licensed psychiatrist or physician. If you are experiencing acute distress, contact emergency services or dial 988."

    const val CRISIS_OVERRIDE_BLOCK = """⚠️ IMMEDIATE SUPPORT & CRISIS RESOURCES
It sounds like you may be going through an intensely overwhelming or distressing moment. Compassionate, confidential support is available right now:
• In the US & Canada (24/7, Free, Confidential): Call or Text 988 (Suicide & Crisis Lifeline) or chat at 988lifeline.org
• Crisis Text Line: Text HOME to 741741 (US, UK, Canada)
• Veterans Crisis Line: Dial 988 and press 1, or text 838255
• The Trevor Project (LGBTQ+ Youth): Call 1-866-488-7386 or text START to 678-678
• UK: Call 111 (NHS Mental Health) or 999 for emergencies. Call Samaritans at 116 123.
• International: Find local resources at findahelpline.com or befrienders.org
Please reach out to one of these resources right away or go to the nearest emergency room."""

    val SYSTEM_PROMPT = """You are a compassionate, grounded neuroscience and psychoeducational AI guide.
You strictly adhere to these 4 non-negotiable safety guardrails at all times:

1. NEVER DIAGNOSE OR PRESCRIBE:
- Under no circumstances diagnose medical or psychiatric illnesses, predict clinical trajectories, or prescribe, recommend, or alter medications or dosages.
- Clarify that all information is educational and neuroscientific.
- Encourage individuals to consult a qualified physician, psychiatrist, or licensed mental health professional regarding personal health choices.

2. NEVER VALIDATE UNGROUNDED BELIEFS, DELUSIONS, OR HALLUCINATIONS:
- If a user describes ungrounded premises, persecutory thoughts, thought insertion, or surveillance (e.g., chips in teeth, government mind-control, gangstalking, alien tracking):
  a. DO NOT validate, confirm, agree with, or elaborate upon the delusion as factual.
  b. DO NOT mock, challenge aggressively, or debate the user's perception.
  c. Maintain a calm, neutral, compassionate, and reality-anchored tone.
  d. Validate the EMOTIONAL REALITY (e.g., "It sounds exhausting and frightening to carry those feelings of being watched. While I cannot confirm anyone is monitoring you, your stress and anxiety are very real.").
  e. Offer tangible somatosensory grounding techniques (such as the 5-4-3-2-1 technique or deep 4-4-4-4 box breathing).
  f. Gently encourage contacting a trusted healthcare provider.

3. MANDATORY NON-CLINICAL DISCLAIMER:
- Always conclude your responses with this exact standard educational disclaimer:
$MANDATORY_DISCLAIMER

4. IMMEDIATE CRISIS OVERRIDE:
- If the user expresses suicidal ideation, intent, planning, self-harm, severe disorientation, or acute crisis, suspend standard discourse immediately and deliver this exact crisis support block:
---
$CRISIS_OVERRIDE_BLOCK
---
""".trimIndent()

    private val CRISIS_KEYWORDS = listOf(
        "suicide", "suicidal", "kill myself", "end my life", "want to die",
        "better off dead", "self-harm", "self harm", "cutting myself", "hurt myself",
        "take all my pills", "hang myself", "slit my wrists", "overdose",
        "can't go on anymore", "no reason to live", "ending it all", "end it all"
    )

    private val UNGROUNDED_BELIEF_KEYWORDS = listOf(
        "gangstalk", "gang stalk", "gang-stalk", "chips in teeth", "chip in teeth", "chip in my teeth",
        "in my teeth", "microchip", "microchips", "chip in my head", "chip in my brain",
        "government mind control", "government mind-control", "mind control", "mind-control",
        "government tracking", "spying on me", "alien tracking", "thought insertion",
        "tracking my thoughts", "watching me through", "implanted device", "targeted individual"
    )

    fun isAcuteCrisis(input: String): Boolean {
        val lower = input.lowercase()
        return CRISIS_KEYWORDS.any { lower.contains(it) }
    }

    fun isUngroundedBelief(input: String): Boolean {
        val lower = input.lowercase()
        return UNGROUNDED_BELIEF_KEYWORDS.any { lower.contains(it) }
    }

    /**
     * Provides an expert, neuroscientific and psychoeducational response honoring all 4 rules
     * when working offline or prior to network connection.
     */
    fun generateOfflineSafeResponse(input: String): String {
        val trimmed = input.trim()

        // Rule 4: Immediate Crisis Override
        if (isAcuteCrisis(trimmed)) {
            return "$CRISIS_OVERRIDE_BLOCK\n\n$MANDATORY_DISCLAIMER"
        }

        // Rule 2: Ungrounded Beliefs / Delusions / Surveillance
        if (isUngroundedBelief(trimmed)) {
            return """It sounds genuinely exhausting, overwhelming, and frightening to carry those intense feelings of being monitored or targeted. While I cannot confirm or validate that anyone is tracking or monitoring you, the emotional stress, fear, and nervous system activation you are experiencing right now are very real.

When our nervous system enters a state of heightened hypervigilance (often driven by the amygdala and salience network), the brain works overtime scanning the environment for threats, which can make benign sensory inputs feel charged or alarming.

Let us bring your focus gently into the present physical room with a tangible grounding exercise:
1. Place both feet firmly flat on the floor. Feel the solid earth supporting your weight.
2. The 5-4-3-2-1 Sensory Reset: Notice 5 objects around you, touch 4 physical textures (like your clothes or chair), listen for 3 distinct sounds, identify 2 scents, and notice 1 physical taste.
3. Try 4-4-4-4 Box Breathing: Inhale slowly for 4 seconds, hold gently for 4 seconds, exhale smoothly for 4 seconds, and hold for 4 seconds.

Because navigating persistent thoughts like this can be very depleting, I warmly encourage you to reach out to a trusted physician, psychiatrist, or licensed mental health professional who can provide a safe, caring, and thorough evaluation.

$MANDATORY_DISCLAIMER"""
        }

        // Educational neuroscience responses based on user queries
        val lower = trimmed.lowercase()
        val educationalBody = when {
            lower.contains("5-4-3-2-1") || lower.contains("sensory") || lower.contains("grounding") -> {
                """The 5-4-3-2-1 sensory technique is a validated somatosensory grounding exercise grounded in cognitive neuroscience. 

When you experience acute anxiety, overwhelm, or panic, the amygdala—the brain's emotional threat detector—signals the sympathetic nervous system to initiate a fight-or-flight cascade, causing cortisol and epinephrine release, elevated heart rate, and tunnel vision.

By systematically redirecting your attention to:
• 5 things you can see (activating visual cortex)
• 4 things you can touch (activating somatosensory cortex)
• 3 things you can hear (activating auditory cortex)
• 2 things you can smell (activating olfactory bulb)
• 1 thing you can taste (activating gustatory cortex)

You actively recruit the prefrontal cortex (executive control) and sensory cortices. This conscious sensory processing sends inhibitory signals (via GABAergic pathways) back to the amygdala, signaling that the current physical environment is safe and down-regulating the autonomic stress response."""
            }
            lower.contains("breathing") || lower.contains("box breathing") || lower.contains("4-4-4-4") -> {
                """Box breathing (4 seconds inhale, 4 seconds hold, 4 seconds exhale, 4 seconds hold) works directly through the vagus nerve and the autonomic nervous system.

During prolonged or controlled exhalations, the vagus nerve releases acetylcholine onto the sinoatrial node of the heart, slowing the heart rate and stimulating the parasympathetic 'rest-and-digest' branch. The 4-second breath retention stabilizes blood carbon dioxide (CO2) levels, helping prevent hyperventilation and restoring natural arterial balance. Practicing 3 to 5 cycles can rapidly shift physiological arousal from panic to equilibrium."""
            }
            lower.contains("panic") || lower.contains("anxiety") || lower.contains("heart") -> {
                """From a neurobiological standpoint, panic is an abrupt surge of sympathetic nervous system activation, commonly known as a false alarm in the amygdala. 

During this surge:
• The locus coeruleus releases norepinephrine, accelerating cardiac rhythm and respiration.
• Blood flow redirects from the prefrontal cortex to major skeletal muscles.
• Hyperventilation can drop CO2 levels, leading to tingling sensations in fingers and dizziness.

Understanding that these physical sensations—though profoundly uncomfortable—are harmless evolutionary survival mechanisms can help diminish the secondary fear loop. Physical somatic anchors, such as cold water on the face (triggering the mammalian dive reflex to drop heart rate) or bilateral tactile tapping, help reset autonomic tone."""
            }
            lower.contains("delusion") || lower.contains("intrusive") || lower.contains("thought") -> {
                """In cognitive neuroscience, intrusive thoughts and heightened vigilance are linked to aberrant salience within the brain's salience network (anchored by the anterior insula and dorsal anterior cingulate cortex).

When the salience network becomes dysregulated or hyper-reactive due to extreme stress, trauma, or neurochemical shifts, neutral thoughts or everyday environmental details can be perceived as intensely meaningful, urgent, or threatening. 

It is important to know that having an intrusive or intense thought does not make it an objective reality. Grounding back into the five senses and speaking with a licensed clinician can help disentangle internal perceptions from objective external reality."""
            }
            lower.contains("medication") || lower.contains("pill") || lower.contains("dose") || lower.contains("diagnos") -> {
                """As an educational resource, I cannot diagnose medical or psychological conditions, predict clinical trajectories, or recommend or modify medications or dosages. 

Psychiatric medications (such as SSRIs, SNRIs, mood stabilizers, or anxiolytics) interact with complex neurochemical receptors (including serotonin, dopamine, norepinephrine, and GABA) and require careful personalized evaluation by a licensed psychiatrist or medical doctor. If you are exploring medication options or having concerns about current prescriptions, please consult your prescribing physician."""
            }
            else -> {
                """Thank you for sharing your thoughts. From a neuroscience and psychoeducational perspective, our nervous system is constantly responding to sensory input, cognitive narratives, and autonomic feedback loops.

When stress or emotional intensity rises, conscious grounding—re-anchoring in physical sensations, pacing respiration, and engaging the senses—allows the prefrontal cortex to modulate emotional reactivity in the limbic system.

Feel free to explore our guided 5-4-3-2-1 Sensory Tool or 4-4-4-4 Box Breathing Visualizer, or ask any educational questions about brain mechanisms, stress physiology, and somatosensory regulation."""
            }
        }

        return "$educationalBody\n\n$MANDATORY_DISCLAIMER"
    }
}
