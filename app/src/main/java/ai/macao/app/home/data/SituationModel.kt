package ai.macao.app.home.data

import ai.macao.app.R

enum class SituationStepType {
    INTRO,
    PACKING,
    LEARN_PLACES,
    LISTENING_ANNOUNCEMENT,
    FIND_SEAT,
    SPEAKING_CHALLENGE,
    MISSION_COMPLETE,
}

data class PackingItem(
    val id: String = "",
    val targetName: String = "",
    val nativeTranslation: String = "",
    val imageRes: Int = R.drawable.passport,
    val assetKey: String? = null,
    val isRequired: Boolean = true,
) {
    fun getResolvedImageRes(): Int {
        return when (assetKey) {
            "passport" -> R.drawable.passport
            "headphones" -> R.drawable.headphones
            "glasses" -> R.drawable.glasses
            "bottles" -> R.drawable.bottles
            "book1" -> R.drawable.book1
            "coffee" -> R.drawable.coffee
            "sittingarea" -> R.drawable.sittingarea
            else -> imageRes
        }
    }
}

data class PlaceItem(
    val id: String = "",
    val targetName: String = "",
    val nativeTranslation: String = "",
    val iconRes: Int = R.drawable.takeoff,
    val iconKey: String? = null,
)

data class ListeningQuestion(
    val audioText: String = "",
    val prompt: String = "",
    val options: List<String> = emptyList(),
    val correctAnswer: String = "",
)

data class SeatChallenge(
    val boardingPassSeat: String = "18A",
    val prompt: String = "",
    val options: List<String> = emptyList(),
    val correctAnswer: String = "",
    val vocabulary: List<Pair<String, String>> = emptyList(), // Target -> Native
)

data class SpeakingChallenge(
    val phrase: String = "",
    val translation: String = "",
    val phoneticHint: String = "",
    val imageRes: Int = R.drawable.coffee,
    val assetKey: String? = null,
) {
    fun getResolvedImageRes(): Int {
        return when (assetKey) {
            "coffee" -> R.drawable.coffee
            "passport" -> R.drawable.passport
            "headphones" -> R.drawable.headphones
            "sittingarea" -> R.drawable.sittingarea
            else -> imageRes
        }
    }
}

data class MissionSummary(
    val title: String = "",
    val subtitle: String = "",
    val wordsCount: Int = 12,
    val phrasesCount: Int = 3,
    val situationsCount: Int = 4,
    val skills: List<String> = emptyList(),
    val tip: String = "",
)

data class MissionData(
    val id: String = "mission_airport_01",
    val missionNumber: String = "Mission 01",
    val levelId: String = "level_1",
    val languageCode: String = "es",
    val title: String = "",
    val subtitle: String = "",
    val goalDescription: String = "",
    val arrivalCode: String = "MAD",
    val rewardTitle: String = "Café con leche",
    val destination: String = "Spain",
    val heroImageRes: Int = R.drawable.sittingarea,
    val heroAssetKey: String? = "sittingarea",
    val order: Int = 1,
    val published: Boolean = true,
    val packingItems: List<PackingItem> = emptyList(),
    val places: List<PlaceItem> = emptyList(),
    val announcement: ListeningQuestion = ListeningQuestion(),
    val seatChallenge: SeatChallenge = SeatChallenge(),
    val speakingChallenge: SpeakingChallenge = SpeakingChallenge(),
    val summary: MissionSummary = MissionSummary(),
) {
    fun resolveHeroImageRes(): Int {
        return when (heroAssetKey) {
            "sittingarea" -> R.drawable.sittingarea
            "passport" -> R.drawable.passport
            else -> heroImageRes
        }
    }
}

/**
 * Localized content repository for Situation-Based Missions.
 */
object MissionRepository {

    fun getAirportMission(languageCode: String): MissionData {
        val lang = languageCode.lowercase().split("-", "_").firstOrNull() ?: "es"
        return when (lang) {
            "es" -> createSpanishAirportMission()
            "ja" -> createJapaneseAirportMission()
            "fr" -> createFrenchAirportMission()
            "de" -> createGermanAirportMission()
            "pt" -> createPortugueseAirportMission()
            "it" -> createItalianAirportMission()
            else -> createSpanishAirportMission()
        }
    }

    private fun createSpanishAirportMission() = MissionData(
        title = "Arrival in Madrid",
        subtitle = "Your first morning in Spain.",
        goalDescription = "Find your gate, locate your seat, and order your first coffee.",
        arrivalCode = "MAD",
        rewardTitle = "Café con leche",
        heroImageRes = R.drawable.sittingarea,
        packingItems = listOf(
            PackingItem("1", "El Pasaporte", "Passport", R.drawable.passport, isRequired = true),
            PackingItem("2", "Los Auriculares", "Headphones", R.drawable.headphones, isRequired = true),
            PackingItem("3", "Las Gafas de Sol", "Sunglasses", R.drawable.glasses, isRequired = true),
            PackingItem("4", "La Botella de Agua", "Water Bottle", R.drawable.bottles, isRequired = false),
            PackingItem("5", "La Revista", "Magazine", R.drawable.book1, isRequired = false),
        ),
        places = listOf(
            PlaceItem("p1", "Aeropuerto", "Airport"),
            PlaceItem("p2", "Estación de tren", "Railway Station"),
            PlaceItem("p3", "Parada de autobús", "Bus Stop"),
            PlaceItem("p4", "Salidas", "Departures"),
            PlaceItem("p5", "Equipaje", "Baggage Claim"),
        ),
        announcement = ListeningQuestion(
            audioText = "El vuelo a Madrid sale de la puerta A24.",
            prompt = "Listen carefully. Which gate was announced for the flight?",
            options = listOf("Gate A24", "Gate B12", "Gate C18", "Gate D05"),
            correctAnswer = "Gate A24"
        ),
        seatChallenge = SeatChallenge(
            boardingPassSeat = "18A",
            prompt = "Your boarding pass says 18A. Select your row & seat position.",
            options = listOf("Row 18 - Window A", "Row 12 - Aisle B", "Row 24 - Middle C", "Row 18 - Aisle D"),
            correctAnswer = "Row 18 - Window A",
            vocabulary = listOf(
                "Asiento" to "Seat",
                "Fila" to "Row",
                "Ventana" to "Window",
                "Pasillo" to "Aisle"
            )
        ),
        speakingChallenge = SpeakingChallenge(
            phrase = "Café con leche",
            translation = "Coffee with milk",
            phoneticHint = "Try emphasizing the \"leche\".",
            imageRes = R.drawable.coffee
        ),
        summary = MissionSummary(
            title = "Mission Accomplished",
            subtitle = "You can now handle your first airport morning in Spanish.",
            wordsCount = 12,
            phrasesCount = 3,
            situationsCount = 4,
            skills = listOf(
                "Pack your travel essentials",
                "Understand basic airport signs",
                "Understand an airport announcement",
                "Find your seat",
                "Order coffee"
            ),
            tip = "Most locals in Madrid appreciate a polite \"Hola\" before ordering."
        )
    )

    private fun createJapaneseAirportMission() = MissionData(
        title = "Arrival in Tokyo",
        subtitle = "Your first morning in Japan.",
        goalDescription = "Find your gate, locate your seat, and order your first coffee.",
        arrivalCode = "HND",
        rewardTitle = "Kōhī (コーヒー)",
        heroImageRes = R.drawable.sittingarea,
        packingItems = listOf(
            PackingItem("1", "パスポート (Pasupōto)", "Passport", R.drawable.passport, isRequired = true),
            PackingItem("2", "ヘッドフォン (Heddofon)", "Headphones", R.drawable.headphones, isRequired = true),
            PackingItem("3", "サングラス (Sangurasu)", "Sunglasses", R.drawable.glasses, isRequired = true),
            PackingItem("4", "水筒 (Suitō)", "Water Bottle", R.drawable.bottles, isRequired = false),
            PackingItem("5", "雑誌 (Zasshi)", "Magazine", R.drawable.book1, isRequired = false),
        ),
        places = listOf(
            PlaceItem("p1", "空港 (Kūkō)", "Airport"),
            PlaceItem("p2", "駅 (Eki)", "Railway Station"),
            PlaceItem("p3", "バス停 (Basutei)", "Bus Stop"),
            PlaceItem("p4", "出発 (Shuppatsu)", "Departures"),
            PlaceItem("p5", "手荷物受取所 (Tenimotsu)", "Baggage Claim"),
        ),
        announcement = ListeningQuestion(
            audioText = "東京行きは24番ゲートからです。",
            prompt = "Listen carefully. Which gate was announced?",
            options = listOf("Gate A24", "Gate B12", "Gate C18", "Gate D05"),
            correctAnswer = "Gate A24"
        ),
        seatChallenge = SeatChallenge(
            boardingPassSeat = "18A",
            prompt = "Your boarding pass says 18A. Select your row & seat.",
            options = listOf("Row 18 - Window A", "Row 12 - Aisle B", "Row 24 - Middle C", "Row 18 - Aisle D"),
            correctAnswer = "Row 18 - Window A",
            vocabulary = listOf(
                "席 (Seki)" to "Seat",
                "列 (Retsui)" to "Row",
                "窓側 (Madogawa)" to "Window",
                "通路側 (Tsūrogawa)" to "Aisle"
            )
        ),
        speakingChallenge = SpeakingChallenge(
            phrase = "コーヒーをお願いします",
            translation = "Coffee please",
            phoneticHint = "Kōhī o onegaishimasu",
            imageRes = R.drawable.coffee
        ),
        summary = MissionSummary(
            title = "Mission Accomplished",
            subtitle = "You can now handle your first airport morning in Japanese.",
            wordsCount = 12,
            phrasesCount = 3,
            situationsCount = 4,
            skills = listOf(
                "Pack your travel essentials",
                "Understand basic airport signs",
                "Understand an airport announcement",
                "Find your seat",
                "Order coffee"
            ),
            tip = "Saying \"Sumimasen\" (Excuse me) gets the staff's attention politely."
        )
    )

    private fun createFrenchAirportMission() = MissionData(
        title = "Arrival in Paris",
        subtitle = "Your first morning in France.",
        goalDescription = "Find your gate, locate your seat, and order your first coffee.",
        arrivalCode = "CDG",
        rewardTitle = "Café au lait",
        heroImageRes = R.drawable.sittingarea,
        packingItems = listOf(
            PackingItem("1", "Le Passeport", "Passport", R.drawable.passport, isRequired = true),
            PackingItem("2", "Le Casque", "Headphones", R.drawable.headphones, isRequired = true),
            PackingItem("3", "Les Lunettes de soleil", "Sunglasses", R.drawable.glasses, isRequired = true),
            PackingItem("4", "La Bouteille d'eau", "Water Bottle", R.drawable.bottles, isRequired = false),
            PackingItem("5", "Le Magazine", "Magazine", R.drawable.book1, isRequired = false),
        ),
        places = listOf(
            PlaceItem("p1", "Aéroport", "Airport"),
            PlaceItem("p2", "Gare", "Railway Station"),
            PlaceItem("p3", "Arrêt de bus", "Bus Stop"),
            PlaceItem("p4", "Départs", "Departures"),
            PlaceItem("p5", "Livraison des bagages", "Baggage Claim"),
        ),
        announcement = ListeningQuestion(
            audioText = "Le vol pour Paris part de la porte A24.",
            prompt = "Listen carefully. Which gate was announced for the flight?",
            options = listOf("Gate A24", "Gate B12", "Gate C18", "Gate D05"),
            correctAnswer = "Gate A24"
        ),
        seatChallenge = SeatChallenge(
            boardingPassSeat = "18A",
            prompt = "Your boarding pass says 18A. Select your row & seat position.",
            options = listOf("Row 18 - Window A", "Row 12 - Aisle B", "Row 24 - Middle C", "Row 18 - Aisle D"),
            correctAnswer = "Row 18 - Window A",
            vocabulary = listOf(
                "Siège" to "Seat",
                "Rangée" to "Row",
                "Hublot" to "Window",
                "Couloir" to "Aisle"
            )
        ),
        speakingChallenge = SpeakingChallenge(
            phrase = "Un café au lait, s'il vous plaît",
            translation = "A coffee with milk, please",
            phoneticHint = "Remember a friendly \"s'il vous plaît\".",
            imageRes = R.drawable.coffee
        ),
        summary = MissionSummary(
            title = "Mission Accomplished",
            subtitle = "You can now handle your first airport morning in French.",
            wordsCount = 12,
            phrasesCount = 3,
            situationsCount = 4,
            skills = listOf(
                "Pack your travel essentials",
                "Understand basic airport signs",
                "Understand an airport announcement",
                "Find your seat",
                "Order coffee"
            ),
            tip = "Always start with a polite \"Bonjour\" when addressing service staff."
        )
    )

    private fun createGermanAirportMission() = MissionData(
        title = "Arrival in Berlin",
        subtitle = "Your first morning in Germany.",
        goalDescription = "Find your gate, locate your seat, and order your first coffee.",
        arrivalCode = "BER",
        rewardTitle = "Kaffee mit Milch",
        heroImageRes = R.drawable.sittingarea,
        packingItems = listOf(
            PackingItem("1", "Der Reisepass", "Passport", R.drawable.passport, isRequired = true),
            PackingItem("2", "Die Kopfhörer", "Headphones", R.drawable.headphones, isRequired = true),
            PackingItem("3", "Die Sonnenbrille", "Sunglasses", R.drawable.glasses, isRequired = true),
            PackingItem("4", "Die Wasserflasche", "Water Bottle", R.drawable.bottles, isRequired = false),
            PackingItem("5", "Das Magazin", "Magazine", R.drawable.book1, isRequired = false),
        ),
        places = listOf(
            PlaceItem("p1", "Flughafen", "Airport"),
            PlaceItem("p2", "Bahnhof", "Railway Station"),
            PlaceItem("p3", "Bushaltestelle", "Bus Stop"),
            PlaceItem("p4", "Abflug", "Departures"),
            PlaceItem("p5", "Gepäckausgabe", "Baggage Claim"),
        ),
        announcement = ListeningQuestion(
            audioText = "Der Flug nach Berlin startet von Gate A24.",
            prompt = "Listen carefully. Which gate was announced?",
            options = listOf("Gate A24", "Gate B12", "Gate C18", "Gate D05"),
            correctAnswer = "Gate A24"
        ),
        seatChallenge = SeatChallenge(
            boardingPassSeat = "18A",
            prompt = "Your boarding pass says 18A. Select your row & seat position.",
            options = listOf("Row 18 - Window A", "Row 12 - Aisle B", "Row 24 - Middle C", "Row 18 - Aisle D"),
            correctAnswer = "Row 18 - Window A",
            vocabulary = listOf(
                "Sitzplatz" to "Seat",
                "Reihe" to "Row",
                "Fensterplatz" to "Window",
                "Gangplatz" to "Aisle"
            )
        ),
        speakingChallenge = SpeakingChallenge(
            phrase = "Kaffee mit Milch, bitte",
            translation = "Coffee with milk, please",
            phoneticHint = "Emphasize \"bitte\" at the end.",
            imageRes = R.drawable.coffee
        ),
        summary = MissionSummary(
            title = "Mission Accomplished",
            subtitle = "You can now handle your first airport morning in German.",
            wordsCount = 12,
            phrasesCount = 3,
            situationsCount = 4,
            skills = listOf(
                "Pack your travel essentials",
                "Understand basic airport signs",
                "Understand an airport announcement",
                "Find your seat",
                "Order coffee"
            ),
            tip = "Saying \"Guten Tag\" is customary when entering any café or store."
        )
    )

    private fun createPortugueseAirportMission() = MissionData(
        title = "Arrival in Lisbon",
        subtitle = "Your first morning in Portugal.",
        goalDescription = "Find your gate, locate your seat, and order your first coffee.",
        arrivalCode = "LIS",
        rewardTitle = "Café com leite",
        heroImageRes = R.drawable.sittingarea,
        packingItems = listOf(
            PackingItem("1", "O Passaporte", "Passport", R.drawable.passport, isRequired = true),
            PackingItem("2", "Os Fones de ouvido", "Headphones", R.drawable.headphones, isRequired = true),
            PackingItem("3", "Os Óculos de sol", "Sunglasses", R.drawable.glasses, isRequired = true),
            PackingItem("4", "A Garrafa de água", "Water Bottle", R.drawable.bottles, isRequired = false),
            PackingItem("5", "A Revista", "Magazine", R.drawable.book1, isRequired = false),
        ),
        places = listOf(
            PlaceItem("p1", "Aeroporto", "Airport"),
            PlaceItem("p2", "Estação de trem", "Railway Station"),
            PlaceItem("p3", "Ponto de ônibus", "Bus Stop"),
            PlaceItem("p4", "Partidas", "Departures"),
            PlaceItem("p5", "Bagagens", "Baggage Claim"),
        ),
        announcement = ListeningQuestion(
            audioText = "O voo para Lisboa parte do portão A24.",
            prompt = "Listen carefully. Which gate was announced for the flight?",
            options = listOf("Gate A24", "Gate B12", "Gate C18", "Gate D05"),
            correctAnswer = "Gate A24"
        ),
        seatChallenge = SeatChallenge(
            boardingPassSeat = "18A",
            prompt = "Your boarding pass says 18A. Select your row & seat position.",
            options = listOf("Row 18 - Window A", "Row 12 - Aisle B", "Row 24 - Middle C", "Row 18 - Aisle D"),
            correctAnswer = "Row 18 - Window A",
            vocabulary = listOf(
                "Assento" to "Seat",
                "Fileira" to "Row",
                "Janela" to "Window",
                "Corredor" to "Aisle"
            )
        ),
        speakingChallenge = SpeakingChallenge(
            phrase = "Café com leite, por favor",
            translation = "Coffee with milk, please",
            phoneticHint = "Say \"por favor\" warmly.",
            imageRes = R.drawable.coffee
        ),
        summary = MissionSummary(
            title = "Mission Accomplished",
            subtitle = "You can now handle your first airport morning in Portuguese.",
            wordsCount = 12,
            phrasesCount = 3,
            situationsCount = 4,
            skills = listOf(
                "Pack your travel essentials",
                "Understand basic airport signs",
                "Understand an airport announcement",
                "Find your seat",
                "Order coffee"
            ),
            tip = "Saying \"Bom dia\" in the morning is always appreciated!"
        )
    )

    private fun createItalianAirportMission() = MissionData(
        title = "Arrival in Rome",
        subtitle = "Your first morning in Italy.",
        goalDescription = "Find your gate, locate your seat, and order your first coffee.",
        arrivalCode = "FCO",
        rewardTitle = "Cappuccino",
        heroImageRes = R.drawable.sittingarea,
        packingItems = listOf(
            PackingItem("1", "Il Passaporto", "Passport", R.drawable.passport, isRequired = true),
            PackingItem("2", "Le Cuffie", "Headphones", R.drawable.headphones, isRequired = true),
            PackingItem("3", "Gli Occhiali da sole", "Sunglasses", R.drawable.glasses, isRequired = true),
            PackingItem("4", "La Bottiglia d'acqua", "Water Bottle", R.drawable.bottles, isRequired = false),
            PackingItem("5", "La Rivista", "Magazine", R.drawable.book1, isRequired = false),
        ),
        places = listOf(
            PlaceItem("p1", "Aeroporto", "Airport"),
            PlaceItem("p2", "Stazione ferroviaria", "Railway Station"),
            PlaceItem("p3", "Fermata dell'autobus", "Bus Stop"),
            PlaceItem("p4", "Partenze", "Departures"),
            PlaceItem("p5", "Ritiro bagagli", "Baggage Claim"),
        ),
        announcement = ListeningQuestion(
            audioText = "Il volo per Roma parte dal gate A24.",
            prompt = "Listen carefully. Which gate was announced?",
            options = listOf("Gate A24", "Gate B12", "Gate C18", "Gate D05"),
            correctAnswer = "Gate A24"
        ),
        seatChallenge = SeatChallenge(
            boardingPassSeat = "18A",
            prompt = "Your boarding pass says 18A. Select your row & seat position.",
            options = listOf("Row 18 - Window A", "Row 12 - Aisle B", "Row 24 - Middle C", "Row 18 - Aisle D"),
            correctAnswer = "Row 18 - Window A",
            vocabulary = listOf(
                "Posto" to "Seat",
                "Fila" to "Row",
                "Finestrino" to "Window",
                "Corridoio" to "Aisle"
            )
        ),
        speakingChallenge = SpeakingChallenge(
            phrase = "Un caffè per favore",
            translation = "A coffee please",
            phoneticHint = "Say \"per favore\" politely.",
            imageRes = R.drawable.coffee
        ),
        summary = MissionSummary(
            title = "Mission Accomplished",
            subtitle = "You can now handle your first airport morning in Italian.",
            wordsCount = 12,
            phrasesCount = 3,
            situationsCount = 4,
            skills = listOf(
                "Pack your travel essentials",
                "Understand basic airport signs",
                "Understand an airport announcement",
                "Find your seat",
                "Order coffee"
            ),
            tip = "In Italy, coffee is usually enjoyed standing at the bar!"
        )
    )
}
