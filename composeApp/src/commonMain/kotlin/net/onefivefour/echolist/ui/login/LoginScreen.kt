package net.onefivefour.echolist.ui.login

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.rememberScrollableState
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import echolist.composeapp.generated.resources.Res
import echolist.composeapp.generated.resources.app_name
import echolist.composeapp.generated.resources.hide_password
import echolist.composeapp.generated.resources.login_backend_url_label
import echolist.composeapp.generated.resources.login_button
import echolist.composeapp.generated.resources.login_password_label
import echolist.composeapp.generated.resources.login_username_label
import echolist.composeapp.generated.resources.show_password
import echolist.composeapp.generated.resources.visibility_off
import echolist.composeapp.generated.resources.visibility_on
import net.onefivefour.echolist.domain.model.AuthError
import net.onefivefour.echolist.ui.common.ElButton
import net.onefivefour.echolist.ui.common.ElOutlinedTextField
import net.onefivefour.echolist.ui.theme.EchoListTheme
import org.jetbrains.compose.resources.painterResource
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
    val usernameFocusRequester = remember { FocusRequester() }
    val passwordFocusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = dimensions.xl)
            .imePadding(),
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
        ElOutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            text = uiState.backendUrl,
            label = stringResource(Res.string.login_backend_url_label),
            isError = uiState.backendUrlError != null,
            keyboardType = KeyboardType.Uri,
            imeAction = ImeAction.Next,
            keyboardActions = KeyboardActions(onNext = { usernameFocusRequester.requestFocus() }),
            onValueChange = onBackendUrlChanged
        )
        uiState.backendUrlError?.let { errorMessage ->
            Text(
                text = errorMessage,
                style = EchoListTheme.typography.bodySmall,
                color = EchoListTheme.materialColors.error,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = dimensions.xs, top = dimensions.xxs)
            )
        }

        Spacer(modifier = Modifier.height(dimensions.m))

        // Username field
        ElOutlinedTextField(
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(usernameFocusRequester),
            text = uiState.username,
            label = stringResource(Res.string.login_username_label),
            isError = uiState.usernameError != null,
            keyboardType = KeyboardType.Text,
            imeAction = ImeAction.Next,
            keyboardActions = KeyboardActions(onNext = { passwordFocusRequester.requestFocus() }),
            onValueChange = onUsernameChanged
        )
        uiState.usernameError?.let { errorMessage ->
            Text(
                text = errorMessage,
                style = EchoListTheme.typography.bodySmall,
                color = EchoListTheme.materialColors.error,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = dimensions.xs, top = dimensions.xxs)
            )
        }

        Spacer(modifier = Modifier.height(dimensions.m))

        // Password field
        var showPassword by remember { mutableStateOf(false) }
        val interactionSource = remember { MutableInteractionSource() }
        ElOutlinedTextField(
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(passwordFocusRequester),
            text = uiState.password,
            label = stringResource(Res.string.login_password_label),
            isError = uiState.passwordError != null,
            keyboardType = KeyboardType.Password,
            imeAction = ImeAction.Done,
            keyboardActions = KeyboardActions(onDone = {
                keyboardController?.hide()
                onLoginClick()
            }),
            onValueChange = onPasswordChanged,
            visualTransformation = when {
                showPassword -> VisualTransformation.None
                else -> PasswordVisualTransformation()
            },
            trailingIcon = {
                val iconRes = when (showPassword) {
                    true -> Res.drawable.visibility_off
                    else -> Res.drawable.visibility_on
                }
                val contentDescriptionRes = when (showPassword) {
                    true -> Res.string.hide_password
                    else -> Res.string.show_password
                }
                Icon(
                    modifier = Modifier
                        .padding(end = EchoListTheme.dimensions.m)
                        .clickable(
                            interactionSource = interactionSource,
                            indication = ripple(bounded = false)
                        ) { showPassword = !showPassword }
                        .background(Color.Transparent),
                    painter = painterResource(iconRes),
                    contentDescription = stringResource(contentDescriptionRes),
                    tint = EchoListTheme.materialColors.onSurface
                )
            }
        )
        uiState.passwordError?.let { errorMessage ->
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

        // Login button
        ElButton(
            modifier = Modifier.width(150.dp).height(60.dp),
            onClick = onLoginClick,
            isEnabled = !uiState.isLoading,
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    color = EchoListTheme.materialColors.onPrimary,
                    modifier = Modifier.size(dimensions.xl)
                )
            } else {
                Text(
                    text = stringResource(Res.string.login_button),
                    style = EchoListTheme.typography.labelMedium
                )
            }
        }

        // Authentication error card
        uiState.authError?.let { error ->
            Spacer(modifier = Modifier.height(dimensions.xl))
            AuthErrorCard(
                error = error,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Preview
@Composable
private fun LoginScreenPreview() {
    EchoListTheme {
        LoginScreen(
            uiState = LoginUiState(
                backendUrl = "https://example.com",
                username = "",
                password = "asdf",
                isLoading = false,
                authError = AuthError.Unknown("Unknown error")
            ),
            onBackendUrlChanged = { },
            onUsernameChanged = { },
            onPasswordChanged = { },
            onLoginClick = { }
        )
    }

}
