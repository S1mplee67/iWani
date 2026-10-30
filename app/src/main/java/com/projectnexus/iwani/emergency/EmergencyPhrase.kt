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
            ),
            EmergencyPhrase(
                id = "fire",
                titleEn = "Fire / Smoke Emergency",
                titleTl = "May Sunog / Bumbero",
                descEn = "Alert people nearby about fire or smoke",
                descTl = "Babala sa sunog at pagtawag ng bumbero",
                utteranceEn = "Fire emergency! There is fire or smoke nearby! I am deaf, please help evacuate and call the fire department!",
                utteranceTl = "May sunog o usok po! Pipi at bingi po ako, pakiusap tulungan po tayong lumikas at tumawag ng bumbero!"
            ),
            EmergencyPhrase(
                id = "accident",
                titleEn = "Road / Vehicle Accident",
                titleTl = "Aksidente sa Daan / Sasakyan",
                descEn = "Call rescue or traffic enforcers for a road accident",
                descTl = "Tulong para sa banggaan o aksidente sa kalsada",
                utteranceEn = "There has been a road accident! I am deaf. Please call rescue or traffic responders right away!",
                utteranceTl = "May aksidente po sa daan! Pipi at bingi po ako. Pakiusap, tumawag po kayo agad ng rescue o traffic enforcer!"
            ),
            EmergencyPhrase(
                id = "breathing",
                titleEn = "Difficulty Breathing / Dizzy",
                titleTl = "Hirap Huminga / Nahihilo",
                descEn = "Asthma, chest pain, or sudden dizziness alert",
                descTl = "Para sa hika, paninikip ng dibdib, o pagkahilo",
                utteranceEn = "Urgent! I am deaf and I am having trouble breathing and feeling very dizzy. Please help me sit down and get a medic!",
                utteranceTl = "Tulong po! Pipi at bingi po ako, nahihirapan po akong huminga at nahihilo. Pakitulungan po akong maupo at tumawag ng medic!"
            ),
            EmergencyPhrase(
                id = "theft",
                titleEn = "Stolen Item / Harassment Alert",
                titleTl = "Ninakawan / Humihingi ng Saklolo",
                descEn = "Alert bystanders or guards about theft or danger",
                descTl = "Ipaalam sa guard o tao sa paligid kung ninakawan o nanganganib",
                utteranceEn = "Help please! Someone took my belongings or is bothering me! I am deaf, please call security or help me!",
                utteranceTl = "Tulong po! May kumuha po ng gamit ko o nang-aabala sa akin! Pipi at bingi po ako, pakitawag po ang guard!"
            ),
            EmergencyPhrase(
                id = "disaster",
                titleEn = "Earthquake / Flood Rescue",
                titleTl = "Sakuna / Lindol o Baha (Rescue)",
                descEn = "Request evacuation or barangay disaster rescue",
                descTl = "Tulong sa paglikas sa oras ng baha, bagyo, o lindol",
                utteranceEn = "Disaster rescue needed! I am deaf and stranded here. Please help me evacuate safely!",
                utteranceTl = "Saklolo po! Pipi at bingi po ako at na-stranded po ako rito dahil sa sakuna. Pakitulungan po akong makalikas!"
            ),
            EmergencyPhrase(
                id = "commute_stop",
                titleEn = "Driver: Please Stop Here (Para Po)",
                titleTl = "Para Po sa Tabi (Bababa Ngayon)",
                descEn = "Loudly notify jeepney, bus, or tricycle driver to stop",
                descTl = "Iparinig sa driver ng jeep, bus, o tricycle na bababa na",
                utteranceEn = "Excuse me driver! I am deaf. Please pull over safely here, I need to get off now. Thank you!",
                utteranceTl = "Para po Manong Driver! Pipi at bingi po ako. Dito na lang po ako bababa sa tabi, maraming salamat po!"
            )
        )
    }
}
