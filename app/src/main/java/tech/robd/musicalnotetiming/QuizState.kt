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

data class QuestionPos(
    val noteIndex: Int,
    val questionIndex: Int
)

sealed class AttemptState {
    data object Idle : AttemptState()
    data class Choosing(val selected: String? = null) : AttemptState()
    data class Playing(val startedAtMs: Long, val endedAtMs: Long? = null) : AttemptState()
    data object Locked : AttemptState()
}

data class LastOutcome(
    val isCorrect: Boolean,
    val message: String,
    val correctAnswer: String,
    val userAnswer: String?,
    val expectedBeats: Int? = null,
    val playedBeats: Double? = null
)

data class QuizUiState(
    val pos: QuestionPos,
    val score: Int,
    val maxScore: Int,
    val mood: Mood,
    val attempt: AttemptState,
    val lastOutcome: LastOutcome? = null
)
