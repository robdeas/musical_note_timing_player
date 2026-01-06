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

object MusicData {
    val noteLengthNames = listOf("Whole Note", "Half Note", "Quarter Note")

    val noteBeats = listOf("4", "2", "1")
    val questionPlayText = "Play any note, of this length?"
    val notesWithQuestions = listOf(
        Note(
            beats = 4,
            imageResId = R.drawable.music_whole_note,
            lessons = listOf(
                        Lesson(questionPlayText, noteBeats, "4", QuestionType.PlayNote),
            )
        ),
        Note(
            beats = 2,
            imageResId = R.drawable.music_half_note,
            lessons = listOf(
                Lesson(questionPlayText, noteBeats, "2", QuestionType.PlayNote),
            )
        ),
        Note(
            beats = 1,
            imageResId = R.drawable.music_quarter_note,
            lessons = listOf(
                Lesson(questionPlayText, noteBeats, "1", QuestionType.PlayNote),
            )
        ),
    )
    // Add more notes with questions as needed
}
