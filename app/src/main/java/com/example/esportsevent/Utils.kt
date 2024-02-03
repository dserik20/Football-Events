package com.example.esportsevent

import android.text.format.DateUtils
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

fun getTitleForDate(dateString: String): String {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val date = dateFormat.parse(dateString) ?: return dateString

    val calendar = Calendar.getInstance()
    calendar.time = date

    return when {
        DateUtils.isToday(calendar.timeInMillis) -> "Today"
        DateUtils.isToday(calendar.timeInMillis - DateUtils.DAY_IN_MILLIS) -> "Tomorrow"
        else -> SimpleDateFormat("dd MMM", Locale.getDefault()).format(date)
    }
}

fun getMockMatches(): List<Match> {
    return listOf(
        Match(
            Fixture(50, "Referee 1", "UTC", "2024-01-10", 1704886560, Venue(1, "Venue 1", "City 1"), Status("Scheduled", "NS", null)),
            League(843, "Copa Verde", "England", "https://media.api-sports.io/football/leagues/39.png", "https://upload.wikimedia.org/wikipedia/commons/4/42/Flag_of_the_United_Kingdom.png", 2023, "Regular Season - 1"),
            Teams(Team(1, "Manchester United", "https://upload.wikimedia.org/wikipedia/tr/b/b6/Manchester_United_FC_logo.png", true), Team(2, "Aston Villa", "https://upload.wikimedia.org/wikipedia/tr/5/57/Aston_Villa.png", false)),
            Goals(3, 2),
            Score(ScoreDetail(1, 1), ScoreDetail(3, 2), null, null)
        ),
    )
}