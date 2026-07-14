package com.radiooracle.oracletoy.logic

import com.radiooracle.oracletoy.ai.OracleVerdict
import com.radiooracle.oracletoy.toy.PatternStep
import com.radiooracle.oracletoy.toy.ToyController

/**
 * Maps a solver [OracleVerdict] to a toy action, always within the intensity cap.
 * Pure and deterministic so it can be unit-tested without hardware or network.
 *
 *  - correct == true  -> a short reward pattern scaled by confidence
 *  - correct == false -> stop (empty pattern)
 *  - correct == null  -> a gentle single pulse scaled by confidence
 */
object OutcomeMapper {

    fun map(verdict: OracleVerdict, intensityCap: Int = ToyController.MAX_INTENSITY): List<PatternStep> {
        val cap = intensityCap.coerceIn(0, ToyController.MAX_INTENSITY)
        val scaled = (verdict.confidence.coerceIn(0.0, 1.0) * cap).toInt().coerceIn(0, cap)

        return when (verdict.correct) {
            true -> listOf(
                PatternStep(scaled, 600),
                PatternStep(0, 200),
                PatternStep(scaled, 600),
                PatternStep(0, 0),
            )
            false -> emptyList() // caller issues stop() for an empty list
            null -> listOf(
                PatternStep((scaled / 2).coerceAtLeast(1), 800),
                PatternStep(0, 0),
            )
        }
    }
}
