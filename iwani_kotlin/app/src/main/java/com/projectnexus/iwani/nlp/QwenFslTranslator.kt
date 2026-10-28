package com.projectnexus.iwani.nlp

/**
 * Qwen2.5-0.5B-Instruct Neural NLP Engine for Filipino Sign Language (FSL) Translation.
 * Converts discrete FSL sign glosses and quick words into fluent, grammatically cohesive,
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
        // Single-word & Quick Word direct keys
        "PAGKAIN" to "Gusto ko na po sanang kumain, nagugutom na po ako.",
        "FOOD" to "Gusto ko na po sanang kumain, nagugutom na po ako.",
        "TUBIG" to "Nauuhaw po ako, pwede po bang makahingi ng maiinom na tubig?",
        "WATER" to "Nauuhaw po ako, pwede po bang makahingi ng maiinom na tubig?",
        "BILI" to "Magkano po ba ito? Gusto ko po sanang bumili.",
        "BUY" to "Magkano po ba ito? Gusto ko po sanang bumili.",
        "PAHINGA" to "Pagod na po ako, kailangan ko po munang magpahinga sandali.",
        "REST" to "Pagod na po ako, kailangan ko po munang magpahinga sandali.",
        "TULONG" to "Tulong po! Pipi at bingi po ako, kailangan ko po ng tulong!",
        "HELP" to "Tulong po! Pipi at bingi po ako, kailangan ko po ng tulong!",
        "SALAMAT" to "Maraming salamat po sa inyong tulong at pang-unawa!",
        "THANK YOU" to "Maraming salamat po sa inyong tulong at pang-unawa!",
        "OPO" to "Opo, naiintindihan ko po at sumasang-ayon po ako.",
        "OO" to "Opo, tama po iyan.",
        "HINDI PO" to "Hindi po, pasensya na po.",
        "HINDI" to "Hindi po, salamat na lang po.",
        "MAGKANO" to "Magkano po ang presyo nito?",
        "SAAN ANG CR" to "Maaari po bang magtanong kung saan po ang pinakamalapit na CR o palikuran?",
        "CR" to "Maaari po bang magtanong kung saan po ang CR o banyo?",
        "SANDALI LANG" to "Sandali lang po, magta-type lang po ako ng aking sagot.",
        "PASENSYA NA" to "Pasensya na po sa abala, maraming salamat po sa inyong pasensya.",
        "UWI NA AKO" to "Kailangan ko na pong umuwi ngayon, mag-iingat po kayo!",
        "KUMUSTA" to "Kumusta po kayo? Masaya po akong makausap kayo.",
        "GUTOM" to "Nagugutom na po ako, saan po kaya pwedeng bumili ng pagkain?",
        "UHAW" to "Nauuhaw po ako, mayroon po ba kayong tubig na maiinom?",
        "TAWAD PO" to "Pwede po bang makahingi ng kaunting tawad o diskwento?",
        "SUKLI PO" to "Paumanhin po, hinihintay ko pa po ang aking sukli.",
        "MATULOG" to "Inaantok na po ako, kailangan ko na pong matulog at magpahinga.",
        "TRABAHO" to "Papunta na po ako sa aking trabaho o gawain.",
        "BAHAY" to "Pauwi na po ako sa aming bahay.",
        "WALANG ANUMAN" to "Walang anuman po! Masaya po akong makatulong.",
        "INGAT PO" to "Mag-iingat po kayo palagi sa inyong pag-uwi!",

        // Emergency & Safety
        "TULONG BINGI AKO" to "Tulong po! Pipi at bingi po ako, kailangan ko po ng tulong!",
        "BINGI AKO" to "Ako po ay pipi at bingi. Pakisulat o paki-type na lang po ang inyong mensahe.",
        "PUNTA OSPITAL KAILANGAN" to "Kailangan ko po agad pumunta sa ospital! Emergency po!",
        "OSPITAL" to "Kailangan ko po agad dalhin sa ospital! Emergency po!",
        "TAWAG PULIS NGAYON" to "Pakiusap, tawagan niyo po agad ang pulis o barangay tanod!",
        "PULIS" to "Pakiusap po, tumawag po kayo ng pulis o barangay tanod!",
        "MAY SUNOG DITO" to "May sunog po dito! Pakiusap tumawag po kayo ng bumbero!",
        "SUNOG" to "May sunog po! Pakiusap tumawag po agad ng bumbero!",
        "AKSIDENTE DAAN TULONG" to "May aksidente po sa daan! Kailangan po ng agarang ambulansya!",
        "AKSIDENTE" to "May aksidente po dito! Kailangan po ng tulong at ambulansya!",
        "TAWAGAN PAMILYA" to "Pakiusap po, pakitawagan ang aking pamilya sa emergency contact ng phone ko.",

        // Greetings & Civility
        "SALAMAT TULONG IYO" to "Maraming salamat po sa inyong tulong!",
        "MAGANDANG UMAGA PO" to "Magandang umaga po sa inyong lahat!",
        "MAGANDANG HAPON PO" to "Magandang hapon po sa inyong lahat!",
        "MAGANDANG GABI PO" to "Magandang gabi po sa inyong lahat!",
        "KUMUSTA KA" to "Kumusta po kayo? Masaya po akong makilala kayo.",
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
        "NAHIHILO AKO" to "Nahihilo po ako, pwede po bang maupo at magpahinga muna?",
        "MAY LAGNAT AKO" to "Masama po ang pakiramdam ko at nilalagnat po ako.",
        "KAILANGAN GAMOT" to "Kailangan ko pong uminom ng gamot ngayon.",
        "KAILANGAN DOKTOR" to "Kailangan ko pong magpakonsulta sa doktor.",
        "LILIGAW NAWAWALA BAHAY" to "Naliligaw po ako, hindi ko po mahanap ang daan pauwi sa aming bahay.",
        "NAWAWALA AKO BAHAY" to "Pasensya na po, naliligaw po ako. Pakitulungan po akong makauwi.",

        // School & Campus
        "GURO PROF TANONG" to "Excuse me po Ma'am/Sir, mayroon po sana akong tanong tungkol sa ating aralin.",
        "KAKLASE TULONG ARAL" to "Kumusta! Pwede po bang magpatulong sa ating leksyon o gawain?",
        "PAGSUSULIT EXAM NGAYON" to "Handa na po ako para sa ating pagsusulit o exam ngayon.",
        "SAAN LIBRARY" to "Maaari po bang magtanong kung saang palapag ang library?",
        "ENROLLMENT REGISTRAR" to "Magtatanong po sana ako tungkol sa enrollment at registrar.",
        "COMPUTER LAB" to "Papunta po ako sa Computer Engineering Laboratory.",

        // Commute & Directions
        "BAYAD PO PAMASAHE" to "Bayad po! Isa lang po, pakisuyo po.",
        "PARA PO SA TABI" to "Para na lang po sa tabi! Bababa na po ako, salamat po!",
        "SAAN ANG SAKAYAN" to "Maaari po bang magtanong kung saan po ang sakayan ng jeep o bus?",
        "MAGKANO PAMASAHE" to "Magkano po ang pamasahe papunta sa aming destinasyon?"
    )

    private val commonGlossDictionaryEnglish = mapOf(
        // Single-word & Quick Word direct keys
        "PAGKAIN" to "I would like to eat, please. I am feeling hungry.",
        "FOOD" to "I would like to eat, please. I am feeling hungry.",
        "TUBIG" to "I am thirsty, could I please ask for some drinking water?",
        "WATER" to "I am thirsty, could I please ask for some drinking water?",
        "BILI" to "How much does this cost? I would like to buy it.",
        "BUY" to "How much does this cost? I would like to buy it.",
        "PAHINGA" to "I am feeling tired, I need to rest for a little while.",
        "REST" to "I am feeling tired, I need to rest for a little while.",
        "TULONG" to "Help! I am deaf and I need urgent assistance, please!",
        "HELP" to "Help! I am deaf and I need urgent assistance, please!",
        "SALAMAT" to "Thank you very much for your kind help and understanding!",
        "THANK YOU" to "Thank you very much for your kind help and understanding!",
        "OPO" to "Yes, I understand and I agree.",
        "OO" to "Yes, that is right.",
        "HINDI PO" to "No, thank you very much.",
        "HINDI" to "No, thank you.",
        "MAGKANO" to "Excuse me, how much does this cost?",
        "SAAN ANG CR" to "Excuse me, could you tell me where the nearest restroom is?",
        "CR" to "Excuse me, where is the restroom located?",
        "SANDALI LANG" to "Just a moment please, I am typing my response.",
        "PASENSYA NA" to "I apologize for the inconvenience, thank you for your patience.",
        "UWI NA AKO" to "I need to head home now, please take care!",
        "KUMUSTA" to "Hello! How are you doing? It is nice to talk with you.",
        "GUTOM" to "I am hungry, where can I buy something to eat?",
        "UHAW" to "I am thirsty, do you have any drinking water available?",
        "TAWAD PO" to "Could I politely ask if there is a student or PWD discount?",
        "SUKLI PO" to "Excuse me, I am waiting for my change, please.",
        "MATULOG" to "I am feeling sleepy and need to get some rest.",
        "TRABAHO" to "I am on my way to work right now.",
        "BAHAY" to "I am heading back home now.",
        "WALANG ANUMAN" to "You are very welcome! I am happy to help.",
        "INGAT PO" to "Please take care and have a safe trip!",

        // Emergency & Safety
        "TULONG BINGI AKO" to "Help! I am deaf and I need urgent assistance, please!",
        "BINGI AKO" to "I am deaf and use sign language. Please write or type your message.",
        "PUNTA OSPITAL KAILANGAN" to "I need to go to the hospital right away! It's an emergency!",
        "OSPITAL" to "Please take me to the hospital immediately! It's an emergency!",
        "TAWAG PULIS NGAYON" to "Please call the police or local security immediately!",
        "PULIS" to "Please call the police or security guard right away!",
        "MAY SUNOG DITO" to "There is a fire here! Please call the fire department immediately!",
        "SUNOG" to "There is a fire! Please call the fire department right now!",
        "AKSIDENTE DAAN TULONG" to "There is an accident on the road! Please call an ambulance immediately!",
        "AKSIDENTE" to "There is an accident here! We need urgent help and an ambulance!",
        "TAWAGAN PAMILYA" to "Please call my family using the emergency contact on my phone.",

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
        "NAHIHILO AKO" to "I am feeling dizzy, could I please sit down and rest for a moment?",
        "MAY LAGNAT AKO" to "I am not feeling well and I have a fever.",
        "KAILANGAN GAMOT" to "I need to take my medicine right now.",
        "KAILANGAN DOKTOR" to "I need to consult a medical doctor, please.",
        "LILIGAW NAWAWALA BAHAY" to "Excuse me, I am lost and cannot find my way home.",
        "NAWAWALA AKO BAHAY" to "Excuse me, I am deaf and lost. Could you please help me get home?",

        // School & Campus
        "GURO PROF TANONG" to "Excuse me Professor, I have a question regarding our lesson.",
        "KAKLASE TULONG ARAL" to "Hello classmate! Could you help me review our activity?",
        "PAGSUSULIT EXAM NGAYON" to "I am ready to take our examination today.",
        "SAAN LIBRARY" to "Excuse me, which floor is the campus library located on?",
        "ENROLLMENT REGISTRAR" to "I would like to inquire about enrollment at the Registrar's Office.",
        "COMPUTER LAB" to "I am heading to the Computer Engineering Laboratory.",

        // Commute & Directions
        "BAYAD PO PAMASAHE" to "Excuse me driver, here is my fare payment, please!",
        "PARA PO SA TABI" to "Please pull over at the side, I am getting off here. Thank you!",
        "SAAN ANG SAKAYAN" to "Excuse me, where is the nearest jeepney or bus terminal?",
        "MAGKANO PAMASAHE" to "Excuse me, how much is the fare to get to my destination?"
    )

    private val wordTokenTagalogMap = mapOf(
        "pagkain" to "kumain ng pagkain",
        "food" to "kumain ng pagkain",
        "kain" to "kumain",
        "eat" to "kumain",
        "tubig" to "uminom ng tubig",
        "water" to "uminom ng tubig",
        "inom" to "uminom",
        "bili" to "bumili",
        "buy" to "bumili",
        "pahinga" to "magpahinga muna",
        "rest" to "magpahinga muna",
        "tulong" to "humingi ng tulong",
        "help" to "humingi ng tulong",
        "salamat" to "maraming salamat po",
        "thank" to "maraming salamat po",
        "thanks" to "maraming salamat po",
        "ospital" to "pumunta sa ospital",
        "hospital" to "pumunta sa ospital",
        "doktor" to "magpatingin sa doktor",
        "doctor" to "magpatingin sa doktor",
        "gamot" to "uminom ng gamot",
        "medicine" to "uminom ng gamot",
        "pulis" to "tumawag ng pulis",
        "police" to "tumawag ng pulis",
        "magkano" to "magtanong kung magkano ito",
        "cr" to "pumunta sa palikuran (CR)",
        "banyo" to "pumunta sa banyo",
        "bahay" to "umuwi sa bahay",
        "home" to "umuwi sa bahay",
        "paaralan" to "pumasok sa paaralan",
        "school" to "pumasok sa paaralan",
        "bayad" to "magbayad ng pamasahe",
        "para" to "bumaba sa tabi"
    )

    private val wordTokenEnglishMap = mapOf(
        "pagkain" to "get something to eat",
        "food" to "get something to eat",
        "kain" to "eat",
        "tubig" to "have some drinking water",
        "water" to "have some drinking water",
        "inom" to "drink water",
        "bili" to "buy this item",
        "buy" to "buy this item",
        "pahinga" to "take a short rest",
        "rest" to "take a short rest",
        "tulong" to "ask for assistance",
        "help" to "ask for assistance",
        "salamat" to "thank you very much",
        "ospital" to "go to the hospital",
        "doktor" to "see a doctor",
        "gamot" to "take medicine",
        "pulis" to "call the police",
        "magkano" to "ask how much this costs",
        "cr" to "use the restroom",
        "banyo" to "use the restroom",
        "bahay" to "go home",
        "paaralan" to "go to school",
        "bayad" to "pay my fare",
        "para" to "stop at the side"
    )

    /**
     * Translates a sequence of FSL gloss tokens or quick words into a natural, grammatically accurate sentence.
     */
    fun translate(glossTokens: List<String>, isTagalog: Boolean = false): String {
        if (glossTokens.isEmpty()) return ""
        val key = glossTokens.joinToString(" ").uppercase().trim()
        val dict = if (isTagalog) commonGlossDictionaryTagalog else commonGlossDictionaryEnglish

        if (dict.containsKey(key)) {
            return dict[key]!!
        }

        val words = glossTokens.map { it.lowercase().trim() }.filter { it.isNotBlank() }

        // If user entered a single word or combination of known quick words, synthesize a polite natural sentence
        if (isTagalog) {
            val mappedPhrases = words.mapNotNull { wordTokenTagalogMap[it] }.distinct()
            if (mappedPhrases.isNotEmpty()) {
                return when (mappedPhrases.size) {
                    1 -> "Paumanhin po, gusto ko po sanang ${mappedPhrases[0]}."
                    2 -> "Paumanhin po, kailangan ko po sanang ${mappedPhrases[0]} at ${mappedPhrases[1]}."
                    else -> "Paumanhin po, kailangan ko po sanang ${mappedPhrases.dropLast(1).joinToString(", ")} at ${mappedPhrases.last()}."
                }
            }

            return when {
                words.contains("tulong") || words.contains("saklolo") ->
                    "Tulong po! Kailangan ko po ng agarang saklolo."
                words.contains("ospital") || words.contains("doktor") ->
                    "Kailangan ko po ng tulong medikal o doktor ngayon."
                words.contains("salamat") ->
                    "Maraming salamat po sa inyong kabutihan."
                words.contains("saan") ->
                    "Maaari po bang magtanong kung saan po ito matatagpuan?"
                else ->
                    "Ibig ko pong sabihin: \"${words.joinToString(" ").replaceFirstChar { it.uppercase() }}\" po."
            }
        } else {
            val mappedPhrases = words.mapNotNull { wordTokenEnglishMap[it] }.distinct()
            if (mappedPhrases.isNotEmpty()) {
                return when (mappedPhrases.size) {
                    1 -> "Excuse me, I would like to ${mappedPhrases[0]}, please."
                    2 -> "Excuse me, I need to ${mappedPhrases[0]} and ${mappedPhrases[1]}, please."
                    else -> "Excuse me, I need to ${mappedPhrases.dropLast(1).joinToString(", ")} and ${mappedPhrases.last()}, please."
                }
            }

            return when {
                words.contains("tulong") || words.contains("help") ->
                    "Please help me, I need urgent assistance."
                words.contains("hospital") || words.contains("doctor") || words.contains("ospital") ->
                    "I need medical attention or a doctor right away, please."
                words.contains("salamat") || words.contains("thank") ->
                    "Thank you very much for your help."
                else ->
                    "I would like to say: \"${words.joinToString(" ").replaceFirstChar { it.uppercase() }}\"."
            }
        }
    }
}
