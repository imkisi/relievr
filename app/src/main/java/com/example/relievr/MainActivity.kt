package com.example.relievr

import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.relievr.ui.theme.RelievrTheme
import kotlin.math.roundToInt

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RelievrTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Column(
                        modifier = Modifier
                            .padding(innerPadding)
                            .fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Spacer(modifier = Modifier.height(32.dp))
                        VibrationController(modifier = Modifier.fillMaxWidth(0.8f)) // Constrain width
                    }
                }
            }
        }
    }
}

@Composable
fun VibrationController(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val vibrator = context.getSystemService(Vibrator::class.java)

    var isVibrationOn by remember { mutableStateOf(false) }
    var vibrationIntensity by remember { mutableFloatStateOf(0.5f) } // 0.0f to 1.0f

    val canControlAmplitude = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O

    // Effect to start/stop vibration based on isVibrationOn and intensity
    DisposableEffect(isVibrationOn, vibrationIntensity, canControlAmplitude) {
        if (isVibrationOn && vibrator != null) {
            if (canControlAmplitude) {
                // Repeating waveform for continuous vibration with intensity
                // Amplitude from 1 to 255.
                val amplitude = (vibrationIntensity * 255).roundToInt().coerceIn(1, 255)
                val timings = longArrayOf(0, 200, 0) // delay, vibrate, pause
                val amplitudes = intArrayOf(0, amplitude, 0)   // Off, On (with intensity), Off
                val vibrationEffect = VibrationEffect.createWaveform(timings, amplitudes, 0)
                vibrator.vibrate(vibrationEffect)
            } else {
                // For older APIs, continuous vibration without direct intensity control.
                @Suppress("DEPRECATION")
                vibrator.vibrate(longArrayOf(0, 400, 100), 0) // lenght, sleep and repeat from index 0
            }
        } else {
            vibrator?.cancel()
        }

        onDispose {
            vibrator?.cancel()
        }
    }

    Column(
        modifier = modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Vibration", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.weight(1f))
            IconButton(onClick = { isVibrationOn = !isVibrationOn }) {
                Icon(
                    painter = if (isVibrationOn) {
                        painterResource(id = R.drawable.sensors)
                    } else {
                        painterResource(id = R.drawable.sensors_off)
                    },
                    contentDescription = if (isVibrationOn) "Turn Vibration Off" else "Turn Vibration On"
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (canControlAmplitude) {
            Text(
                text = "Intensity: ${(vibrationIntensity * 100).roundToInt()}%",
                style = MaterialTheme.typography.bodyLarge
            )
        } else {
            Text(
                text = "Intensity: Not Supported (Defaults to Max)",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.error
            )
        }

        Slider(
            value = if (canControlAmplitude) vibrationIntensity else 1f, // If not supported, show slider at max
            onValueChange = { if (canControlAmplitude) vibrationIntensity = it },
            valueRange = 0f..1f,
            steps = if (canControlAmplitude) 9 else 0, // No steps if disabled
            enabled = canControlAmplitude, // Disable slider if amplitude control is not available
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun VibrationControllerPreview() {
    RelievrTheme {
        VibrationController()
    }
}