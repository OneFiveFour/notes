package net.onefivefour.echolist.ui.edittasklist

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
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
    val textStyle = EchoListTheme.typography.bodyMedium
    val textColor = EchoListTheme.materialColors.onSurface

    Row(
        modifier = modifier
            .padding(horizontal = EchoListTheme.dimensions.l),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Every ",
            style = textStyle,
            color = textColor
        )

        BasicTextField(
            value = everyNMonths.toString(),
            onValueChange = { newValue ->
                if (isValidPositiveInt(newValue)) {
                    newValue.toIntOrNull()?.let { onMonthIntervalChanged(it) }
                }
            },
            textStyle = textStyle.copy(color = textColor),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier.width(40.dp)
        )

        Text(
            text = " month(s) on the ",
            style = textStyle,
            color = textColor
        )

        BasicTextField(
            value = dayOfMonth.toString(),
            onValueChange = { newValue ->
                if (isValidDayOfMonth(newValue)) {
                    newValue.toIntOrNull()?.let { onDayOfMonthChanged(it) }
                }
            },
            textStyle = textStyle.copy(color = textColor),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier.width(40.dp)
        )

        Text(
            text = "th day",
            style = textStyle,
            color = textColor
        )
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
