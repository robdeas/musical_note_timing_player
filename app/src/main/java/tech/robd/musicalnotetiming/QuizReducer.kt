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


import android.os.SystemClock
import kotlin.math.abs

sealed class QuizEvent {
    data class ChooseOption(val option: String) : QuizEvent()
    data object StartPlay : QuizEvent()
    data class StopPlay(val endedAtMs: Long) : QuizEvent()
    data object Submit : QuizEvent()
    data object Next : QuizEvent()
    data object Reset : QuizEvent()
}

fun initialState(): QuizUiState =
    QuizUiState(
        pos = QuestionPos(noteIndex = 0, questionIndex = 0),
        score = 0,
        maxScore = 0,
        mood = Mood.Bored,
        attempt = AttemptState.Idle,
        lastOutcome = null
    )

fun reduce(
    state: QuizUiState,
    event: QuizEvent,
    notes: List<Note>,
    nowMs: () -> Long = { SystemClock.elapsedRealtime() }
): QuizUiState {
    val note = notes[state.pos.noteIndex]
    val lesson = note.lessons[state.pos.questionIndex]

    return when (event) {
        QuizEvent.Reset -> initialState()

        is QuizEvent.ChooseOption -> {
            if (state.attempt is AttemptState.Locked) state
            else state.copy(attempt = AttemptState.Choosing(selected = event.option))
        }

        QuizEvent.StartPlay -> {
            if (state.attempt is AttemptState.Locked) state
            else state.copy(attempt = AttemptState.Playing(startedAtMs = nowMs()))
        }

        is QuizEvent.StopPlay -> {
            val playing = state.attempt as? AttemptState.Playing ?: return state
            state.copy(attempt = playing.copy(endedAtMs = event.endedAtMs))
        }

        QuizEvent.Submit -> {
            if (state.attempt is AttemptState.Locked) return state

            val (isCorrect, userAnswer, expectedBeats, playedBeats) =
                when (lesson.questionType) {
                    QuestionType.MultipleChoice -> {
                        val selected = (state.attempt as? AttemptState.Choosing)?.selected
                        val ok = selected != null && selected == lesson.correctAnswer
                        Quad(ok, selected, null, null)
                    }

                    QuestionType.PlayNote -> {
                        val playing = state.attempt as? AttemptState.Playing
                            ?: return state // not started yet
                        val endedAt = playing.endedAtMs ?: return state // not stopped yet

                        val expected = note.beats
                        val actualMs = endedAt - playing.startedAtMs
                        val expectedMs = expected * MusicConfig.MS_PER_BEAT
                        val ok = abs(actualMs - expectedMs) <= MusicConfig.MS_TOLERANCE

                        // store beats for UI display
                        val beatsPlayed = actualMs.toDouble() / MusicConfig.MS_PER_BEAT.toDouble()

                        Quad(ok, null, expected, beatsPlayed)
                    }
                }

            val newMood = nextMood(state.mood, isCorrect)
            state.copy(
                score = state.score + if (isCorrect) 1 else 0,
                maxScore = state.maxScore + 1,
                mood = newMood,
                attempt = AttemptState.Locked,
                lastOutcome = LastOutcome(
                    isCorrect = isCorrect,
                    message = if (isCorrect) "Correct! Well done." else "Incorrect. Try again!",
                    correctAnswer = lesson.correctAnswer,
                    userAnswer = userAnswer,
                    expectedBeats = expectedBeats,
                    playedBeats = playedBeats
                )
            )
        }

        QuizEvent.Next -> {
            val nextPos = advancePos(state.pos, notes)

            val nextLesson = notes[nextPos.noteIndex].lessons[nextPos.questionIndex]
            val nextAttempt =
                when (nextLesson.questionType) {
                    QuestionType.MultipleChoice -> AttemptState.Choosing(selected = null)
                    QuestionType.PlayNote -> AttemptState.Idle
                }

            state.copy(
                pos = nextPos,
                attempt = nextAttempt
                // keep lastOutcome visible until next submit, or clear it here if you prefer
                // lastOutcome = null
            )
        }
    }
}

private fun advancePos(pos: QuestionPos, notes: List<Note>): QuestionPos {
    val currentNote = notes[pos.noteIndex]
    var q = pos.questionIndex + 1
    var n = pos.noteIndex

    if (q >= currentNote.lessons.size) {
        q = 0
        n++
        if (n >= notes.size) n = 0
    }
    return QuestionPos(noteIndex = n, questionIndex = q)
}

private fun nextMood(current: Mood, correct: Boolean): Mood {
    return if (correct) {
        when (current) {
            Mood.Happy, Mood.Delighted -> Mood.Delighted
            else -> Mood.Happy
        }
    } else {
        when (current) {
            Mood.Delighted, Mood.Happy, Mood.Bored -> Mood.Sad
            else -> Mood.Angry
        }
    }
}

// tiny helper to return 4 values without pulling in Pair/Triple nesting
private data class Quad<A, B, C, D>(val a: A, val b: B, val c: C, val d: D)
