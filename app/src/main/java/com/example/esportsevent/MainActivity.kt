package com.example.esportsevent

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.esportsevent.ui.theme.EsportsEventTheme
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException


class MainActivity : ComponentActivity() {
    private val favoritesManager: FavoritesManager by lazy {
        FavoritesManager(applicationContext)
    }
    private var triggeredMatchIds = mutableSetOf<Int>()
    private val matches = mutableStateListOf<Match>()
    private val favorites = mutableStateOf(listOf<Match>())
    private val currentScreen = mutableStateOf("main")
    private val leagues = mutableStateOf<List<LeaguesOfMatchViewer>>(emptyList())


    private fun saveTriggeredMatchIds() {
        val json = Gson().toJson(triggeredMatchIds)
        val editor = getSharedPreferences("app_preferences", Context.MODE_PRIVATE).edit()
        editor.putString("triggered_match_ids", json)
        editor.apply()
    }

    private fun loadTriggeredMatchIds() {
        val prefs = getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
        val json = prefs.getString("triggered_match_ids", null)
        if (json != null) {
            val type = object : TypeToken<MutableSet<Int>>() {}.type
            triggeredMatchIds = Gson().fromJson(json, type)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            createNotificationChannel()
            //matches.addAll(getMockMatches())
            favorites.value = favoritesManager.getFavorites()
            fetchMatches()

            fetchLeagues { fetchedLeagues ->
                leagues.value = fetchedLeagues
                Log.d("MainActivity", "Leagues updated: ${leagues.value.size}")
            }

            val navController = rememberNavController()
            val toggleFavorite: (Match) -> Unit = { match ->
                favoritesManager.toggleFavorite(match)
                favorites.value = favoritesManager.getFavorites()

                val updatedMatches = matches.map { if (it.fixture.id == match.fixture.id) match.copy() else it }
                matches.clear()
                matches.addAll(updatedMatches)
            }
            loadTriggeredMatchIds()

            EsportsEventTheme {
                NavHost(navController = navController, startDestination = "main") {
                    composable("main") {
                        currentScreen.value = "main"
                        EsportsEventApp(
                            navController,
                            matches,
                            favoritesManager,
                            currentScreen,
                            toggleFavorite
                        )
                    }
                    composable("favorites") {
                        currentScreen.value = "favorites"
                        FavoritesScreen(
                            navController,
                            favorites.value,
                            currentScreen,
                            toggleFavorite
                        )
                    }
                    composable(
                        "matchDetails/{matchId}",
                        arguments = listOf(navArgument("matchId") { type = NavType.IntType })
                    ) { backStackEntry ->
                        val matchId = backStackEntry.arguments?.getInt("matchId")
                        val match = matches.find { it.fixture.id == matchId }

                        match?.let {
                            val viewModel = viewModel<MatchDetailViewModel>()
                            MatchDetailsScreen(it, navController, viewModel = viewModel)
                        }
                    }
                    composable("leagues") {
                        currentScreen.value = "leagues"
                        LeagueListScreen(navController, leagues.value, currentScreen)
                    }
                    composable("leagueDetails/{leagueId}", arguments = listOf(navArgument("leagueId") { type = NavType.IntType })) { backStackEntry ->
                        val leagueId = backStackEntry.arguments?.getInt("leagueId") ?: return@composable
                        LeagueDetailsScreen(leagueId, leagues.value, matches, favorites.value, navController, toggleFavorite)
                    }
                }
            }
        }
        CoroutineScope(Dispatchers.Main).launch {
            while (true) {
                checkMatchTimesAndVibrate()
                checkMatchTimesAndNotify()
                delay(3000)
            }
        }
    }

    private fun checkMatchTimesAndVibrate() {
        val currentTimeUtc = (System.currentTimeMillis() + 3 * 3600 * 1000) / 1000
        val favoriteMatches = favoritesManager.getFavorites()

        for (match in favoriteMatches) {
            val matchTime = match.fixture.timestamp
            if (matchTime > currentTimeUtc && matchTime - currentTimeUtc <= 60 && match.fixture.id !in triggeredMatchIds) {
                vibrate()
                triggeredMatchIds.add(match.fixture.id)
                saveTriggeredMatchIds()
            }
        }
    }

    private fun vibrate() {
        val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        vibrator.vibrate(VibrationEffect.createOneShot(1000, VibrationEffect.DEFAULT_AMPLITUDE))
    }

    private fun createNotificationChannel() {
        val name = getString(R.string.channel_name)
        val descriptionText = getString(R.string.channel_description)
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel("match_start_channel", name, importance).apply {
            description = descriptionText
        }
        val notificationManager: NotificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    private fun showMatchNotification(match: Match) {
        val builder = NotificationCompat.Builder(this, "match_start_channel")
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Match Starting Soon")
            .setContentText("${match.teams.home.name} vs ${match.teams.away.name} is starting soon!")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        with(NotificationManagerCompat.from(this)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (ActivityCompat.checkSelfPermission(
                        this@MainActivity,
                        Manifest.permission.POST_NOTIFICATIONS
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    return
                }
            }
            notify(match.fixture.id, builder.build())
        }
        Log.d("notcheck", "Triggering notification for match starting soon.")
    }

    private fun checkMatchTimesAndNotify() {
        val currentTimeUtc = (System.currentTimeMillis() + 3 * 3600 * 1000) / 1000
        val favoriteMatches = favoritesManager.getFavorites()

        for (match in favoriteMatches) {
            val matchTime = match.fixture.timestamp
            showMatchNotification(match)
            if (matchTime > currentTimeUtc && matchTime - currentTimeUtc <= 60 && match.fixture.id !in triggeredMatchIds) {
                showMatchNotification(match)
                triggeredMatchIds.add(match.fixture.id)
                saveTriggeredMatchIds()
            }
        }
    }

    private fun fetchLeagues(callback: (List<LeaguesOfMatchViewer>) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            val client = OkHttpClient()
            val request = Request.Builder()
                .url("https://api-football-v1.p.rapidapi.com/v3/leagues?id=39&season=2023&last=15")
                .get()
                .addHeader("X-RapidAPI-Key", "f553732919msh90339d7020d3a54p16b6dfjsnae421a10c7ac")
                .addHeader("X-RapidAPI-Host", "api-football-v1.p.rapidapi.com")
                .build()

            try {
                val response = client.newCall(request).execute()
                val responseBody = response.body?.string()
                Log.d("Raw JSON", responseBody ?: "No response body")


                if (response.isSuccessful && responseBody != null) {
                    val fetchedLeagues = Gson().fromJson(responseBody, LeagueApiResponse::class.java).response
                    withContext(Dispatchers.Main) {
                        callback(fetchedLeagues)
                        Log.d("LeagueFetch", "Fetched leagues: $fetchedLeagues")
                    }
                } else {
                    Log.e("API Error", "Response not successful or body is null")
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Log.e("API Error", "Failed to fetch data: ${e.message}")
                }
            }
        }
    }

    private fun fetchMatches() {
        CoroutineScope(Dispatchers.IO).launch {
            val client = OkHttpClient()
            val request = Request.Builder()
                .url("https://api-football-v1.p.rapidapi.com/v3/fixtures?league=39&season=2023&last=15")
                .get()
                .addHeader("X-RapidAPI-Key", "f553732919msh90339d7020d3a54p16b6dfjsnae421a10c7ac")
                .addHeader("X-RapidAPI-Host", "api-football-v1.p.rapidapi.com")
                .build()

            try {
                val response = client.newCall(request).execute()
                val responseBody = response.body?.string()

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful && responseBody != null) {
                        val gson = Gson()
                        val apiResponse = gson.fromJson(responseBody, ApiResponse::class.java)
                        matches.addAll(apiResponse.response)
                    } else {
                        Log.e("API Error", "Response not successful or body is null")
                    }
                }
            } catch (e: IOException) {
                withContext(Dispatchers.Main) {
                    Log.e("API Error", "Failed to fetch data: ${e.message}")
                }
            }
        }
    }
}


@Composable
fun Header(navController: NavController, currentScreen: MutableState<String>) {
    Row(
        modifier = Modifier
            .padding(15.dp)
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Matches",
            fontWeight = FontWeight.Bold,
            fontFamily = fontFamily,
            fontSize = 20.sp,
            textDecoration = if (currentScreen.value == "main") TextDecoration.Underline else TextDecoration.None,
            modifier = Modifier.clickable { navController.navigate("main") }
        )
        Spacer(modifier = Modifier.width(16.dp))

        Text(
            text = "Favorites",
            fontWeight = FontWeight.Bold,
            fontFamily = fontFamily,
            fontSize = 20.sp,
            textDecoration = if (currentScreen.value == "favorites") TextDecoration.Underline else TextDecoration.None,
            modifier = Modifier.clickable { navController.navigate("favorites") }
        )

        Spacer(Modifier.width(16.dp))
        Text(
            text = "Leagues",
            fontWeight = FontWeight.Bold,
            fontFamily = fontFamily,
            fontSize = 20.sp,
            textDecoration = if (currentScreen.value == "leagues") TextDecoration.Underline else TextDecoration.None,
            modifier = Modifier.clickable { navController.navigate("leagues") }
        )
    }
}


val dummyTeamStatisticsResponse = TeamStatisticsResponse(
    get = "teams/statistics",
    parameters = Parameters(
        league = "39",
        season = "2023",
        team = "55"
    ),
    errors = listOf(),
    results = 11,
    paging = Paging(
        current = 1,
        total = 1
    ),
    response = Response(
        league = TeamLeague(
            id = 39,
            name = "Premier League",
            country = "England",
            logo = "https://media.api-sports.io/football/leagues/39.png",
            flag = "https://media.api-sports.io/flags/gb.svg",
            season = 2023
        ),
        team = TeamInfo(
            id = 55,
            name = "Brentford",
            logo = "https://media.api-sports.io/football/teams/55.png"
        ),
        form = "DWDDLLDLWWWLLWLLLLL",
        fixtures = TeamFixtures(
            played = Played(home = 10, away = 9, total = 19),
            wins = Wins(home = 3, away = 2, total = 5),
            draws = Draws(home = 3, away = 1, total = 4),
            loses = Loses(home = 4, away = 6, total = 10)
        ),
        goals = TeamGoals(
            `for` = ForAgainst(
                total = Total(home = 17, away = 9, total = 26),
                average = Average(home = "1.7", away = "1.0", total = "1.4"),
                minute = mapOf(
                    "0-15" to MinuteDetail(total = 3, percentage = "12.00%"),
                    "16-30" to MinuteDetail(total = 7, percentage = "28.00%"),
                    "31-45" to MinuteDetail(total = 3, percentage = "12.00%"),
                    "46-60" to MinuteDetail(total = 4, percentage = "16.00%"),
                    "61-75" to MinuteDetail(total = 3, percentage = "12.00%"),
                    "76-90" to MinuteDetail(total = 2, percentage = "8.00%"),
                    "91-105" to MinuteDetail(total = 3, percentage = "12.00%"),
                    "106-120" to MinuteDetail(total = null, percentage = null)
                )
            ),
            against = ForAgainst(
                total = Total(home = 18, away = 13, total = 31),
                average = Average(home = "1.8", away = "1.4", total = "1.6"),
                minute = mapOf(
                    "0-15" to MinuteDetail(total = 5, percentage = "15.63%"),
                    "16-30" to MinuteDetail(total = 4, percentage = "12.50%"),
                    "31-45" to MinuteDetail(total = 3, percentage = "9.38%"),
                    "46-60" to MinuteDetail(total = 5, percentage = "15.63%"),
                    "61-75" to MinuteDetail(total = 6, percentage = "18.75%"),
                    "76-90" to MinuteDetail(total = 7, percentage = "21.88%"),
                    "91-105" to MinuteDetail(total = 2, percentage = "6.25%"),
                    "106-120" to MinuteDetail(total = null, percentage = null)
                )
            )
        ),
        biggest = Biggest(
            streak = Streak(wins = 3, draws = 2, loses = 2),
            wins = HomeAway(home = "3-0", away = "0-3"),
            loses = HomeAway(home = "1-4", away = "3-0"),
            goals = GoalsBiggest(
                `for` = HomeAwayInt(home = 3, away = 3),
                against = HomeAwayInt(home = 4, away = 3)
            )
        ),
        clean_sheet = CleanSheet(home = 1, away = 2, total = 3),
        failed_to_score = FailedToScore(home = 1, away = 3, total = 4),
        penalty = Penalty(
            scored = ScoredMissed(total = 3, percentage = "100.00%"),
            missed = ScoredMissed(total = 0, percentage = "0%"),
            total = 3
        ),
        lineups = listOf(
            Lineup(formation = "4-3-3", played = 10),
            Lineup(formation = "5-3-2", played = 4),
            Lineup(formation = "3-5-2", played = 4),
            Lineup(formation = "4-4-2", played = 1)
        ),
        cards = Cards(
            yellow = mapOf(
                "0-15" to CardDetail(total = 2, percentage = "4.76%"),
                "16-30" to CardDetail(total = 4, percentage = "9.52%"),

            ),
            red = mapOf(
                "61-75" to CardDetail(total = 1, percentage = "100.00%")

            )
        )
    )
)







