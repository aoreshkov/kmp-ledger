package app.oreshkov.ledger.feature.settings.impl

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.oreshkov.ledger.core.compose.resources.Res as CoreRes
import app.oreshkov.ledger.core.compose.resources.back_content_description
import app.oreshkov.ledger.core.model.settings.ThemeMode
import ledger.feature.settings.impl.generated.resources.Res
import ledger.feature.settings.impl.generated.resources.settings_theme_dark
import ledger.feature.settings.impl.generated.resources.settings_theme_light
import ledger.feature.settings.impl.generated.resources.settings_theme_section
import ledger.feature.settings.impl.generated.resources.settings_theme_system
import ledger.feature.settings.impl.generated.resources.settings_title
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: SettingsViewModel
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    SettingsContent(
        uiState = uiState,
        onNavigateBack = onNavigateBack,
        onThemeModeChange = viewModel::onThemeModeChange,
    )
}

private val themeModeLabels: List<Pair<ThemeMode, StringResource>> = listOf(
    ThemeMode.LIGHT to Res.string.settings_theme_light,
    ThemeMode.DARK to Res.string.settings_theme_dark,
    ThemeMode.SYSTEM to Res.string.settings_theme_system,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SettingsContent(
    uiState: SettingsUiState,
    onNavigateBack: () -> Unit,
    onThemeModeChange: (ThemeMode) -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Res.string.settings_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(CoreRes.string.back_content_description)
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Text(
                text = stringResource(Res.string.settings_theme_section),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Column(Modifier.selectableGroup()) {
                themeModeLabels.forEach { (mode, label) ->
                    ThemeModeRow(
                        label = stringResource(label),
                        selected = uiState.themeMode == mode,
                        onClick = { onThemeModeChange(mode) },
                        testTag = "theme_${mode.name.lowercase()}",
                    )
                }
            }
        }
    }
}

@Composable
private fun ThemeModeRow(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    testTag: String,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(
                selected = selected,
                onClick = onClick,
                role = Role.RadioButton,
            )
            .padding(vertical = 12.dp)
            .testTag(testTag),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(selected = selected, onClick = null)
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(start = 16.dp),
        )
    }
}
