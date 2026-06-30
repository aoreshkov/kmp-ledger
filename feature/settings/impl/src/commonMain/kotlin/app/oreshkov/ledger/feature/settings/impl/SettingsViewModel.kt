package app.oreshkov.ledger.feature.settings.impl

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.oreshkov.ledger.core.domain.GetThemeModeUseCase
import app.oreshkov.ledger.core.domain.SetThemeModeUseCase
import app.oreshkov.ledger.core.model.settings.ThemeMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel
import org.koin.core.annotation.Provided

data class SettingsUiState(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val saveError: Boolean = false,
)

@KoinViewModel
class SettingsViewModel(
    @Provided private val getThemeModeUseCase: GetThemeModeUseCase,
    @Provided private val setThemeModeUseCase: SetThemeModeUseCase,
) : ViewModel() {

    private val _saveError = MutableStateFlow(false)

    val uiState: StateFlow<SettingsUiState> = combine(
        getThemeModeUseCase(),
        _saveError,
    ) { themeMode, saveError -> SettingsUiState(themeMode = themeMode, saveError = saveError) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = SettingsUiState()
        )

    fun onThemeModeChange(mode: ThemeMode) {
        _saveError.value = false
        viewModelScope.launch {
            setThemeModeUseCase(mode).onFailure { _saveError.value = true }
        }
    }
}
