package net.onefivefour.echolist

import net.onefivefour.echolist.feature.browser.ui.BrowserFeature

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.rememberNavigationEventState
import echolist.composeapp.generated.resources.Res
import echolist.composeapp.generated.resources.ic_arrow_back
import echolist.composeapp.generated.resources.navigate_back
import net.onefivefour.echolist.core.files.domain.normalizePath
import net.onefivefour.echolist.ui.AuthState
import net.onefivefour.echolist.ui.AuthViewModel
import net.onefivefour.echolist.core.designsystem.ui.components.GradientBackground
import net.onefivefour.echolist.core.designsystem.ui.components.RoundIconButton
import net.onefivefour.echolist.ui.editnote.EditNoteMode
import net.onefivefour.echolist.ui.edittasklist.EditTaskListMode
import net.onefivefour.echolist.feature.login.ui.LoginFeature
import net.onefivefour.echolist.ui.editnote.EditNoteScreen
import net.onefivefour.echolist.ui.editnote.EditNoteViewModel
import net.onefivefour.echolist.ui.edittasklist.EditTaskListScreen
import net.onefivefour.echolist.ui.edittasklist.EditTaskListViewModel
import net.onefivefour.echolist.ui.maintasksettings.MainTaskSettingsScreen
import net.onefivefour.echolist.ui.maintasksettings.MainTaskSettingsViewModel
import net.onefivefour.echolist.ui.navigation.EditNoteRoute
import net.onefivefour.echolist.ui.navigation.EditTaskListRoute
import net.onefivefour.echolist.ui.navigation.HomeRoute
import net.onefivefour.echolist.ui.navigation.MainTaskSettingsRoute
import net.onefivefour.echolist.ui.navigation.echoListSavedStateConfig
import net.onefivefour.echolist.core.designsystem.ui.theme.EchoListTheme
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import org.jetbrains.compose.resources.stringResource

@Composable
fun App() {
    EchoListTheme {
        GradientBackground {
            val authViewModel = koinViewModel<AuthViewModel>()
            val authState by authViewModel.authState.collectAsStateWithLifecycle()

            when (authState) {
                AuthState.Loading -> Unit
                AuthState.Unauthenticated -> UnauthenticatedApp(authViewModel)
                AuthState.Authenticated -> AuthenticatedApp()
            }
        }
    }
}

@Composable
private fun UnauthenticatedApp(authViewModel: AuthViewModel) {
    LoginFeature(onAuthenticated = authViewModel::onAuthenticated)
}

@Composable
private fun AuthenticatedApp() {
    val backStack = rememberNavBackStack(echoListSavedStateConfig, HomeRoute())
    val canNavigateBack = backStack.size > 1

    NavigationBackHandler(
        state = rememberNavigationEventState(NavigationEventInfo.None),
        isBackEnabled = canNavigateBack,
        onBackCompleted = { backStack.removeLastOrNull() }
    )

    Box(modifier = Modifier.fillMaxSize()) {
        AuthenticatedNavDisplay(
            backStack = backStack,
            canNavigateBack = canNavigateBack
        )

        if (canNavigateBack) {
            RoundIconButton(
                iconRes = Res.drawable.ic_arrow_back,
                onClick = { backStack.removeLastOrNull() },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(
                        end = EchoListTheme.dimensions.l,
                        bottom = EchoListTheme.dimensions.m
                    ),
                containerColor = EchoListTheme.materialColors.primary,
                contentColor = EchoListTheme.materialColors.onPrimary,
                contentDescription = stringResource(Res.string.navigate_back)
            )
        }
    }
}

@Composable
private fun AuthenticatedNavDisplay(
    backStack: NavBackStack<NavKey>,
    canNavigateBack: Boolean
) {
    NavDisplay(
        modifier = Modifier
            .fillMaxSize()
            .padding(
                start = EchoListTheme.dimensions.l,
                top = EchoListTheme.dimensions.m,
                end = EchoListTheme.dimensions.l,
                bottom = if (canNavigateBack) {
                    EchoListTheme.dimensions.xxxl + EchoListTheme.dimensions.xl
                } else {
                    EchoListTheme.dimensions.m
                }
            ),
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator()
        ),
        transitionSpec = { EnterTransition.None togetherWith ExitTransition.None },
        popTransitionSpec = { EnterTransition.None togetherWith ExitTransition.None },
        predictivePopTransitionSpec = { EnterTransition.None togetherWith ExitTransition.None },
        entryProvider = entryProvider {
            entry<HomeRoute> { route ->
                BrowserFeature(
                    parentDir = route.parentDir,
                    onBreadcrumbClick = { parentDir ->
                        val index = backStack.indexOfLast { it is HomeRoute && it.parentDir == parentDir }
                        if (index >= 0) {
                            while (backStack.size > index + 1) backStack.removeLast()
                        } else {
                            backStack.add(HomeRoute(parentDir))
                        }
                    },
                    onReplaceFolder = { parentDir ->
                        val index = backStack.indexOfLast { it is HomeRoute && it.parentDir == parentDir }
                        if (index >= 0) {
                            while (backStack.size > index + 1) backStack.removeLast()
                        } else {
                            backStack.removeLastOrNull()
                            backStack.add(HomeRoute(parentDir))
                        }
                    },
                    onOpenFolder = { backStack.add(HomeRoute(it)) },
                    onOpenNote = { backStack.add(EditNoteRoute(route.parentDir, it)) },
                    onOpenTaskList = { backStack.add(EditTaskListRoute(route.parentDir, it)) }
                )
            }

            entry<EditNoteRoute> { route ->
                val noteId = route.noteId?.takeIf { it.isNotBlank() }

                val mode = noteId?.let(EditNoteMode::Edit)
                    ?: EditNoteMode.Create(normalizePath(route.parentDir))
                val viewModel = koinViewModel<EditNoteViewModel>(
                    key = "editNote-${route.parentDir}-${noteId.orEmpty()}"
                ) { parametersOf(mode) }
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()

                LaunchedEffect(viewModel) {
                    viewModel.navigateBack.collect { backStack.removeLastOrNull() }
                }

                EditNoteScreen(
                    uiState = uiState,
                    onPreviewToggle = viewModel::onPreviewToggle,
                    onBeginEdit = viewModel::onBeginEdit,
                    onToolbarAction = viewModel::onToolbarAction,
                    onSaveClick = viewModel::onSaveClick,
                    onDeleteClick = viewModel::onDeleteClick
                )
            }

            entry<EditTaskListRoute> { route ->

                val taskListId = route.taskListId?.takeIf { it.isNotBlank() }

                val mode = taskListId?.let(EditTaskListMode::Edit)
                    ?: EditTaskListMode.Create(normalizePath(route.parentDir))

                val viewModel = koinViewModel<EditTaskListViewModel>(
                    key = "editTaskList-${route.parentDir}-${taskListId.orEmpty()}"
                ) { parametersOf(mode) }

                val uiState by viewModel.uiState.collectAsStateWithLifecycle()

                LaunchedEffect(viewModel) {
                    viewModel.navigateBack.collect { backStack.removeLastOrNull() }
                }

                DisposableEffect(viewModel) {
                    onDispose { viewModel.onScreenLeft() }
                }

                EditTaskListScreen(
                    uiState = uiState,
                    onAddMainTask = viewModel::onAddMainTask,
                    onRemoveMainTask = viewModel::onRemoveMainTask,
                    onAddSubTask = viewModel::onAddSubTask,
                    onRemoveSubTask = viewModel::onRemoveSubTask,
                    onMainTaskCheckedChange = viewModel::onMainTaskCheckedChange,
                    onSubTaskCheckedChange = viewModel::onSubTaskCheckedChange,
                    onToggleAutoDelete = viewModel::onToggleAutoDelete,
                    onFieldFocusLost = viewModel::onFieldFocusLost,
                    onNavigateToSettings = {
                            mainTaskId,
                            currentDueDate,
                            currentRecurrence,
                            currentIsNotificationEnabled ->
                        viewModel.onSettingsNavigationStarted()
                        backStack.add(
                            MainTaskSettingsRoute(
                                mainTaskId = mainTaskId,
                                currentDueDate = currentDueDate,
                                currentRecurrence = currentRecurrence,
                                currentIsNotificationEnabled = currentIsNotificationEnabled
                            )
                        )
                    },
                    onDeleteClick = viewModel::onDeleteClick
                )
            }

            entry<MainTaskSettingsRoute> { route ->
                val viewModel = koinViewModel<MainTaskSettingsViewModel>(
                    key = "mainTaskSettings-${route.mainTaskId}"
                ) {
                    parametersOf(
                        route.mainTaskId,
                        route.currentDueDate,
                        route.currentRecurrence,
                        route.currentIsNotificationEnabled
                    )
                }

                val uiState by viewModel.uiState.collectAsStateWithLifecycle()

                DisposableEffect(viewModel) {
                    onDispose { viewModel.onScreenLeaving() }
                }

                MainTaskSettingsScreen(
                    uiState = uiState,
                    onDateSelected = viewModel::onDateSelected,
                    onRecurrenceIntervalSelected = viewModel::onRecurrenceIntervalSelected,
                    onRecurrenceDetailChanged = viewModel::onRecurrenceDetailChanged,
                    onNotificationToggleChanged = viewModel::onNotificationToggleChanged
                )
            }
        }
    )
}
