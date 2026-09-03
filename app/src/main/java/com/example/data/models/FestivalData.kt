package com.example.data.models

import java.time.LocalDate

data class CalendarFestival(
    val date: String, // YYYY-MM-DD
    val nameKn: String,
    val nameEn: String,
    val nameTcy: String,
    val icon: String = "🪔",
    val descriptionKn: String
)

object FestivalData {
    // Correct dates for major Indian & Karnataka festivals (2026 & recurring)
    val FESTIVALS_2026 = listOf(
        CalendarFestival("2026-01-14", "ಮಕರ ಸಂಕ್ರಾಂತಿ", "Makar Sankranti", "ಸುಗ್ಗಿ ಪರ್ಬೊ", "🌾", "ಸುಗ್ಗಿ ಹಬ್ಬ ಹಾಗೂ ಸೂರ್ಯ ಸಂಕ್ರಮಣ"),
        CalendarFestival("2026-01-26", "ಗಣರಾಜ್ಯೋತ್ಸವ", "Republic Day", "ಗಣರಾಜ್ಯೋತ್ಸವ", "🇮🇳", "ಭಾರತದ ಗಣರಾಜ್ಯೋತ್ಸವ ದಿನಾಚರಣೆ"),
        CalendarFestival("2026-02-15", "ಮಹಾ ಶಿವರಾತ್ರಿ", "Maha Shivaratri", "ಶಿವರಾತ್ರಿ", "🕉️", "ಶಿವನ ಆರಾಧನೆಯ ಮಂಗಳಕರ ರಾತ್ರಿ"),
        CalendarFestival("2026-03-19", "ಯುಗಾದಿ", "Ugadi", "ಯುಗಾದಿ / ಬಿಸು ಪರ್ಬೊ", "🌿", "ಕನ್ನಡ ಹೊಸ ವರ್ಷಾರಂಭ ಬೇವು-ಬೆಲ್ಲ"),
        CalendarFestival("2026-04-14", "ಡಾ. ಅಂಬೇಡ್ಕರ್ ಜಯಂತಿ", "Ambedkar Jayanti", "ಅಂಬೇಡ್ಕರ್ ಜಯಂತಿ", "📜", "ಸಂವಿಧಾನ ಶಿಲ್ಪಿ ದಿನ"),
        CalendarFestival("2026-05-01", "ಕಾರ್ಮಿಕರ ದಿನ", "May Day", "ಕಾರ್ಮಿಕರ ದಿನ", "🛠️", "ಅಂತರರಾಷ್ಟ್ರೀಯ ಕಾರ್ಮಿಕರ ದಿನಾಚರಣೆ"),
        CalendarFestival("2026-08-15", "ಸ್ವಾತಂತ್ರ್ಯ ದಿನ", "Independence Day", "ಸ್ವಾತಂತ್ರ್ಯ ದಿನೊ", "🇮🇳", "ಭಾರತದ 80ನೇ ಸ್ವಾತಂತ್ರ್ಯ ದಿನಾಚರಣೆ"),
        CalendarFestival("2026-08-28", "ವರಮಹಾಲಕ್ಷ್ಮಿ ವ್ರತ", "Varamahalakshmi", "ವರಮಹಾಲಕ್ಷ್ಮಿ", "🌸", "ಸಂಪತ್ತು-ಸಮೃದ್ಧಿಯ ಪೂಜೆ"),
        CalendarFestival("2026-09-14", "ಗಣೇಶ ಚತುರ್ಥಿ", "Ganesha Chaturthi", "ಚೌತಿ ಪರ್ಬೊ", "🐘", "ವಿಘ್ನನಿವಾರಕ ಗಣಪತಿ ಹಬ್ಬ"),
        CalendarFestival("2026-10-02", "ಗಾಂಧಿ ಜಯಂತಿ", "Gandhi Jayanti", "ಗಾಂಧಿ ಜಯಂತಿ", "🕊️", "ರಾಷ್ಟ್ರಪಿತ ಮಹಾತ್ಮ ಗಾಂಧಿ ಜನ್ಮದಿನ"),
        CalendarFestival("2026-10-20", "ವಿಜಯದಶಮಿ / ದಸರಾ", "Dussehra", "ದಸರಾ / ಆಯುಧ ಪೂಜೆ", "🏹", "ನಾಡಹಬ್ಬ ಮೈಸೂರು ದಸರಾ"),
        CalendarFestival("2026-11-01", "ಕನ್ನಡ ರಾಜ್ಯೋತ್ಸವ", "Kannada Rajyotsava", "ಕನ್ನಡ ರಾಜ್ಯೋತ್ಸವ", "💛❤️", "ಕರ್ನಾಟಕ ರಾಜ್ಯ ರಚನೆ ದಿನ"),
        CalendarFestival("2026-11-08", "ದೀಪಾವಳಿ / ನರಕ ಚತುರ್ದಶಿ", "Deepavali", "ದೀಪಾವಳಿ ಪರ್ಬೊ", "🪔", "ಬೆಳಕಿನ ಹಬ್ಬ ಹಾಗೂ ಲಕ್ಷ್ಮಿ ಪೂಜೆ"),
        CalendarFestival("2026-11-10", "ಬಲಿಪಾಡ್ಯಮಿ / ಗೋಪೂಜೆ", "Balipadyami / Cow Puja", "ಗೋಪೂಜೆ / ಬಲೀಂದ್ರ ಪೂಜೆ", "🐄", "ಹಸುಗಳ ಪೂಜೆ ಮತ್ತು ಬಲೀಂದ್ರ ಆರಾಧನೆ"),
        CalendarFestival("2026-12-25", "ಕ್ರಿಸ್ಮಸ್", "Christmas", "ಕ್ರಿಸ್ಮಸ್", "⭐", "ಶಾಂತಿ ಮತ್ತು ಸಂತೋಷದ ಹಬ್ಬ")
    )

    fun getFestivalForDate(dateStr: String): CalendarFestival? {
        return FESTIVALS_2026.find { it.date == dateStr }
    }
}
