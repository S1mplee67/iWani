package com.projectnexus.iwani.emergency

/**
 * Bilingual model for Emergency SOS situations in iWani (English & Natural Filipino/Tagalog).
 */
data class EmergencyPhrase(
    val id: String,
    val titleEn: String,
    val titleTl: String,
    val descEn: String,
    val descTl: String,
    val utteranceEn: String,
    val utteranceTl: String,
    val iconRes: String = "ic_sos"
) {
    fun getTitle(isTagalog: Boolean): String = if (isTagalog) titleTl else titleEn
    fun getDescription(isTagalog: Boolean): String = if (isTagalog) descTl else descEn
    fun getUtterance(isTagalog: Boolean): String = if (isTagalog) utteranceTl else utteranceEn

    // Backward compatibility shortcuts
    val title: String get() = titleEn
    val utterance: String get() = utteranceEn

    companion object {
        val standardPhrases = listOf(
            EmergencyPhrase(
                id = "help",
                titleEn = "Emergency SOS (Urgent Help)",
                titleTl = "Humingi ng Saklolo (SOS)",
                descEn = "Immediate loud audio alert asking for urgent help",
                descTl = "Agad na boses para humingi ng saklolo",
                utteranceEn = "Help please! I am deaf and I need urgent assistance. Please help me!",
                utteranceTl = "Tulong po! Pipi at bingi po ako. Pakiusap, kailangan ko po ng tulong ngayon!"
            ),
            EmergencyPhrase(
                id = "medical",
                titleEn = "Medical Emergency / Hospital",
                titleTl = "Kailangan ng Doktor / Ospital",
                descEn = "Medical emergency, ambulance, or injury",
                descTl = "Para sa aksidente, sakit, o kailangang ambulansya",
                utteranceEn = "Medical emergency! I am deaf. I need a doctor or an ambulance right away!",
                utteranceTl = "Emergency po! Pipi at bingi po ako. Kailangan ko po agad ng doktor o ambulansya. May emergency medikal po!"
            ),
            EmergencyPhrase(
                id = "lost",
                titleEn = "I Am Lost / Need Directions",
                titleTl = "Naliligaw po ako",
                descEn = "Ask for directions or help getting back home",
                descTl = "Humihingi ng tulong para makauwi o magtanong ng daan",
                utteranceEn = "Excuse me, I am deaf and I am lost. Can you please help me find my way home?",
                utteranceTl = "Pasensya na po, pipi at bingi po ako at naliligaw po ako. Pwede po bang magpaturo ng daan o tulungan akong makauwi?"
            ),
            EmergencyPhrase(
                id = "deaf_intro",
                titleEn = "I Am Deaf (Communication Note)",
                titleTl = "Pipi at Bingi po ako (Pansin)",
                descEn = "Inform others to write or type on screen",
                descTl = "Ipaalam na kailangang isulat o i-type ang sasabihin",
                utteranceEn = "Hello! I am deaf and I use sign language. Please write on paper or type on my phone so we can communicate.",
                utteranceTl = "Magandang araw po! Pipi at bingi po ako. Pwede po bang pakisulat na lang sa papel o i-type sa cellphone para magkaintindihan tayo?"
            ),
            EmergencyPhrase(
                id = "police",
                titleEn = "Police / Barangay Tanod",
                titleTl = "Kailangan ng Pulis o Tanod",
                descEn = "Security, disturbance, or authority assistance",
                descTl = "Para sa seguridad, gulo, o tulong ng awtoridad",
                utteranceEn = "I need police or security assistance right now! Please call the police for me!",
                utteranceTl = "Kailangan ko po ng tulong ng Pulis o Barangay Tanod! Pipi at bingi po ako, pakiusap tawagin niyo po sila!"
            ),
            EmergencyPhrase(
                id = "family",
                titleEn = "Call My Family / Emergency Contact",
                titleTl = "Tawagan ang Pamilya Ko",
                descEn = "Call the emergency number on my phone",
                descTl = "Ipakita at tawagan ang emergency contact",
                utteranceEn = "I am deaf. Please call my emergency contact number saved in my phone.",
                utteranceTl = "Pipi at bingi po ako. Pwede po bang pakitawagan ang pamilya ko gamit ang numero sa emergency contact ng phone ko?"
            )
        )
    }
}
