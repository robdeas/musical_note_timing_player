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

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import tech.robd.musicalnotetiming.ui.AboutDialog
import tech.robd.musicalnotetiming.ui.MainTopBar
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizScreen(viewModel: LessonsViewModel = viewModel()) {

    // Single source of truth for the whole screen
    val ui by viewModel.state.collectAsStateWithLifecycle()

    val notes = MusicData.notesWithQuestions
    val note = notes[ui.pos.noteIndex]
    val lesson = note.lessons[ui.pos.questionIndex]

    // Mood image randomness is UI-only "flair", so it can remain local
    var moodRandomId by rememberSaveable { mutableIntStateOf(1) }
    val moodImageResId = moodImage(moodRandomId, ui.mood)

    // Audio player: keep for now (later can move it to ViewModel side-effects)
    val musicPlayer = remember { MusicPlayer(CoroutineScope(Dispatchers.IO + Job())) }

    // About dialog state
    var showAboutDialog by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        topBar = {
            MainTopBar(
                title = "Music Timing Intro",
                onAbout = { showAboutDialog = true }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Note image for the current lesson group
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = note.imageResId),
                    contentDescription = "Note Image",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
            }

            Spacer(Modifier.height(16.dp))

            Text(
                lesson.questionText,
                style = MaterialTheme.typography.headlineMedium,
                maxLines = 2,
                minLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(Modifier.height(8.dp))

            when (lesson.questionType) {
                QuestionType.PlayNote -> {
                    PianoKeyboard(
                        musicPlayer = musicPlayer,
                        onStart = {
                            viewModel.send(QuizEvent.StartPlay)
                        },
                        onStop = { endedAtMs, _ ->
                            viewModel.send(QuizEvent.StopPlay(endedAtMs))
                            viewModel.send(QuizEvent.Submit)
                            viewModel.send(QuizEvent.Next)
                        }
                    )

                    Spacer(Modifier.height(8.dp))
                }

                QuestionType.MultipleChoice -> {
                    lesson.options.forEach { option ->
                        Button(
                            enabled = ui.attempt !is AttemptState.Locked,
                            onClick = {
                                moodRandomId = randomMood(moodRandomId)
                                viewModel.send(QuizEvent.ChooseOption(option))
                                viewModel.send(QuizEvent.Submit)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Text(option)
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    Button(
                        enabled = ui.attempt is AttemptState.Locked,
                        onClick = { viewModel.send(QuizEvent.Next) }
                    ) {
                        Text("Next")
                    }
                }
            }

            // Previous-outcome banner
            ui.lastOutcome?.let { outcome ->
                Spacer(Modifier.height(8.dp))
                Text(
                    outcome.message,
                    color = if (outcome.isCorrect) Color.Green else Color.Red
                )

                if (outcome.expectedBeats != null && outcome.playedBeats != null) {
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "Expected: ${outcome.expectedBeats} beats • Played: %.1f beats".format(outcome.playedBeats),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            // Mood image + metronome
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.5f),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = moodImageResId),
                    contentDescription = "Mood Image",
                    modifier = Modifier
                        .weight(1f)
                        .padding(8.dp)
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Metronome(
                        modifier = Modifier
                            .padding(8.dp)
                            .size(180.dp),
                        isPlaying = true
                    )
                }
            }

            val reactionSeed = ui.maxScore
            val encouragingMessage = moodPhrase(ui.mood, reactionSeed)

            if (encouragingMessage.isNotEmpty()) {
                Text(
                    encouragingMessage,
                    style = MaterialTheme.typography.headlineSmall,
                    color = when (ui.mood) {
                        Mood.Delighted, Mood.Happy -> Color(0xFF4CAF50)
                        Mood.Sad -> Color(0xFFFF9800)
                        Mood.Angry -> Color(0xFFF44336)
                        else -> Color.Gray
                    },
                    modifier = Modifier.padding(vertical = 2.dp)
                )
            }

            Spacer(Modifier.height(8.dp))

            // Score
            val percentage = calculatePercentage(ui.score, ui.maxScore)
            Text(
                "Score: ${ui.score} / ${ui.maxScore}  (${percentage}%)",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }

        // About Dialog
        if (showAboutDialog) {
            AboutDialog(onDismiss = { showAboutDialog = false })
        }
    }
}

fun moodPhrase(mood: Mood, seed: Int): String {
    val lines = when (mood) {
        Mood.Delighted -> listOf(
            "Yes! That felt great.",
            "Spot on – lovely timing.",
            "Perfect. Keep that feel."
        )
        Mood.Happy -> listOf(
            "Nice – keep that groove.",
            "Good! Same again.",
            "Solid. Next one."
        )
        Mood.Sad -> listOf(
            "Close – hold it just the right amount.",
            "Nearly. Try matching the swing.",
            "Good effort – adjust the length."
        )
        Mood.Angry -> listOf(
            "Reset and go again.",
            "Focus on the beat.",
            "You can do it – Try just a little harder."
        )
        Mood.Bored -> emptyList()
    }

    if (lines.isEmpty()) return ""
    return lines[seed % lines.size]
}

fun randomMood(previousMoodId: Int): Int {
    val availableMoods = (1..5).toMutableList()
    availableMoods.remove(previousMoodId)
    return availableMoods.random()
}

@Composable
private fun calculatePercentage(score: Int, maxScore: Int) =
    if (maxScore == 0) 0 else ((score * 100.0) / maxScore).roundToInt()