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


object MusicConfig {
    // Beats per minute - controls the tempo for the entire app
    // DEFAULT_DURATION in MusicPlayer is based on this value
    // At 120 BPM: quarter note = 0.5 seconds
    // At 60 BPM: quarter note = 1.0 second
    const val BPM = 120

    // Calculate milliseconds per beat (quarter note)
    val MS_PER_BEAT: Long
        get() = (60000L / BPM)

    // Calculate seconds per beat for MusicPlayer
    val SECONDS_PER_BEAT: Double
        get() = 60.0 / BPM

    // Tolerance for accepting answers (in beats)
    // e.g., 0.5 means within half a beat, 0.3 means within 3/10ths of a beat
    const val BEAT_TOLERANCE = 0.5

    // Calculate tolerance in milliseconds
    val MS_TOLERANCE: Long
        get() = (BEAT_TOLERANCE * MS_PER_BEAT).toLong()
}
