package com.zhangs_tech.runtomo.tomo

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.zhangs_tech.runtomo.R
import com.zhangs_tech.runtomo.core.formatKm
import com.zhangs_tech.runtomo.ui.components.GroundedGlow
import com.zhangs_tech.runtomo.ui.components.RuntomoCard
import com.zhangs_tech.runtomo.ui.components.SecondaryButton
import com.zhangs_tech.runtomo.ui.theme.RuntomoTheme
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

/** The Tomo's profile card (UC-09 step 4, UC-11 step 4), with optional [actions] below the details. */
@Composable
fun TomoProfileDialog(
    tomo: Tomo,
    onDismiss: () -> Unit,
    actions: @Composable ColumnScope.() -> Unit,
) {
    Dialog(onDismissRequest = onDismiss) {
        TomoProfileContent(tomo, actions = actions)
    }
}

@Composable
fun TomoProfileContent(
    tomo: Tomo,
    modifier: Modifier = Modifier,
    actions: @Composable ColumnScope.() -> Unit,
) {
    RuntomoCard(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
    ) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    GroundedGlow(modifier = Modifier.size(140.dp), color = tomo.species.rarity.color)
                    TomoArt(tomo.species, size = 112.dp)
                }
                Text(tomo.nickname, style = MaterialTheme.typography.headlineSmall)
                Row(
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.surfaceContainer, CircleShape)
                        .padding(horizontal = 12.dp, vertical = 5.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(Modifier.size(10.dp).background(tomo.species.rarity.color, CircleShape))
                    Text(
                        text = stringResource(tomo.species.rarity.labelRes),
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceContainerLow, MaterialTheme.shapes.small)
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    ProfileStatRow(stringResource(R.string.profile_species), tomo.species.name)
                    ProfileStatRow(stringResource(R.string.profile_bond_level), tomo.bondLevel.toString())
                    ProfileStatRow(
                        label = stringResource(R.string.profile_distance_together),
                        value = stringResource(R.string.leaderboard_km, formatKm(tomo.distanceTogetherM)),
                    )
                    ProfileStatRow(stringResource(R.string.profile_birthday), formatDate(tomo.birthday))
                }
                actions()
            }
        }
}

@Composable
private fun ProfileStatRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall,
        )
    }
}

private fun formatDate(isoDate: String): String =
    LocalDate.parse(isoDate).format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM))

@Preview(showBackground = true)
@Composable
private fun TomoProfilePreview() {
    val luppy = Species("luppy", "Luppy", Rarity.COMMON, "Runs hot.")
    RuntomoTheme {
        Box(
            modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(24.dp),
            contentAlignment = Alignment.Center,
        ) {
            TomoProfileContent(
                tomo = Tomo("t1", luppy, "Tung", 120, 1, 700, "2026-11-13"),
            ) {
                SecondaryButton(onClick = {}, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.close))
                }
            }
        }
    }
}
