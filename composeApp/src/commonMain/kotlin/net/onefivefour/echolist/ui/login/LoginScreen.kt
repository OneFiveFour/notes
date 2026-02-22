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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import net.onefivefour.echolist.ui.theme.EchoListTheme

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
            .background(EchoListTheme.materialColors.background)
            .padding(horizontal = dimensions.xl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "EchoList",
            style = EchoListTheme.typography.titleLarge,
            color = EchoListTheme.materialColors.primary
        )

        Spacer(modifier = Modifier.height(dimensions.xxl))

        // Backend URL field
        OutlinedTextField(
            value = uiState.backendUrl,
            onValueChange = onBackendUrlChanged,
            label = { Text("Backend URL") },
            isError = uiState.backendUrlError != null,
            singleLine = true,
            shape = MaterialTheme.shapes.small,
            modifier = Modifier.fillMaxWidth()
        )
        if (uiState.backendUrlError != null) {
            Text(
                text = uiState.backendUrlError,
                style = MaterialTheme.typography.bodySmall,
                color = EchoListTheme.materialColors.secondary,
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
            label = { Text("Username") },
            isError = uiState.usernameError != null,
            singleLine = true,
            shape = MaterialTheme.shapes.small,
            modifier = Modifier.fillMaxWidth()
        )
        if (uiState.usernameError != null) {
            Text(
                text = uiState.usernameError,
                style = MaterialTheme.typography.bodySmall,
                color = EchoListTheme.materialColors.secondary,
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
            label = { Text("Password") },
            isError = uiState.passwordError != null,
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            shape = MaterialTheme.shapes.small,
            modifier = Modifier.fillMaxWidth()
        )
        if (uiState.passwordError != null) {
            Text(
                text = uiState.passwordError,
                style = MaterialTheme.typography.bodySmall,
                color = EchoListTheme.materialColors.secondary,
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
            shape = MaterialTheme.shapes.small,
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
                    text = "Log in",
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }

        // General error message
        if (uiState.error != null) {
            Spacer(modifier = Modifier.height(dimensions.m))
            Text(
                text = uiState.error,
                style = MaterialTheme.typography.bodySmall,
                color = EchoListTheme.materialColors.secondary,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
