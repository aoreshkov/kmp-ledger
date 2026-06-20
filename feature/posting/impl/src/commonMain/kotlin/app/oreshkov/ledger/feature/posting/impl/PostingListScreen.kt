package app.oreshkov.ledger.feature.posting.impl

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.oreshkov.ledger.core.model.data.Posting
import ledger.feature.posting.impl.generated.resources.Res
import ledger.feature.posting.impl.generated.resources.posting_list_add_content_description
import ledger.feature.posting.impl.generated.resources.posting_list_empty
import ledger.feature.posting.impl.generated.resources.posting_list_error
import ledger.feature.posting.impl.generated.resources.posting_list_retry
import ledger.feature.posting.impl.generated.resources.posting_list_title
import org.jetbrains.compose.resources.stringResource

@Composable
fun PostingListScreen(
    onNavigateToEdit: (String?) -> Unit,
    onNavigateToDetails: (String) -> Unit,
    viewModel: PostingListViewModel
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    PostingListContent(
        uiState = uiState,
        onAddClick = { onNavigateToEdit(null) },
        onPostingClick = onNavigateToDetails,
        onRetry = viewModel::retry,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun PostingListContent(
    uiState: PostingListUiState,
    onAddClick: () -> Unit,
    onPostingClick: (String) -> Unit,
    onRetry: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text(stringResource(Res.string.posting_list_title)) })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddClick) {
                Icon(Icons.Default.Add, contentDescription = stringResource(Res.string.posting_list_add_content_description))
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            when (uiState) {
                is PostingListUiState.Loading -> CircularProgressIndicator(modifier = Modifier.testTag("loading"))
                is PostingListUiState.Error -> Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(stringResource(Res.string.posting_list_error))
                    Button(onClick = onRetry) {
                        Text(stringResource(Res.string.posting_list_retry))
                    }
                }
                is PostingListUiState.Empty -> Text(
                    text = stringResource(Res.string.posting_list_empty),
                    style = MaterialTheme.typography.bodyLarge
                )
                is PostingListUiState.Success -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(uiState.postings, key = { it.id }) { posting ->
                            PostingListItem(posting = posting, onClick = { onPostingClick(posting.id) })
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PostingListItem(posting: Posting, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = posting.narrative, style = MaterialTheme.typography.titleMedium)
        }
    }
}