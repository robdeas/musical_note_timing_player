package tech.robd.musicalnotetiming
/*
 * Music Timing Intro - A rhythm training application
 * Copyright (C) 2025 Rob Deas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */


import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun Metronome(
    modifier: Modifier = Modifier,
    bpm: Int = MusicConfig.BPM,
    isPlaying: Boolean = true,
) {
    // Calculate duration for one full swing (back and forth) based on BPM
    val swingDuration = (60000 / bpm) // milliseconds for one beat

    val infiniteTransition = rememberInfiniteTransition(label = "metronome")

    val angle by infiniteTransition.animateFloat(
        initialValue = -45f,
        targetValue = 45f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = swingDuration,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pendulum_angle"
    )

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val centerX = size.width / 2
            val baseY = size.height * 0.2f // Top anchor point
            val pendulumLength = size.height * 0.6f

            // Convert angle to radians
            val angleRad = if (isPlaying) {
                Math.toRadians(angle.toDouble())
            } else {
                0.0 // Straight down when stopped
            }

            // Calculate pendulum end point
            val endX = centerX + (pendulumLength * sin(angleRad)).toFloat()
            val endY = baseY + (pendulumLength * cos(angleRad)).toFloat()

            // Draw the pivot point
            drawCircle(
                color = Color.DarkGray,
                radius = 20f,
                center = Offset(centerX, baseY)
            )

            // Draw the pendulum rod
            drawLine(
                color = Color.Black,
                start = Offset(centerX, baseY),
                end = Offset(endX, endY),
                strokeWidth = 8f,
                cap = StrokeCap.Round
            )

            // Draw the pendulum bob (weight at the end)
            drawCircle(
                color = Color.Red,
                radius = 20f,
                center = Offset(endX, endY)
            )

            // Draw a subtle arc showing the swing path
            drawArc(
                color = Color.LightGray.copy(alpha = 0.3f),
                startAngle = 45f,
                sweepAngle = 90f,
                useCenter = false,
                topLeft = Offset(centerX - pendulumLength, baseY - pendulumLength),
                size = Size(pendulumLength * 2, pendulumLength * 2),
                style = Stroke(width = 2f)
            )
        }
    }
}

@Composable
fun MetronomeWithControls(
    bpm: Int = 60,
    isPlaying: Boolean = true,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Metronome(
            bpm = bpm,
            isPlaying = isPlaying,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        )
    }
}