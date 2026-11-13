package com.zhangs_tech.runtomo.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp

/** The main action of a screen: a tall pill that sinks a little when pressed. */
@Composable
fun PrimaryButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable RowScope.() -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    Button(
        onClick = onClick,
        modifier = modifier.heightIn(min = ButtonHeight).pressScale(interactionSource),
        enabled = enabled,
        colors = filledColors(),
        contentPadding = ButtonPadding,
        interactionSource = interactionSource,
        content = content,
    )
}

/** A second action next to a [PrimaryButton]. */
@Composable
fun SecondaryButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable RowScope.() -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.heightIn(min = ButtonHeight).pressScale(interactionSource),
        enabled = enabled,
        colors = outlinedColors(),
        border = outlinedBorder(enabled),
        contentPadding = ButtonPadding,
        interactionSource = interactionSource,
        content = content,
    )
}

/** A smaller pill button for inline card actions such as Claim, Give, or Accept. */
@Composable
fun CompactButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    contentPadding: PaddingValues = ButtonDefaults.ContentPadding,
    content: @Composable RowScope.() -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    Button(
        onClick = onClick,
        modifier = modifier.pressScale(interactionSource),
        enabled = enabled,
        colors = filledColors(),
        contentPadding = contentPadding,
        interactionSource = interactionSource,
        content = content,
    )
}

/** A smaller outlined pill button for secondary inline card actions such as Buy. */
@Composable
fun CompactOutlinedButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    contentPadding: PaddingValues = ButtonDefaults.ContentPadding,
    content: @Composable RowScope.() -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.pressScale(interactionSource),
        enabled = enabled,
        colors = outlinedColors(),
        border = outlinedBorder(enabled),
        contentPadding = contentPadding,
        interactionSource = interactionSource,
        content = content,
    )
}

/** A soft-tinted pill button for secondary form actions such as Find. */
@Composable
fun TonalButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable RowScope.() -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    FilledTonalButton(
        onClick = onClick,
        modifier = modifier.heightIn(min = 48.dp).pressScale(interactionSource),
        enabled = enabled,
        colors = ButtonDefaults.filledTonalButtonColors(
            disabledContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f),
        ),
        contentPadding = ButtonPadding,
        interactionSource = interactionSource,
        content = content,
    )
}

@Composable
private fun filledColors() = ButtonDefaults.buttonColors(
    disabledContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
    disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f),
)

@Composable
private fun outlinedColors() = ButtonDefaults.outlinedButtonColors(
    containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
    disabledContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
    disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f),
)

@Composable
private fun outlinedBorder(enabled: Boolean) = BorderStroke(
    width = 1.5.dp,
    color = if (enabled) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.outlineVariant,
)

@Composable
private fun Modifier.pressScale(interactionSource: MutableInteractionSource): Modifier {
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) 0.96f else 1f, spring(stiffness = 800f), label = "press")
    return graphicsLayer {
        scaleX = scale
        scaleY = scale
    }
}

private val ButtonHeight = 52.dp
private val ButtonPadding = PaddingValues(horizontal = 28.dp, vertical = 12.dp)
