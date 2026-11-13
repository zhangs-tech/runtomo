package com.zhangs_tech.runtomo.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.zhangs_tech.runtomo.R

@Composable
fun CoinIcon(modifier: Modifier = Modifier, size: Dp = 18.dp) {
    Image(painterResource(R.drawable.ic_coin), contentDescription = null, modifier = modifier.size(size))
}

/** A coin icon and a number. [description] is what a screen reader says instead. */
@Composable
fun CoinAmount(
    text: String,
    description: String,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.labelLarge,
    iconSize: Dp = 18.dp,
) {
    Row(
        modifier = modifier.clearAndSetSemantics { contentDescription = description },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        CoinIcon(size = iconSize)
        Text(text, style = style)
    }
}

/** The Player's coin balance as a pill, for top bars. */
@Composable
fun CoinBalanceChip(text: String, description: String, modifier: Modifier = Modifier) {
    CoinAmount(
        text = text,
        description = description,
        modifier = modifier
            .background(MaterialTheme.colorScheme.surfaceContainerLowest, CircleShape)
            .border(1.5.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)
            .padding(start = 6.dp, end = 12.dp, top = 4.dp, bottom = 4.dp),
        style = MaterialTheme.typography.titleSmall,
        iconSize = 22.dp,
    )
}
