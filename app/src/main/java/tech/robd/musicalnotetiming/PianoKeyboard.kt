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


import android.graphics.Paint
import android.graphics.Typeface
import android.os.SystemClock
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp

/**
 * PianoKeyboard is now a dumb input widget:
 * - Plays tone on press
 * - Stops tone on release
 * - Emits timing events
 *
 * Validation (correct/incorrect), retries, feedback text, and progression are handled outside (reducer + VM).
 */
@Composable
fun PianoKeyboard(
    modifier: Modifier = Modifier,
    musicPlayer: MusicPlayer,
    onStart: (startedAtMs: Long) -> Unit,
    onStop: (endedAtMs: Long, playedBeats: Double) -> Unit
) {
    var pressedKeyIndex by remember { mutableIntStateOf(-1) }       // white key index
    var pressedBlackKeyIndex by remember { mutableIntStateOf(-1) }  // black key index (unused for now)
    val numberOfWhiteKeys = 7
    val canvasHeight = 80.dp
    val blackKeyHeight = 10.dp

    val textPaint = remember {
        Paint().apply {
            color = android.graphics.Color.CYAN
            textSize = 40f
            typeface = Typeface.create(
                Typeface.DEFAULT,
                Typeface.BOLD
            )
        }
    }

    val canvasModifier = modifier
        .fillMaxWidth()
        .requiredHeight(canvasHeight)
        .pointerInput(Unit) {
            detectTapGestures(
                onPress = { offset ->
                    val pressStartTime = SystemClock.elapsedRealtime()
                    onStart(pressStartTime)

                    val whiteKeyWidth = size.width / numberOfWhiteKeys
                    val x = offset.x

                    // Determine which white key was pressed
                    pressedKeyIndex = (x / whiteKeyWidth).toInt().coerceIn(0, numberOfWhiteKeys - 1)

                    // (Optional) black keys currently not used. Keep index reset.
                    pressedBlackKeyIndex = -1

                    // Choose a note frequency based on the key index.
                    // Your noteFrequencies map has 7 notes (A3..G4) so this lines up with 7 white keys.
                    val notes = MusicPlayer.Companion.noteFrequencies.keys.toList()
                    val note = notes.getOrNull(pressedKeyIndex)
                    if (note != null) {
                        musicPlayer.startTone(note)
                    }

                    // Wait until the user releases the press
                    awaitRelease()

                    val pressEndTime = SystemClock.elapsedRealtime()
                    musicPlayer.stopTone()

                    val durationMs = pressEndTime - pressStartTime
                    val playedBeats = durationMs.toDouble() / MusicConfig.MS_PER_BEAT.toDouble()

                    // Reset highlight
                    pressedKeyIndex = -1
                    pressedBlackKeyIndex = -1

                    onStop(pressEndTime, playedBeats)
                }
            )
        }

    Canvas(modifier = canvasModifier) {
        val whiteKeyWidth = size.width / numberOfWhiteKeys
        val blackKeyWidth = whiteKeyWidth * 0.6f

        // White keys
        for (i in 0 until numberOfWhiteKeys) {
            val left = i * whiteKeyWidth
            val keyColor = if (i == pressedKeyIndex) Color.LightGray else Color.White

            // Fill
            drawRect(
                color = keyColor,
                topLeft = Offset(left, 0f),
                size = Size(whiteKeyWidth, size.height)
            )

            // Outline
            drawRect(
                color = Color.Gray,
                topLeft = Offset(left, 0f),
                size = Size(whiteKeyWidth, size.height),
                style = Stroke(width = 1.dp.toPx())
            )

            // Label middle C on 4th key (0-based index 3) as you did
            if (i == 3) {
                drawContext.canvas.nativeCanvas.drawText(
                    "C",
                    left + whiteKeyWidth / 2 - textPaint.measureText("C") / 2,
                    size.height - 20.dp.toPx(),
                    textPaint
                )
            }
        }

        // Black keys (visual only currently)
        for (i in 0 until numberOfWhiteKeys - 1) {
            if (i % 7 != 2 && i % 7 != 6) {
                val color = if (i == pressedBlackKeyIndex) Color.DarkGray else Color.Black
                drawRect(
                    color = color,
                    topLeft = Offset((i + 0.7f) * whiteKeyWidth, 0f),
                    size = Size(blackKeyWidth, blackKeyHeight.toPx())
                )
            }
        }
    }
}
