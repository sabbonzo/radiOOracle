package com.radiooracle.oracletoy.toy

import android.util.Log
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

/**
 * Hardware-free backend. Logs every command instead of driving BLE, so the full
 * camera -> solver -> action flow can be exercised without a device. Select this
 * backend in the UI for dry-run testing.
 */
class MockToyController : ToyController {

    private val _state = MutableStateFlow(ToyState())
    override val state: StateFlow<ToyState> = _state

    override suspend fun connect() {
        _state.update { it.copy(connection = ToyState.Connection.CONNECTING) }
        delay(300)
        _state.update {
            it.copy(
                connection = ToyState.Connection.CONNECTED,
                deviceName = "MOCK-Toy",
                batteryPercent = 88,
            )
        }
        Log.i(TAG, "connect()")
    }

    override suspend fun vibrate(level: Int) {
        val clamped = level.coerceIn(0, ToyController.MAX_INTENSITY)
        _state.update { it.copy(currentLevel = clamped) }
        Log.i(TAG, "Vibrate:$clamped;")
    }

    override suspend fun pattern(steps: List<PatternStep>) {
        for (step in steps) {
            vibrate(step.level)
            delay(step.durationMs.coerceAtMost(ToyController.MAX_CONTINUOUS_MS))
        }
        stop()
    }

    override suspend fun stop() {
        _state.update { it.copy(currentLevel = 0) }
        Log.i(TAG, "Vibrate:0; (stop)")
    }

    override suspend fun battery(): Int? = _state.value.batteryPercent

    override fun disconnect() {
        _state.update { ToyState() }
        Log.i(TAG, "disconnect()")
    }

    private companion object {
        const val TAG = "MockToyController"
    }
}
