package com.zhangs_tech.runtomo.tomo

import androidx.annotation.StringRes
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
import com.zhangs_tech.runtomo.R

// Main colors from docs/tomos/. Placeholder until the species art is in the app.
private val speciesColors = mapOf(
    "luppy" to Color(0xFFF28C38),
    "rocky" to Color(0xFF5E9E4A),
    "aeroblast" to Color(0xFF8FD3F4),
)

@Composable
fun TomoArt(species: Species, modifier: Modifier = Modifier, size: Dp = 96.dp) {
    ArtCircle(
        color = speciesColors[species.code] ?: MaterialTheme.colorScheme.secondary,
        label = species.name.take(1),
        size = size,
        modifier = modifier,
    )
}

/** A species the Player does not own yet (UC-11 step 1). */
@Composable
fun SpeciesSilhouette(modifier: Modifier = Modifier, size: Dp = 96.dp) {
    ArtCircle(color = MaterialTheme.colorScheme.outlineVariant, label = "?", size = size, modifier = modifier)
}

@Composable
private fun ArtCircle(color: Color, label: String, size: Dp, modifier: Modifier) {
    Box(
        modifier = modifier.size(size).background(color, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            color = Color.White,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
        )
    }
}

val Rarity.color: Color
    get() = when (this) {
        Rarity.COMMON -> Color(0xFF8D8D8D)
        Rarity.UNCOMMON -> Color(0xFF4CAF50)
        Rarity.RARE -> Color(0xFF2F80ED)
        Rarity.EPIC -> Color(0xFF9B51E0)
        Rarity.LEGENDARY -> Color(0xFFF2B01E)
    }

@get:StringRes
val Rarity.labelRes: Int
    get() = when (this) {
        Rarity.COMMON -> R.string.rarity_common
        Rarity.UNCOMMON -> R.string.rarity_uncommon
        Rarity.RARE -> R.string.rarity_rare
        Rarity.EPIC -> R.string.rarity_epic
        Rarity.LEGENDARY -> R.string.rarity_legendary
    }
