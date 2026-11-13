package com.zhangs_tech.runtomo.tomo

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.zhangs_tech.runtomo.R

/** Which picture of the Tomo to show. The art is in assets/tomos/v1. */
enum class TomoPose { DEFAULT, HAPPY, EXCITED, ACTIVITY, AVATAR }

/** UC-10: the picture for each mood. Relaxed uses the default picture. */
val Mood.pose: TomoPose
    get() = when (this) {
        Mood.EXCITED -> TomoPose.EXCITED
        Mood.HAPPY -> TomoPose.HAPPY
        Mood.RELAXED -> TomoPose.DEFAULT
    }

@Composable
fun TomoArt(
    species: Species,
    modifier: Modifier = Modifier,
    size: Dp = 96.dp,
    pose: TomoPose = TomoPose.DEFAULT,
) {
    val art = tomoArt(species.code, pose)
    if (art != null) {
        Image(painterResource(art), contentDescription = null, modifier = modifier.size(size))
    } else {
        // A species the app has no art for yet, for example one the server added later.
        LetterCircle(species.name.take(1), MaterialTheme.colorScheme.secondary, size, modifier)
    }
}

/** A species the Player does not own yet (UC-11 step 1): its picture in one grey color. */
@Composable
fun SpeciesSilhouette(species: Species?, modifier: Modifier = Modifier, size: Dp = 96.dp) {
    val art = species?.let { tomoArt(it.code, TomoPose.DEFAULT) }
    if (art != null) {
        Image(
            painter = painterResource(art),
            contentDescription = null,
            modifier = modifier.size(size),
            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.outlineVariant, BlendMode.SrcIn),
        )
    } else {
        LetterCircle("?", MaterialTheme.colorScheme.outlineVariant, size, modifier)
    }
}

@Composable
private fun LetterCircle(label: String, color: Color, size: Dp, modifier: Modifier) {
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

/** Species codes are permanent, so the app can map them to its art. */
@DrawableRes
private fun tomoArt(speciesCode: String, pose: TomoPose): Int? = when (speciesCode) {
    "luppy" -> when (pose) {
        TomoPose.DEFAULT -> R.drawable.tomo_luppy_default
        TomoPose.HAPPY -> R.drawable.tomo_luppy_happy
        TomoPose.EXCITED -> R.drawable.tomo_luppy_excited
        TomoPose.ACTIVITY -> R.drawable.tomo_luppy_activity
        TomoPose.AVATAR -> R.drawable.tomo_luppy_avatar
    }
    "rocky" -> when (pose) {
        TomoPose.DEFAULT -> R.drawable.tomo_rocky_default
        TomoPose.HAPPY -> R.drawable.tomo_rocky_happy
        TomoPose.EXCITED -> R.drawable.tomo_rocky_excited
        TomoPose.ACTIVITY -> R.drawable.tomo_rocky_activity
        TomoPose.AVATAR -> R.drawable.tomo_rocky_avatar
    }
    "aeroblast" -> when (pose) {
        TomoPose.DEFAULT -> R.drawable.tomo_aeroblast_default
        TomoPose.HAPPY -> R.drawable.tomo_aeroblast_happy
        TomoPose.EXCITED -> R.drawable.tomo_aeroblast_excited
        TomoPose.ACTIVITY -> R.drawable.tomo_aeroblast_activity
        TomoPose.AVATAR -> R.drawable.tomo_aeroblast_avatar
    }
    else -> null
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
