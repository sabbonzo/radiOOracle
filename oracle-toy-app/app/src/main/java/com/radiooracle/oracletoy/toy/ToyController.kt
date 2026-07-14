package com.radiooracle.oracletoy.toy

import kotlinx.coroutines.flow.StateFlow

/**
 * Abstract interface for a toy backend. Two implementations exist:
 *  - [MockToyController]      logs commands, needs no hardware (used for dry-run tests)
 *  - [LovenseBleController]   drives a real Lovense device over BLE
 *
 * Safety is part of the contract, not an afterthought:
 *  - [stop] must halt vibration immediately and is wired to the always-visible STOP button.
 *  - Implementations MUST clamp intensity to [MAX_INTENSITY] and cap continuous
 *    runtime so a bug cannot leave the device running away.
 */
interface ToyController {

    val state: StateFlow<ToyState>

    /** Scan, connect, and discover the write characteristic. Safe to call again to reconnect. */
    suspend fun connect()

    /** Vibrate at [level] (0..[MAX_INTENSITY]). Values above the cap are clamped. */
    suspend fun vibrate(level: Int)

    /** Run a timed pattern of (level, durationMs) steps, then stop. */
    suspend fun pattern(steps: List<PatternStep>)

    /** Immediately stop all output. Wired to the STOP / safeword control. */
    suspend fun stop()

    /** Query battery percentage (0..100), or null if unknown/unsupported. */
    suspend fun battery(): Int?

    fun disconnect()

    companion object {
        /** Lovense vibration scale is 0..20. This is the hard ceiling for every backend. */
        const val MAX_INTENSITY = 20

        /** No single command may keep the toy running longer than this without a refresh. */
        const val MAX_CONTINUOUS_MS = 30_000L
    }
}

data class PatternStep(val level: Int, val durationMs: Long)

data class ToyState(
    val connection: Connection = Connection.DISCONNECTED,
    val deviceName: String? = null,
    val batteryPercent: Int? = null,
    val currentLevel: Int = 0,
    val lastError: String? = null,
) {
    enum class Connection { DISCONNECTED, SCANNING, CONNECTING, CONNECTED }
}
