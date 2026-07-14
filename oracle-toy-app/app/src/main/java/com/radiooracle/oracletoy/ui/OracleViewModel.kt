package com.radiooracle.oracletoy.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.radiooracle.oracletoy.ai.OracleSolver
import com.radiooracle.oracletoy.ai.OracleVerdict
import com.radiooracle.oracletoy.camera.CameraController
import com.radiooracle.oracletoy.logic.OutcomeMapper
import com.radiooracle.oracletoy.toy.ToyController
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class UiState(
    val consentGiven: Boolean = false,
    val busy: Boolean = false,
    val intensityCap: Int = 12,          // user-adjustable, always <= MAX_INTENSITY
    val lastVerdict: OracleVerdict? = null,
    val message: String? = null,
)

/**
 * Orchestrates consent -> capture -> solve -> map -> drive.
 * The STOP action bypasses everything and halts the toy immediately.
 */
class OracleViewModel(
    private val camera: CameraController,
    private val toy: ToyController,
    private val solver: OracleSolver,
) : ViewModel() {

    private val _ui = MutableStateFlow(UiState())
    val ui: StateFlow<UiState> = _ui.asStateFlow()

    val toyState = toy.state

    fun setConsent(given: Boolean) = _ui.update { it.copy(consentGiven = given) }

    fun setIntensityCap(cap: Int) =
        _ui.update { it.copy(intensityCap = cap.coerceIn(0, ToyController.MAX_INTENSITY)) }

    fun connectToy() = viewModelScope.launch {
        runCatching { toy.connect() }
            .onFailure { _ui.update { s -> s.copy(message = it.message) } }
    }

    /** The main loop: capture the quiz, solve it, drive the toy from the outcome. */
    fun askOracle() = viewModelScope.launch {
        if (!_ui.value.consentGiven) {
            _ui.update { it.copy(message = "Consenso richiesto prima di procedere.") }
            return@launch
        }
        _ui.update { it.copy(busy = true, message = null) }
        try {
            val frame = camera.capture()
            val verdict = solver.solve(frame)
            val steps = OutcomeMapper.map(verdict, _ui.value.intensityCap)
            if (steps.isEmpty()) toy.stop() else toy.pattern(steps)
            _ui.update { it.copy(lastVerdict = verdict) }
        } catch (t: Throwable) {
            toy.stop() // fail safe
            _ui.update { it.copy(message = t.message ?: "Errore") }
        } finally {
            _ui.update { it.copy(busy = false) }
        }
    }

    /** Always-available emergency stop / safeword. */
    fun stop() = viewModelScope.launch { toy.stop() }

    override fun onCleared() {
        toy.disconnect()
        super.onCleared()
    }
}
