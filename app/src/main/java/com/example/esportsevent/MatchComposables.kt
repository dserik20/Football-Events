package com.example.esportsevent

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberImagePainter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.text.style.TextOverflow

@Composable
fun EsportsEventApp(
    navController: NavController,
    matches: List<Match>,
    favoritesManager: FavoritesManager,
    currentScreen: MutableState<String>,
    toggleFavorite: (Match) -> Unit
) {
    val groupedMatches = matches.groupBy { it.fixture.date }
        .toSortedMap(compareBy { it }) // Ensure the dates are sorted

    LazyColumn {
        item { Header(navController, currentScreen) }

        groupedMatches.forEach { (date, matchesOnDate) ->
            item {
                Text(
                    text = getTitleForDate(date),
                    modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 8.dp),
                    fontWeight = FontWeight.Bold,
                    fontFamily = fontFamily,
                    fontSize = 18.sp
                )
            }

            items(matchesOnDate) { match ->
                MatchRow(match, favoritesManager.getFavorites(), toggleFavorite, navController)
            }
        }
    }
}

@Composable
fun MatchRow(match: Match, favorites: List<Match>, toggleFavorite: (Match) -> Unit, navController: NavController) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
            .clickable { navController.navigate("matchDetails/${match.fixture.id}") },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column {
            Column(modifier = Modifier.padding(8.dp)) {
                MatchHeader(match, favorites, toggleFavorite)
            }
        }
    }
}


@Composable
fun MatchHeader(match: Match, favorites: List<Match>, toggleFavorite: (Match) -> Unit) {
    val isFavorite = match in favorites // Check if match is in favorites
    val dateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    dateFormat.timeZone = TimeZone.getTimeZone("UTC")
    val matchTime = dateFormat.format(Date(match.fixture.timestamp * 1000))
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        IconButton(
            onClick = { toggleFavorite(match) },
            modifier = Modifier
                .size(24.dp)
        ) {
            Icon(
                imageVector = if (isFavorite) Icons.Filled.Star else Icons.Default.Star,
                contentDescription = "Favorite",
                tint = if (isFavorite) Color.Green else Color.Gray
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.weight(1f)
        ) {
            TeamInfo(team = match.teams.home)
            Spacer(modifier = Modifier.height(8.dp))
            TeamInfo(team = match.teams.away)
        }

        Text(
            text = matchTime,
            fontFamily = fontFamily,
            fontWeight = FontWeight.Bold
        )
    }
}


@Composable
fun MatchDetailsScreen(match: Match, navController: NavController, viewModel: MatchDetailViewModel, teamStatistics: TeamStatisticsResponse = dummyTeamStatisticsResponse) {
    val dateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    dateFormat.timeZone = TimeZone.getTimeZone("UTC")
    val matchTime = dateFormat.format(Date(match.fixture.timestamp * 1000))

    val homeTeamStatistics = viewModel.homeTeamStatistics.observeAsState()
    val awayTeamStatistics = viewModel.awayTeamStatistics.observeAsState()

    LaunchedEffect(Unit) {
        viewModel.fetchTeamStatistics(match.teams.home.id, match.teams.away.id, match.league.id, match.league.season)
    }

    LazyColumn(modifier = Modifier.padding(16.dp)) {
        item {
            HeaderRow(navController)
        }
        item { Spacer(modifier = Modifier.height(16.dp)) }
        item { TeamInformationCard(match) }
        item { Spacer(modifier = Modifier.height(16.dp)) }
        item { MatchLocation(match) }
        item { Spacer(modifier = Modifier.height(16.dp)) }
        item { LeagueInformation(match, navController) }
        item { Spacer(modifier = Modifier.height(16.dp)) }
        item { CountryAndRoundRow(match) }
        item { Spacer(modifier = Modifier.height(16.dp)) }
        item { TeamStatisticsRow(homeTeamStatistics, awayTeamStatistics) }
    }
}


@Composable
fun HeaderRow(navController: NavController) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = { navController.navigate("main") }) {
            Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Go back")
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "Match Details",
            fontWeight = FontWeight.Bold,
            fontFamily = fontFamily,
            fontSize = 24.sp
        )
    }
}

@Composable
fun TeamInformationCard(match: Match) {
    val dateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    dateFormat.timeZone = TimeZone.getTimeZone("UTC")
    val matchTime = dateFormat.format(Date(match.fixture.timestamp * 1000))

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Home Team
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Image(
                    painter = rememberImagePainter(match.teams.home.logo),
                    contentDescription = "Home Team Logo",
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                )
                Text(
                    text = match.teams.home.name,
                    color = Color.White,
                    fontFamily = fontFamily,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Match Time
            Text(
                text = matchTime,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontFamily = fontFamily,
            )

            // Away Team
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Image(
                    painter = rememberImagePainter(match.teams.away.logo),
                    contentDescription = "Away Team Logo",
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                )
                Text(
                    text = match.teams.away.name,
                    color = Color.White,
                    fontFamily = fontFamily,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun MatchLocation(match: Match) {
    Text(
        text = "Match Location",
        fontFamily = fontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp
    )
    Spacer(modifier = Modifier.height(16.dp))
    Column(){
        Row(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.surface)
                .fillMaxWidth()
                .padding(16.dp)
                .clip(RoundedCornerShape(32.dp)),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ){
            Text("Venue", fontFamily = fontFamily)
            Spacer(modifier = Modifier.width(16.dp))
            Text("${match.fixture.venue.name}", fontFamily = fontFamily)
        }
        Row(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.surface)
                .fillMaxWidth()
                .clip(RoundedCornerShape(32.dp))
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ){
            Text("City", fontFamily = fontFamily)
            Spacer(modifier = Modifier.width(16.dp))
            Text("${match.fixture.venue.city}", fontFamily = fontFamily)
        }

    }
}

@Composable
fun LeagueInformation(match: Match, navController: NavController) {
    Text(
        text = "League",
        fontFamily = fontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp
    )
    Spacer(modifier = Modifier.height(16.dp))
    Column(){
        Row(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.surface)
                .fillMaxWidth()
                .padding(16.dp)
                .clip(RoundedCornerShape(32.dp))
                .clickable {
                    navController.navigate("leagueDetails/${match.league.id}")
                },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text("Name  ", fontFamily = fontFamily)
            Spacer(modifier = Modifier.width(16.dp))
            Text("${match.league.name}", fontFamily = fontFamily)
        }
        Row(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.surface)
                .fillMaxWidth()
                .clip(RoundedCornerShape(32.dp))
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ){
            Text("Season", fontFamily = fontFamily)
            Spacer(modifier = Modifier.width(16.dp))
            Text("${match.league.season}", fontFamily = fontFamily)
        }
    }
}

@Composable
fun CountryAndRoundRow(match: Match) {
    Text(
        text = "Country and round",
        fontFamily = fontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp
    )
    Spacer(modifier = Modifier.height(16.dp))
    Row(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.surface)
            .fillMaxWidth()
            .clip(RoundedCornerShape(32.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ){
        Row(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.surface),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ){
            Image(
                painter = rememberImagePainter(match.league.flag),
                contentDescription = "League Country Logo",
                modifier = Modifier.size(30.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text("${match.league.country}", fontFamily = fontFamily, )
        }
        Text("${match.league.round}", fontFamily = fontFamily,)
    }
}

@Composable
fun TeamStatisticsRow(homeTeamStatistics: State<TeamStatisticsResponse?>, awayTeamStatistics: State<TeamStatisticsResponse?>, teamStatistics : TeamStatisticsResponse = dummyTeamStatisticsResponse) {
    Text(
        text = "Forms",
        fontFamily = fontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp
    )
    Column(modifier = Modifier.padding(0.dp)) {
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(16.dp)
                .clip(RoundedCornerShape(32.dp)),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
                    homeTeamStatistics.value?.let { stats ->
                        TeamLogoAndName(stats.response.team, Modifier.weight(0.5f))
                        Spacer(modifier = Modifier.width(10.dp))
                        TeamFormRow(stats.response.form, Modifier.weight(0.5f))
                    }
        }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(16.dp)
                        .clip(RoundedCornerShape(32.dp)),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    awayTeamStatistics.value?.let { stats ->
                        TeamLogoAndName(stats.response.team, Modifier.weight(0.5f))
                        Spacer(modifier = Modifier.width(10.dp))
                        TeamFormRow(stats.response.form, Modifier.weight(0.5f))
                    }
                }
        }
}