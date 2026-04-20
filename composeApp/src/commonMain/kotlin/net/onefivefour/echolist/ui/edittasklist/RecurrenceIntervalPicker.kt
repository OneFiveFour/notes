package net.onefivefour.echolist.ui.edittasklist

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import net.onefivefour.echolist.ui.common.GradientBackground
import net.onefivefour.echolist.ui.theme.EchoListTheme

@Composable
internal fun RecurrenceIntervalPicker(
    selectedInterval: RecurrenceInterval,
    onIntervalSelected: (RecurrenceInterval) -> Unit,
    modifier: Modifier = Modifier
) {
    val intervals = remember { RecurrenceInterval.entries }

    Row(
        modifier = modifier
            .padding(horizontal = EchoListTheme.dimensions.l),
        horizontalArrangement = Arrangement.spacedBy(EchoListTheme.dimensions.xs),
        verticalAlignment = Alignment.CenterVertically
    ) {
        intervals.forEach { interval ->
            val isSelected = interval == selectedInterval

            Surface(
                onClick = { onIntervalSelected(interval) },
                shape = EchoListTheme.shapes.small,
                color = if (isSelected) {
                    EchoListTheme.materialColors.primary
                } else {
                    EchoListTheme.materialColors.surfaceVariant
                },
                modifier = Modifier.animateContentSize()
            ) {
                Text(
                    text = if (isSelected) interval.fullLabel else interval.shortLabel,
                    style = EchoListTheme.typography.labelMedium,
                    color = if (isSelected) {
                        EchoListTheme.materialColors.onPrimary
                    } else {
                        EchoListTheme.materialColors.onSurfaceVariant
                    },
                    modifier = Modifier.padding(
                        horizontal = EchoListTheme.dimensions.l,
                        vertical = EchoListTheme.dimensions.s
                    )
                )
            }
        }
    }
}

@Preview
@Composable
private fun RecurrenceIntervalPickerPreview() {
    EchoListTheme {
        GradientBackground {
            RecurrenceIntervalPicker(
                selectedInterval = RecurrenceInterval.Off,
                onIntervalSelected = {}
            )
        }
    }
}

@Preview
@Composable
private fun RecurrenceIntervalPickerWeeklyPreview() {
    EchoListTheme {
        GradientBackground {
            RecurrenceIntervalPicker(
                selectedInterval = RecurrenceInterval.Weekly,
                onIntervalSelected = {}
            )
        }
    }
}
