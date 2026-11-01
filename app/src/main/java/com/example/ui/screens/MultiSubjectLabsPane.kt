package com.example.ui.screens

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.sin
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

import com.example.data.curriculum.LanguageCurriculum

@Composable
fun MultiSubjectLabsPane(
    initialSubjectIndex: Int = 0,
    studentClassHint: String? = null,
    onEarnSubjectXp: (subjectName: String, earnedXp: Int, earnedGems: Int) -> Unit,
    onOpenMathFiftyLevels: () -> Unit = {}
) {
    var selectedSubject by rememberSaveable { mutableIntStateOf(initialSubjectIndex.coerceIn(0, 3)) }
    LaunchedEffect(initialSubjectIndex) {
        selectedSubject = initialSubjectIndex.coerceIn(0, 3)
    }

    val subjects = listOf(
        "🧮 Maths Lab",
        "🎵 Music & Piano",
        "♟️ Chess Tactics",
        "🏫 School Subjects"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("multi_subject_labs_pane")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            subjects.forEachIndexed { idx, label ->
                FilterChip(
                    selected = selectedSubject == idx,
                    onClick = { selectedSubject = idx },
                    label = { Text(label, fontWeight = FontWeight.SemiBold) },
                    modifier = Modifier
                        .heightIn(min = 48.dp)
                        .testTag("subject_lab_chip_$idx")
                )
            }
        }

        when (selectedSubject) {
            0 -> MathsInteractiveLab(
                onEarnSubjectXp = onEarnSubjectXp,
                onOpenMathFiftyLevels = onOpenMathFiftyLevels
            )
            1 -> MusicSynthAndEarLab(onEarnSubjectXp = onEarnSubjectXp)
            2 -> ChessTacticsLab(onEarnSubjectXp = onEarnSubjectXp)
            3 -> SchoolSubjectsLab(
                studentClassHint = studentClassHint,
                onEarnSubjectXp = onEarnSubjectXp
            )
        }
    }
}

private data class MathChallenge(
    val category: String,
    val question: String,
    val visualFractionNumerator: Int? = null,
    val visualFractionDenominator: Int? = null,
    val options: List<String>,
    val correctAnswer: String,
    val explanation: String
)

@Composable
private fun MathsInteractiveLab(
    onEarnSubjectXp: (String, Int, Int) -> Unit,
    onOpenMathFiftyLevels: () -> Unit = {}
) {
    val challenges = remember {
        listOf(
            MathChallenge(
                category = "Visual Fractions",
                question = "Which fraction is equivalent to 3/4 shown on the bar below?",
                visualFractionNumerator = 3,
                visualFractionDenominator = 4,
                options = listOf("6/8", "4/6", "5/8", "2/4"),
                correctAnswer = "6/8",
                explanation = "Multiplying both numerator and denominator of 3/4 by 2 gives 6/8 (75%)."
            ),
            MathChallenge(
                category = "Algebra Sprint",
                question = "Solve for x:   3x + 7 = 25",
                options = listOf("x = 4", "x = 6", "x = 8", "x = 9"),
                correctAnswer = "x = 6",
                explanation = "Subtract 7 from both sides: 3x = 18. Divide by 3: x = 6!"
            ),
            MathChallenge(
                category = "Mental Percentages",
                question = "What is 15% of 240?",
                options = listOf("24", "32", "36", "42"),
                correctAnswer = "36",
                explanation = "10% of 240 is 24, and 5% is 12. Add them together: 24 + 12 = 36!"
            )
        )
    }

    var index by rememberSaveable { mutableIntStateOf(0) }
    var picked by remember(index) { mutableStateOf<String?>(null) }
    var rewarded by remember(index) { mutableStateOf(false) }
    val current = challenges[index]

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "➗ Full Math Mastery Path (Levels 1–50 + Noxra AI)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = "Select Class (1–12) • Pick Basic, Medium, or Hard • Practice Levels 1, 2, 3 … 50 with live Noxra AI step-by-step hints & bonus questions!",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Button(
                        onClick = onOpenMathFiftyLevels,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp)
                            .testTag("open_math_50_levels_from_lab_button"),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text("🚀 Open Math Levels 1–50 + Noxra AI", fontWeight = FontWeight.ExtraBold)
                    }
                }
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "🧮 Interactive Maths Challenge • ${current.category} (${index + 1}/${challenges.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold
                    )
                    LinearProgressIndicator(
                        progress = { (index + 1f) / challenges.size.toFloat() },
                        modifier = Modifier.fillMaxWidth().height(8.dp)
                    )
                    Text(current.question, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)

                    if (current.visualFractionNumerator != null && current.visualFractionDenominator != null) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            repeat(current.visualFractionDenominator) { block ->
                                val filled = block < current.visualFractionNumerator
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(28.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(
                                            if (filled) MaterialTheme.colorScheme.primary
                                            else MaterialTheme.colorScheme.surface
                                        )
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                current.options.forEachIndexed { optIdx, option ->
                    val isSelected = picked == option
                    OutlinedButton(
                        onClick = {
                            picked = option
                            if (option == current.correctAnswer && !rewarded) {
                                rewarded = true
                                onEarnSubjectXp("Maths", 20, 10)
                            }
                        },
                        border = BorderStroke(
                            width = if (isSelected) 2.5.dp else 1.dp,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 52.dp)
                            .testTag("math_option_$optIdx")
                    ) {
                        Text(option, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        if (picked != null) {
            item {
                val ok = picked == current.correctAnswer
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (ok) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = if (ok) "✅ Brilliant! +20 XP & +10 Gems" else "💡 Try again! Target: ${current.correctAnswer}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(current.explanation, style = MaterialTheme.typography.bodyMedium)
                        Button(
                            onClick = { index = (index + 1) % challenges.size },
                            modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp).testTag("next_math_puzzle_button")
                        ) {
                            Text("Next Maths Puzzle")
                        }
                    }
                }
            }
        }
    }
}

private data class PianoNote(val label: String, val solfege: String, val freqHz: Double)

private fun playSynthTone(freqHz: Double, durationMs: Int = 360) {
    val sampleRate = 22050
    val numSamples = (durationMs * sampleRate) / 1000
    val pcm = ShortArray(numSamples)
    val fadeSamples = (numSamples * 0.15).toInt().coerceAtLeast(1)
    for (i in 0 until numSamples) {
        val env = when {
            i < fadeSamples -> i.toDouble() / fadeSamples
            i > numSamples - fadeSamples -> (numSamples - i).toDouble() / fadeSamples
            else -> 1.0
        }
        val sample = sin(2.0 * PI * i * freqHz / sampleRate) * 0.65 * env
        pcm[i] = (sample * Short.MAX_VALUE).toInt().toShort()
    }
    try {
        val track = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(sampleRate)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build()
            )
            .setBufferSizeInBytes(pcm.size * 2)
            .setTransferMode(AudioTrack.MODE_STATIC)
            .build()
        track.write(pcm, 0, pcm.size)
        track.play()
        Thread.sleep(durationMs.toLong() + 40L)
        track.stop()
        track.release()
    } catch (_: Exception) {
        // Safe no-op on headless JVM unit tests without audio hardware
    }
}

@Composable
private fun MusicSynthAndEarLab(
    onEarnSubjectXp: (String, Int, Int) -> Unit
) {
    val scope = rememberCoroutineScope()
    val notes = remember {
        listOf(
            PianoNote("C4", "Do", 261.63),
            PianoNote("D4", "Re", 293.66),
            PianoNote("E4", "Mi", 329.63),
            PianoNote("F4", "Fa", 349.23),
            PianoNote("G4", "Sol", 392.00),
            PianoNote("A4", "La", 440.00),
            PianoNote("B4", "Ti", 493.88)
        )
    }

    var targetIndex by rememberSaveable { mutableIntStateOf(4) } // G4 Sol
    var lastPlayedNote by remember { mutableStateOf<String?>(null) }
    var feedback by remember { mutableStateOf<String?>(null) }
    var solvedCurrent by remember(targetIndex) { mutableStateOf(false) }

    val targetNote = notes[targetIndex]

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "🎵 Interactive Piano & Ear Training",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = "Tap any piano key to play real synthesized musical notes, or listen to the mystery pitch and match it on the keyboard!",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Button(
                            onClick = {
                                scope.launch(Dispatchers.IO) { playSynthTone(targetNote.freqHz, 450) }
                            },
                            modifier = Modifier.weight(1f).heightIn(min = 48.dp).testTag("play_mystery_note_button")
                        ) {
                            Text("🔊 Play Mystery Note")
                        }
                        OutlinedButton(
                            onClick = {
                                targetIndex = (targetIndex + 2) % notes.size
                                feedback = null
                            },
                            modifier = Modifier.heightIn(min = 48.dp).testTag("new_music_note_button")
                        ) {
                            Text("New Note")
                        }
                    }
                }
            }
        }

        // 7-Key Interactive Piano Keyboard
        item {
            Card(shape = RoundedCornerShape(22.dp)) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "🎹 Tap the key matching the mystery pitch (Hint: ${targetNote.solfege}):",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        notes.forEach { note ->
                            val isLast = lastPlayedNote == note.label
                            Surface(
                                onClick = {
                                    lastPlayedNote = note.label
                                    scope.launch(Dispatchers.IO) { playSynthTone(note.freqHz, 350) }
                                    if (note.label == targetNote.label) {
                                        feedback = "🎶 Pitch Perfect! That was ${note.label} (${note.solfege} • ${note.freqHz.toInt()} Hz)!"
                                        if (!solvedCurrent) {
                                            solvedCurrent = true
                                            onEarnSubjectXp("Music", 20, 10)
                                        }
                                    } else {
                                        feedback = "🎵 You played ${note.label} (${note.solfege}). Listen again for ${targetNote.solfege}!"
                                    }
                                },
                                shape = RoundedCornerShape(bottomStart = 14.dp, bottomEnd = 14.dp, topStart = 6.dp, topEnd = 6.dp),
                                border = BorderStroke(1.5.dp, if (isLast) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline),
                                color = if (isLast) MaterialTheme.colorScheme.primaryContainer else Color.White,
                                shadowElevation = 3.dp,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(144.dp)
                                    .testTag("piano_key_${note.label}")
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxSize().padding(vertical = 10.dp),
                                    verticalArrangement = Arrangement.Bottom,
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(note.solfege, style = MaterialTheme.typography.labelSmall, color = Color.DarkGray)
                                    Text(note.label, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.ExtraBold, color = Color.Black)
                                }
                            }
                        }
                    }

                    feedback?.let { msg ->
                        Text(
                            text = msg,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

private data class ChessPuzzle(
    val title: String,
    val goal: String,
    val pieces: Map<Pair<Int, Int>, String>, // (row 0..7 from rank 8..1, col 0..7 from a..h)
    val expectedFrom: Pair<Int, Int>,
    val expectedTo: Pair<Int, Int>,
    val coachingExplanation: String
)

@Composable
private fun ChessTacticsLab(
    onEarnSubjectXp: (String, Int, Int) -> Unit
) {
    val puzzles = remember {
        listOf(
            ChessPuzzle(
                title = "Puzzle 1: Back-Rank Checkmate in 1",
                goal = "White to move: Tap your White Rook (♖ on d1) and deliver checkmate on d8!",
                pieces = mapOf(
                    (0 to 6) to "♚", (1 to 5) to "♟", (1 to 6) to "♟", (1 to 7) to "♟",
                    (7 to 3) to "♖", (7 to 6) to "♔"
                ),
                expectedFrom = 7 to 3,
                expectedTo = 0 to 3,
                coachingExplanation = "Rd8#! Black's King on g8 is trapped behind its own pawns on f7, g7, and h7."
            ),
            ChessPuzzle(
                title = "Puzzle 2: Royal Knight Fork",
                goal = "White to move: Jump your Knight (♘ on d5) to fork Black's King and Rook!",
                pieces = mapOf(
                    (0 to 4) to "♚", (0 to 0) to "♜",
                    (3 to 3) to "♘", (7 to 4) to "♔"
                ),
                expectedFrom = 3 to 3,
                expectedTo = 1 to 2, // c7 forks e8 King & a8 Rook!
                coachingExplanation = "Nc7+! A classic Knight fork attacking both the King on e8 and the Rook on a8 simultaneously."
            )
        )
    }

    var puzzleIdx by rememberSaveable { mutableIntStateOf(0) }
    val puzzle = puzzles[puzzleIdx]
    var selectedSquare by remember(puzzleIdx) { mutableStateOf<Pair<Int, Int>?>(null) }
    var movedDestination by remember(puzzleIdx) { mutableStateOf<Pair<Int, Int>?>(null) }
    var statusText by remember(puzzleIdx) { mutableStateOf<String?>(null) }
    var solved by remember(puzzleIdx) { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "♟️ ${puzzle.title}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(puzzle.goal, style = MaterialTheme.typography.bodySmall)
                }
            }
        }

        // Interactive 8x8 Chess Board
        item {
            Card(shape = RoundedCornerShape(18.dp)) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    for (r in 0..7) {
                        Row(modifier = Modifier.fillMaxWidth()) {
                            for (c in 0..7) {
                                val coord = r to c
                                val piece = when {
                                    solved && coord == puzzle.expectedTo -> puzzle.pieces[puzzle.expectedFrom]
                                    solved && coord == puzzle.expectedFrom -> null
                                    movedDestination == coord -> puzzle.pieces[puzzle.expectedFrom]
                                    else -> puzzle.pieces[coord]
                                }
                                val isDark = (r + c) % 2 == 1
                                val isSelected = selectedSquare == coord
                                val bgColor = when {
                                    isSelected -> Color(0xFFFACC15)
                                    isDark -> Color(0xFF4B7399)
                                    else -> Color(0xFFEAE9D2)
                                }
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .aspectRatio(1f)
                                        .background(bgColor)
                                        .border(
                                            width = if (isSelected) 2.dp else 0.5.dp,
                                            color = if (isSelected) Color.Black else Color.Black.copy(alpha = 0.12f)
                                        )
                                        .clickable {
                                            if (solved) return@clickable
                                            val sel = selectedSquare
                                            if (sel == null) {
                                                if (puzzle.pieces.containsKey(coord)) {
                                                    selectedSquare = coord
                                                    statusText = "Selected ${puzzle.pieces[coord]} — now tap target square!"
                                                }
                                            } else if (sel == coord) {
                                                selectedSquare = null
                                            } else {
                                                if (sel == puzzle.expectedFrom && coord == puzzle.expectedTo) {
                                                    solved = true
                                                    movedDestination = coord
                                                    selectedSquare = null
                                                    statusText = "🏆 Checkmate / Tactical Win! ${puzzle.coachingExplanation}"
                                                    onEarnSubjectXp("Chess", 25, 12)
                                                } else {
                                                    selectedSquare = null
                                                    statusText = "💡 Not the strongest tactic—look closely at Black's King and try again!"
                                                }
                                            }
                                        }
                                        .testTag("chess_sq_${r}_${c}"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (piece != null) {
                                        Text(
                                            text = piece,
                                            style = MaterialTheme.typography.headlineSmall,
                                            color = Color.Black,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        statusText?.let { msg ->
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (solved) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(msg, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                        if (solved) {
                            Button(
                                onClick = { puzzleIdx = (puzzleIdx + 1) % puzzles.size },
                                modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp).testTag("next_chess_puzzle_button")
                            ) {
                                Text("Next Chess Tactic")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SchoolSubjectsLab(
    studentClassHint: String?,
    onEarnSubjectXp: (String, Int, Int) -> Unit
) {
    val allSubjectNames = listOf(
        "All",
        "Science",
        "Mathematics",
        "English Grammar",
        "Hindi",
        "Social Studies",
        "Computer Science"
    )
    val gradeBands = listOf("All Classes", "Class 6–8", "Class 9–10", "Class 11–12")
    val difficulties = listOf("All", "Easy", "Medium", "Hard")

    val defaultGrade = remember(studentClassHint) {
        val hint = studentClassHint.orEmpty()
        when {
            hint.contains("6") || hint.contains("7") || hint.contains("8") -> "Class 6–8"
            hint.contains("9") || hint.contains("10") -> "Class 9–10"
            hint.contains("11") || hint.contains("12") -> "Class 11–12"
            else -> "All Classes"
        }
    }

    var selectedSubject by rememberSaveable { mutableStateOf("All") }
    var selectedGrade by rememberSaveable { mutableStateOf(defaultGrade) }
    var selectedDifficulty by rememberSaveable { mutableStateOf("All") }

    val filtered = remember(selectedSubject, selectedGrade, selectedDifficulty) {
        LanguageCurriculum.schoolSubjectChallenges.filter { item ->
            (selectedSubject == "All" || item.subject == selectedSubject) &&
                (selectedGrade == "All Classes" || item.gradeBand == selectedGrade) &&
                (selectedDifficulty == "All" || item.difficulty == selectedDifficulty)
        }.ifEmpty { LanguageCurriculum.schoolSubjectChallenges }
    }

    var questionIdx by rememberSaveable(selectedSubject, selectedGrade, selectedDifficulty) { mutableIntStateOf(0) }
    val current = filtered[questionIdx % filtered.size]
    var pickedOption by remember(current.id) { mutableStateOf<String?>(null) }
    var rewarded by remember(current.id) { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("school_subjects_lab"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "🏫 School & Academic Subject Mastery",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold
                )
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    allSubjectNames.forEach { subj ->
                        FilterChip(
                            selected = selectedSubject == subj,
                            onClick = { selectedSubject = subj },
                            label = { Text(subj) },
                            modifier = Modifier.heightIn(min = 48.dp)
                        )
                    }
                }
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    gradeBands.forEach { band ->
                        FilterChip(
                            selected = selectedGrade == band,
                            onClick = { selectedGrade = band },
                            label = { Text(band) },
                            modifier = Modifier.heightIn(min = 48.dp)
                        )
                    }
                    difficulties.forEach { diff ->
                        FilterChip(
                            selected = selectedDifficulty == diff,
                            onClick = { selectedDifficulty = diff },
                            label = { Text(diff) },
                            modifier = Modifier.heightIn(min = 48.dp)
                        )
                    }
                }
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${current.subject} • ${current.topic}",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.weight(1f)
                        )
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer
                        ) {
                            Text(
                                text = "${current.gradeBand} • ${current.difficulty}",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                    LinearProgressIndicator(
                        progress = { ((questionIdx % filtered.size) + 1f) / filtered.size.toFloat() },
                        modifier = Modifier.fillMaxWidth().height(8.dp)
                    )
                    Text(
                        text = current.question,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                current.options.forEachIndexed { idx, opt ->
                    val isSelected = pickedOption == opt
                    OutlinedButton(
                        onClick = {
                            pickedOption = opt
                            if (opt == current.correctAnswer && !rewarded) {
                                rewarded = true
                                onEarnSubjectXp(current.subject, 25, 10)
                            }
                        },
                        border = BorderStroke(
                            width = if (isSelected) 2.5.dp else 1.dp,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 52.dp)
                            .testTag("school_subject_opt_$idx")
                    ) {
                        Text(opt, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }

        if (pickedOption != null) {
            item {
                val ok = pickedOption == current.correctAnswer
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (ok) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = if (ok) "✅ Correct! +25 XP & +10 Gems" else "💡 Answer: ${current.correctAnswer}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(current.explanation, style = MaterialTheme.typography.bodyMedium)
                        Button(
                            onClick = { questionIdx = (questionIdx + 1) % filtered.size },
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 48.dp)
                                .testTag("next_school_subject_button")
                        ) {
                            Text("Next Concept Question")
                        }
                    }
                }
            }
        }
    }
}
