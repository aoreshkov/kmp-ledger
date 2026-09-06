package app.oreshkov.ledger.feature.posting.impl

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.oreshkov.ledger.core.compose.LabeledField
import app.oreshkov.ledger.core.compose.resources.Res as CoreRes
import app.oreshkov.ledger.core.compose.resources.back_content_description
import app.oreshkov.ledger.feature.posting.impl.resources.Res
import app.oreshkov.ledger.feature.posting.impl.resources.posting_details_delete_cancel
import app.oreshkov.ledger.feature.posting.impl.resources.posting_details_delete_confirm
import app.oreshkov.ledger.feature.posting.impl.resources.posting_details_delete_content_description
import app.oreshkov.ledger.feature.posting.impl.resources.posting_details_delete_dialog_body
import app.oreshkov.ledger.feature.posting.impl.resources.posting_details_delete_dialog_title
import app.oreshkov.ledger.feature.posting.impl.resources.posting_details_delete_failed
import app.oreshkov.ledger.feature.posting.impl.resources.posting_details_edit_content_description
import app.oreshkov.ledger.feature.posting.impl.resources.posting_details_failed_to_load
import app.oreshkov.ledger.feature.posting.impl.resources.posting_details_field_narrative
import app.oreshkov.ledger.feature.posting.impl.resources.posting_details_go_back
import app.oreshkov.ledger.feature.posting.impl.resources.posting_details_not_found
import app.oreshkov.ledger.feature.posting.impl.resources.posting_details_retry
import app.oreshkov.ledger.feature.posting.impl.resources.posting_details_title
import org.jetbrains.compose.resources.stringResource

@Composable
fun PostingDetailsScreen(
    onNavigateBack: () -> Unit,
    onEditClick: (String) -> Unit,
    onDeleted: () -> Unit,
    viewModel: PostingDetailsViewModel
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val deleteFailedMessage = stringResource(Res.string.posting_details_delete_failed)

    LaunchedEffect(viewModel) {
        viewModel.deletedEvent.collect { onDeleted() }
    }

    LaunchedEffect(viewModel) {
        viewModel.deleteFailedEvent.collect { snackbarHostState.showSnackbar(deleteFailedMessage) }
    }

    PostingDetailsContent(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onNavigateBack = onNavigateBack,
        onEditClick = onEditClick,
        onDeleteClick = viewModel::deletePosting,
        onRetry = viewModel::retry
    )
}

@Composable
internal fun PostingDetailsContent(
    uiState: PostingDetailsUiState,
    snackbarHostState: SnackbarHostState,
    onNavigateBack: () -> Unit,
    onEditClick: (String) -> Unit,
    onDeleteClick: () -> Unit,
    onRetry: () -> Unit,
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    val isSuccess = uiState is PostingDetailsUiState.Success

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(stringResource(Res.string.posting_details_delete_dialog_title)) },
            text = { Text(stringResource(Res.string.posting_details_delete_dialog_body)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        onDeleteClick()
                    }
                ) { Text(stringResource(Res.string.posting_details_delete_confirm), color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text(stringResource(Res.string.posting_details_delete_cancel)) }
            }
        )
    }
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Res.string.posting_details_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(CoreRes.string.back_content_description))
                    }
                },
                actions = {
                    if (isSuccess) {
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = stringResource(Res.string.posting_details_delete_content_description),
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            if (isSuccess) {
                FloatingActionButton(onClick = { onEditClick(uiState.posting.id) }) {
                    Icon(Icons.Default.Edit, contentDescription = stringResource(Res.string.posting_details_edit_content_description))
                }
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            when (uiState) {
                is PostingDetailsUiState.Loading -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(modifier = Modifier.testTag("loading"))
                }
                is PostingDetailsUiState.Error -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(stringResource(Res.string.posting_details_failed_to_load))
                        Button(onClick = onRetry) { Text(stringResource(Res.string.posting_details_retry)) }
                    }
                }
                is PostingDetailsUiState.NotFound -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(stringResource(Res.string.posting_details_not_found))
                        Button(onClick = onNavigateBack) {
                            Text(stringResource(Res.string.posting_details_go_back))
                        }
                    }
                }
                is PostingDetailsUiState.Success -> {
                    val posting = uiState.posting
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        LabeledField(stringResource(Res.string.posting_details_field_narrative), posting.narrative, MaterialTheme.typography.titleLarge)
                    }
                }
            }
        }
    }
}