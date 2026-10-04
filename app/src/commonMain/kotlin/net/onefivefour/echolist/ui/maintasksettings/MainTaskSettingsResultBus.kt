package net.onefivefour.echolist.ui.maintasksettings

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * Concrete DI type for settings results so Koin never confuses this flow with
 * other generic MutableSharedFlow registrations after JVM type erasure.
 */
internal class MainTaskSettingsResultBus(
    private val resultFlow: MutableSharedFlow<MainTaskSettingsResult> = MutableSharedFlow()
) {

    val results: SharedFlow<MainTaskSettingsResult> = resultFlow.asSharedFlow()

    suspend fun emit(result: MainTaskSettingsResult) {
        resultFlow.emit(result)
    }
}
