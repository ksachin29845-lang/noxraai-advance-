package com.example.data.curriculum

data class FiftyLevelQuestion(
    val id: String,
    val prompt: String,
    val options: List<String>,
    val correctAnswer: String,
    val explanation: String,
    val audioHint: String = ""
)

data class FiftyLevelNode(
    val levelNumber: Int,
    val unitTitle: String,
    val title: String,
    val subtitle: String,
    val iconEmoji: String,
    val xpReward: Int,
    val questions: List<FiftyLevelQuestion>
)

object FiftyLevelMasteryCurriculum {

    val SUPPORTED_CLASSES = listOf(
        "Class 1", "Class 2", "Class 3", "Class 4", "Class 5", "Class 6",
        "Class 7", "Class 8", "Class 9", "Class 10", "Class 11", "Class 12",
        "College / General"
    )

    val SUPPORTED_TIERS = listOf("Basic", "Mid", "Advance")

    private fun parseGradeNumber(studentClass: String): Int {
        val digits = studentClass.filter { it.isDigit() }.toIntOrNull()
        return digits ?: 10
    }

    fun generateEnglishFiftyLevels(studentClass: String, tier: String): List<FiftyLevelNode> {
        val safeTier = tier.takeIf { it in SUPPORTED_TIERS } ?: "Basic"
        val grade = parseGradeNumber(studentClass)
        val unitNames = listOf(
            "Unit 1 (Lv 1–10): Everyday Words & Sentence Foundations",
            "Unit 2 (Lv 11–20): Confident Grammar, Tenses & Speaking",
            "Unit 3 (Lv 21–30): Smart Vocabulary, Idioms & Dialogues",
            "Unit 4 (Lv 31–40): Reading Comprehension & Writing Polish",
            "Unit 5 (Lv 41–50): Championship Fluency & Real-World Mastery"
        )
        val emojis = listOf("📘", "🗣️", "✨", "✍️", "🎯", "🌟", "📖", "💬", "🧠", "🏆")

        return (1..50).map { lvl ->
            val unitIdx = ((lvl - 1) / 10).coerceIn(0, 4)
            val title = englishLevelTitle(lvl, safeTier)
            val subtitle = "$studentClass • $safeTier Tier • Easy Step-by-Step Practice"
            val xp = 20 + (lvl / 5) * 5 + when (safeTier) {
                "Mid" -> 5
                "Advance" -> 10
                else -> 0
            }
            FiftyLevelNode(
                levelNumber = lvl,
                unitTitle = unitNames[unitIdx],
                title = "Level $lvl: $title",
                subtitle = subtitle,
                iconEmoji = emojis[(lvl - 1) % emojis.size],
                xpReward = xp,
                questions = buildEnglishQuestionsForLevel(lvl, grade, studentClass, safeTier)
            )
        }
    }

    fun generateMathFiftyLevels(studentClass: String, tier: String): List<FiftyLevelNode> {
        val safeTier = tier.takeIf { it in SUPPORTED_TIERS } ?: "Basic"
        val grade = parseGradeNumber(studentClass)
        val unitNames = listOf(
            "Unit 1 (Lv 1–10): Core Number Sense & Mental Speed",
            "Unit 2 (Lv 11–20): Fractions, Ratios & Percentages",
            "Unit 3 (Lv 21–30): Algebra Equations & Patterns",
            "Unit 4 (Lv 31–40): Geometry, Mensuration & Logic",
            "Unit 5 (Lv 41–50): Olympiad Word Problems & Mastery"
        )
        val emojis = listOf("➗", "🧮", "📐", "⚡", "🔢", "📊", "🧠", "🎲", "🚀", "🏆")

        return (1..50).map { lvl ->
            val unitIdx = ((lvl - 1) / 10).coerceIn(0, 4)
            val title = mathLevelTitle(lvl, safeTier)
            val subtitle = "$studentClass • $safeTier Tier • Visual & AI Math Practice"
            val xp = 20 + (lvl / 5) * 5 + when (safeTier) {
                "Mid" -> 5
                "Advance" -> 10
                else -> 0
            }
            FiftyLevelNode(
                levelNumber = lvl,
                unitTitle = unitNames[unitIdx],
                title = "Level $lvl: $title",
                subtitle = subtitle,
                iconEmoji = emojis[(lvl - 1) % emojis.size],
                xpReward = xp,
                questions = buildMathQuestionsForLevel(lvl, grade, studentClass, safeTier)
            )
        }
    }

    private fun englishLevelTitle(lvl: Int, tier: String): String {
        val topics = listOf(
            "Greetings & Polite Starters", "Action Verbs in Daily Life", "Present Simple Sentences",
            "Articles (A, An, The)", "Asking Clear Questions", "Describing People & Places",
            "Past Tense Storytelling", "Future Plans & Goals", "Prepositions (In, On, At)",
            "Unit Checkpoint Challenge", "Connecting Ideas (Because, So)", "Pronouns & Ownership",
            "Modal Verbs (Can, Should, Must)", "Everyday School Conversations", "Comparatives & Superlatives",
            "Present Perfect Mastery", "Polite Requests & Replies", "Synonyms & Stronger Words",
            "Antonyms & Contrast", "Mid-Path Fluency Boss", "Active vs. Passive Voice",
            "Direct & Indirect Speech", "Conditional Sentences (If…)", "Common English Idioms",
            "Phrasal Verbs in Action", "Spotting Grammar Mistakes", "Paragraph Flow & Clarity",
            "Formal vs. Friendly Tone", "confident Public Speaking", "Unit 3 Mastery Checkpoint",
            "Context Clues in Reading", " Inference & Main Idea", "Precise Adverbs & Adjectives",
            "Subject-Verb Agreement", "Complex Sentence Building", "Email & Message Writing",
            "Debate & Opinion Phrases", "Academic Vocabulary Boost", "Listening & Nuance Check",
            "Unit 4 Champion Trial", "Advanced Tense Harmony", "Subtle Preposition Pairs",
            " persuasive Expressions", " Conjunctions (Although, Despite)", "Editing Real Sentences",
            " Crisp Summary Skills", "Interview & Presentation English", "High-Impact Idioms",
            "Penultimate Fluency Sprint", "Grand Level 50 English Crown"
        )
        return "${topics[(lvl - 1) % topics.size]} ($tier)"
    }

    private fun mathLevelTitle(lvl: Int, tier: String): String {
        val topics = listOf(
            "Fast Addition & Number Bonds", "Smart Subtraction Tricks", "Multiplication Mastery",
            "Division & Remainders", "Order of Operations (BODMAS)", "Factors & Multiples",
            "Prime Numbers & LCM/HCF", "Decimals in Action", "Fractions Equivalence",
            "Unit 1 Math Boss", "Adding & Subtracting Fractions", "Percentages Made Easy",
            "Ratio & Proportion", "Profit, Loss & Discounts", "Speed, Distance & Time",
            "Averages & Data Mean", "Exponents & Powers", "Square Roots & Cubes",
            "Integers & Negative Numbers", "Unit 2 Math Boss", "One-Step Linear Equations",
            "Two-Step Algebra Solver", "Simplifying Expressions", "Sequences & Number Patterns",
            "Coordinate Grid Basics", "Inequalities & Ranges", "Simultaneous Thinking",
            "Word-to-Algebra Translator", "Polynomial Foundations", "Unit 3 Algebra Boss",
            "Angles & Triangles", "Perimeter & Area Explorer", "Circles: Radius & Area",
            "3D Volume & Surface Area", "Pythagoras Right-Triangle", "Symmetry & Polygons",
            "Probability & Fair Coins", "Statistics & Median Mode", "Venn & Logical Sets",
            "Unit 4 Geometry Boss", "Compound Growth Steps", "Work & Time Together",
            "Mixture & Ratio Puzzles", "Quadratic Roots Check", "Trigonometric Ratios Intro",
            "Multi-Step Brain Teasers", "Pattern Generalization", "Speed Estimation Sprint",
            "Pre-Final Math Gauntlet", "Grand Level 50 Math Crown"
        )
        return "${topics[(lvl - 1) % topics.size]} ($tier)"
    }

    private fun buildEnglishQuestionsForLevel(
        lvl: Int,
        grade: Int,
        studentClass: String,
        tier: String
    ): List<FiftyLevelQuestion> {
        val basicBank = listOf(
            Triple(
                "Choose the correct word: 'She ___ to school every morning.'",
                listOf("goes", "go", "going", "gone"),
                "goes" to "With singular subjects (he/she/it) in Present Simple, add -s/-es: 'She goes'."
            ),
            Triple(
                "Pick the right article: 'I saw ___ honest boy helping his friend.'",
                listOf("an", "a", "the", "no article"),
                "an" to "'Honest' starts with a vowel sound ('o'), so we use 'an honest boy'."
            ),
            Triple(
                "Complete politely: '___ you please explain this question again?'",
                listOf("Could", "Are", "Does", "Have"),
                "Could" to "'Could you please…' is a natural, polite way to ask for help in English."
            ),
            Triple(
                "Choose the correct past tense: 'Yesterday, we ___ English on Noxra AI.'",
                listOf("practiced", "practice", "practices", "practicing"),
                "practiced" to "'Yesterday' signals a finished action in the past, so use 'practiced'."
            ),
            Triple(
                "Select the opposite (antonym) of 'Brave':",
                listOf("Timid", "Bold", "Strong", "Clever"),
                "Timid" to "'Timid' (shy/fearful) is the opposite of 'Brave', while 'Bold' is a synonym."
            ),
            Triple(
                "Pick the correct preposition: 'My birthday is ___ July.'",
                listOf("in", "on", "at", "by"),
                "in" to "Use 'in' for months and years ('in July'), 'on' for days, and 'at' for clock times."
            )
        )

        val midBank = listOf(
            Triple(
                "Fix the sentence: 'I have been studying English ___ three years.'",
                listOf("for", "since", "from", "during"),
                "for" to "Use 'for' with a duration of time ('three years') and 'since' with a starting point ('2023')."
            ),
            Triple(
                "Choose the best connector: '___ it was raining, they finished the cricket match.'",
                listOf("Although", "Because", "Unless", "Therefore"),
                "Although" to "'Although' introduces an unexpected contrast between rain and finishing the match."
            ),
            Triple(
                "Select the natural conditional: 'If I study daily, I ___ all 50 levels easily.'",
                listOf("will complete", "completed", "would completed", "completing"),
                "will complete" to "First Conditional uses 'If + Present Simple, will + base verb' for real future outcomes."
            ),
            Triple(
                "What does the idiom 'A piece of cake' mean?",
                listOf("Something very easy", "A sweet dessert only", "Something expensive", "A hard puzzle"),
                "Something very easy" to "In English, 'a piece of cake' means a task is super easy to accomplish!"
            ),
            Triple(
                "Subject-verb check: 'Neither the teacher nor the students ___ late today.'",
                listOf("were", "was", "is", "has been"),
                "were" to "With 'neither…nor', the verb agrees with the closer subject ('students' -> plural 'were')."
            ),
            Triple(
                "Choose the stronger synonym for 'Very happy':",
                listOf("Delighted", "Tired", "Calm", "Curious"),
                "Delighted" to "'Delighted' expresses joyful happiness clearly and vividly."
            )
        )

        val advanceBank = listOf(
            Triple(
                "Choose the grammatically precise sentence ($studentClass • Advance):",
                listOf(
                    "Hardly had the bell rung when the exam began.",
                    "Hardly the bell had rung than the exam began.",
                    "Hardly had the bell rang when the exam began.",
                    "Hardly did the bell rung when the exam began."
                ),
                "Hardly had the bell rung when the exam began." to "Negative inversion after 'Hardly' uses 'had + subject + past participle (rung) … when'."
            ),
            Triple(
                "Passive voice transformation: 'Noxra AI guides millions of learners.'",
                listOf(
                    "Millions of learners are guided by Noxra AI.",
                    "Millions of learners were guided by Noxra AI.",
                    "Millions of learners have guided Noxra AI.",
                    "Millions of learners is guided by Noxra AI."
                ),
                "Millions of learners are guided by Noxra AI." to "Present Simple active ('guides') becomes 'are + past participle (guided)' in passive voice."
            ),
            Triple(
                "Pick the word closest in meaning to 'Meticulous':",
                listOf("Careful and precise", "Careless and rushed", "Loud and outspoken", "Uncertain"),
                "Careful and precise" to "'Meticulous' describes paying great attention to every small detail."
            ),
            Triple(
                "Second Conditional mastery: 'If she ___ the CEO, she would launch a student AI lab.'",
                listOf("were", "was been", "will be", "is"),
                "were" to "Formal/standard English uses the subjunctive 'were' for hypothetical 'If I/she were…' clauses."
            ),
            Triple(
                "Indirect speech: Rohan said, 'I am solving Level $lvl now.'",
                listOf(
                    "Rohan said that he was solving Level $lvl then.",
                    "Rohan said that he is solving Level $lvl now.",
                    "Rohan told that I was solving Level $lvl then.",
                    "Rohan said that he has solved Level $lvl."
                ),
                "Rohan said that he was solving Level $lvl then." to "Present Continuous shifts to Past Continuous ('was solving') and 'now' shifts to 'then'."
            ),
            Triple(
                "Choose the right preposition pair: 'She is adept ___ coding and dedicated ___ innovation.'",
                listOf("at / to", "in / for", "on / with", "by / into"),
                "at / to" to "We say 'adept at' a skill and 'dedicated to' a mission or goal."
            )
        )

        val pool = when {
            tier == "Advance" || (tier == "Mid" && grade >= 10 && lvl > 25) -> advanceBank
            tier == "Mid" || lvl > 30 -> midBank
            else -> basicBank
        }

        return (0..2).map { qIdx ->
            val picked = pool[(lvl * 2 + qIdx + grade) % pool.size]
            val correct = picked.third.first
            val expl = picked.third.second
            // Deterministic rotation so options don't always have correct answer first
            val shift = (lvl + qIdx) % picked.second.size
            val rotatedOptions = picked.second.drop(shift) + picked.second.take(shift)
            FiftyLevelQuestion(
                id = "eng_${tier}_${lvl}_$qIdx",
                prompt = "[Lv.$lvl • $tier] ${picked.first}",
                options = rotatedOptions,
                correctAnswer = correct,
                explanation = expl,
                audioHint = correct
            )
        }
    }

    private fun buildMathQuestionsForLevel(
        lvl: Int,
        grade: Int,
        studentClass: String,
        tier: String
    ): List<FiftyLevelQuestion> {
        val tierMultiplier = when (tier) {
            "Mid" -> 2
            "Advance" -> 3
            else -> 1
        }
        val scale = (grade.coerceIn(1, 13) + (lvl / 5)) * tierMultiplier

        // Question 1: Arithmetic / Mental Speed / Percentages scaled to Class, Tier & Level 1..50
        val q1: FiftyLevelQuestion = if (grade <= 4 && tier == "Basic") {
            val a = 6 + lvl + grade * 2
            val b = 4 + (lvl % 9) + grade
            val ans = a + b
            val opts = listOf("$ans", "${ans + 2}", "${ans - 1}", "${ans + 5}")
            FiftyLevelQuestion(
                id = "math_${tier}_${lvl}_0",
                prompt = "[Lv.$lvl • $studentClass $tier] Calculate quickly:  $a + $b = ?",
                options = rotateList(opts, lvl),
                correctAnswer = "$ans",
                explanation = "Break $b into tens and ones: $a + $b = $ans!"
            )
        } else if (tier == "Basic") {
            val a = 4 + (lvl % 12) + (grade / 2)
            val b = 3 + ((lvl + grade) % 9)
            val c = lvl % 11
            val ans = a * b + c
            val opts = listOf("$ans", "${ans + b}", "${ans - 2}", "${ans + 4}")
            FiftyLevelQuestion(
                id = "math_${tier}_${lvl}_0",
                prompt = "[Lv.$lvl • $studentClass $tier] Using BODMAS, evaluate:  ($a × $b) + $c",
                options = rotateList(opts, lvl),
                correctAnswer = "$ans",
                explanation = "Step 1: Multiply first ($a × $b = ${a * b}). Step 2: Add $c to get $ans!"
            )
        } else {
            val pctOptions = listOf(10, 15, 20, 25, 30, 40)
            val pct = pctOptions[lvl % pctOptions.size]
            val base = 20 * (2 + (lvl % 8) + (grade / 3))
            val ans = (base * pct) / 100
            val opts = listOf("$ans", "${ans + 10}", "${(ans - 5).coerceAtLeast(1)}", "${ans + 15}")
            FiftyLevelQuestion(
                id = "math_${tier}_${lvl}_0",
                prompt = "[Lv.$lvl • $studentClass $tier] What is $pct% of $base?",
                options = rotateList(opts, lvl),
                correctAnswer = "$ans",
                explanation = "$pct% means $pct/100. Multiply ($base × $pct) ÷ 100 = $ans!"
            )
        }

        // Question 2: Missing Number / Linear Equation Solver scaled by Tier & Level
        val xVal = 2 + ((lvl + grade) % 11) + (tierMultiplier - 1) * 2
        val coeff = 2 + (lvl % (2 + tierMultiplier))
        val constant = 3 + ((lvl * 2) % 15)
        val rhs = coeff * xVal + constant
        val q2Prompt = if (grade <= 3 && tier == "Basic") {
            "Find the missing number □:   $coeff × □ + $constant = $rhs"
        } else {
            "Solve for x:   ${coeff}x + $constant = $rhs"
        }
        val q2Ans = if (grade <= 3 && tier == "Basic") "$xVal" else "x = $xVal"
        val q2Opts = if (grade <= 3 && tier == "Basic") {
            listOf("$xVal", "${xVal + 1}", "${(xVal - 1).coerceAtLeast(1)}", "${xVal + 3}")
        } else {
            listOf("x = $xVal", "x = ${xVal + 2}", "x = ${(xVal - 1).coerceAtLeast(1)}", "x = ${xVal + 4}")
        }
        val q2 = FiftyLevelQuestion(
            id = "math_${tier}_${lvl}_1",
            prompt = "[Lv.$lvl • $tier] $q2Prompt",
            options = rotateList(q2Opts, lvl + 1),
            correctAnswer = q2Ans,
            explanation = "Step 1: Subtract $constant from $rhs to get ${rhs - constant}. Step 2: Divide ${rhs - constant} by $coeff = $xVal!"
        )

        // Question 3: Geometry / Word Problem / Sequence Pattern
        val width = 3 + (lvl % 9) + tierMultiplier
        val length = width + 2 + (scale % 6)
        val q3: FiftyLevelQuestion = if (tier == "Advance") {
            val area = length * width
            val perim = 2 * (length + width)
            val sumAns = area + perim
            val opts = listOf("$sumAns", "${area}", "${perim}", "${sumAns + 8}")
            FiftyLevelQuestion(
                id = "math_${tier}_${lvl}_2",
                prompt = "[Lv.$lvl • Advance] A study lab is ${length}m by ${width}m. What is (Area + Perimeter) numerically?",
                options = rotateList(opts, lvl + 2),
                correctAnswer = "$sumAns",
                explanation = "Area = $length × $width = $area. Perimeter = 2×($length + $width) = $perim. Total = $area + $perim = $sumAns!"
            )
        } else {
            val step = 2 + (lvl % 5) * tierMultiplier
            val start = 3 + (grade % 7)
            val n1 = start
            val n2 = start + step
            val n3 = start + step * 2
            val n4 = start + step * 3
            val nextTerm = start + step * 4
            val opts = listOf("$nextTerm", "${nextTerm + step}", "${nextTerm - 2}", "${nextTerm + 3}")
            FiftyLevelQuestion(
                id = "math_${tier}_${lvl}_2",
                prompt = "[Lv.$lvl • $tier] Find the next number in the pattern:  $n1, $n2, $n3, $n4, ___",
                options = rotateList(opts, lvl + 2),
                correctAnswer = "$nextTerm",
                explanation = "Each term increases by +$step. So $n4 + $step = $nextTerm!"
            )
        }

        return listOf(q1, q2, q3)
    }

    private fun rotateList(items: List<String>, shiftSeed: Int): List<String> {
        val distinct = items.distinct()
        if (distinct.size < 4) {
            val padded = distinct.toMutableList()
            var extra = 17
            while (padded.size < 4) {
                val candidate = "${extra++}"
                if (candidate !in padded) padded.add(candidate)
            }
            val s = shiftSeed % padded.size
            return padded.drop(s) + padded.take(s)
        }
        val s = shiftSeed % distinct.size
        return distinct.drop(s) + distinct.take(s)
    }
}
