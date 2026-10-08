package app.oreshkov.ledger.feature.posting.impl

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.oreshkov.ledger.core.common.result.runCatchingCancellable
import app.oreshkov.ledger.core.domain.GetPostingUseCase
import app.oreshkov.ledger.core.domain.SavePostingUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.annotation.InjectedParam
import org.koin.core.annotation.KoinViewModel
import org.koin.core.annotation.Provided

sealed interface PostingEditUiState {
    data object Loading : PostingEditUiState
    data object Error : PostingEditUiState
    data object NotFound : PostingEditUiState
    data class Editing(
        val isEditMode: Boolean = false,
        val narrative: String = "",
        val narrativeTouched: Boolean = false,
        val saveError: Boolean = false,
        val isSaving: Boolean = false,
    ) : PostingEditUiState {
        val narrativeError: Boolean get() = narrativeTouched && narrative.isBlank()
        val isValid: Boolean get() = narrative.isNotBlank()
    }
}

@KoinViewModel
class PostingEditViewModel(
    @Provided private val getPostingUseCase: GetPostingUseCase,
    @Provided private val savePostingUseCase: SavePostingUseCase,
    @InjectedParam private val postingId: String?
) : ViewModel() {

    private val _uiState = MutableStateFlow<PostingEditUiState>(
        if (postingId != null) PostingEditUiState.Loading 
        else PostingEditUiState.Editing()
    )
    val uiState: StateFlow<PostingEditUiState> = _uiState.asStateFlow()

    private val _navigationEvent = Channel<Unit>(Channel.BUFFERED)
    val navigationEvent = _navigationEvent.receiveAsFlow()

    init {
        loadPosting()
    }

    fun retry() {
        loadPosting()
    }

    private fun loadPosting() {
        if (postingId == null) return
        _uiState.value = PostingEditUiState.Loading
        viewModelScope.launch {
            // One-shot snapshot to seed the form. Using first() lets the underlying
            // Room flow complete so the coroutine ends — retry() can no longer stack
            // additional, never-completing collectors on top of each other.
            _uiState.value = runCatchingCancellable { getPostingUseCase(postingId).first() }.fold(
                onSuccess = { posting ->
                    if (posting != null) {
                        PostingEditUiState.Editing(
                            isEditMode = true,
                            narrative = posting.narrative,
                        )
                    } else {
                        PostingEditUiState.NotFound
                    }
                },
                onFailure = { PostingEditUiState.Error }
            )
        }
    }

    fun onNarrativeChange(newValue: String) = updateEditing { it.copy(narrative = newValue, narrativeTouched = true) }

    fun savePosting() {
        val previous = _uiState.getAndUpdate { state ->
            if (state !is PostingEditUiState.Editing || state.isSaving) state
            else state.copy(narrativeTouched = true, saveError = false, isSaving = state.isValid)
        }
        // Re-entry guard: a second tap while a save is in flight must not start another
        // write (in create mode that inserts a duplicate row and navigates back twice).
        if (previous !is PostingEditUiState.Editing || previous.isSaving || !previous.isValid) return

        viewModelScope.launch {
            savePostingUseCase(
                id = postingId,
                narrative = previous.narrative
            ).onSuccess {
                // isSaving stays true: the screen is navigating away, so Save stays disabled.
                _navigationEvent.send(Unit)
            }.onFailure {
                updateEditing { it.copy(saveError = true, isSaving = false) }
            }
        }
    }

    /** Consumes the save error once its snackbar has been shown, so it is not shown again. */
    fun onSaveErrorShown() = updateEditing { it.copy(saveError = false) }

    private fun updateEditing(block: (PostingEditUiState.Editing) -> PostingEditUiState.Editing) {
        _uiState.update { if (it is PostingEditUiState.Editing) block(it) else it }
    }
}