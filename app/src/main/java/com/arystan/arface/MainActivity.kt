package com.arystan.arface

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.arystan.arface.ui.ARFaceScreen
import com.arystan.arface.ui.ARViewModel
import com.arystan.arface.ui.ARViewModelFactory
import com.arystan.arface.ui.theme.ARFaceTheme

class MainActivity : ComponentActivity() {

    private val viewModel: ARViewModel by viewModels {
        ARViewModelFactory(applicationContext)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ARFaceTheme {
                PermissionGate {
                    ARFaceScreen(viewModel)
                }
            }
        }
    }

    @Composable
    private fun PermissionGate(content: @Composable () -> Unit) {
        var granted by remember {
            mutableStateOf(
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.CAMERA,
                ) == PackageManager.PERMISSION_GRANTED,
            )
        }
        var denied by remember { mutableStateOf(false) }

        val launcher = androidx.activity.compose.rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission(),
        ) { ok ->
            granted = ok
            denied = !ok
        }

        androidx.compose.runtime.LaunchedEffect(Unit) {
            if (!granted) launcher.launch(Manifest.permission.CAMERA)
        }

        when {
            granted -> content()
            denied -> Box(
                modifier = Modifier.fillMaxSize().padding(32.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(getString(R.string.permission_denied))
            }
            else -> Box(modifier = Modifier.fillMaxSize())
        }
    }
}
