package net.onefivefour.echolist.ui.recurrence

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import net.onefivefour.echolist.ui.common.GradientBackground
import net.onefivefour.echolist.ui.theme.EchoListTheme

/**
 * Returns the formatted monthly display string for the given month interval and day of month.
 */
internal fun monthlyFormatString(n: Int, m: Int): String = "Every $n month(s) on the ${m}th day"

@Composable
internal fun MonthlyDetailContent(
    everyNMonths: Int,
    dayOfMonth: Int,
    onMonthIntervalChanged: (Int) -> Unit,
    onDayOfMonthChanged: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(EchoListTheme.dimensions.m)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(EchoListTheme.dimensions.s),
            verticalAlignment = Alignment.CenterVertically
        ) {
            LabelText(text = "Repeat every")

            NumberInputField(
                value = everyNMonths.toString(),
                onValueChange = { newValue ->
                    if (isValidPositiveInt(newValue)) {
                        newValue.toIntOrNull()?.let { onMonthIntervalChanged(it) }
                    }
                }
            )

            LabelText(text = if (everyNMonths == 1) "month" else "months")
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(EchoListTheme.dimensions.s),
            verticalAlignment = Alignment.CenterVertically
        ) {
            LabelText(text = "On day")

            NumberInputField(
                value = dayOfMonth.toString(),
                onValueChange = { newValue ->
                    if (isValidDayOfMonth(newValue)) {
                        newValue.toIntOrNull()?.let { onDayOfMonthChanged(it) }
                    }
                }
            )

            LabelText(text = "of the month")
        }
    }
}

@Preview
@Composable
private fun MonthlyDetailContentPreview() {
    EchoListTheme {
        GradientBackground {
            MonthlyDetailContent(
                everyNMonths = 1,
                dayOfMonth = 15,
                onMonthIntervalChanged = {},
                onDayOfMonthChanged = {}
            )
        }
    }
}

@Preview
@Composable
private fun MonthlyDetailContentMultiMonthPreview() {
    EchoListTheme {
        GradientBackground {
            MonthlyDetailContent(
                everyNMonths = 3,
                dayOfMonth = 1,
                onMonthIntervalChanged = {},
                onDayOfMonthChanged = {}
            )
        }
    }
}
