package net.onefivefour.echolist

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.rememberNavigationEventState
import net.onefivefour.echolist.ui.AuthState
import net.onefivefour.echolist.ui.AuthViewModel
import net.onefivefour.echolist.ui.home.HomeScreen
import net.onefivefour.echolist.ui.home.HomeViewModel
import net.onefivefour.echolist.ui.login.LoginScreen
import net.onefivefour.echolist.ui.login.LoginViewModel
import net.onefivefour.echolist.ui.navigation.HomeRoute
import net.onefivefour.echolist.ui.navigation.NoteDetailRoute
import net.onefivefour.echolist.ui.navigation.echoListSavedStateConfig
import net.onefivefour.echolist.ui.notedetail.NoteDetailScreen
import net.onefivefour.echolist.ui.notedetail.NoteDetailViewModel
import net.onefivefour.echolist.ui.theme.EchoListTheme
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun App() {
    EchoListTheme {
        val authViewModel = koinViewModel<AuthViewModel>()
        val authState by authViewModel.authState.collectAsStateWithLifecycle()

        when (authState) {
            AuthState.Loading -> {
                // Empty / splash while checking auth state
            }

            AuthState.Unauthenticated -> {
                val loginViewModel = koinViewModel<LoginViewModel>()
                val loginState by loginViewModel.uiState.collectAsStateWithLifecycle()

                LaunchedEffect(Unit) {
                    loginViewModel.loginSuccess.collect {
                        authViewModel.onAuthenticated()
                    }
                }

                LoginScreen(
                    uiState = loginState,
                    onBackendUrlChanged = loginViewModel::onBackendUrlChanged,
                    onUsernameChanged = loginViewModel::onUsernameChanged,
                    onPasswordChanged = loginViewModel::onPasswordChanged,
                    onLoginClick = loginViewModel::onLoginClick
                )
            }

            AuthState.Authenticated -> {
                val backStack = rememberNavBackStack(echoListSavedStateConfig, HomeRoute())

                NavigationBackHandler(
                    state = rememberNavigationEventState(NavigationEventInfo.None),
                    isBackEnabled = backStack.size > 1,
                    onBackCompleted = { backStack.removeLastOrNull() }
                )

                NavDisplay(
                    backStack = backStack,
                    onBack = { backStack.removeLastOrNull() },
                    transitionSpec = { EnterTransition.None togetherWith ExitTransition.None },
                    popTransitionSpec = { EnterTransition.None togetherWith ExitTransition.None },
                    predictivePopTransitionSpec = { EnterTransition.None togetherWith ExitTransition.None },
                    entryProvider = entryProvider {
                        entry<HomeRoute> { route ->
                            val homeViewModel = koinViewModel<HomeViewModel>(key = route.path) { parametersOf(route.path) }
                            val homeUiState by homeViewModel.uiState.collectAsStateWithLifecycle()
                            HomeScreen(
                                uiState = homeUiState,
                                onNavigationClick = if (backStack.size > 1) {{ backStack.removeLastOrNull() }} else null,
                                onBreadcrumbClick = { path ->
                                    val index = backStack.indexOfLast { it is HomeRoute && it.path == path }
                                    if (index >= 0) {
                                        while (backStack.size > index + 1) backStack.removeLast()
                                    } else {
                                        backStack.add(HomeRoute(path))
                                    }
                                },
                                onFolderClick = { folderId -> backStack.add(HomeRoute(folderId)) },
                                onFileClick = { fileId -> backStack.add(NoteDetailRoute(fileId)) }
                            )
                        }

                        entry<NoteDetailRoute> { route ->
                            val noteDetailViewModel = koinViewModel<NoteDetailViewModel> { parametersOf(route.noteId) }
                            val noteDetailUiState by noteDetailViewModel.uiState.collectAsStateWithLifecycle()
                            NoteDetailScreen(
                                uiState = noteDetailUiState,
                                onBackClick = { backStack.removeLastOrNull() }
                            )
                        }
                    }
                )
            }
        }
    }
}
