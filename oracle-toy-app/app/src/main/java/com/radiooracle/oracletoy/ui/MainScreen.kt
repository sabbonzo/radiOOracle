package com.radiooracle.oracletoy.ui

import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.radiooracle.oracletoy.camera.CameraController
import com.radiooracle.oracletoy.toy.ToyController

@Composable
fun MainScreen(vm: OracleViewModel, camera: CameraController) {
    val ui by vm.ui.collectAsState()
    val toy by vm.toyState.collectAsState()
    val lifecycleOwner = LocalLifecycleOwner.current

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Oracle Toy", style = MaterialTheme.typography.headlineSmall)

        // Live camera preview.
        AndroidView(
            factory = { ctx ->
                PreviewView(ctx).also { camera.bind(it, lifecycleOwner) }
            },
            modifier = Modifier.fillMaxWidth().height(280.dp),
        )

        // Consent gate — required before any toy action.
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = ui.consentGiven, onCheckedChange = vm::setConsent)
            Text("Confermo: entrambi i partner acconsentono a questo controllo.")
        }

        Text("Toy: ${toy.connection} ${toy.deviceName ?: ""}  " +
            (toy.batteryPercent?.let { "batteria $it%" } ?: ""))

        Text("Intensità massima: ${ui.intensityCap} / ${ToyController.MAX_INTENSITY}")
        Slider(
            value = ui.intensityCap.toFloat(),
            onValueChange = { vm.setIntensityCap(it.toInt()) },
            valueRange = 0f..ToyController.MAX_INTENSITY.toFloat(),
            steps = ToyController.MAX_INTENSITY - 1,
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = vm::connectToy, enabled = !ui.busy) { Text("Connetti toy") }
            Button(
                onClick = vm::askOracle,
                enabled = ui.consentGiven && !ui.busy,
            ) { Text(if (ui.busy) "…" else "Chiedi all'oracolo") }
        }

        // Always-visible emergency STOP / safeword.
        Button(
            onClick = vm::stop,
            modifier = Modifier.fillMaxWidth().height(56.dp),
        ) { Text("STOP", color = Color.White) }

        ui.lastVerdict?.let {
            Text("Risposta: ${it.answer}  (corretto=${it.correct}, conf=${"%.2f".format(it.confidence)})")
        }
        ui.message?.let { Text(it, color = MaterialTheme.colorScheme.error) }

        Spacer(Modifier.height(4.dp))
    }
}
