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


import androidx.compose.runtime.Composable



@Composable
fun moodImage(moodRandomId: Int, mood: Mood): Int {
    val imagesMap = mapOf(
        Mood.Happy to listOf(
            R.drawable.happy1,
            R.drawable.happy2,
            R.drawable.happy3,
            R.drawable.happy4,
            R.drawable.happy5
        ),
        Mood.Delighted to listOf(
            R.drawable.dancing1,
            R.drawable.dancing2,
            R.drawable.dancing3,
            R.drawable.dancing4,
            R.drawable.dancing5
        ),
        Mood.Sad to listOf(
            R.drawable.sad1,
            R.drawable.sad2,
            R.drawable.sad3,
            R.drawable.sad4,
            R.drawable.sad5
        ),
        Mood.Angry to listOf(
            R.drawable.angry1,
            R.drawable.angry2,
            R.drawable.angry3,
            R.drawable.angry4,
            R.drawable.angry5
        )
    )

    // Default image if mood is not recognized
    val defaultImage = R.drawable.search1

    // Retrieve the list of images for the given mood or return a list with just the default image
    val moodImages = imagesMap[mood] ?: listOf(defaultImage)

    // Ensure moodRandomId is within the index bounds of the list, defaulting to the last item if out of bounds
    val index = (moodRandomId - 1).coerceIn(0, moodImages.size - 1)

    return moodImages[index]
}