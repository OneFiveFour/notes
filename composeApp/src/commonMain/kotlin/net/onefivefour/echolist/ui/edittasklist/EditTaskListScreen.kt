package net.onefivefour.echolist.ui.edittasklist

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import net.onefivefour.echolist.ui.common.ElButton
import net.onefivefour.echolist.ui.theme.EchoListTheme

@Composable
fun EditTaskListScreen(
    uiState: EditTaskListUiState,
    onSaveClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dimensions = EchoListTheme.dimensions

    Column(
        modifier = modifier
            .padding(
                horizontal = dimensions.xl,
                vertical = dimensions.l
            )
    ) {
        BasicTextField(
            state = uiState.titleState,
            modifier = Modifier.fillMaxWidth(),
            textStyle = EchoListTheme.typography.titleLarge.copy(
                color = EchoListTheme.materialColors.onBackground
            )
        )

        uiState.error?.let { errorMessage ->
            Spacer(modifier = Modifier.height(dimensions.s))
            Text(
                text = errorMessage,
                style = EchoListTheme.typography.bodySmall,
                color = EchoListTheme.materialColors.error,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = dimensions.xs, top = dimensions.xxs)
            )
        }

        Spacer(modifier = Modifier.height(dimensions.xl))

        ElButton(
            onClick = onSaveClick,
            isEnabled = uiState.isSaveEnabled
        ) {
            Text(
                text = "Save",
                style = EchoListTheme.typography.labelMedium
            )
        }
    }
}
