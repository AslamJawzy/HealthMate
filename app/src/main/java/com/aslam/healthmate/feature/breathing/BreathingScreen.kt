package com.aslam.healthmate.feature.breathing

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.aslam.healthmate.ui.theme.BreathCyan
import kotlinx.coroutines.delay

// Box breathing: inhale 4s, hold 4s, exhale 4s, hold 4s, then repeat
val PHASES = listOf("Breathe in", "Hold", "Breathe out", "Hold")
const val PHASE_SECONDS = 4

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BreathingScreen(onBack: () -> Unit) {
    var running by remember { mutableStateOf(false) }
    var phase by remember { mutableIntStateOf(0) }
    var secondsLeft by remember { mutableIntStateOf(PHASE_SECONDS) }
    var rounds by remember { mutableIntStateOf(0) }

    // The timer. Runs again from the start every time "running" changes.
    LaunchedEffect(running) {
        phase = 0
        secondsLeft = PHASE_SECONDS
        if (!running) return@LaunchedEffect
        rounds = 0
        while (true) {
            for (s in PHASE_SECONDS downTo 1) {
                secondsLeft = s
                delay(1000)
            }
            phase = (phase + 1) % 4
            if (phase == 0) rounds++
        }
    }

    // Circle grows while breathing in, shrinks while breathing out, stays still while holding
    val targetSize = if (!running) 0.5f else when (phase) {
        0, 1 -> 1f
        else -> 0.5f
    }
    val circleSize by animateFloatAsState(
        targetValue = targetSize,
        animationSpec = tween(durationMillis = PHASE_SECONDS * 1000, easing = LinearEasing),
        label = "breathing circle"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Breathing Exercise") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Box breathing helps you relax. Follow the circle.",
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(Modifier.height(24.dp))

            // The animated circle
            Box(Modifier.size(260.dp), contentAlignment = Alignment.Center) {
                Box(
                    Modifier
                        .size(260.dp * circleSize)
                        .background(BreathCyan.copy(alpha = 0.35f), CircleShape)
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        if (running) PHASES[phase] else "Ready",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    if (running) {
                        Text("$secondsLeft", style = MaterialTheme.typography.displaySmall, color = BreathCyan)
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // The 4 steps, current one highlighted
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                PHASES.forEachIndexed { i, name ->
                    Text(
                        name,
                        style = MaterialTheme.typography.labelMedium,
                        color = if (running && i == phase) BreathCyan else MaterialTheme.colorScheme.onSurface,
                        fontWeight = if (running && i == phase) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }

            Spacer(Modifier.height(16.dp))
            Text("Rounds completed: $rounds")
            Spacer(Modifier.height(24.dp))

            Button(
                onClick = { running = !running },
                colors = ButtonDefaults.buttonColors(containerColor = BreathCyan),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (running) "Stop" else "Start")
            }
        }
    }
}
