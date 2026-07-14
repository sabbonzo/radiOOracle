package com.radiooracle.oracletoy

import com.radiooracle.oracletoy.ai.OracleVerdict
import com.radiooracle.oracletoy.logic.OutcomeMapper
import com.radiooracle.oracletoy.toy.ToyController
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class OutcomeMapperTest {

    @Test
    fun letterMapsToPulseCount() {
        // A=1, B=2, C=3 -> each pulse is an (on, off) pair, so 2 steps per pulse.
        assertEquals(1, OutcomeMapper.countForAnswer("A"))
        assertEquals(2, OutcomeMapper.countForAnswer("B"))
        assertEquals(3, OutcomeMapper.countForAnswer("c")) // case-insensitive
        assertEquals(26, OutcomeMapper.countForAnswer("Z"))

        val bPulses = OutcomeMapper.pulsesForAnswer("B", level = 10)
        assertEquals(2 * 2, bPulses.size)                 // 2 pulses = 4 steps
        assertEquals(2, bPulses.count { it.level > 0 })   // 2 "on" steps
    }

    @Test
    fun answerWithSurroundingTextStillReadsFirstLetter() {
        // e.g. "Opzione C" -> the first letter is 'O' (15). Extracts first A..Z.
        assertEquals('O' - 'A' + 1, OutcomeMapper.countForAnswer("Opzione C"))
    }

    @Test
    fun pulseLevelNeverExceedsCap() {
        val steps = OutcomeMapper.pulsesForAnswer("D", level = ToyController.MAX_INTENSITY + 5)
        assertTrue(steps.all { it.level <= ToyController.MAX_INTENSITY })
    }

    @Test
    fun noLetterOrZeroLevelProducesNoPattern() {
        assertTrue(OutcomeMapper.pulsesForAnswer("123", level = 10).isEmpty())
        assertTrue(OutcomeMapper.pulsesForAnswer("A", level = 0).isEmpty())
    }

    @Test
    fun mapDelegatesToLetterPulses() {
        val steps = OutcomeMapper.map(
            OracleVerdict(answer = "A", correct = true, confidence = 1.0),
            intensityCap = 12,
        )
        assertEquals(1 * 2, steps.size)   // A -> 1 pulse -> 2 steps
        assertEquals(12, steps.first().level)
    }
}
