package net.onefivefour.echolist.ui.recurrence

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
 * Returns the formatted weekly display string for the given week count.
 */
internal fun weeklyFormatString(n: Int): String = "Every $n week(s)"

@Composable
internal fun WeeklyDetailContent(
    everyNWeeks: Int,
    onWeekCountChanged: (Int) -> Unit,
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
            value = everyNWeeks.toString(),
            onValueChange = { newValue ->
                if (isValidPositiveInt(newValue)) {
                    newValue.toIntOrNull()?.let { onWeekCountChanged(it) }
                }
            },
            textStyle = textStyle.copy(color = textColor),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier.width(40.dp)
        )

        Text(
            text = " week(s)",
            style = textStyle,
            color = textColor
        )
    }
}

@Preview
@Composable
private fun WeeklyDetailContentPreview() {
    EchoListTheme {
        GradientBackground {
            WeeklyDetailContent(
                everyNWeeks = 1,
                onWeekCountChanged = {}
            )
        }
    }
}

@Preview
@Composable
private fun WeeklyDetailContentMultiWeekPreview() {
    EchoListTheme {
        GradientBackground {
            WeeklyDetailContent(
                everyNWeeks = 3,
                onWeekCountChanged = {}
            )
        }
    }
}
