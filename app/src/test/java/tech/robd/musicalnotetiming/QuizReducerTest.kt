package tech.robd.musicalnotetiming

import org.junit.Assert.*
import org.junit.Test

class QuizReducerTest {

    private fun notesFixture(): List<Note> {
        // Use dummy imageResId (unit tests don’t need Android resources)
        return listOf(
            Note(
                beats = 4,
                imageResId = 0,
                lessons = listOf(
                    Lesson(
                        questionText = "Pick 4",
                        options = listOf("1", "2", "4"),
                        correctAnswer = "4",
                        questionType = QuestionType.MultipleChoice
                    )
                )
            ),
            Note(
                beats = 2,
                imageResId = 0,
                lessons = listOf(
                    Lesson(
                        questionText = "Play 2 beats",
                        options = listOf("1", "2", "4"),
                        correctAnswer = "2",
                        questionType = QuestionType.PlayNote
                    )
                )
            )
        )
    }

    @Test
    fun multipleChoice_correct_incrementsScore_andLocks_andSetsOutcome() {
        val notes = notesFixture()
        var s = initialState()

        s = reduce(s, QuizEvent.ChooseOption("4"), notes)
        assertTrue(s.attempt is AttemptState.Choosing)

        s = reduce(s, QuizEvent.Submit, notes)

        assertEquals(1, s.score)
        assertEquals(1, s.maxScore)
        assertTrue(s.attempt is AttemptState.Locked)

        val outcome = requireNotNull(s.lastOutcome)
        assertTrue(outcome.isCorrect)
        assertEquals("4", outcome.correctAnswer)
        assertEquals("4", outcome.userAnswer)
        // For MC, beat fields should be null
        assertNull(outcome.expectedBeats)
        assertNull(outcome.playedBeats)
    }

    @Test
    fun multipleChoice_incorrect_doesNotIncrementScore_butIncrementsMaxScore() {
        val notes = notesFixture()
        var s = initialState()

        s = reduce(s, QuizEvent.ChooseOption("1"), notes)
        s = reduce(s, QuizEvent.Submit, notes)

        assertEquals(0, s.score)
        assertEquals(1, s.maxScore)

        val outcome = requireNotNull(s.lastOutcome)
        assertFalse(outcome.isCorrect)
        assertEquals("4", outcome.correctAnswer)
        assertEquals("1", outcome.userAnswer)
    }

    @Test
    fun locked_ignoresChoose_andSubmit_untilNext() {
        val notes = notesFixture()
        var s = initialState()

        s = reduce(s, QuizEvent.ChooseOption("4"), notes)
        s = reduce(s, QuizEvent.Submit, notes)
        assertTrue(s.attempt is AttemptState.Locked)
        assertEquals(1, s.score)
        assertEquals(1, s.maxScore)

        // Try to change answer while locked
        val s2 = reduce(s, QuizEvent.ChooseOption("1"), notes)
        assertSame(s, s2)

        val s3 = reduce(s, QuizEvent.Submit, notes)
        assertSame(s, s3)

        val s4 = reduce(s, QuizEvent.Next, notes)
        assertNotEquals(s.pos, s4.pos)
        assertFalse(s4.attempt is AttemptState.Locked)
    }

    @Test
    fun playNote_correct_whenWithinTolerance() {
        val notes = notesFixture()
        // advance to noteIndex=1 questionIndex=0 (PlayNote)
        var s = initialState()
        s = reduce(s, QuizEvent.Next, notes)

        // Use deterministic time: start at 1000ms
        val startMs = 1_000L
        s = reduce(s, QuizEvent.StartPlay, notes, nowMs = { startMs })
        val playing = s.attempt as AttemptState.Playing
        assertEquals(startMs, playing.startedAtMs)

        // Expected beats=2 => expectedMs = 2 * MS_PER_BEAT
        val expectedMs = 2 * MusicConfig.MS_PER_BEAT
        val endMs = startMs + expectedMs  // perfectly correct
        s = reduce(s, QuizEvent.StopPlay(endMs), notes, nowMs = { startMs })
        s = reduce(s, QuizEvent.Submit, notes, nowMs = { startMs })

        assertEquals(1, s.score)
        assertEquals(1, s.maxScore)
        assertTrue(s.attempt is AttemptState.Locked)

        val outcome = requireNotNull(s.lastOutcome)
        assertTrue(outcome.isCorrect)
        assertEquals(2, outcome.expectedBeats)
        assertNotNull(outcome.playedBeats)
        // 2 beats exactly (allow tiny FP wiggle)
        assertEquals(2.0, outcome.playedBeats!!, 1e-9)
    }

    @Test
    fun playNote_incorrect_whenOutsideTolerance() {
        val notes = notesFixture()
        var s = initialState()
        s = reduce(s, QuizEvent.Next, notes)

        val startMs = 2_000L
        s = reduce(s, QuizEvent.StartPlay, notes, nowMs = { startMs })

        val expectedMs = 2 * MusicConfig.MS_PER_BEAT
        val tooLong = MusicConfig.MS_TOLERANCE + 50
        val endMs = startMs + expectedMs + tooLong

        s = reduce(s, QuizEvent.StopPlay(endMs), notes, nowMs = { startMs })
        s = reduce(s, QuizEvent.Submit, notes, nowMs = { startMs })

        assertEquals(0, s.score)
        assertEquals(1, s.maxScore)

        val outcome = requireNotNull(s.lastOutcome)
        assertFalse(outcome.isCorrect)
        assertEquals(2, outcome.expectedBeats)
        assertTrue(outcome.playedBeats!! > 2.0)
    }
}
