package net.onefivefour.echolist.ui.edittasklist

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import echolist.composeapp.generated.resources.Res
import echolist.composeapp.generated.resources.edit_tasklist_placeholder
import echolist.composeapp.generated.resources.edit_tasklist_title_edit
import echolist.composeapp.generated.resources.edit_tasklist_title_new
import echolist.composeapp.generated.resources.navigate_back
import echolist.composeapp.generated.resources.save_button
import net.onefivefour.echolist.ui.theme.EchoListTheme
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTaskListScreen(
    taskListId: String?,
    text: String,
    onTextChange: (String) -> Unit,
    onSaveClick: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = EchoListTheme.materialColors
    val typography = EchoListTheme.typography
    val dimensions = EchoListTheme.dimensions

    val title = if (taskListId == null) {
        stringResource(Res.string.edit_tasklist_title_new)
    } else {
        stringResource(Res.string.edit_tasklist_title_edit)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        TopAppBar(
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = colors.background
            ),
            title = {
                Text(
                    text = title,
                    style = typography.titleLarge,
                    color = colors.primary
                )
            },
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(Res.string.navigate_back),
                        tint = colors.primary
                    )
                }
            }
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = dimensions.l)
        ) {
            OutlinedTextField(
                value = text,
                onValueChange = onTextChange,
                modifier = Modifier.fillMaxWidth(),
                textStyle = typography.bodyMedium.copy(color = colors.onBackground),
                placeholder = {
                    Text(
                        text = stringResource(Res.string.edit_tasklist_placeholder),
                        style = typography.bodyMedium,
                        color = colors.onBackground.copy(alpha = 0.6f)
                    )
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = colors.primary,
                    unfocusedBorderColor = colors.onBackground.copy(alpha = 0.3f),
                    cursorColor = colors.primary
                )
            )

            Spacer(modifier = Modifier.height(dimensions.l))

            Button(
                onClick = onSaveClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.primary,
                    contentColor = colors.onPrimary
                )
            ) {
                Text(
                    text = stringResource(Res.string.save_button),
                    style = typography.labelMedium
                )
            }
        }
    }
}