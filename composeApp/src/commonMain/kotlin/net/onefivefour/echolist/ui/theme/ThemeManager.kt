package net.onefivefour.echolist.ui.theme

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ThemeManager(
    val availableThemes: List<ColorTheme>,
    initialTheme: ColorTheme = availableThemes.first()
) {
    private val _selectedTheme = MutableStateFlow(initialTheme)
    val selectedTheme: StateFlow<ColorTheme> = _selectedTheme.asStateFlow()

    fun selectTheme(theme: ColorTheme) {
        if (theme in availableThemes) {
            _selectedTheme.value = theme
        }
    }
}
