package com.radiooracle.oracletoy.logic

import com.radiooracle.oracletoy.ai.OracleVerdict
import com.radiooracle.oracletoy.toy.PatternStep
import com.radiooracle.oracletoy.toy.ToyController

/**
 * Maps a solver [OracleVerdict] to a toy action, always within the intensity cap.
 * Pure and deterministic so it can be unit-tested without hardware or network.
 *
 * Behaviour: the answer is a LETTER and the toy pulses that many times.
 *   A -> 1 pulse, B -> 2 pulses, C -> 3, ... Z -> 26.
 * Each pulse runs at [intensityCap] (the user-set maximum). An unrecognised
 * answer produces an empty pattern (caller issues stop()).
 */
object OutcomeMapper {

    const val MAX_PULSES = 26            // A..Z
    const val PULSE_ON_MS = 400L
    const val PULSE_OFF_MS = 300L

    /** Convenience wrapper used by the ViewModel. */
    fun map(verdict: OracleVerdict, intensityCap: Int = ToyController.MAX_INTENSITY): List<PatternStep> =
        pulsesForAnswer(verdict.answer, intensityCap)

    /**
     * Extracts the first A..Z letter from [answer] and builds that many on/off
     * pulses at [level] (clamped to the toy's hard cap). A=1 … Z=26.
     */
    fun pulsesForAnswer(answer: String, level: Int): List<PatternStep> {
        val count = countForAnswer(answer)
        if (count <= 0) return emptyList()

        val lvl = level.coerceIn(0, ToyController.MAX_INTENSITY)
        if (lvl == 0) return emptyList()

        val steps = ArrayList<PatternStep>(count * 2)
        repeat(count) {
            steps.add(PatternStep(lvl, PULSE_ON_MS))
            steps.add(PatternStep(0, PULSE_OFF_MS))
        }
        return steps
    }

    /** A=1, B=2, … Z=26; 0 if no letter is present. */
    fun countForAnswer(answer: String): Int {
        val letter = answer.firstOrNull { it.isLetter() }?.uppercaseChar() ?: return 0
        if (letter < 'A' || letter > 'Z') return 0
        return (letter - 'A' + 1).coerceIn(0, MAX_PULSES)
    }
}
