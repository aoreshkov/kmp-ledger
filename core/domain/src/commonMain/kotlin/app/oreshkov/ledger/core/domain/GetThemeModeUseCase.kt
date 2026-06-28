package app.oreshkov.ledger.core.domain

import app.oreshkov.ledger.core.domain.repository.SettingsRepository
import app.oreshkov.ledger.core.model.settings.ThemeMode
import kotlinx.coroutines.flow.Flow
import org.koin.core.annotation.Factory
import org.koin.core.annotation.Provided

@Factory
class GetThemeModeUseCase(
    @Provided private val repository: SettingsRepository
) {
    operator fun invoke(): Flow<ThemeMode> = repository.themeMode()
}
