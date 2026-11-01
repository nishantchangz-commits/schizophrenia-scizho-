package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.safety.SafetyGuardrails
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context matches NeuroGround`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("NeuroGround", appName)
    }

    @Test
    fun `acute crisis keywords trigger crisis override block`() {
        val crisisInput = "I am feeling suicidal and want to kill myself"
        assertTrue(SafetyGuardrails.isAcuteCrisis(crisisInput))

        val response = SafetyGuardrails.generateOfflineSafeResponse(crisisInput)
        assertTrue(response.contains("⚠️ IMMEDIATE SUPPORT & CRISIS RESOURCES"))
        assertTrue(response.contains("988 (Suicide & Crisis Lifeline)"))
        assertTrue(response.contains("DISCLAIMER: This application is an educational and psychoeducational tool"))
    }

    @Test
    fun `ungrounded beliefs trigger reality anchor and grounding intervention`() {
        val input = "There are microchips in my teeth tracking my thoughts"
        assertTrue(SafetyGuardrails.isUngroundedBelief(input))

        val response = SafetyGuardrails.generateOfflineSafeResponse(input)
        assertTrue(response.contains("It sounds genuinely exhausting"))
        assertTrue(response.contains("5-4-3-2-1 Sensory Reset"))
        assertTrue(response.contains("DISCLAIMER: This application is an educational and psychoeducational tool"))
    }

    @Test
    fun `general queries always conclude with mandatory non clinical disclaimer`() {
        val query = "How does the 5-4-3-2-1 technique work?"
        val response = SafetyGuardrails.generateOfflineSafeResponse(query)
        assertTrue(response.endsWith(SafetyGuardrails.MANDATORY_DISCLAIMER))
    }
}
