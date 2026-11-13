package com.zhangs_tech.runtomo.tomo

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.zhangs_tech.runtomo.R
import com.zhangs_tech.runtomo.core.formatKm
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
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                TomoArt(tomo.species, size = 128.dp)
                Text(tomo.nickname, style = MaterialTheme.typography.headlineSmall)
                Text(stringResource(R.string.profile_species, tomo.species.name))
                Text(
                    text = stringResource(R.string.profile_rarity, stringResource(tomo.species.rarity.labelRes)),
                    color = tomo.species.rarity.color,
                )
                Text(stringResource(R.string.profile_bond_level, tomo.bondLevel))
                Text(stringResource(R.string.profile_distance_together, formatKm(tomo.distanceTogetherM)))
                Text(stringResource(R.string.profile_birthday, formatDate(tomo.birthday)))
                actions()
            }
        }
    }
}

private fun formatDate(isoDate: String): String =
    LocalDate.parse(isoDate).format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM))
