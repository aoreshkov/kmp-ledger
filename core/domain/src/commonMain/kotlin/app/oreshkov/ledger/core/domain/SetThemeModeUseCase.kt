package app.oreshkov.ledger.core.domain

import app.oreshkov.ledger.core.common.result.runCatchingCancellable
import app.oreshkov.ledger.core.domain.repository.SettingsRepository
import app.oreshkov.ledger.core.model.settings.ThemeMode
import org.koin.core.annotation.Factory
import org.koin.core.annotation.Provided

@Factory
class SetThemeModeUseCase(
    @Provided private val repository: SettingsRepository
) {
    suspend operator fun invoke(mode: ThemeMode): Result<Unit> = runCatchingCancellable {
        repository.setThemeMode(mode)
    }
}
