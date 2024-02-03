package com.example.esportsevent

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@Composable
fun FavoritesScreen(navController: NavController, favorites: List<Match>, currentScreen: MutableState<String>, toggleFavorite: (Match) -> Unit) {
    Column {
        Header(navController, currentScreen)

        if (favorites.isEmpty()) {
            Text(
                text = "You don't have favorite matches yet.",
                fontFamily = fontFamily,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
            )
        } else {
            LazyColumn {
                favorites.groupBy { it.fixture.date }.forEach { (date, matchesOnDate) ->
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
                        MatchRow(match, favorites, toggleFavorite, navController)
                    }
                }
            }
        }
    }
}

