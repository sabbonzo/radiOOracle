package com.radiooracle.oracletoy.toy

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.util.Log
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import java.util.UUID

/**
 * Drives a Lovense toy over Bluetooth LE using the documented ASCII command
 * protocol: commands are written to a GATT characteristic terminated with ';'.
 * Examples: "Vibrate:10;", "Battery;", "DeviceType;".
 *
 * The service/characteristic UUIDs below are the commonly documented Lovense
 * pair. If a specific model differs, confirm the write characteristic UUID with
 * a GATT scan (e.g. nRF Connect) against YOUR device and update [WRITE_CHAR_UUID].
 * That confirmation step on your own device is the only real "reverse engineering"
 * here — the command grammar itself is public.
 *
 * Caller must hold BLUETOOTH_SCAN / BLUETOOTH_CONNECT (API 31+) before calling.
 */
@SuppressLint("MissingPermission")
class LovenseBleController(context: Context) : ToyController {

    private val appContext = context.applicationContext
    private val adapter: BluetoothAdapter? =
        (appContext.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager)?.adapter

    private val _state = MutableStateFlow(ToyState())
    override val state: StateFlow<ToyState> = _state

    private var gatt: BluetoothGatt? = null
    private var writeChar: BluetoothGattCharacteristic? = null

    override suspend fun connect() {
        val scanner = adapter?.bluetoothLeScanner ?: run {
            _state.update { it.copy(lastError = "Bluetooth non disponibile") }
            return
        }
        _state.update { it.copy(connection = ToyState.Connection.SCANNING, lastError = null) }

        val device = scanForLovense(scanner)
        if (device == null) {
            _state.update {
                it.copy(connection = ToyState.Connection.DISCONNECTED, lastError = "Nessun toy trovato")
            }
            return
        }

        _state.update {
            it.copy(connection = ToyState.Connection.CONNECTING, deviceName = device.name)
        }
        gatt = device.connectGatt(appContext, false, gattCallback, BluetoothDevice.TRANSPORT_LE)

        // Wait (bounded) for service discovery to populate writeChar.
        var waited = 0L
        while (writeChar == null && waited < CONNECT_TIMEOUT_MS) {
            delay(100)
            waited += 100
        }
        if (writeChar == null) {
            _state.update {
                it.copy(connection = ToyState.Connection.DISCONNECTED, lastError = "Caratteristica non trovata")
            }
        }
    }

    private suspend fun scanForLovense(
        scanner: android.bluetooth.le.BluetoothLeScanner,
    ): BluetoothDevice? {
        var found: BluetoothDevice? = null
        val cb = object : ScanCallback() {
            override fun onScanResult(callbackType: Int, result: ScanResult) {
                val name = result.device.name ?: return
                if (name.startsWith("LVS-") || name.contains("Lovense", ignoreCase = true)) {
                    found = result.device
                }
            }
        }
        scanner.startScan(cb)
        var waited = 0L
        while (found == null && waited < SCAN_TIMEOUT_MS) {
            delay(100)
            waited += 100
        }
        scanner.stopScan(cb)
        return found
    }

    private val gattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(g: BluetoothGatt, status: Int, newState: Int) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                g.discoverServices()
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                writeChar = null
                _state.update { it.copy(connection = ToyState.Connection.DISCONNECTED) }
            }
        }

        override fun onServicesDiscovered(g: BluetoothGatt, status: Int) {
            val char = g.getService(SERVICE_UUID)?.getCharacteristic(WRITE_CHAR_UUID)
                ?: findWritableCharacteristic(g)
            if (char != null) {
                writeChar = char
                _state.update { it.copy(connection = ToyState.Connection.CONNECTED) }
            } else {
                _state.update { it.copy(lastError = "Nessuna caratteristica scrivibile") }
            }
        }
    }

    private fun findWritableCharacteristic(g: BluetoothGatt): BluetoothGattCharacteristic? {
        for (service in g.services) {
            for (c in service.characteristics) {
                val props = c.properties
                val writable = props and BluetoothGattCharacteristic.PROPERTY_WRITE != 0 ||
                    props and BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE != 0
                if (writable) return c
            }
        }
        return null
    }

    private fun writeCommand(command: String) {
        val g = gatt ?: return
        val char = writeChar ?: return
        val bytes = command.toByteArray(Charsets.US_ASCII)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            g.writeCharacteristic(char, bytes, BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE)
        } else {
            @Suppress("DEPRECATION")
            char.value = bytes
            @Suppress("DEPRECATION")
            g.writeCharacteristic(char)
        }
        Log.d(TAG, "-> $command")
    }

    override suspend fun vibrate(level: Int) {
        val clamped = level.coerceIn(0, ToyController.MAX_INTENSITY)
        writeCommand("Vibrate:$clamped;")
        _state.update { it.copy(currentLevel = clamped) }
    }

    override suspend fun pattern(steps: List<PatternStep>) {
        for (step in steps) {
            vibrate(step.level)
            delay(step.durationMs.coerceAtMost(ToyController.MAX_CONTINUOUS_MS))
        }
        stop()
    }

    override suspend fun stop() {
        writeCommand("Vibrate:0;")
        _state.update { it.copy(currentLevel = 0) }
    }

    override suspend fun battery(): Int? {
        // The reply arrives via a notification characteristic that varies by model;
        // returning the last cached value keeps the interface simple for the skeleton.
        writeCommand("Battery;")
        return _state.value.batteryPercent
    }

    override fun disconnect() {
        try {
            gatt?.disconnect()
            gatt?.close()
        } finally {
            gatt = null
            writeChar = null
            _state.update { ToyState() }
        }
    }

    private companion object {
        const val TAG = "LovenseBleController"
        const val SCAN_TIMEOUT_MS = 8_000L
        const val CONNECT_TIMEOUT_MS = 8_000L

        // Commonly documented Lovense GATT UUIDs. Confirm against your own device
        // if connection succeeds but writes have no effect.
        val SERVICE_UUID: UUID = UUID.fromString("0000fff0-0000-1000-8000-00805f9b34fb")
        val WRITE_CHAR_UUID: UUID = UUID.fromString("0000fff2-0000-1000-8000-00805f9b34fb")
    }
}
