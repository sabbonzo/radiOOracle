package com.radiooracle.oracletoy

import com.radiooracle.oracletoy.ai.OracleVerdict
import com.radiooracle.oracletoy.logic.OutcomeMapper
import com.radiooracle.oracletoy.toy.ToyController
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class OutcomeMapperTest {

    @Test
    fun correctAnswerProducesRewardPatternWithinCap() {
        val steps = OutcomeMapper.map(
            OracleVerdict(answer = "42", correct = true, confidence = 1.0),
            intensityCap = 10,
        )
        assertTrue(steps.isNotEmpty())
        assertTrue(steps.all { it.level <= 10 })
        assertEquals(10, steps.first().level)
    }

    @Test
    fun wrongAnswerProducesNoPattern() {
        val steps = OutcomeMapper.map(
            OracleVerdict(answer = "wrong", correct = false, confidence = 0.9),
        )
        assertTrue(steps.isEmpty())
    }

    @Test
    fun confidenceScalesIntensityAndNeverExceedsMax() {
        val steps = OutcomeMapper.map(
            OracleVerdict(answer = "x", correct = true, confidence = 2.0), // out of range
            intensityCap = ToyController.MAX_INTENSITY + 5,                 // out of range
        )
        assertTrue(steps.all { it.level <= ToyController.MAX_INTENSITY })
    }

    @Test
    fun unknownCorrectnessProducesGentlePulse() {
        val steps = OutcomeMapper.map(
            OracleVerdict(answer = "?", correct = null, confidence = 0.5),
            intensityCap = 12,
        )
        assertTrue(steps.isNotEmpty())
        assertTrue(steps.first().level >= 1)
    }
}
