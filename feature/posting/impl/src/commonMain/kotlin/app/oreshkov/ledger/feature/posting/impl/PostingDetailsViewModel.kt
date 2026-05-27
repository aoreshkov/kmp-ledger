package app.oreshkov.ledger.feature.posting.impl

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.oreshkov.ledger.core.common.result.DataResult
import app.oreshkov.ledger.core.common.result.asResult
import app.oreshkov.ledger.core.domain.DeletePostingUseCase
import app.oreshkov.ledger.core.domain.GetPostingUseCase
import app.oreshkov.ledger.core.model.data.Posting
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.annotation.InjectedParam
import org.koin.core.annotation.KoinViewModel
import org.koin.core.annotation.Provided

sealed interface PostingDetailsUiState {
    data object Loading : PostingDetailsUiState
    data class Success(val posting: Posting) : PostingDetailsUiState
    data object Error : PostingDetailsUiState
    data object NotFound : PostingDetailsUiState
}

@OptIn(ExperimentalCoroutinesApi::class)
@KoinViewModel
class PostingDetailsViewModel(
    @Provided private val getPostingUseCase: GetPostingUseCase,
    @Provided private val deletePostingUseCase: DeletePostingUseCase,
    @InjectedParam private val postingId: Long
) : ViewModel() {
    private val retryTrigger = MutableStateFlow(0)

    val uiState: StateFlow<PostingDetailsUiState> = retryTrigger
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
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = PostingDetailsUiState.Loading
        )

    fun retry() { retryTrigger.update { it + 1 } }

    private val _deletedEvent = Channel<Unit>(Channel.BUFFERED)
    val deletedEvent = _deletedEvent.receiveAsFlow()

    fun deletePosting() {
        val state = uiState.value
        if (state !is PostingDetailsUiState.Success) return
        viewModelScope.launch {
            deletePostingUseCase(state.posting)
            _deletedEvent.send(Unit)
        }
    }
}