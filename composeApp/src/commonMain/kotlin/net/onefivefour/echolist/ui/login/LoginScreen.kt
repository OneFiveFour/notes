package net.onefivefour.echolist.ui.login

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import echolist.composeapp.generated.resources.Res
import echolist.composeapp.generated.resources.app_name
import echolist.composeapp.generated.resources.login_backend_url_label
import echolist.composeapp.generated.resources.login_button
import echolist.composeapp.generated.resources.login_password_label
import echolist.composeapp.generated.resources.login_username_label
import net.onefivefour.echolist.ui.theme.EchoListTheme
import org.jetbrains.compose.resources.stringResource

@Composable
fun LoginScreen(
    uiState: LoginUiState,
    onBackendUrlChanged: (String) -> Unit,
    onUsernameChanged: (String) -> Unit,
    onPasswordChanged: (String) -> Unit,
    onLoginClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dimensions = EchoListTheme.dimensions

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = dimensions.xl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(Res.string.app_name),
            style = EchoListTheme.typography.titleLarge,
            color = EchoListTheme.materialColors.primary
        )

        Spacer(modifier = Modifier.height(dimensions.xxl))

        // Backend URL field
        OutlinedTextField(
            value = uiState.backendUrl,
            onValueChange = onBackendUrlChanged,
            label = { Text(stringResource(Res.string.login_backend_url_label)) },
            isError = uiState.backendUrlError != null,
            singleLine = true,
            shape = EchoListTheme.shapes.small,
            modifier = Modifier.fillMaxWidth()
        )
        if (uiState.backendUrlError != null) {
            Text(
                text = uiState.backendUrlError,
                style = EchoListTheme.typography.bodySmall,
                color = EchoListTheme.materialColors.error,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = dimensions.xs, top = dimensions.xxs)
            )
        }

        Spacer(modifier = Modifier.height(dimensions.m))

        // Username field
        OutlinedTextField(
            value = uiState.username,
            onValueChange = onUsernameChanged,
            label = { Text(stringResource(Res.string.login_username_label)) },
            isError = uiState.usernameError != null,
            singleLine = true,
            shape = EchoListTheme.shapes.small,
            modifier = Modifier.fillMaxWidth()
        )
        if (uiState.usernameError != null) {
            Text(
                text = uiState.usernameError,
                style = EchoListTheme.typography.bodySmall,
                color = EchoListTheme.materialColors.error,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = dimensions.xs, top = dimensions.xxs)
            )
        }

        Spacer(modifier = Modifier.height(dimensions.m))

        // Password field
        OutlinedTextField(
            value = uiState.password,
            onValueChange = onPasswordChanged,
            label = { Text(stringResource(Res.string.login_password_label)) },
            isError = uiState.passwordError != null,
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            shape = EchoListTheme.shapes.small,
            modifier = Modifier.fillMaxWidth()
        )
        if (uiState.passwordError != null) {
            Text(
                text = uiState.passwordError,
                style = EchoListTheme.typography.bodySmall,
                color = EchoListTheme.materialColors.error,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = dimensions.xs, top = dimensions.xxs)
            )
        }

        Spacer(modifier = Modifier.height(dimensions.xl))

        // Login button
        Button(
            onClick = onLoginClick,
            enabled = !uiState.isLoading,
            shape = EchoListTheme.shapes.small,
            colors = ButtonDefaults.buttonColors(
                containerColor = EchoListTheme.materialColors.primary,
                contentColor = EchoListTheme.materialColors.onPrimary
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    color = EchoListTheme.materialColors.onPrimary,
                    modifier = Modifier.size(dimensions.l)
                )
            } else {
                Text(
                    text = stringResource(Res.string.login_button),
                    style = EchoListTheme.typography.labelMedium
                )
            }
        }

        // General error message
        if (uiState.error != null) {
            Spacer(modifier = Modifier.height(dimensions.m))
            Text(
                text = uiState.error,
                style = EchoListTheme.typography.bodySmall,
                color = EchoListTheme.materialColors.error,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
