package net.onefivefour.notes.ui.notedetail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import net.onefivefour.notes.ui.theme.EchoListTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteDetailScreen(
    uiState: NoteDetailUiState,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(EchoListTheme.materialColors.background)
    ) {
        TopAppBar(
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = EchoListTheme.materialColors.background
            ),
            title = {
                if (uiState is NoteDetailUiState.Success) {
                    Text(
                        text = uiState.title,
                        style = EchoListTheme.typography.titleLarge,
                        color = EchoListTheme.materialColors.primary
                    )
                }
            },
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Navigate back",
                        tint = EchoListTheme.materialColors.primary
                    )
                }
            }
        )

        when (uiState) {
            is NoteDetailUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = EchoListTheme.materialColors.primary
                    )
                }
            }

            is NoteDetailUiState.Success -> {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = EchoListTheme.dimensions.l)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = uiState.lastUpdated,
                        style = EchoListTheme.typography.bodySmall,
                        color = EchoListTheme.materialColors.onBackground.copy(alpha = 0.6f)
                    )

                    Spacer(modifier = Modifier.height(EchoListTheme.dimensions.l))

                    Text(
                        text = uiState.content,
                        style = EchoListTheme.typography.bodyMedium,
                        color = EchoListTheme.materialColors.onBackground
                    )
                }
            }

            is NoteDetailUiState.Error -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = uiState.message,
                        style = EchoListTheme.typography.bodyMedium,
                        color = EchoListTheme.materialColors.secondary
                    )
                }
            }
        }
    }
}
