package com.example.data.curriculum

enum class ExerciseType(val label: String) {
    MULTIPLE_CHOICE("Multiple Choice"),
    TRANSLATE("Translate Sentence"),
    TYPE_TRANSLATION("Type Translation"),
    FILL_BLANK("Fill in the Blank"),
    ARRANGE_WORDS("Sentence Builder"),
    WORD_MATCH("Word Matching"),
    UNSCRAMBLE("Unscramble Word"),
    LISTEN_AND_TYPE("Type What You Hear"),
    SPEAK_SENTENCE("Speak Aloud"),
    FIX_GRAMMAR("Correct the Sentence"),
    TRUE_FALSE("True or False"),
    CONVERSATION("Complete Conversation")
}

data class LanguageInfo(
    val name: String,
    val nativeName: String,
    val flagEmoji: String,
    val scriptSample: String,
    val greeting: String,
    val greetingMeaning: String
)

data class ExerciseQuestion(
    val id: String,
    val type: ExerciseType,
    val prompt: String,
    val audioPhrase: String,
    val options: List<String>,
    val correctAnswer: String,
    val wordBank: List<String> = emptyList(),
    val matchPairs: List<Pair<String, String>> = emptyList(),
    val explanation: String,
    val keyVocabWord: String,
    val keyVocabMeaning: String
)

data class LessonNode(
    val id: String,
    val index: Int,
    val unitTitle: String,
    val title: String,
    val subtitle: String,
    val tier: String, // Beginner, Intermediate, Advanced
    val category: String, // Core, Conversation, Travel, Business, Story, Mission, Slang
    val xpReward: Int,
    val gemReward: Int,
    val iconEmoji: String,
    val questions: List<ExerciseQuestion>
)

data class GrammarTopic(
    val id: String,
    val title: String,
    val level: String,
    val summary: String,
    val formula: String,
    val examples: List<Pair<String, String>>,
    val commonMistakeTip: String
)

data class DictionaryWord(
    val word: String,
    val language: String,
    val phonetic: String,
    val partOfSpeech: String,
    val meaningHindi: String,
    val meaningGlobal: String,
    val synonyms: List<String>,
    val antonyms: List<String>,
    val conjugations: List<String>,
    val exampleSentence: String,
    val exampleTranslation: String
)

data class SpeakingDrill(
    val id: String,
    val scenario: String,
    val phrase: String,
    val phoneticHint: String,
    val translation: String,
    val difficulty: String
)

data class SchoolSubjectChallenge(
    val id: String,
    val subject: String,
    val gradeBand: String,
    val difficulty: String,
    val topic: String,
    val question: String,
    val options: List<String>,
    val correctAnswer: String,
    val explanation: String
)

object LanguageCurriculum {

    val supportedLanguages: List<LanguageInfo> = listOf(
        LanguageInfo("English", "English", "🇬🇧", "Aa Bb Cc", "Hello, nice to meet you!", "नमस्ते, आपसे मिलकर खुशी हुई!"),
        LanguageInfo("Hindi", "हिन्दी", "🇮🇳", "अ आ इ क ख", "नमस्ते, आप कैसे हैं?", "Hello, how are you?"),
        LanguageInfo("Spanish", "Español", "🇪🇸", "Ñ ñ Á É", "¡Hola! ¿Cómo estás?", "Hello! How are you?"),
        LanguageInfo("French", "Français", "🇫🇷", "Ç É È Ê", "Bonjour, enchanté !", "Hello, pleased to meet you!"),
        LanguageInfo("German", "Deutsch", "🇩🇪", "Ä Ö Ü ß", "Guten Tag! Wie geht's?", "Good day! How's it going?"),
        LanguageInfo("Japanese", "日本語", "🇯🇵", "あ い う カ キ", "こんにちは、元気ですか？", "Hello, are you well?"),
        LanguageInfo("Korean", "한국어", "🇰🇷", "한 글 가 나", "안녕하세요! 반갑습니다.", "Hello! Nice to meet you."),
        LanguageInfo("Chinese", "中文", "🇨🇳", "你好 学习", "你好，很高兴认识你！", "Hello, nice to meet you!"),
        LanguageInfo("Arabic", "العربية", "🇸🇦", "أ ب ت ث", "مرحباً، كيف حالك؟", "Welcome, how are you?"),
        LanguageInfo("Italian", "Italiano", "🇮🇹", "À È Ì Ò", "Ciao! Piacere di conoscerti.", "Hi! Nice to meet you."),
        LanguageInfo("Portuguese", "Português", "🇧🇷", "Ã Õ Ç Á", "Olá! Tudo bem com você?", "Hello! All good with you?"),
        LanguageInfo("Russian", "Русский", "🇷🇺", "А Б В Г Д", "Привет! Как дела?", "Hi! How are things?")
    )

    fun wordOfTheDay(language: String): Pair<String, String> = when (language) {
        "Spanish" -> "Resiliencia (Resilience)" to "La capacidad de adaptarse con éxito — Strength to bounce back."
        "French" -> "Épanouissement (Blossoming)" to "Personal growth and joyful fulfillment in daily life."
        "Japanese" -> "改善 • Kaizen" to "Continuous daily improvement — one small step every day."
        "Hindi" -> "आत्मविश्वास • Aatmavishwas" to "Self-confidence — believing in your voice and skills."
        "German" -> "Fernweh (Wanderlust)" to "A deep longing to explore distant places and cultures."
        "Korean" -> "정 • Jeong" to "Warmth, connection, and deep bond shared with friends."
        else -> "Eloquent (/ˈel.ə.kwənt/)" to "Fluent, clear, and expressive in speaking or writing (प्रभावशाली वक्ता)."
    }

    fun phraseOfTheDay(language: String): Pair<String, String> = when (language) {
        "Spanish" -> "Poco a poco se llega lejos." to "Little by little, one goes far."
        "French" -> "Petit à petit, l'oiseau fait son nid." to "Little by little, the bird builds its nest."
        "Japanese" -> "七転び八起き (Nanakorobi yaoki)" to "Fall seven times, stand up eight."
        "Hindi" -> "अभ्यास से ही निपुणता आती है।" to "Practice brings true mastery."
        else -> "Consistency turns small daily practice into fluency." to "नियमित अभ्यास छोटे कदमों को धाराप्रवाह भाषा में बदल देता है।"
    }

    fun alphabetCardsFor(language: String): List<Triple<String, String, String>> = when (language) {
        "Hindi" -> listOf(
            Triple("अ", "a", "अनार (Pomegranate)"),
            Triple("आ", "aa", "आम (Mango)"),
            Triple("इ", "i", "इमली (Tamarind)"),
            Triple("क", "ka", "कमल (Lotus)"),
            Triple("नमस्ते", "namaste", "Hello / Respectful greeting")
        )
        "Japanese" -> listOf(
            Triple("あ", "a", "あさ (Morning)"),
            Triple("い", "i", "いぬ (Dog)"),
            Triple("う", "u", "うみ (Sea)"),
            Triple("え", "e", "えき (Station)"),
            Triple("お", "o", "おちゃ (Tea)")
        )
        "Korean" -> listOf(
            Triple("ㅏ", "a", "아이 (Child)"),
            Triple("ㅓ", "eo", "어머니 (Mother)"),
            Triple("ㄱ", "g/k", "가방 (Bag)"),
            Triple("ㄴ", "n", "나라 (Country)"),
            Triple("ㅎ", "h", "하늘 (Sky)")
        )
        "Spanish" -> listOf(
            Triple("Ñ ñ", "nye", "Mañana (Tomorrow)"),
            Triple("LL ll", "ye", "Lluvia (Rain)"),
            Triple("RR rr", "rolled r", "Perro (Dog)"),
            Triple("Á É Í", "stressed vowel", "Café (Coffee)"),
            Triple("¿ ¡", "inverted marks", "¡Hola! ¿Qué tal?")
        )
        else -> listOf(
            Triple("Th (θ)", "voiceless th", "Think, Thank, Three"),
            Triple("Th (ð)", "voiced th", "This, That, Together"),
            Triple("Schwa (ə)", "relaxed vowel", "About, Teacher, Support"),
            Triple("Silent K/W", "silent letters", "Know, Write, Honest"),
            Triple("Ch / Sh", "consonant pair", "Champion, Shine, Share")
        )
    }

    fun lessonsForLanguage(language: String): List<LessonNode> {
        val langInfo = supportedLanguages.find { it.name == language } ?: supportedLanguages.first()
        return if (language == "English") {
            englishCourseNodes()
        } else {
            localizedCourseNodes(langInfo)
        }
    }

    private fun englishCourseNodes(): List<LessonNode> = listOf(
        LessonNode(
            id = "en_u1_greetings",
            index = 1,
            unitTitle = "Unit 1 • Everyday Foundations",
            title = "Greetings & Introductions",
            subtitle = "Say hello, introduce yourself & polite expressions",
            tier = "Beginner",
            category = "Core",
            xpReward = 25,
            gemReward = 15,
            iconEmoji = "👋",
            questions = listOf(
                ExerciseQuestion(
                    id = "q1",
                    type = ExerciseType.MULTIPLE_CHOICE,
                    prompt = "Which phrase means 'आपसे मिलकर खुशी हुई' in polite English?",
                    audioPhrase = "Nice to meet you!",
                    options = listOf("Nice to meet you!", "Where is the station?", "See you yesterday", "Give me water"),
                    correctAnswer = "Nice to meet you!",
                    explanation = "'Nice to meet you!' is the standard warm English greeting when meeting someone for the first time.",
                    keyVocabWord = "Nice to meet you",
                    keyVocabMeaning = "आपसे मिलकर खुशी हुई"
                ),
                ExerciseQuestion(
                    id = "q2",
                    type = ExerciseType.ARRANGE_WORDS,
                    prompt = "Arrange the words to introduce yourself:",
                    audioPhrase = "Hello my name is Sachin",
                    options = emptyList(),
                    correctAnswer = "Hello my name is Sachin",
                    wordBank = listOf("Sachin", "Hello", "is", "name", "my", "tomorrow"),
                    explanation = "English follows Subject + Verb + Complement order: 'My name is Sachin'.",
                    keyVocabWord = "Introduce",
                    keyVocabMeaning = "परिचय देना"
                ),
                ExerciseQuestion(
                    id = "q3",
                    type = ExerciseType.FILL_BLANK,
                    prompt = "Complete the sentence: 'How ___ you doing today?'",
                    audioPhrase = "How are you doing today?",
                    options = listOf("are", "is", "am", "be"),
                    correctAnswer = "are",
                    explanation = "With the pronoun 'you', always pair the verb 'are' in present tense.",
                    keyVocabWord = "How are you",
                    keyVocabMeaning = "आप कैसे हैं"
                ),
                ExerciseQuestion(
                    id = "q4",
                    type = ExerciseType.WORD_MATCH,
                    prompt = "Match each English word with its Hindi meaning:",
                    audioPhrase = "Thank you, Friend, Morning, Welcome",
                    options = listOf("Thank you -> धन्यवाद", "Friend -> दोस्त", "Morning -> सुबह", "Welcome -> स्वागत है"),
                    correctAnswer = "ALL_MATCHED",
                    matchPairs = listOf(
                        "Thank you" to "धन्यवाद",
                        "Friend" to "दोस्त",
                        "Morning" to "सुबह",
                        "Welcome" to "स्वागत है"
                    ),
                    explanation = "Core courtesy words build instant confidence in daily conversations.",
                    keyVocabWord = "Welcome",
                    keyVocabMeaning = "स्वागत है"
                ),
                ExerciseQuestion(
                    id = "q5",
                    type = ExerciseType.UNSCRAMBLE,
                    prompt = "Unscramble the letters to spell the word for 'सुप्रभात' (Morning):",
                    audioPhrase = "MORNING",
                    options = listOf("MORNING", "MONRING", "MORNIGN", "MRONING"),
                    correctAnswer = "MORNING",
                    explanation = "M-O-R-N-I-N-G! Used in 'Good morning!' until 12:00 PM.",
                    keyVocabWord = "Morning",
                    keyVocabMeaning = "सुबह / सुप्रभात"
                )
            )
        ),
        LessonNode(
            id = "en_u1_cafe",
            index = 2,
            unitTitle = "Unit 1 • Everyday Foundations",
            title = "Real-Life Café & Food Mission",
            subtitle = "Order meals, coffee & ask politely for the bill",
            tier = "Beginner",
            category = "Mission",
            xpReward = 30,
            gemReward = 20,
            iconEmoji = "☕",
            questions = listOf(
                ExerciseQuestion(
                    id = "c1",
                    type = ExerciseType.CONVERSATION,
                    prompt = "Barista: 'Hi! What can I get for you today?'\nChoose the most natural, polite reply:",
                    audioPhrase = "Could I have a cup of masala tea, please?",
                    options = listOf(
                        "Could I have a cup of masala tea, please?",
                        "Give tea fast now.",
                        "I am going yesterday.",
                        "Where is library book?"
                    ),
                    correctAnswer = "Could I have a cup of masala tea, please?",
                    explanation = "'Could I have ..., please?' sounds warm, natural, and polite when ordering food or drinks.",
                    keyVocabWord = "Could I have",
                    keyVocabMeaning = "क्या मुझे मिल सकता है"
                ),
                ExerciseQuestion(
                    id = "c2",
                    type = ExerciseType.TRUE_FALSE,
                    prompt = "True or False: 'Can we have the check, please?' is a polite way to ask for the restaurant bill.",
                    audioPhrase = "Can we have the check, please?",
                    options = listOf("True", "False"),
                    correctAnswer = "True",
                    explanation = "Both 'bill' (UK/India) and 'check' (US) are widely used in restaurants!",
                    keyVocabWord = "Check / Bill",
                    keyVocabMeaning = "रेस्टोरेंट का बिल"
                ),
                ExerciseQuestion(
                    id = "c3",
                    type = ExerciseType.ARRANGE_WORDS,
                    prompt = "Build the sentence: 'यह खाना बहुत स्वादिष्ट है'",
                    audioPhrase = "This food is very delicious",
                    options = emptyList(),
                    correctAnswer = "This food is very delicious",
                    wordBank = listOf("delicious", "This", "very", "food", "is", "loud"),
                    explanation = "'Delicious' means स्वादिष्ट (very tasty).",
                    keyVocabWord = "Delicious",
                    keyVocabMeaning = "स्वादिष्ट"
                ),
                ExerciseQuestion(
                    id = "c4",
                    type = ExerciseType.TRANSLATE,
                    prompt = "Choose the best translation for: 'मुझे बिना चीनी की कॉफ़ी चाहिए।'",
                    audioPhrase = "I would like coffee without sugar.",
                    options = listOf(
                        "I would like coffee without sugar.",
                        "I have sugar with milk.",
                        "Coffee is very sweet.",
                        "Where is the coffee shop?"
                    ),
                    correctAnswer = "I would like coffee without sugar.",
                    explanation = "'I would like...' expresses a polite wish, and 'without sugar' means बिना चीनी के.",
                    keyVocabWord = "Without",
                    keyVocabMeaning = "के बिना"
                )
            )
        ),
        LessonNode(
            id = "en_u2_tenses",
            index = 3,
            unitTitle = "Unit 2 • Smart Grammar & Storytelling",
            title = "Past, Present & Future Action",
            subtitle = "Master tenses, time markers & sentence accuracy",
            tier = "Intermediate",
            category = "Core",
            xpReward = 35,
            gemReward = 20,
            iconEmoji = "⏳",
            questions = listOf(
                ExerciseQuestion(
                    id = "t1",
                    type = ExerciseType.FILL_BLANK,
                    prompt = "She ___ to the science museum yesterday.",
                    audioPhrase = "She went to the science museum yesterday.",
                    options = listOf("went", "go", "goes", "going"),
                    correctAnswer = "went",
                    explanation = "Because 'yesterday' refers to completed past time, use Simple Past 'went' (irregular past of 'go').",
                    keyVocabWord = "Yesterday",
                    keyVocabMeaning = "बीता हुआ कल"
                ),
                ExerciseQuestion(
                    id = "t2",
                    type = ExerciseType.MULTIPLE_CHOICE,
                    prompt = "Which sentence is grammatically 100% correct?",
                    audioPhrase = "I have been learning English for three months.",
                    options = listOf(
                        "I have been learning English for three months.",
                        "I am learn English since three months.",
                        "I has learned English tomorrow.",
                        "I learning English from three month."
                    ),
                    correctAnswer = "I have been learning English for three months.",
                    explanation = "Use Present Perfect Continuous ('have been + verb-ing') with 'for' + duration to show an action that started in the past and continues now!",
                    keyVocabWord = "Have been learning",
                    keyVocabMeaning = "सीखता आ रहा हूँ"
                ),
                ExerciseQuestion(
                    id = "t3",
                    type = ExerciseType.ARRANGE_WORDS,
                    prompt = "Arrange into a future goal sentence:",
                    audioPhrase = "We will build smart apps together",
                    options = emptyList(),
                    correctAnswer = "We will build smart apps together",
                    wordBank = listOf("together", "build", "We", "apps", "will", "smart"),
                    explanation = "'Will + base verb' expresses confident future plans.",
                    keyVocabWord = "Together",
                    keyVocabMeaning = "एक साथ"
                ),
                ExerciseQuestion(
                    id = "t4",
                    type = ExerciseType.FIX_GRAMMAR,
                    prompt = "Correct the grammatical mistake by typing the fixed sentence:\n\"She go to school every day.\"",
                    audioPhrase = "She goes to school every day",
                    options = emptyList(),
                    correctAnswer = "She goes to school every day",
                    explanation = "With third-person singular subjects (he/she/it) in Present Simple, add '-es' to 'go' → 'goes'.",
                    keyVocabWord = "Goes",
                    keyVocabMeaning = "जाती है / जाता है"
                )
            )
        ),
        LessonNode(
            id = "en_u2_story",
            index = 4,
            unitTitle = "Unit 2 • Smart Grammar & Storytelling",
            title = "Story Mode: The Lost Passport",
            subtitle = "Interactive airport travel mystery & idioms",
            tier = "Intermediate",
            category = "Story",
            xpReward = 40,
            gemReward = 25,
            iconEmoji = "✈️",
            questions = listOf(
                ExerciseQuestion(
                    id = "s1",
                    type = ExerciseType.CONVERSATION,
                    prompt = "Story: Aarav reaches Gate 12 at the airport and realizes his jacket pocket is empty!\nOfficer: 'May I see your boarding pass and passport?'\nHow should Aarav ask for help?",
                    audioPhrase = "Excuse me officer, I think I left my passport at the security desk!",
                    options = listOf(
                        "Excuse me officer, I think I left my passport at the security desk!",
                        "Passport fly away sky!",
                        "I am eating lunch on the plane.",
                        "No thank you goodbye."
                    ),
                    correctAnswer = "Excuse me officer, I think I left my passport at the security desk!",
                    explanation = "'Excuse me' + 'I think I left...' clearly explains where an item was misplaced.",
                    keyVocabWord = "Boarding pass",
                    keyVocabMeaning = "हवाई यात्रा का प्रवेश पत्र"
                ),
                ExerciseQuestion(
                    id = "s2",
                    type = ExerciseType.MULTIPLE_CHOICE,
                    prompt = "The officer smiles: 'Don't worry, someone just turned it in! You made it in the nick of time.'\nWhat does 'in the nick of time' mean?",
                    audioPhrase = "You made it in the nick of time.",
                    options = listOf(
                        "Just before it was too late",
                        "Two days early",
                        "Very slowly and sadly",
                        "Without a ticket"
                    ),
                    correctAnswer = "Just before it was too late",
                    explanation = "Idiom alert! 'In the nick of time' means ठीक समय पर — right at the last possible moment.",
                    keyVocabWord = "In the nick of time",
                    keyVocabMeaning = "ठीक आखिरी समय पर"
                ),
                ExerciseQuestion(
                    id = "s3",
                    type = ExerciseType.TRUE_FALSE,
                    prompt = "True or False: 'Turned it in' means handed a lost item safely to the authorities.",
                    audioPhrase = "Someone turned it in at the desk.",
                    options = listOf("True", "False"),
                    correctAnswer = "True",
                    explanation = "Phrasal verb 'turn in' means to submit or hand something over to staff/authorities.",
                    keyVocabWord = "Turn in",
                    keyVocabMeaning = "जमा करना / लौटाना"
                )
            )
        ),
        LessonNode(
            id = "en_u3_business",
            index = 5,
            unitTitle = "Unit 3 • Fluency, Tech & Interviews",
            title = "Job Interviews & Professional Pitch",
            subtitle = "Business English, confident answers & persuasion",
            tier = "Advanced",
            category = "Business",
            xpReward = 50,
            gemReward = 30,
            iconEmoji = "💼",
            questions = listOf(
                ExerciseQuestion(
                    id = "b1",
                    type = ExerciseType.CONVERSATION,
                    prompt = "Interviewer: 'Tell me about a challenge you solved recently.'\nPick the strongest professional opening:",
                    audioPhrase = "Recently, I led a team project where we improved app speed by forty percent.",
                    options = listOf(
                        "Recently, I led a team project where we improved app speed by forty percent.",
                        "I don't know any challenge ever.",
                        "Everything was easy nothing happened.",
                        "Why are you asking me this?"
                    ),
                    correctAnswer = "Recently, I led a team project where we improved app speed by forty percent.",
                    explanation = "Using concrete action verbs ('led', 'improved') and measurable impact makes interview answers stand out!",
                    keyVocabWord = "Collaborate",
                    keyVocabMeaning = "मिलकर काम करना"
                ),
                ExerciseQuestion(
                    id = "b2",
                    type = ExerciseType.FILL_BLANK,
                    prompt = "I look forward to ___ from you soon.",
                    audioPhrase = "I look forward to hearing from you soon.",
                    options = listOf("hearing", "hear", "heard", "hears"),
                    correctAnswer = "hearing",
                    explanation = "In 'look forward to', 'to' is a preposition, so it is followed by a gerund ('hearing')!",
                    keyVocabWord = "Look forward to",
                    keyVocabMeaning = "उत्सुकता से प्रतीक्षा करना"
                ),
                ExerciseQuestion(
                    id = "b3",
                    type = ExerciseType.MULTIPLE_CHOICE,
                    prompt = "What does the business idiom 'think outside the box' mean?",
                    audioPhrase = "Let us think outside the box for this launch.",
                    options = listOf(
                        "Explore creative, innovative ideas beyond usual limits",
                        "Put packages outside the office door",
                        "Cancel the meeting immediately",
                        "Copy old solutions without changing anything"
                    ),
                    correctAnswer = "Explore creative, innovative ideas beyond usual limits",
                    explanation = "'Think outside the box' celebrates originality and creative problem-solving.",
                    keyVocabWord = "Innovative",
                    keyVocabMeaning = "नवाचारी / नया सोचने वाला"
                )
            )
        ),
        LessonNode(
            id = "en_u3_unit_test",
            index = 6,
            unitTitle = "Unit 3 • Fluency, Tech & Interviews",
            title = "Unit-End Mastery Test",
            subtitle = "Listening, typing, speaking & grammar checkpoint",
            tier = "Advanced",
            category = "Unit Test",
            xpReward = 60,
            gemReward = 35,
            iconEmoji = "🎓",
            questions = listOf(
                ExerciseQuestion(
                    id = "ut1",
                    type = ExerciseType.LISTEN_AND_TYPE,
                    prompt = "Tap Listen and type the exact English sentence you hear:",
                    audioPhrase = "Practice makes every student confident",
                    options = emptyList(),
                    correctAnswer = "Practice makes every student confident",
                    explanation = "Singular noun 'every student' takes singular verb 'makes' + adjective 'confident'.",
                    keyVocabWord = "Confident",
                    keyVocabMeaning = "आत्मविश्वासी"
                ),
                ExerciseQuestion(
                    id = "ut2",
                    type = ExerciseType.TYPE_TRANSLATION,
                    prompt = "Type the English translation for: 'धन्यवाद दोस्त' (Thank you friend)",
                    audioPhrase = "Thank you friend",
                    options = emptyList(),
                    correctAnswer = "Thank you friend",
                    explanation = "'धन्यवाद' = Thank you, and 'दोस्त' = friend.",
                    keyVocabWord = "Thank you friend",
                    keyVocabMeaning = "धन्यवाद दोस्त"
                ),
                ExerciseQuestion(
                    id = "ut3",
                    type = ExerciseType.SPEAK_SENTENCE,
                    prompt = "Speak this sentence clearly aloud (or use keyboard mode):",
                    audioPhrase = "Learning every day builds strong skills",
                    options = emptyList(),
                    correctAnswer = "Learning every day builds strong skills",
                    explanation = "Speaking full phrases aloud links listening, pronunciation, and recall.",
                    keyVocabWord = "Skills",
                    keyVocabMeaning = "कौशल"
                ),
                ExerciseQuestion(
                    id = "ut4",
                    type = ExerciseType.MULTIPLE_CHOICE,
                    prompt = "Identify the sentence with the correct article and preposition:",
                    audioPhrase = "She is an honest engineer interested in science.",
                    options = listOf(
                        "She is an honest engineer interested in science.",
                        "She is a honest engineer interested on science.",
                        "She is the honest engineer interested at science.",
                        "She are an honest engineer interested for science."
                    ),
                    correctAnswer = "She is an honest engineer interested in science.",
                    explanation = "'Honest' begins with a vowel sound (/ɒ/), so use 'an', and 'interested' pairs with 'in'.",
                    keyVocabWord = "Interested in",
                    keyVocabMeaning = "में रुचि रखने वाला"
                )
            )
        )
    )

    private fun localizedCourseNodes(lang: LanguageInfo): List<LessonNode> = listOf(
        LessonNode(
            id = "${lang.name.lowercase()}_u1",
            index = 1,
            unitTitle = "Unit 1 • ${lang.name} Essentials",
            title = "${lang.name} Greetings & Politeness",
            subtitle = "Speak your very first ${lang.name} sentences with confidence",
            tier = "Beginner",
            category = "Core",
            xpReward = 25,
            gemReward = 15,
            iconEmoji = lang.flagEmoji,
            questions = listOf(
                ExerciseQuestion(
                    id = "l1",
                    type = ExerciseType.MULTIPLE_CHOICE,
                    prompt = "What is the warm greeting in ${lang.name}?",
                    audioPhrase = lang.greeting,
                    options = listOf(lang.greeting, "12345", "Goodnight Airport", "Ticket Counter"),
                    correctAnswer = lang.greeting,
                    explanation = "'${lang.greeting}' means '${lang.greetingMeaning}' in ${lang.name}!",
                    keyVocabWord = lang.greeting,
                    keyVocabMeaning = lang.greetingMeaning
                ),
                ExerciseQuestion(
                    id = "l2",
                    type = ExerciseType.TRUE_FALSE,
                    prompt = "True or False: '${lang.scriptSample}' shows characters/letters used in ${lang.name}.",
                    audioPhrase = lang.greeting,
                    options = listOf("True", "False"),
                    correctAnswer = "True",
                    explanation = "${lang.name} (${lang.nativeName}) uses '${lang.scriptSample}' in its writing system.",
                    keyVocabWord = lang.nativeName,
                    keyVocabMeaning = "${lang.name} native name"
                ),
                ExerciseQuestion(
                    id = "l3",
                    type = ExerciseType.TRANSLATE,
                    prompt = "Select the meaning of '${lang.greeting}':",
                    audioPhrase = lang.greeting,
                    options = listOf(
                        lang.greetingMeaning,
                        "Where is the train?",
                        "The weather is cold.",
                        "Please close the window."
                    ),
                    correctAnswer = lang.greetingMeaning,
                    explanation = "Great job mastering the signature ${lang.name} greeting!",
                    keyVocabWord = lang.greeting,
                    keyVocabMeaning = lang.greetingMeaning
                )
            )
        ),
        LessonNode(
            id = "${lang.name.lowercase()}_u2",
            index = 2,
            unitTitle = "Unit 2 • Travel & Café Conversations",
            title = "Exploring Cities in ${lang.name}",
            subtitle = "Directions, café orders & friendly small talk",
            tier = "Intermediate",
            category = "Travel",
            xpReward = 35,
            gemReward = 20,
            iconEmoji = "🗺️",
            questions = listOf(
                ExerciseQuestion(
                    id = "l4",
                    type = ExerciseType.MULTIPLE_CHOICE,
                    prompt = "When traveling where ${lang.name} is spoken, what is the smartest way to start asking for directions?",
                    audioPhrase = lang.greeting,
                    options = listOf(
                        "Start with a polite greeting: ${lang.greeting}",
                        "Shout in a hurry without greeting",
                        "Walk away silently",
                        "Ignore local signs"
                    ),
                    correctAnswer = "Start with a polite greeting: ${lang.greeting}",
                    explanation = "Starting with '${lang.greeting}' shows cultural respect everywhere ${lang.name} is spoken!",
                    keyVocabWord = "Polite Greeting",
                    keyVocabMeaning = lang.greeting
                ),
                ExerciseQuestion(
                    id = "l5",
                    type = ExerciseType.TRUE_FALSE,
                    prompt = "True or False: You can practice live ${lang.name} roleplay anytime with Noxra AI!",
                    audioPhrase = lang.greeting,
                    options = listOf("True", "False"),
                    correctAnswer = "True",
                    explanation = "Open the Noxra AI tab anytime to generate unlimited ${lang.name} conversations and quizzes!",
                    keyVocabWord = lang.nativeName,
                    keyVocabMeaning = "${lang.name} fluency"
                )
            )
        ),
        LessonNode(
            id = "${lang.name.lowercase()}_u3",
            index = 3,
            unitTitle = "Unit 3 • Advanced Fluency & Culture",
            title = "${lang.name} Stories & Idioms",
            subtitle = "Express opinions, stories & natural expressions",
            tier = "Advanced",
            category = "Story",
            xpReward = 45,
            gemReward = 25,
            iconEmoji = "🌟",
            questions = listOf(
                ExerciseQuestion(
                    id = "l6",
                    type = ExerciseType.MULTIPLE_CHOICE,
                    prompt = "Which daily habit accelerates ${lang.name} retention through spaced repetition?",
                    audioPhrase = lang.greeting,
                    options = listOf(
                        "Reviewing saved vocabulary & speaking aloud for 10 minutes daily",
                        "Studying once every six months",
                        "Skipping listening practice",
                        "Avoiding full sentences"
                    ),
                    correctAnswer = "Reviewing saved vocabulary & speaking aloud for 10 minutes daily",
                    explanation = "Short daily active recall + speaking aloud builds lasting neural pathways!",
                    keyVocabWord = "Spaced Repetition",
                    keyVocabMeaning = "स्मार्ट दोहराव प्रणाली"
                )
            )
        ),
        LessonNode(
            id = "${lang.name.lowercase()}_u4_test",
            index = 4,
            unitTitle = "Unit 3 • Advanced Fluency & Culture",
            title = "${lang.name} Unit-End Checkpoint",
            subtitle = "Verify listening, greeting recall & cultural fluency",
            tier = "Advanced",
            category = "Unit Test",
            xpReward = 55,
            gemReward = 30,
            iconEmoji = "🎓",
            questions = listOf(
                ExerciseQuestion(
                    id = "lt1",
                    type = ExerciseType.LISTEN_AND_TYPE,
                    prompt = "Listen to the ${lang.name} greeting or check its English meaning, then type '${lang.greetingMeaning}':",
                    audioPhrase = lang.greeting,
                    options = emptyList(),
                    correctAnswer = lang.greetingMeaning,
                    explanation = "'${lang.greeting}' translates directly to '${lang.greetingMeaning}'.",
                    keyVocabWord = lang.greeting,
                    keyVocabMeaning = lang.greetingMeaning
                ),
                ExerciseQuestion(
                    id = "lt2",
                    type = ExerciseType.MULTIPLE_CHOICE,
                    prompt = "Which script sample belongs to ${lang.name} (${lang.nativeName})?",
                    audioPhrase = lang.greeting,
                    options = listOf(lang.scriptSample, "010101", "XYZ-999", "###"),
                    correctAnswer = lang.scriptSample,
                    explanation = "${lang.name} writes with characters such as '${lang.scriptSample}'.",
                    keyVocabWord = lang.nativeName,
                    keyVocabMeaning = "${lang.name} script"
                )
            )
        )
    )

    val grammarTopics: List<GrammarTopic> = listOf(
        GrammarTopic(
            id = "g_present_perfect",
            title = "Present Perfect vs. Simple Past",
            level = "Beginner → Intermediate",
            summary = "Connect past experiences to the present moment vs. finished past timestamps.",
            formula = "Subject + have/has + Past Participle (V3)",
            examples = listOf(
                "I have visited Delhi three times." to "मैं तीन बार दिल्ली जा चुका हूँ। (Life experience)",
                "I visited Delhi last Sunday." to "मैं पिछले रविवार दिल्ली गया था। (Specific past time)"
            ),
            commonMistakeTip = "Avoid pairing 'have/has + V3' with 'yesterday' or 'last week' — use Simple Past instead!"
        ),
        GrammarTopic(
            id = "g_articles",
            title = "Parts of Speech & Articles (A, An, The)",
            level = "Beginner",
            summary = "Use 'a/an' for general singular nouns (by vowel sound!) and 'the' for specific shared items.",
            formula = "an + vowel sound (an hour, an idea) | a + consonant sound (a university)",
            examples = listOf(
                "She is an honest developer." to "वह एक ईमानदार डेवलपर है। ('h' is silent!)",
                "Please pass me the blue notebook." to "कृपया मुझे वह नीली नोटबुक दें।"
            ),
            commonMistakeTip = "Listen to the FIRST SOUND, not just the first letter: 'a university' (yoo-sound), 'an hour' (ow-sound)."
        ),
        GrammarTopic(
            id = "g_conditionals",
            title = "Sentence Structure & Conditionals (If Clauses)",
            level = "Intermediate",
            summary = "Express real possibilities and hypothetical dreams with crisp clause balance.",
            formula = "First Conditional: If + Present Simple, will + Base Verb",
            examples = listOf(
                "If I practice daily, my fluency will skyrocket." to "यदि मैं रोज़ अभ्यास करूँगा, तो मेरी भाषा शानदार हो जाएगी।",
                "If I had wings, I would fly around the world." to "अगर मेरे पंख होते, तो मैं पूरी दुनिया में उड़ता।"
            ),
            commonMistakeTip = "Never put 'will' inside the 'If' part! Say 'If it rains, we will stay inside' (NOT 'If it will rain')."
        ),
        GrammarTopic(
            id = "g_idioms_slang",
            title = "Everyday Idioms, Phrasal Verbs & Polite Tone",
            level = "Advanced",
            summary = "Sound natural in casual chats, travel, and global workplace conversations.",
            formula = "Break the ice • Piece of cake • Hit the books • Keep me posted",
            examples = listOf(
                "This grammar quiz was a piece of cake!" to "यह व्याकरण क्विज़ बहुत आसान था!",
                "Please keep me posted on the project launch." to "कृपया मुझे प्रोजेक्ट लॉन्च के बारे में अपडेट देते रहें।"
            ),
            commonMistakeTip = "Idioms carry figurative meanings — translate the whole idea, not word-by-word."
        )
    )

    val builtInDictionary: List<DictionaryWord> = listOf(
        DictionaryWord(
            word = "Resilient",
            language = "English",
            phonetic = "/rɪˈzɪl.jənt/",
            partOfSpeech = "Adjective",
            meaningHindi = "लचीला / कठिनाइयों से उबरने वाला",
            meaningGlobal = "Able to recover quickly from challenges and setbacks.",
            synonyms = listOf("Strong", "Adaptable", "Tenacious"),
            antonyms = listOf("Fragile", "Weak"),
            conjugations = listOf("Resilience (Noun)", "Resiliently (Adverb)"),
            exampleSentence = "Young innovators stay resilient when debugging complex code.",
            exampleTranslation = "युवा नवप्रवर्तक जटिल कोड सुधारते समय अडिग रहते हैं।"
        ),
        DictionaryWord(
            word = "Collaborate",
            language = "English",
            phonetic = "/kəˈlæb.ə.reɪt/",
            partOfSpeech = "Verb",
            meaningHindi = "सहयोग करना / मिलकर काम करना",
            meaningGlobal = "Work jointly with others on an activity or shared vision.",
            synonyms = listOf("Cooperate", "Team up", "Partner"),
            antonyms = listOf("Compete alone", "Isolate"),
            conjugations = listOf("Collaborates", "Collaborated", "Collaborating"),
            exampleSentence = "Students from twelve countries collaborate inside our study group.",
            exampleTranslation = "बारह देशों के छात्र हमारे अध्ययन समूह में मिलकर सीखते हैं।"
        ),
        DictionaryWord(
            word = "Eloquent",
            language = "English",
            phonetic = "/ˈel.ə.kwənt/",
            partOfSpeech = "Adjective",
            meaningHindi = "प्रभावशाली और स्पष्ट वक्ता",
            meaningGlobal = "Fluent, persuasive, and expressive in speaking or writing.",
            synonyms = listOf("Articulate", "Expressive", "Well-spoken"),
            antonyms = listOf("Hesitant", "Inarticulate"),
            conjugations = listOf("Eloquence (Noun)", "Eloquently (Adverb)"),
            exampleSentence = "Her eloquent speech inspired the entire classroom.",
            exampleTranslation = "उनके प्रभावशाली भाषण ने पूरी कक्षा को प्रेरित किया।"
        ),
        DictionaryWord(
            word = "Curiosity",
            language = "English",
            phonetic = "/ˌkjʊə.riˈɒs.ə.ti/",
            partOfSpeech = "Noun",
            meaningHindi = "जिज्ञासा / जानने की इच्छा",
            meaningGlobal = "A strong desire to learn, explore, and understand something new.",
            synonyms = listOf("Inquisitiveness", "Wonder", "Interest"),
            antonyms = listOf("Indifference", "Apathy"),
            conjugations = listOf("Curious (Adjective)", "Curiously (Adverb)"),
            exampleSentence = "Curiosity is the spark behind every great invention.",
            exampleTranslation = "जिज्ञासा हर महान आविष्कार के पीछे की चिंगारी है।"
        ),
        DictionaryWord(
            word = "¡Vamos!",
            language = "Spanish",
            phonetic = "/ˈba.mos/",
            partOfSpeech = "Exclamation / Verb (Ir)",
            meaningHindi = "चलो चलें! / आगे बढ़ो!",
            meaningGlobal = "Let's go! Come on! (1st person plural of 'ir' - to go).",
            synonyms = listOf("¡Adelante!", "¡Ánimo!"),
            antonyms = listOf("Detenerse"),
            conjugations = listOf("Voy (I go)", "Vas (You go)", "Va (He/She goes)", "Vamos (We go)"),
            exampleSentence = "¡Vamos a practicar español todos los días!",
            exampleTranslation = "Let's practice Spanish every single day!"
        ),
        DictionaryWord(
            word = "Bonjour",
            language = "French",
            phonetic = "/bɔ̃.ʒuʁ/",
            partOfSpeech = "Interjection",
            meaningHindi = "नमस्ते / सुप्रभात (फ़्रेंच)",
            meaningGlobal = "Good day / Hello — universal polite daytime greeting in French.",
            synonyms = listOf("Salut (informal)", "Bonsoir (evening)"),
            antonyms = listOf("Au revoir (goodbye)"),
            conjugations = listOf("Invariant greeting"),
            exampleSentence = "Bonjour ! Je voudrais un croissant, s'il vous plaît.",
            exampleTranslation = "Hello! I would like a croissant, please."
        ),
        DictionaryWord(
            word = "ありがとう (Arigatō)",
            language = "Japanese",
            phonetic = "/a.ɾi.ɡa.toː/",
            partOfSpeech = "Expression",
            meaningHindi = "धन्यवाद (जापानी)",
            meaningGlobal = "Thank you! Use 'Arigatō gozaimasu' for extra politeness.",
            synonyms = listOf("ありがとうございます (Polite)", "感謝 (Gratitude)"),
            antonyms = listOf("どういたしまして (You're welcome)"),
            conjugations = listOf("ありがとうございます (Formal)"),
            exampleSentence = "いつも教えてくれてありがとうございます。",
            exampleTranslation = "Thank you very much for always teaching me."
        )
    )

    val speakingDrills: List<SpeakingDrill> = listOf(
        SpeakingDrill(
            id = "sp1",
            scenario = "☕ Ordering Food & Drinks",
            phrase = "Could I have an iced coffee and a veggie sandwich, please?",
            phoneticHint = "kood eye hav an ayst KAW-fee and a VEJ-ee SAND-wich pleez",
            translation = "क्या मुझे एक आइस्ड कॉफ़ी और वेज सैंडविच मिल सकता है?",
            difficulty = "Beginner"
        ),
        SpeakingDrill(
            id = "sp2",
            scenario = "🛍️ Shopping Conversation",
            phrase = "Excuse me, do you have this jacket in a medium size?",
            phoneticHint = "ik-SKYOOZ mee, doo yoo hav this JAK-it in a MEE-dee-uhm sayz",
            translation = "माफ़ कीजिए, क्या आपके पास यह जैकेट मीडियम साइज़ में है?",
            difficulty = "Beginner"
        ),
        SpeakingDrill(
            id = "sp3",
            scenario = "✈️ Airport & Travel",
            phrase = "Which platform does the express train to the city center depart from?",
            phoneticHint = "wich PLAT-form duz thee ik-SPRES trayn too thuh SIT-ee SEN-ter di-PART from",
            translation = "सिटी सेंटर जाने वाली एक्सप्रेस ट्रेन किस प्लेटफ़ॉर्म से छूटती है?",
            difficulty = "Intermediate"
        ),
        SpeakingDrill(
            id = "sp4",
            scenario = "💼 Job & Tech Interview",
            phrase = "I enjoy building technology that helps students learn faster every day.",
            phoneticHint = "eye in-JOY BIL-ding tek-NOL-uh-jee that helps STOO-dents lurn FAS-ter EV-ree day",
            translation = "मुझे ऐसी तकनीक बनाना पसंद है जो छात्रों को हर दिन तेज़ी से सीखने में मदद करे।",
            difficulty = "Advanced"
        )
    )

    fun speakingDrillsForLanguage(languageName: String): List<SpeakingDrill> {
        return when (languageName.lowercase()) {
            "spanish" -> listOf(
                SpeakingDrill(
                    id = "es_sp1",
                    scenario = "☕ Café & Ordering",
                    phrase = "Hola, ¿me podría traer un café con leche y un pan dulce, por favor?",
                    phoneticHint = "OH-lah, meh poh-DREE-ah trah-EHR oon kah-FEH kohn LEH-cheh ee oon pahn DOOL-seh, por fah-VOR",
                    translation = "Hello, could you bring me a coffee with milk and a sweet roll, please?",
                    difficulty = "Beginner"
                ),
                SpeakingDrill(
                    id = "es_sp2",
                    scenario = "✈️ Travel & Directions",
                    phrase = "Disculpe, ¿dónde está la estación de tren más cercana?",
                    phoneticHint = "dees-KOOL-peh, DOHN-deh ehs-TAH lah ehs-tah-SYOHN deh trehn mahs sehr-KAH-nah",
                    translation = "Excuse me, where is the nearest train station?",
                    difficulty = "Beginner"
                ),
                SpeakingDrill(
                    id = "es_sp3",
                    scenario = "💼 Career & Goals",
                    phrase = "Estoy aprendiendo español todos los días para comunicarme con confianza.",
                    phoneticHint = "ehs-TOY ah-prehn-DYEHN-doh ehs-pah-NYOHL TOH-dohs lohs DEE-ahs PAH-rah koh-moo-nee-KAR-meh kohn kohn-FYAHN-sah",
                    translation = "I am learning Spanish every day to communicate with confidence.",
                    difficulty = "Intermediate"
                ),
                SpeakingDrill(
                    id = "es_sp4",
                    scenario = "🎭 Rolling R Accent Drill",
                    phrase = "El perro rápido corre alrededor del ferrocarril.",
                    phoneticHint = "ehl PEH-rroh RRAH-pee-doh KOH-rreh ahl-rreh-deh-DOR dehl feh-rroh-kah-RREEL",
                    translation = "The fast dog runs around the railway.",
                    difficulty = "Advanced"
                )
            )
            "french" -> listOf(
                SpeakingDrill(
                    id = "fr_sp1",
                    scenario = "🥐 Parisian Bakery",
                    phrase = "Bonjour, je voudrais un croissant chaud et un café crème, s'il vous plaît.",
                    phoneticHint = "bohn-ZHOOR, zhuh voo-DREH uhn krwah-SAHN shoh eh uhn kah-FEH krehm, seel voo PLEH",
                    translation = "Hello, I would like a warm croissant and a cream coffee, please.",
                    difficulty = "Beginner"
                ),
                SpeakingDrill(
                    id = "fr_sp2",
                    scenario = "🚇 Metro & Travel",
                    phrase = "Excusez-moi, comment puis-je aller au musée du Louvre ?",
                    phoneticHint = "ehk-skew-ZEH mwah, koh-MAHN pweezh ah-LEH oh mew-ZEH dew LOOVR",
                    translation = "Excuse me, how can I get to the Louvre Museum?",
                    difficulty = "Intermediate"
                ),
                SpeakingDrill(
                    id = "fr_sp3",
                    scenario = "🗣️ Everyday Fluency",
                    phrase = "Pratiquer à voix haute chaque jour améliore ma prononciation.",
                    phoneticHint = "prah-tee-KEH ah vwah oht shahk zhoor ah-meh-LYOR mah proh-nohn-syah-SYOHN",
                    translation = "Practicing aloud every day improves my pronunciation.",
                    difficulty = "Advanced"
                )
            )
            "hindi" -> listOf(
                SpeakingDrill(
                    id = "hi_sp1",
                    scenario = "☕ चाय और बातचीत (Café)",
                    phrase = "नमस्ते, कृपया मुझे एक मसाला चाय और समोसा दीजिए।",
                    phoneticHint = "Namaste, kripya mujhe ek masala chai aur samosa dijiye.",
                    translation = "Hello, please give me one masala tea and a samosa.",
                    difficulty = "Beginner"
                ),
                SpeakingDrill(
                    id = "hi_sp2",
                    scenario = "✈️ यात्रा और दिशा (Travel)",
                    phrase = "माफ़ कीजिए, सबसे नज़दीकी मेट्रो स्टेशन किस तरफ़ है?",
                    phoneticHint = "Maaf kijiye, sabse nazdeeki metro station kis taraf hai?",
                    translation = "Excuse me, which way is the nearest metro station?",
                    difficulty = "Intermediate"
                ),
                SpeakingDrill(
                    id = "hi_sp3",
                    scenario = "💼 आत्मविश्वास (Fluency)",
                    phrase = "नियमित अभ्यास से मेरा उच्चारण हर दिन बेहतर हो रहा है।",
                    phoneticHint = "Niyamit abhyas se mera uchcharan har din behtar ho raha hai.",
                    translation = "With regular practice, my pronunciation is getting better every day.",
                    difficulty = "Advanced"
                )
            )
            "german" -> listOf(
                SpeakingDrill(
                    id = "de_sp1",
                    scenario = "🥨 Café in Berlin",
                    phrase = "Guten Morgen, ich hätte gern einen Kaffee und eine frische Brezel.",
                    phoneticHint = "GOO-ten MOR-gen, ikh HET-uh gern EYE-nen KAF-eh oont EYE-nuh FRISH-uh BRET-sel",
                    translation = "Good morning, I would like a coffee and a fresh pretzel.",
                    difficulty = "Beginner"
                ),
                SpeakingDrill(
                    id = "de_sp2",
                    scenario = "🚆 Train Station",
                    phrase = "Entschuldigung, wann fährt der nächste Zug nach München ab?",
                    phoneticHint = "ent-SHOOL-di-goong, vahn fehrt dehr NEHKH-stuh tsook nahkh MEWN-khen ahp",
                    translation = "Excuse me, when does the next train to Munich depart?",
                    difficulty = "Intermediate"
                )
            )
            "japanese" -> listOf(
                SpeakingDrill(
                    id = "ja_sp1",
                    scenario = "🍵 Tokyo Café",
                    phrase = "すみません、抹茶ラテを一つお願いします。",
                    phoneticHint = "Sumimasen, matcha rate o hitotsu onegaishimasu.",
                    translation = "Excuse me, one matcha latte, please.",
                    difficulty = "Beginner"
                ),
                SpeakingDrill(
                    id = "ja_sp2",
                    scenario = "🚅 Shinkansen Travel",
                    phrase = "東京駅へ行く電車は何番線ですか？",
                    phoneticHint = "Tōkyō-eki e iku densha wa nan-bansen desu ka?",
                    translation = "Which platform is the train bound for Tokyo Station?",
                    difficulty = "Intermediate"
                )
            )
            "korean" -> listOf(
                SpeakingDrill(
                    id = "ko_sp1",
                    scenario = "☕ Seoul Coffee Shop",
                    phrase = "안녕하세요, 아이스 아메리카노 한 잔 주세요.",
                    phoneticHint = "Annyeonghaseyo, aiseu amerikano han jan juseyo.",
                    translation = "Hello, please give me one iced Americano.",
                    difficulty = "Beginner"
                ),
                SpeakingDrill(
                    id = "ko_sp2",
                    scenario = "🚇 Subway Directions",
                    phrase = "실례합니다, 가장 가까운 지하철역이 어디에 있나요?",
                    phoneticHint = "Sillyehamnida, gajang gakkaun jihacheol-yeogi eodie innayo?",
                    translation = "Excuse me, where is the nearest subway station?",
                    difficulty = "Intermediate"
                )
            )
            else -> speakingDrills + listOf(
                SpeakingDrill(
                    id = "sp5",
                    scenario = "🎭 Intonation & Rhythm Twister",
                    phrase = "Thoughtful speakers practice clear pronunciation through steady daily habits.",
                    phoneticHint = "THAWT-fuhl SPEE-kerz PRAK-tis kleer proh-nun-see-AY-shuhn throo STED-ee DAY-lee HAB-its",
                    translation = "विचारशील वक्ता नियमित दैनिक आदतों के माध्यम से स्पष्ट उच्चारण का अभ्यास करते हैं।",
                    difficulty = "Advanced"
                )
            )
        }
    }

    val schoolSubjectChallenges: List<SchoolSubjectChallenge> = listOf(
        SchoolSubjectChallenge(
            id = "sci_1",
            subject = "Science",
            gradeBand = "Class 6–8",
            difficulty = "Easy",
            topic = "Photosynthesis & Plant Nutrition",
            question = "Which gas do green plants absorb from the atmosphere during photosynthesis?",
            options = listOf("Carbon dioxide (CO₂)", "Oxygen (O₂)", "Nitrogen (N₂)", "Hydrogen (H₂)"),
            correctAnswer = "Carbon dioxide (CO₂)",
            explanation = "Chlorophyll in leaves uses sunlight to convert Carbon dioxide (CO₂) and water into glucose while releasing Oxygen."
        ),
        SchoolSubjectChallenge(
            id = "sci_2",
            subject = "Science",
            gradeBand = "Class 9–10",
            difficulty = "Medium",
            topic = "Laws of Motion & Physics",
            question = "A 5 kg object accelerates at 3 m/s². What is the net force acting on it (F = m × a)?",
            options = listOf("15 N", "8 N", "1.67 N", "45 N"),
            correctAnswer = "15 N",
            explanation = "By Newton's Second Law, Force F = mass × acceleration = 5 kg × 3 m/s² = 15 Newtons (N)."
        ),
        SchoolSubjectChallenge(
            id = "sci_3",
            subject = "Science",
            gradeBand = "Class 11–12",
            difficulty = "Hard",
            topic = "Chemical Bonding & Thermodynamics",
            question = "For a spontaneous process at constant temperature and pressure, Gibbs free energy change (ΔG) must be:",
            options = listOf("Negative (ΔG < 0)", "Positive (ΔG > 0)", "Zero (ΔG = 0)", "Infinite"),
            correctAnswer = "Negative (ΔG < 0)",
            explanation = "ΔG = ΔH − TΔS. A process occurs spontaneously at constant T and P only when ΔG < 0."
        ),
        SchoolSubjectChallenge(
            id = "math_1",
            subject = "Mathematics",
            gradeBand = "Class 6–8",
            difficulty = "Easy",
            topic = "Ratios & Mensuration",
            question = "The perimeter of a rectangle with length 12 cm and width 8 cm is:",
            options = listOf("40 cm", "20 cm", "96 cm", "48 cm"),
            correctAnswer = "40 cm",
            explanation = "Perimeter of a rectangle = 2 × (length + width) = 2 × (12 + 8) = 40 cm."
        ),
        SchoolSubjectChallenge(
            id = "math_2",
            subject = "Mathematics",
            gradeBand = "Class 9–10",
            difficulty = "Medium",
            topic = "Quadratic Equations & Trigonometry",
            question = "What are the roots of x² − 5x + 6 = 0?",
            options = listOf("x = 2 and x = 3", "x = −2 and x = −3", "x = 1 and x = 6", "x = −1 and x = 6"),
            correctAnswer = "x = 2 and x = 3",
            explanation = "Factoring gives (x − 2)(x − 3) = 0, so x = 2 or x = 3."
        ),
        SchoolSubjectChallenge(
            id = "math_3",
            subject = "Mathematics",
            gradeBand = "Class 11–12",
            difficulty = "Hard",
            topic = "Calculus • Derivatives",
            question = "What is the derivative d/dx of f(x) = 3x² + 4x − 7?",
            options = listOf("6x + 4", "3x + 4", "6x − 7", "x³ + 2x²"),
            correctAnswer = "6x + 4",
            explanation = "Using the power rule d/dx(xⁿ) = n·xⁿ⁻¹: d/dx(3x²) = 6x, d/dx(4x) = 4, and constant −7 differentiates to 0."
        ),
        SchoolSubjectChallenge(
            id = "eng_1",
            subject = "English Grammar",
            gradeBand = "Class 6–8",
            difficulty = "Easy",
            topic = "Active & Passive Voice",
            question = "Choose the passive voice of: 'Riya wrote a thoughtful essay.'",
            options = listOf(
                "A thoughtful essay was written by Riya.",
                "A thoughtful essay is write by Riya.",
                "Riya was writing a thoughtful essay.",
                "A thoughtful essay has wrote by Riya."
            ),
            correctAnswer = "A thoughtful essay was written by Riya.",
            explanation = "Simple Past active ('wrote') converts to 'was/were + past participle' ('was written')."
        ),
        SchoolSubjectChallenge(
            id = "eng_2",
            subject = "English Grammar",
            gradeBand = "Class 9–10",
            difficulty = "Medium",
            topic = "Subject-Verb Concord",
            question = "Neither the teacher nor the students ___ late for the morning assembly.",
            options = listOf("were", "was", "is", "has been"),
            correctAnswer = "were",
            explanation = "With 'Neither...nor', the verb agrees with the closer subject ('students' → plural 'were')."
        ),
        SchoolSubjectChallenge(
            id = "hin_1",
            subject = "Hindi",
            gradeBand = "Class 6–8",
            difficulty = "Easy",
            topic = "व्याकरण • संधि और समास",
            question = "'विद्या + आलय' की सही संधि क्या है?",
            options = listOf("विद्यालय", "विद्यलय", "विद्याआलय", "विदालय"),
            correctAnswer = "विद्यालय",
            explanation = "दीर्घ स्वर संधि के नियम से 'आ + आ = आ' मिलकर 'विद्यालय' बनता है।"
        ),
        SchoolSubjectChallenge(
            id = "hin_2",
            subject = "Hindi",
            gradeBand = "Class 9–10",
            difficulty = "Medium",
            topic = "मुहावरे और अलंकार",
            question = "'आकाश-पाताल एक करना' मुहावरे का सही अर्थ क्या है?",
            options = listOf("अत्यधिक कठिन परिश्रम करना", "आसमान की ओर देखना", "आराम करना", "यात्रा पर जाना"),
            correctAnswer = "अत्यधिक कठिन परिश्रम करना",
            explanation = "'आकाश-पाताल एक करना' का अर्थ किसी लक्ष्य के लिए जी-तोड़ मेहनत करना है।"
        ),
        SchoolSubjectChallenge(
            id = "sst_1",
            subject = "Social Studies",
            gradeBand = "Class 6–8",
            difficulty = "Easy",
            topic = "Civics & Indian Constitution",
            question = "Who chaired the Drafting Committee of the Constitution of India?",
            options = listOf("Dr. B. R. Ambedkar", "Mahatma Gandhi", "Subhas Chandra Bose", "Rabindranath Tagore"),
            correctAnswer = "Dr. B. R. Ambedkar",
            explanation = "Dr. B. R. Ambedkar served as Chairman of the Constitutional Drafting Committee set up in August 1947."
        ),
        SchoolSubjectChallenge(
            id = "sst_2",
            subject = "Social Studies",
            gradeBand = "Class 9–10",
            difficulty = "Medium",
            topic = "Geography & Sustainable Resources",
            question = "Black soil (Regur soil) found across the Deccan Trap is especially ideal for cultivating:",
            options = listOf("Cotton", "Tea", "Apples", "Jute"),
            correctAnswer = "Cotton",
            explanation = "Black soil retains moisture well and is rich in calcium carbonate and potash, making it ideal for cotton."
        ),
        SchoolSubjectChallenge(
            id = "cs_1",
            subject = "Computer Science",
            gradeBand = "Class 9–10",
            difficulty = "Easy",
            topic = "Algorithms & Data Structures",
            question = "Which data structure follows the LIFO (Last-In, First-Out) principle?",
            options = listOf("Stack", "Queue", "Linked List", "Hash Table"),
            correctAnswer = "Stack",
            explanation = "A Stack pushes and pops items from the top, so the last element added is the first removed (LIFO)."
        ),
        SchoolSubjectChallenge(
            id = "cs_2",
            subject = "Computer Science",
            gradeBand = "Class 11–12",
            difficulty = "Hard",
            topic = "Time Complexity & Binary Search",
            question = "What is the worst-case time complexity of Binary Search on a sorted array of N elements?",
            options = listOf("O(log N)", "O(N)", "O(N log N)", "O(N²)"),
            correctAnswer = "O(log N)",
            explanation = "Binary Search halves the search space on every comparison, taking at most log₂(N) steps."
        )
    )

    fun leagueForXp(xp: Long): String = when {
        xp >= 1500L -> "Emerald"
        xp >= 800L -> "Sapphire"
        xp >= 400L -> "Gold"
        xp >= 150L -> "Silver"
        else -> "Bronze"
    }
}
