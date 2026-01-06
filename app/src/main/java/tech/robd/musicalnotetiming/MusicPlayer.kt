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


import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.experimental.and
import kotlin.math.PI
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlinx.coroutines.*

class MusicPlayer(private val scope: CoroutineScope) {

    companion object {
        private const val SAMPLE_RATE = 44100 // Hz
        private const val MAX_16_BIT = 32767
        private val DEFAULT_DURATION = MusicConfig.SECONDS_PER_BEAT

        val noteFrequencies = mapOf(
            MusicalNoteFrequency.A3 to 220.00,
            MusicalNoteFrequency.B3 to 246.94,
            MusicalNoteFrequency.C4 to 261.63, // Middle C
            MusicalNoteFrequency.D4 to 293.66,
            MusicalNoteFrequency.E4 to 329.63,
            MusicalNoteFrequency.F4 to 349.23,
            MusicalNoteFrequency.G4 to 392.00
        )
    }

    private var audioTrack: AudioTrack? = null

    private fun releaseResources() {
        audioTrack?.let {
            if (it.playState == AudioTrack.PLAYSTATE_PLAYING) {
                it.stop() // Stop playback
            }
            it.release() // Release the resources
            audioTrack = null // Help garbage collector by nullifying the reference
        }
    }

    private fun generateAudioTrack(frequency: Double, duration: Double): ByteArray? {
        try {
            val numSamples = (duration * SAMPLE_RATE).toInt()
            val samples = DoubleArray(numSamples) { i ->
                sin(2.0 * PI * i / (SAMPLE_RATE / frequency))
            }

            return ByteArray(2 * numSamples).apply {
                var idx = 0
                samples.forEach { sample ->
                    val value = (sample * MAX_16_BIT).toInt().toShort()
                    this[idx++] = (value and 0x00FF).toByte()
                    this[idx++] = (value.toInt() shr 8).toByte()
                }
            }
        } catch (e: Exception) {
            // Handle specific exceptions or log them as needed
            e.printStackTrace()
            return null
        }
    }

    private fun startTone(frequency: Double) {
        val duration =
            1.0 // seconds, for generating the buffer, but we will loop this for continuous play
        val numSamples = (duration * SAMPLE_RATE).toInt()
        val sample = DoubleArray(numSamples)
        val generatedSnd = ByteArray(2 * numSamples)

        // Generate the samples for one second
        for (i in sample.indices) {
            sample[i] = sin(2.0 * Math.PI * i.toDouble() / (SAMPLE_RATE / frequency))
        }

        // Convert to 16 bit PCM sound array
        var idx = 0
        for (dVal in sample) {
            val valShort = (dVal * 32767).roundToInt().toShort()
            generatedSnd[idx++] = (valShort and 0x00ff).toByte()
            generatedSnd[idx++] = ((valShort.toInt() and 0xff00).ushr(8)).toShort().toByte()
        }

        audioTrack = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(SAMPLE_RATE)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build()
            )
            .setBufferSizeInBytes(generatedSnd.size)
            .setTransferMode(AudioTrack.MODE_STATIC)
            .build()

        // Write the generated audio data to the AudioTrack object
        audioTrack?.write(generatedSnd, 0, generatedSnd.size)

        // Set loop points to loop from the start to the end of the buffer
        audioTrack?.setLoopPoints(0, numSamples / 2, -1) // Loop continuously

        // Start playback
        audioTrack?.play()
    }


//    private fun getFrequency(freq: MusicalNoteFrequency): Double {
//        return noteFrequencies[freq]!!
//    }

    private fun playTone(frequency: Double, beats: Int = 4): Job {

        return scope.launch(Dispatchers.Default) {
            val duration = DEFAULT_DURATION * beats
            val generatedSnd = generateAudioTrack(frequency, duration) ?: return@launch

            releaseAudioTrack()  // Ensure any previous AudioTrack is released
            // I feel this will be enough pause to show the user a new note has started
            delay(10)
            audioTrack = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build()
                )
                .setAudioFormat(
                    AudioFormat.Builder().setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(SAMPLE_RATE)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(generatedSnd.size)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build().also { track ->
                    track.write(generatedSnd, 0, generatedSnd.size)
                    track.play()
                }

            val totalFrames = (duration * SAMPLE_RATE).toInt()

            // Monitor playback completion
            try {
                while (isActive && audioTrack?.playState == AudioTrack.PLAYSTATE_PLAYING) {
                    val currentPosition = audioTrack?.playbackHeadPosition ?: break
                    if (currentPosition >= totalFrames) break  // Playback has completed
                    delay(100)  // Check every 100 milliseconds
                }
            } finally {
                withContext(NonCancellable) {
                    releaseAudioTrack() // Ensure AudioTrack is released even if the Job is cancelled
                }
            }
        }
    }

    private fun releaseAudioTrack() {
        scope.launch(Dispatchers.Default) {
            try {
                audioTrack?.apply {
                    stop()
                    release()
                }
            } catch (e: Exception) {
                println("Error releasing audiotrack ${e.message}")
                //  e.printStackTrace()
            } finally {
                audioTrack = null
            }
        }
    }

//    fun playRandomNote(beats: Int = 4): Job {
//        return playNote(beats, MusicalNoteFrequency.entries.toTypedArray().random())
//    }

    fun playNote(beats: Int = 4, noteFrequency: MusicalNoteFrequency): Job {
        println("* play music note ")
        return playTone(noteFrequencies[noteFrequency]!!, beats)
    }

    fun startTone(noteFrequency: MusicalNoteFrequency) {
        println("* start music note ")
        startTone(noteFrequencies[noteFrequency]!!)
    }

    fun stopTone() {
        releaseResources()
    }

}


enum class MusicalNoteFrequency {
    A3, B3, C4, D4, E4, F4, G4
}