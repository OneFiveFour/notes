package net.onefivefour.echolist.ui.maintasksettings

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.kotest.property.Arb
import io.kotest.property.PropTestConfig
import io.kotest.property.arbitrary.boolean
import io.kotest.property.checkAll
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import net.onefivefour.echolist.domain.NotificationPermissionChecker
import net.onefivefour.echolist.domain.NotificationPermissionRequester
import net.onefivefour.echolist.ui.recurrence.RecurrenceInterval

/**
 * Property-based tests for MainTaskSettingsViewModel notification toggle logic.
 *
 * **Validates: Requirements 1.3, 1.4, 1.5, 2.3, 3.1, 3.4, 4.2, 4.3**
 */
@OptIn(ExperimentalCoroutinesApi::class, io.kotest.common.ExperimentalKotest::class)
class MainTaskSettingsViewModelNotificationPropertyTest : FunSpec({

    val testDispatcher = StandardTestDispatcher()

    beforeEach {
        Dispatchers.setMain(testDispatcher)
    }

    afterEach {
        Dispatchers.resetMain()
    }

    test("Feature: notification-permission-toggle, Property 1: Toggle-Konsistenz") {
        // **Validates: Requirements 1.3, 2.3**
        checkAll(PropTestConfig(iterations = 100), Arb.boolean()) { enabled ->
            runTest(testDispatcher) {
                val resultBus = MainTaskSettingsResultBus()
                val vm = MainTaskSettingsViewModel(
                    mainTaskId = "task-1",
                    currentDueDate = "2026-01-01",
                    currentRecurrence = "FREQ=DAILY",
                    currentIsNotificationEnabled = !enabled,
                    permissionChecker = object : NotificationPermissionChecker {
                        override suspend fun isGranted(): Boolean = true
                    },
                    permissionRequester = object : NotificationPermissionRequester {
                        override suspend fun request(): Boolean = true
                    },
                    resultBus = resultBus
                )

                val resultDeferred = async { resultBus.results.first() }
                vm.onNotificationToggleChanged(enabled)
                testScheduler.advanceUntilIdle()

                val result = resultDeferred.await()
                result.isNotificationEnabled shouldBe enabled
            }
        }
    }

    test("Feature: notification-permission-toggle, Property 2: Recurrence.Off erzwingt Deaktivierung") {
        // **Validates: Requirements 1.4, 1.5**
        checkAll(PropTestConfig(iterations = 100), Arb.boolean()) { initialNotificationEnabled ->
            runTest(testDispatcher) {
                val resultBus = MainTaskSettingsResultBus()
                val vm = MainTaskSettingsViewModel(
                    mainTaskId = "task-1",
                    currentDueDate = "2026-01-01",
                    currentRecurrence = "FREQ=DAILY",
                    currentIsNotificationEnabled = initialNotificationEnabled,
                    permissionChecker = object : NotificationPermissionChecker {
                        override suspend fun isGranted(): Boolean = true
                    },
                    permissionRequester = object : NotificationPermissionRequester {
                        override suspend fun request(): Boolean = true
                    },
                    resultBus = resultBus
                )

                vm.onRecurrenceIntervalSelected(RecurrenceInterval.Off)

                val state = vm.uiState.value
                state.shouldBeInstanceOf<MainTaskSettingsUiState.Ready>()
                state.isNotificationEnabled shouldBe false
                state.isNotificationToggleEnabled shouldBe false
            }
        }
    }

    test("Feature: notification-permission-toggle, Property 3: Berechtigungsprüfungs-Guard (onScreenLeaving)") {
        // **Validates: Requirements 3.1, 3.4**
        checkAll(PropTestConfig(iterations = 100), Arb.boolean(), Arb.boolean()) { notificationEnabled, recurrenceOff ->
            runTest(testDispatcher) {
                var checkerCalled = false
                val resultBus = MainTaskSettingsResultBus()
                val vm = MainTaskSettingsViewModel(
                    mainTaskId = "task-1",
                    currentDueDate = "2026-01-01",
                    currentRecurrence = if (recurrenceOff) "" else "FREQ=DAILY",
                    currentIsNotificationEnabled = notificationEnabled,
                    permissionChecker = object : NotificationPermissionChecker {
                        override suspend fun isGranted(): Boolean {
                            checkerCalled = true
                            return true
                        }
                    },
                    permissionRequester = object : NotificationPermissionRequester {
                        override suspend fun request(): Boolean = true
                    },
                    resultBus = resultBus
                )

                vm.onScreenLeaving()
                testScheduler.advanceUntilIdle()

                val shouldCallChecker = notificationEnabled && !recurrenceOff
                checkerCalled shouldBe shouldCallChecker
            }
        }
    }

    test("Feature: notification-permission-toggle, Property 4: Berechtigungsverweigerung erzwingt Deaktivierung (onScreenLeaving)") {
        // **Validates: Requirements 4.2, 4.3**
        checkAll(
            PropTestConfig(iterations = 100),
            Arb.boolean()
        ) { requesterThrows ->
            runTest(testDispatcher) {
                val resultBus = MainTaskSettingsResultBus()
                val vm = MainTaskSettingsViewModel(
                    mainTaskId = "task-1",
                    currentDueDate = "2026-01-15",
                    currentRecurrence = "FREQ=DAILY",
                    currentIsNotificationEnabled = true,
                    permissionChecker = object : NotificationPermissionChecker {
                        override suspend fun isGranted(): Boolean = false
                    },
                    permissionRequester = object : NotificationPermissionRequester {
                        override suspend fun request(): Boolean {
                            if (requesterThrows) throw RuntimeException("Permission dialog failed")
                            return false
                        }
                    },
                    resultBus = resultBus
                )

                val resultDeferred = async { resultBus.results.first() }
                vm.onScreenLeaving()
                testScheduler.advanceUntilIdle()

                val result = resultDeferred.await()
                result.isNotificationEnabled shouldBe false
            }
        }
    }
})
