package net.onefivefour.echolist.ui.edittasklist

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.format.DayOfWeekNames
import net.onefivefour.echolist.ui.common.GradientBackground
import net.onefivefour.echolist.ui.theme.EchoListTheme

private val dayAbbreviations = DayOfWeekNames.ENGLISH_ABBREVIATED.names

@Composable
internal fun DailyDetailContent(
    selectedDays: Set<DayOfWeek>,
    onDayToggled: (DayOfWeek, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .padding(horizontal = EchoListTheme.dimensions.l),
        horizontalArrangement = Arrangement.spacedBy(EchoListTheme.dimensions.xs),
        verticalAlignment = Alignment.CenterVertically
    ) {
        DayOfWeek.entries.forEachIndexed { index, day ->
            val isChecked = day in selectedDays

            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Checkbox(
                    checked = isChecked,
                    onCheckedChange = { checked -> onDayToggled(day, checked) }
                )
                Text(
                    text = dayAbbreviations[index],
                    style = EchoListTheme.typography.labelSmall,
                    color = EchoListTheme.materialColors.onSurface
                )
            }
        }
    }
}

@Preview
@Composable
private fun DailyDetailContentPreview() {
    EchoListTheme {
        GradientBackground {
            DailyDetailContent(
                selectedDays = setOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY),
                onDayToggled = { _, _ -> }
            )
        }
    }
}

@Preview
@Composable
private fun DailyDetailContentEmptyPreview() {
    EchoListTheme {
        GradientBackground {
            DailyDetailContent(
                selectedDays = emptySet(),
                onDayToggled = { _, _ -> }
            )
        }
    }
}
