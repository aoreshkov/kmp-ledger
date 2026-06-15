package app.oreshkov.ledger.feature.posting.impl

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.oreshkov.ledger.core.compose.resources.Res as CoreRes
import app.oreshkov.ledger.core.compose.resources.back_content_description
import ledger.feature.posting.impl.generated.resources.Res
import ledger.feature.posting.impl.generated.resources.posting_details_retry
import ledger.feature.posting.impl.generated.resources.posting_edit_error_load_failed
import ledger.feature.posting.impl.generated.resources.posting_edit_error_not_found
import ledger.feature.posting.impl.generated.resources.posting_edit_go_back
import ledger.feature.posting.impl.generated.resources.posting_edit_error_narrative_required
import ledger.feature.posting.impl.generated.resources.posting_edit_error_save_failed
import ledger.feature.posting.impl.generated.resources.posting_edit_field_narrative
import ledger.feature.posting.impl.generated.resources.posting_edit_save
import ledger.feature.posting.impl.generated.resources.posting_edit_title_add
import ledger.feature.posting.impl.generated.resources.posting_edit_title_edit
import org.jetbrains.compose.resources.stringResource

@Composable
fun PostingEditScreen(
    onNavigateBack: () -> Unit,
    viewModel: PostingEditViewModel
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val errorMessage = stringResource(Res.string.posting_edit_error_save_failed)

    val saveError = (uiState as? PostingEditUiState.Editing)?.saveError == true
    LaunchedEffect(saveError) {
        if (saveError) snackbarHostState.showSnackbar(errorMessage)
    }

    LaunchedEffect(viewModel) {
        viewModel.navigationEvent.collect { onNavigateBack() }
    }

    PostingEditContent(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onNavigateBack = onNavigateBack,
        onNarrativeChange = viewModel::onNarrativeChange,
        onSaveClick = viewModel::savePosting,
        onRetry = viewModel::retry,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun PostingEditContent(
    uiState: PostingEditUiState,
    snackbarHostState: SnackbarHostState,
    onNavigateBack: () -> Unit,
    onNarrativeChange: (String) -> Unit,
    onSaveClick: () -> Unit,
    onRetry: () -> Unit,
) {
    val isEditMode = (uiState as? PostingEditUiState.Editing)?.isEditMode == true
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (isEditMode) stringResource(Res.string.posting_edit_title_edit)
                        else stringResource(Res.string.posting_edit_title_add)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(CoreRes.string.back_content_description))
                    }
                },
                actions = {
                    if (uiState is PostingEditUiState.Editing) {
                        TextButton(
                            onClick = onSaveClick,
                            enabled = uiState.isValid
                        ) {
                            Text(stringResource(Res.string.posting_edit_save))
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        when (uiState) {
            is PostingEditUiState.Loading -> Box(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }

            is PostingEditUiState.Error -> Box(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(stringResource(Res.string.posting_edit_error_load_failed))
                    Button(onClick = onRetry) {
                        Text(stringResource(Res.string.posting_details_retry))
                    }
                }
            }

            is PostingEditUiState.NotFound -> Box(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(stringResource(Res.string.posting_edit_error_not_found))
                    Button(onClick = onNavigateBack) {
                        Text(stringResource(Res.string.posting_edit_go_back))
                    }
                }
            }

            is PostingEditUiState.Editing -> Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = uiState.narrative,
                    onValueChange = onNarrativeChange,
                    label = { Text(stringResource(Res.string.posting_edit_field_narrative)) },
                    isError = uiState.narrativeError,
                    supportingText = if (uiState.narrativeError) {
                        { Text(stringResource(Res.string.posting_edit_error_narrative_required)) }
                    } else null,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}