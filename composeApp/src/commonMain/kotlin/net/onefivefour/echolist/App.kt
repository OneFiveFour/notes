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
import net.onefivefour.echolist.data.repository.normalizePath
import net.onefivefour.echolist.ui.AuthState
import net.onefivefour.echolist.ui.AuthViewModel
import net.onefivefour.echolist.ui.common.GradientBackground
import net.onefivefour.echolist.ui.editnote.EditNoteMode
import net.onefivefour.echolist.ui.home.CreateFolderViewModel
import net.onefivefour.echolist.ui.home.CreateItemCallbacks
import net.onefivefour.echolist.ui.home.HomeScreen
import net.onefivefour.echolist.ui.home.HomeViewModel
import net.onefivefour.echolist.ui.login.LoginScreen
import net.onefivefour.echolist.ui.login.LoginViewModel
import net.onefivefour.echolist.ui.editnote.EditNoteScreen
import net.onefivefour.echolist.ui.editnote.EditNoteViewModel
import net.onefivefour.echolist.ui.edittasklist.EditTaskListScreen
import net.onefivefour.echolist.ui.edittasklist.EditTaskListViewModel
import net.onefivefour.echolist.ui.navigation.EditNoteRoute
import net.onefivefour.echolist.ui.navigation.EditTaskListRoute
import net.onefivefour.echolist.ui.navigation.HomeRoute
import net.onefivefour.echolist.ui.navigation.echoListSavedStateConfig
import net.onefivefour.echolist.ui.theme.EchoListTheme
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun App() {
    EchoListTheme {
        GradientBackground {
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
                        onBackendUrlChange = loginViewModel::onBackendUrlChanged,
                        onUsernameChange = loginViewModel::onUsernameChanged,
                        onPasswordChange = loginViewModel::onPasswordChanged,
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
                                val homeViewModel =
                                    koinViewModel<HomeViewModel>(key = route.path) { parametersOf(route.path) }
                                val homeUiState by homeViewModel.uiState.collectAsStateWithLifecycle()

                                val createFolderViewModel =
                                    koinViewModel<CreateFolderViewModel>(
                                        key = "createFolder-${route.path}"
                                    ) { parametersOf(route.path) }
                                val createFolderUiState by createFolderViewModel.uiState.collectAsStateWithLifecycle()

                                HomeScreen(
                                    uiState = homeUiState,
                                    createFolderUiState = createFolderUiState,
                                    onBreadcrumbClick = { path ->
                                        val index =
                                            backStack.indexOfLast { it is HomeRoute && it.path == path }
                                        if (index >= 0) {
                                            while (backStack.size > index + 1) backStack.removeLast()
                                        } else {
                                            backStack.add(HomeRoute(path))
                                        }
                                    },
                                    createItemCallbacks = CreateItemCallbacks(
                                        onCreateFolder = createFolderViewModel::showDialog,
                                        onCreateNote = { backStack.add(EditNoteRoute(parentPath = route.path)) },
                                        onCreateTaskList = { backStack.add(EditTaskListRoute) }
                                    ),
                                    onNoteClick = { notePath ->
                                        backStack.add(
                                            EditNoteRoute(
                                                parentPath = route.path,
                                                filePath = normalizePath(notePath)
                                            )
                                        )
                                    },
                                    onFolderNameChange = createFolderViewModel::onNameChange,
                                    onConfirmCreateFolder = createFolderViewModel::onConfirm,
                                    onDismissCreateFolder = createFolderViewModel::dismissDialog
                                )
                            }

                            entry<EditNoteRoute> { route ->
                                val mode = route.filePath?.let { filePath ->
                                    EditNoteMode.Edit(normalizePath(filePath))
                                } ?: EditNoteMode.Create(normalizePath(route.parentPath))
                                val viewModel = koinViewModel<EditNoteViewModel>(
                                    key = "editNote-${route.parentPath}-${route.filePath.orEmpty()}"
                                ) { parametersOf(mode) }
                                val uiState by viewModel.uiState.collectAsStateWithLifecycle()

                                LaunchedEffect(Unit) {
                                    viewModel.navigateBack.collect { backStack.removeLastOrNull() }
                                }

                                EditNoteScreen(
                                    uiState = uiState,
                                    onPreviewToggle = viewModel::onPreviewToggle,
                                    onToolbarAction = viewModel::onToolbarAction,
                                    onSaveClick = viewModel::onSaveClick
                                )
                            }

                            entry<EditTaskListRoute> {
                                val currentPath = backStack.filterIsInstance<HomeRoute>().lastOrNull()?.path ?: "/"
                                val viewModel = koinViewModel<EditTaskListViewModel>(
                                    key = "editTaskList-$currentPath"
                                ) { parametersOf(currentPath) }
                                val uiState by viewModel.uiState.collectAsStateWithLifecycle()

                                LaunchedEffect(Unit) {
                                    viewModel.navigateBack.collect { backStack.removeLastOrNull() }
                                }

                                EditTaskListScreen(
                                    uiState = uiState,
                                    onSaveClick = viewModel::onSaveClick
                                )
                            }
                        }
                    )
                }
            }
        }
    }
}
