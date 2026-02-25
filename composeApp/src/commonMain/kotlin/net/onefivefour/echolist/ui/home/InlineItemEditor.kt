package net.onefivefour.echolist.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import net.onefivefour.echolist.ui.theme.EchoListTheme
import net.onefivefour.echolist.ui.theme.LocalDimensions

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun InlineItemEditor(
    value: String,
    onValueChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
    isLoading: Boolean,
    errorMessage: String?,
    icon: ImageVector,
    placeholder: String,
    modifier: Modifier = Modifier
) {
    val dimensions = LocalDimensions.current
    val primaryColor = MaterialTheme.colorScheme.primary
    val onPrimaryColor = MaterialTheme.colorScheme.onPrimary
    val mediumShape = MaterialTheme.shapes.medium
    val smallShape = MaterialTheme.shapes.small

    val focusRequester = remember { FocusRequester() }
    val bringIntoViewRequester = remember { BringIntoViewRequester() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
        bringIntoViewRequester.bringIntoView()
    }

    Column(modifier = modifier.bringIntoViewRequester(bringIntoViewRequester)) {
        Row(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.surface, mediumShape)
                .border(dimensions.borderWidth, primaryColor, mediumShape)
                .padding(dimensions.m),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(dimensions.iconMedium)
                    .background(primaryColor, smallShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = onPrimaryColor
                )
            }

            Spacer(modifier = Modifier.width(dimensions.s))

            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier
                    .weight(1f)
                    .focusRequester(focusRequester),
                textStyle = MaterialTheme.typography.titleSmall.merge(
                    TextStyle(color = MaterialTheme.colorScheme.onSurface)
                ),
                singleLine = true,
                cursorBrush = SolidColor(primaryColor),
                decorationBox = { innerTextField ->
                    Box {
                        if (value.isEmpty()) {
                            Text(
                                text = placeholder,
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                        }
                        innerTextField()
                    }
                }
            )

            Spacer(modifier = Modifier.width(dimensions.s))

            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(dimensions.xl),
                    color = primaryColor,
                    strokeWidth = 2.dp
                )
            } else {
                IconButton(onClick = onConfirm) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Confirm",
                        tint = primaryColor
                    )
                }
            }

            IconButton(onClick = onCancel, enabled = !isLoading) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Cancel",
                    tint = primaryColor
                )
            }
        }

        if (errorMessage != null) {
            Text(
                text = errorMessage,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(
                    start = dimensions.m,
                    top = dimensions.xs
                )
            )
        }
    }
}

@Preview
@Composable
fun InlineItemEditorPreview() {
    EchoListTheme {
        InlineItemEditor(
            value = "",
            onValueChange = { },
            onConfirm = { },
            onCancel = { },
            isLoading = false,
            errorMessage = null,
            icon = Icons.Default.Folder,
            placeholder = "Folder name",
        )
    }
}

@Preview
@Composable
fun InlineItemEditorLoadingPreview() {
    EchoListTheme {
        InlineItemEditor(
            value = "My Folder",
            onValueChange = { },
            onConfirm = { },
            onCancel = { },
            isLoading = true,
            errorMessage = null,
            icon = Icons.Default.Folder,
            placeholder = "Folder name",
        )
    }
}

@Preview
@Composable
fun InlineItemEditorErrorPreview() {
    EchoListTheme {
        InlineItemEditor(
            value = "My Folder",
            onValueChange = { },
            onConfirm = { },
            onCancel = { },
            isLoading = false,
            errorMessage = "Failed to create folder",
            icon = Icons.Default.Folder,
            placeholder = "Folder name",
        )
    }
}