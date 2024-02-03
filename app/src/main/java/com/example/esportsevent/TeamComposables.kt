package com.example.esportsevent

import androidx.compose.foundation.Image
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.rememberImagePainter

@Composable
fun TeamInfo(team: Team) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.height(IntrinsicSize.Min)
    ) {
        Image(
            painter = rememberImagePainter(team.logo),
            contentDescription = "Team Logo",
            modifier = Modifier
                .size(25.dp)
                .clip(CircleShape)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = team.name,
            fontFamily = fontFamily,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f)
        )

    }
}

@Composable
fun TeamFormRow(form: String, weight: Modifier) {
    val scrollState = rememberScrollState()
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier
            .horizontalScroll(scrollState)
            .padding(5.dp)
    ) {
        form.forEach { result ->
            FormCard(result.toString())
        }
    }
}

@Composable
fun TeamLogoAndName(team: TeamInfo, weight: Modifier) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Image(
            painter = rememberImagePainter(team.logo),
            contentDescription = "Team Logo",
            modifier = Modifier.size(20.dp).clip(CircleShape)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = team.name,
            fontFamily = fontFamily
        )
    }
}

@Composable
fun FormCard(result: String) {
    val backgroundColor = when (result.uppercase()) {
        "W" -> Color(0xFF4CAF50)
        "L" -> Color(0xFFF44336)
        "D" -> Color(0xFFFFEB3B)
        else -> Color.Gray
    }

    Card(
        modifier = Modifier
            .padding(2.dp)
            .size(30.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            Text(
                text = result,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

