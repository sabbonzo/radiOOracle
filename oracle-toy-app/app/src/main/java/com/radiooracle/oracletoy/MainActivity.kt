package com.radiooracle.oracletoy

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.viewmodel.compose.viewModel
import com.radiooracle.oracletoy.ai.OracleSolver
import com.radiooracle.oracletoy.camera.CameraController
import com.radiooracle.oracletoy.toy.LovenseBleController
import com.radiooracle.oracletoy.toy.MockToyController
import com.radiooracle.oracletoy.toy.ToyController
import com.radiooracle.oracletoy.ui.MainScreen
import com.radiooracle.oracletoy.ui.OracleViewModel

class MainActivity : ComponentActivity() {

    // Flip to true to run without hardware (logs commands instead of driving BLE).
    private val useMockToy = false

    private val permissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { /* result surfaced via UI actions */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestNeededPermissions()

        val camera = CameraController(this)
        val toy: ToyController =
            if (useMockToy) MockToyController() else LovenseBleController(this)
        val solver = OracleSolver()

        setContent {
            val vm: OracleViewModel = viewModel(
                factory = object : androidx.lifecycle.ViewModelProvider.Factory {
                    @Suppress("UNCHECKED_CAST")
                    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T =
                        OracleViewModel(camera, toy, solver) as T
                },
            )
            MainScreen(vm, camera)
        }
    }

    private fun requestNeededPermissions() {
        val perms = mutableListOf(Manifest.permission.CAMERA)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            perms += Manifest.permission.BLUETOOTH_SCAN
            perms += Manifest.permission.BLUETOOTH_CONNECT
        } else {
            perms += Manifest.permission.ACCESS_FINE_LOCATION
        }
        permissionLauncher.launch(perms.toTypedArray())
    }
}
