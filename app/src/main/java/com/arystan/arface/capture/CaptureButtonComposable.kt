package com.arystan.arface.capture

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import com.arystan.arface.ui.ARViewModel
import kotlinx.coroutines.delay

@Composable
fun CaptureButton(vm: ARViewModel) {
    val context = LocalContext.current
    val view = LocalView.current
    val inProgress by vm.captureInProgress.collectAsState()
    val lastResult by vm.lastCaptureResult.collectAsState()
    var toast by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(lastResult) {
        when (val r = lastResult) {
            is com.arystan.arface.capture.CaptureResult.Success -> toast = "Фото сохранено"
            is com.arystan.arface.capture.CaptureResult.Failure -> toast = "Не удалось сохранить"
            else -> {}
        }
        if (lastResult != null) {
            delay(1800)
            vm.consumeCaptureResult()
            toast = null
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 120.dp)
                .size(76.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.95f))
                .border(4.dp, Color.White, CircleShape)
                .clickable(enabled = !inProgress) { vm.capturePhoto(view) },
            contentAlignment = Alignment.Center,
        ) {
            if (inProgress) {
                CircularProgressIndicator(strokeWidth = 3.dp, color = Color.Black)
            } else {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .border(2.dp, Color(0xFF222222), CircleShape),
                )
            }
        }
        toast?.let {
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 56.dp)
                    .background(Color.Black.copy(alpha = 0.7f))
                    .padding(horizontal = 16.dp, vertical = 8.dp),
            ) {
                Text(text = it, color = Color.White)
            }
        }
    }
}
