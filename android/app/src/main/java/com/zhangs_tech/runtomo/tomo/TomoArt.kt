package com.zhangs_tech.runtomo.tomo

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// Main colors from docs/tomos/. Placeholder until the species art is in the app.
private val speciesColors = mapOf(
    "luppy" to Color(0xFFF28C38),
    "rocky" to Color(0xFF5E9E4A),
    "aeroblast" to Color(0xFF8FD3F4),
)

@Composable
fun TomoArt(species: Species, modifier: Modifier = Modifier, size: Dp = 96.dp) {
    Box(
        modifier = modifier
            .size(size)
            .background(speciesColors[species.code] ?: MaterialTheme.colorScheme.secondary, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = species.name.take(1),
            color = Color.White,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
        )
    }
}
