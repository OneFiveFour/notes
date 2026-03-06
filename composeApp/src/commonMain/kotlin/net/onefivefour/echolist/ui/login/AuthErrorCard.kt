package net.onefivefour.echolist.ui.login

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import echolist.composeapp.generated.resources.Res
import echolist.composeapp.generated.resources.error_auth_invalid_credentials
import echolist.composeapp.generated.resources.error_auth_invalid_credentials_description
import echolist.composeapp.generated.resources.error_auth_network
import echolist.composeapp.generated.resources.error_auth_network_description
import echolist.composeapp.generated.resources.error_auth_server
import echolist.composeapp.generated.resources.error_auth_server_description
import echolist.composeapp.generated.resources.error_auth_unknown
import echolist.composeapp.generated.resources.error_auth_unknown_description
import net.onefivefour.echolist.domain.model.AuthError
import net.onefivefour.echolist.ui.theme.EchoListTheme
import org.jetbrains.compose.resources.stringResource

@Composable
fun AuthErrorCard(
    error: AuthError,
    modifier: Modifier = Modifier
) {
    val dimensions = EchoListTheme.dimensions

    val (title, description) = when (error) {
        is AuthError.InvalidCredentials -> {
            stringResource(Res.string.error_auth_invalid_credentials) to
                stringResource(Res.string.error_auth_invalid_credentials_description)
        }
        is AuthError.ServerError -> {
            stringResource(Res.string.error_auth_server) to
                stringResource(Res.string.error_auth_server_description)
        }
        is AuthError.NetworkError -> {
            stringResource(Res.string.error_auth_network) to
                stringResource(Res.string.error_auth_network_description)
        }
        is AuthError.Unknown -> {
            stringResource(Res.string.error_auth_unknown) to
                stringResource(Res.string.error_auth_unknown_description)
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = EchoListTheme.materialColors.errorContainer,
                shape = EchoListTheme.shapes.small
            )
            .border(
                width = dimensions.borderWidth,
                color = EchoListTheme.materialColors.error,
                shape = EchoListTheme.shapes.small
            )
            .padding(dimensions.m),
        verticalArrangement = Arrangement.spacedBy(dimensions.xs)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint = EchoListTheme.materialColors.error,
                modifier = Modifier.size(dimensions.l)
            )
            Spacer(modifier = Modifier.width(dimensions.s))
            Text(
                text = title,
                style = EchoListTheme.typography.titleSmall,
                color = EchoListTheme.materialColors.onErrorContainer
            )
        }

        Text(
            text = description,
            style = EchoListTheme.typography.bodySmall,
            color = EchoListTheme.materialColors.onErrorContainer
        )
    }
}