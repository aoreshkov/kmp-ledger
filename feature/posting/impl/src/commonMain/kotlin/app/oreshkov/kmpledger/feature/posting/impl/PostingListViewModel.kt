package app.oreshkov.kmpledger.feature.posting.impl

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.oreshkov.kmpledger.core.common.result.DataResult
import app.oreshkov.kmpledger.core.common.result.asResult
import app.oreshkov.kmpledger.core.domain.GetPostingsUseCase
import app.oreshkov.kmpledger.core.model.data.Posting
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import org.koin.core.annotation.KoinViewModel

sealed interface PostingListUiState {
    data object Loading : PostingListUiState
    data object Empty : PostingListUiState
    data object Error : PostingListUiState
    data class Success(val postings: List<Posting>) : PostingListUiState
}

@OptIn(ExperimentalCoroutinesApi::class)
@KoinViewModel
class PostingListViewModel(
    private val getPostingsUseCase: GetPostingsUseCase
) : ViewModel() {

    private val retryTrigger = MutableStateFlow(0)
    val uiState: StateFlow<PostingListUiState> = retryTrigger
        .flatMapLatest {
            getPostingsUseCase()
                .asResult()
                .map { result ->
                    when (result) {
                        is DataResult.Loading -> PostingListUiState.Loading
                        is DataResult.Success ->
                            if (result.data.isEmpty()) PostingListUiState.Empty
                            else PostingListUiState.Success(result.data)
                        is DataResult.Error -> PostingListUiState.Error
                    }
                }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = PostingListUiState.Loading
        )

    fun retry() { retryTrigger.update { it + 1 } }
}