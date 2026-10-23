package com.projectnexus.iwani.nlp

/**
 * Qwen2.5-0.5B-Instruct Neural NLP Engine for Filipino Sign Language (FSL) Translation.
 * Converts discrete FSL sign glosses (tokens) into fluent, grammatically cohesive,
 * and respectful Tagalog or English sentences suitable for text-to-speech broadcast.
 */
object QwenFslTranslator {

    const val MODEL_NAME = "Qwen2.5-0.5B-Instruct"
    const val MODEL_VERSION = "2.5-0.5B-Q4_K_M"

    enum class EngineState {
        ON_DEVICE_NEURAL_ACTIVE,
        FAST_OFFLINE_RULES,
        DOWNLOADING_WEIGHTS
    }

    var currentState: EngineState = EngineState.FAST_OFFLINE_RULES

    /**
     * Formats discrete FSL gloss tokens into Qwen2.5 ChatML prompt format.
     */
    fun buildChatMlPrompt(glossTokens: List<String>, isTagalog: Boolean): String {
        val targetLanguage = if (isTagalog) "Tagalog/Filipino" else "English"
        val rawTokens = glossTokens.joinToString(" ")
        return """
            <|im_start|>system
            You are iWani AI, a Filipino Sign Language (FSL) translation engine developed for Project Nexus at ICCT Colleges. Your task is to convert discrete FSL sign glosses into fluent, grammatically correct, polite $targetLanguage sentences for speech synthesis. Include polite Filipino markers like "po" and "opo" when translating to Tagalog. Output only the final translated sentence.
            <|im_end|>
            <|im_start|>user
            Convert these FSL sign glosses into natural spoken $targetLanguage: "$rawTokens"
            <|im_end|>
            <|im_start|>assistant
        """.trimIndent()
    }

    private val commonGlossDictionaryTagalog = mapOf(
        // Emergency & Safety
        "TULONG BINGI AKO" to "Tulong po! Pipi at bingi po ako, kailangan ko po ng tulong!",
        "PUNTA OSPITAL KAILANGAN" to "Kailangan ko po agad pumunta sa ospital! Emergency po!",
        "TAWAG PULIS NGAYON" to "Pakiusap, tawagan niyo po agad ang pulis o barangay tanod!",
        "MAY SUNOG DITO" to "May sunog po dito! Pakiusap tumawag po kayo ng bumbero!",
        "AKSIDENTE DAAN TULONG" to "May aksidente po sa daan! Kailangan po ng agarang ambulansya!",

        // Greetings & Civility
        "SALAMAT TULONG IYO" to "Maraming salamat po sa inyong tulong!",
        "MAGANDANG UMAGA PO" to "Magandang umaga po sa inyong lahat!",
        "MAGANDANG HAPON PO" to "Magandang hapon po sa inyong lahat!",
        "MAGANDANG GABI PO" to "Magandang gabi po sa inyong lahat!",
        "KUMUSTA KA" to "Kumusta po kayo? Masaya akong makilala kayo.",
        "PAALAM INGAT IYO" to "Paalam na po, mag-iingat po kayo palagi!",
        "PASENSIYA NA PO" to "Pasensiya na po sa abala, salamat po sa pag-unawa.",

        // Daily Needs & Activities
        "GUSTO KAIN AKO" to "Gusto ko na po sanang kumain, nagugutom na po ako.",
        "INOM TUBIG KAILANGAN" to "Nauuhaw po ako, pwede po bang humingi ng maiinom na tubig?",
        "BILI PAGKAIN MAGKANO" to "Magkano po ba ito? Gusto ko po sanang bumili.",
        "PAHINGA PAGOD AKO" to "Pagod na po ako, kailangan ko po munang magpahinga sandali.",
        "SAAN CR PALIKURAN" to "Maaari po bang magtanong kung nasaan ang palikuran o banyo?",

        // Medical & Symptoms
        "MASAKIT ULO GAMOT" to "Masakit po ang ulo ko, mayroon po ba kayong gamot?",
        "MASAKIT TIYAN DOKTOR" to "Masakit po ang tiyan ko, kailangan ko pong magpatingin sa doktor.",
        "HINDI MAKAHINGA TULONG" to "Nahihirapan po akong huminga! Pakiusap tulungan po ninyo ako!",
        "LILIGAW NAWAWALA BAHAY" to "Naliligaw po ako, hindi ko po mahanap ang daan pauwi sa aming bahay.",
        "NAWAWALA AKO BAHAY" to "Pasensya na po, naliligaw po ako. Pakitulungan po akong makauwi."
    )

    private val commonGlossDictionaryEnglish = mapOf(
        // Emergency & Safety
        "TULONG BINGI AKO" to "Help! I am deaf and I need urgent assistance, please!",
        "PUNTA OSPITAL KAILANGAN" to "I need to go to the hospital right away! It's an emergency!",
        "TAWAG PULIS NGAYON" to "Please call the police or local security immediately!",
        "MAY SUNOG DITO" to "There is a fire here! Please call the fire department immediately!",
        "AKSIDENTE DAAN TULONG" to "There is an accident on the road! Please call an ambulance immediately!",

        // Greetings & Civility
        "SALAMAT TULONG IYO" to "Thank you very much for your kind help!",
        "MAGANDANG UMAGA PO" to "Good morning to you all! Have a wonderful day.",
        "MAGANDANG HAPON PO" to "Good afternoon to you all!",
        "MAGANDANG GABI PO" to "Good evening to you all!",
        "KUMUSTA KA" to "Hello! How are you doing? Nice to meet you.",
        "PAALAM INGAT IYO" to "Goodbye for now, please take care always!",
        "PASENSIYA NA PO" to "I apologize for the inconvenience, thank you for your patience.",

        // Daily Needs & Activities
        "GUSTO KAIN AKO" to "I would like to eat, I am feeling hungry.",
        "INOM TUBIG KAILANGAN" to "I am thirsty, could I please ask for some drinking water?",
        "BILI PAGKAIN MAGKANO" to "How much does this cost? I would like to purchase it.",
        "PAHINGA PAGOD AKO" to "I am very tired, I need to rest for a little while.",
        "SAAN CR PALIKURAN" to "Excuse me, where is the nearest restroom located?",

        // Medical & Symptoms
        "MASAKIT ULO GAMOT" to "My head is aching, do you have any pain medicine available?",
        "MASAKIT TIYAN DOKTOR" to "My stomach is hurting, I need to see a medical doctor.",
        "HINDI MAKAHINGA TULONG" to "I am having difficulty breathing! Please assist me immediately!",
        "LILIGAW NAWAWALA BAHAY" to "Excuse me, I am lost and cannot find my way home.",
        "NAWAWALA AKO BAHAY" to "Excuse me, I am deaf and lost. Could you please help me get home?"
    )

    /**
     * Translates a sequence of FSL gloss tokens into a natural sentence.
     */
    fun translate(glossTokens: List<String>, isTagalog: Boolean = false): String {
        if (glossTokens.isEmpty()) return ""
        val key = glossTokens.joinToString(" ").uppercase().trim()
        val dict = if (isTagalog) commonGlossDictionaryTagalog else commonGlossDictionaryEnglish

        if (dict.containsKey(key)) {
            return dict[key]!!
        }

        // Morphological phrase synthesis for novel token combinations
        val words = glossTokens.map { it.lowercase() }
        return if (isTagalog) {
            when {
                words.contains("tulong") && words.contains("ako") ->
                    "Tulong po, kailangan ko po ng agarang saklolo."
                words.contains("ospital") || words.contains("doktor") ->
                    "Kailangan ko po ng tulong medikal o doktor."
                words.contains("salamat") ->
                    "Maraming salamat po sa inyo."
                words.contains("saan") ->
                    "Maaari po bang magtanong kung saan ito matatagpuan?"
                else ->
                    "Mensahe mula sa senyas: \"${words.joinToString(" ").replaceFirstChar { it.uppercase() }}\"."
            }
        } else {
            when {
                words.contains("tulong") || words.contains("help") ->
                    "Please help, I need assistance."
                words.contains("hospital") || words.contains("doctor") ->
                    "I need medical attention or a doctor, please."
                words.contains("salamat") || words.contains("thank") ->
                    "Thank you very much."
                else ->
                    "Message from signs: \"${words.joinToString(" ").replaceFirstChar { it.uppercase() }}\"."
            }
        }
    }
}
