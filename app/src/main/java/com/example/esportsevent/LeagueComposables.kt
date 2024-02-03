package com.example.esportsevent

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberImagePainter
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun LeagueListScreen(navController: NavController, leagues: List<LeaguesOfMatchViewer>, currentScreen: MutableState<String>) {
    Column {
        Header(navController, currentScreen)
        LazyColumn {
            val chunkedLeagues = leagues.chunked(3)
            chunkedLeagues.forEach { leagueRow ->
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        leagueRow.forEach { league ->
                            LeagueRow(league.league, navController, Modifier.weight(1f))
                        }
                        if (leagueRow.size < 3) {
                            repeat(3 - leagueRow.size) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LeagueRow(league: Leagues, navController: NavController, modifier: Modifier = Modifier) {
    val localDensity = LocalDensity.current
    BoxWithConstraints(modifier = modifier) {
        val cardWidth = maxWidth
        var cardHeight by remember { mutableStateOf(0.dp) }

        Card(
            modifier = Modifier
                .padding(8.dp)
                .clickable { navController.navigate("leagueDetails/${league.id}") }
                .width(cardWidth)
                .height(IntrinsicSize.Min)
                .onGloballyPositioned { coordinates ->
                    cardHeight = with(localDensity) { coordinates.size.height.toDp() }
                },
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(8.dp).fillMaxHeight().align(Alignment.CenterHorizontally),
            ) {
                Image(
                    painter = rememberImagePainter(league.logo),
                    contentDescription = "League Logo",
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape)
                )
                Text(
                    text = league.name,
                    fontFamily = fontFamily,
                    textAlign = TextAlign.Center
                )
            }
        }
        Spacer(modifier = Modifier.height(cardHeight))
    }
}


@Composable
fun LeagueDetailsScreen(
    leagueId: Int,
    leagues: List<LeaguesOfMatchViewer>,
    matches: List<Match>,
    favorites: List<Match>,
    navController: NavController,
    toggleFavorite: (Match) -> Unit
) {
    val league = leagues.find { it.league.id == leagueId }
    val matchesInLeague = matches.filter { it.league.id == leagueId }
    val groupedMatches = matchesInLeague.groupBy { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(it.fixture.date)?.let { date -> SimpleDateFormat("dd MMM", Locale.getDefault()).format(date) } ?: it.fixture.date }.toSortedMap()

    Column(modifier = Modifier.padding(start = 16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { navController.navigateUp() }) {
                Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Go back")
            }
            Text("League Details", fontWeight = FontWeight.Bold, fontSize = 20.sp, fontFamily = fontFamily)
        }

        Spacer(modifier = Modifier.height(8.dp))

        // League details
        league?.let {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = rememberImagePainter(it.country.flag),
                    contentDescription = "Country Flag",
                    modifier = Modifier.size(30.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(it.country.name, fontWeight = FontWeight.Medium, fontFamily = fontFamily)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = rememberImagePainter(it.league.logo),
                    contentDescription = "League Logo",
                    modifier = Modifier.size(30.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(it.league.name, fontWeight = FontWeight.Medium, fontFamily = fontFamily)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn {
            groupedMatches.forEach { (date, matchesOnDate) ->
                item {
                    Text(date, fontWeight = FontWeight.Bold, fontSize = 18.sp, fontFamily = fontFamily)
                    Spacer(modifier = Modifier.height(8.dp))
                }
                items(matchesOnDate) { match ->
                    MatchRow(match, favorites, toggleFavorite, navController)
                }
            }
        }
    }
}