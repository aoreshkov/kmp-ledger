package app.oreshkov.ledger.feature.posting.impl

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.oreshkov.ledger.core.common.result.DataResult
import app.oreshkov.ledger.core.common.result.asResult
import app.oreshkov.ledger.core.domain.DeletePostingUseCase
import app.oreshkov.ledger.core.domain.GetPostingUseCase
import app.oreshkov.ledger.core.model.data.Posting
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.annotation.InjectedParam
import org.koin.core.annotation.KoinViewModel
import org.koin.core.annotation.Provided

sealed interface PostingDetailsUiState {
    data object Loading : PostingDetailsUiState
    data class Success(
        val posting: Posting,
        val deleteError: Boolean = false,
        /** The posting was deleted; the screen leaves as soon as it sees this. */
        val isDeleted: Boolean = false,
    ) : PostingDetailsUiState
    data object Error : PostingDetailsUiState
    data object NotFound : PostingDetailsUiState
}

@OptIn(ExperimentalCoroutinesApi::class)
@KoinViewModel
class PostingDetailsViewModel(
    @Provided private val getPostingUseCase: GetPostingUseCase,
    @Provided private val deletePostingUseCase: DeletePostingUseCase,
    @InjectedParam private val postingId: String
) : ViewModel() {
    private val retryTrigger = MutableStateFlow(0)

    // Outcomes of deletePosting(), held as state rather than sent as one-off events so a
    // screen that is not composed when the delete completes still sees them on return.
    private data class DeleteStatus(val deleted: Posting? = null, val failed: Boolean = false)
    private val deleteStatus = MutableStateFlow(DeleteStatus())

    private val loadState = retryTrigger
        .flatMapLatest {
            getPostingUseCase(postingId).asResult()
        }
        .map { result ->
            when (result) {
                is DataResult.Loading -> PostingDetailsUiState.Loading
                is DataResult.Success -> {
                    val posting = result.data
                    if (posting != null) PostingDetailsUiState.Success(posting)
                    else PostingDetailsUiState.NotFound
                }

                is DataResult.Error -> PostingDetailsUiState.Error
            }
        }

    val uiState: StateFlow<PostingDetailsUiState> = combine(loadState, deleteStatus) { loaded, delete ->
        when {
            // The row is gone, so the load flow now reports NotFound; keep showing the
            // deleted posting instead while the screen navigates away.
            delete.deleted != null -> PostingDetailsUiState.Success(delete.deleted, isDeleted = true)
            loaded is PostingDetailsUiState.Success -> loaded.copy(deleteError = delete.failed)
            else -> loaded
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = PostingDetailsUiState.Loading
    )

    fun retry() { retryTrigger.update { it + 1 } }

    fun deletePosting() {
        val state = uiState.value
        // Already deleted: the screen is leaving, and a second delete would be a redundant write.
        if (state !is PostingDetailsUiState.Success || state.isDeleted) return
        deleteStatus.update { it.copy(failed = false) }
        viewModelScope.launch {
            deletePostingUseCase(state.posting.id).onSuccess {
                deleteStatus.value = DeleteStatus(deleted = state.posting)
            }.onFailure {
                deleteStatus.update { it.copy(failed = true) }
            }
        }
    }

    /** Consumes the delete error once its snackbar has been shown, so it is not shown again. */
    fun onDeleteErrorShown() = deleteStatus.update { it.copy(failed = false) }
}
