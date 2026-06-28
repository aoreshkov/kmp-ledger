package app.oreshkov.ledger.feature.settings.impl

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.oreshkov.ledger.core.domain.GetThemeModeUseCase
import app.oreshkov.ledger.core.domain.SetThemeModeUseCase
import app.oreshkov.ledger.core.model.settings.ThemeMode
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel
import org.koin.core.annotation.Provided

data class SettingsUiState(
    val themeMode: ThemeMode = ThemeMode.SYSTEM
)

@KoinViewModel
class SettingsViewModel(
    @Provided private val getThemeModeUseCase: GetThemeModeUseCase,
    @Provided private val setThemeModeUseCase: SetThemeModeUseCase,
) : ViewModel() {

    val uiState: StateFlow<SettingsUiState> = getThemeModeUseCase()
        .map { SettingsUiState(themeMode = it) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = SettingsUiState()
        )

    fun onThemeModeChange(mode: ThemeMode) {
        viewModelScope.launch { setThemeModeUseCase(mode) }
    }
}
