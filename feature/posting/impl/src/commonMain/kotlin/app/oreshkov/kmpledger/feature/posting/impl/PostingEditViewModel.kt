package app.oreshkov.kmpledger.feature.posting.impl

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.oreshkov.kmpledger.core.common.result.DataResult
import app.oreshkov.kmpledger.core.common.result.asResult
import app.oreshkov.kmpledger.core.domain.GetPostingUseCase
import app.oreshkov.kmpledger.core.domain.SavePostingUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.annotation.InjectedParam
import org.koin.core.annotation.KoinViewModel

import kotlin.time.Clock
import kotlin.time.Instant

sealed interface PostingEditUiState {
    data object Loading : PostingEditUiState
    data object Error : PostingEditUiState
    data object NotFound : PostingEditUiState
    data class Editing(
        val isEditMode: Boolean = false,
        val amount: String = "",
        val timestamp: String = "",
        val currency: String = "",
        val narrative: String = "",
        val amountTouched: Boolean = false,
        val timestampTouched: Boolean = false,
        val currencyTouched: Boolean = false,
        val narrativeTouched: Boolean = false,
        val saveError: Boolean = false,
    ) : PostingEditUiState {

        private val parsedTimestamp: Result<Instant> = runCatching { Instant.parse(timestamp) }
        val amountError: Boolean get() = amountTouched && amount.toLongOrNull() == null
        val timestampError: Boolean get() = timestampTouched && parsedTimestamp.isFailure
        val currencyError: Boolean get() = currencyTouched && currency.isBlank()
        val narrativeError: Boolean get() = narrativeTouched && narrative.isBlank()
        val isValid: Boolean get() =
            amount.toLongOrNull() != null && parsedTimestamp.isSuccess &&
                    currency.isNotBlank() && narrative.isNotBlank()
    }
}

@KoinViewModel
class PostingEditViewModel(
    private val getPostingUseCase: GetPostingUseCase,
    private val savePostingUseCase: SavePostingUseCase,
    @InjectedParam private val postingId: Long?
) : ViewModel() {

    private val _uiState = MutableStateFlow<PostingEditUiState>(
        if (postingId != null) PostingEditUiState.Loading 
        else PostingEditUiState.Editing(timestamp = Clock.System.now().toString())
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
            val result = getPostingUseCase(postingId)
                .asResult()
                .first { it !is DataResult.Loading }

            _uiState.value = when (result) {
                is DataResult.Success -> {
                    val posting = result.data
                    if (posting != null) {
                        PostingEditUiState.Editing(
                            isEditMode = true,
                            amount = posting.amount.toString(),
                            timestamp = posting.timestamp.toString(),
                            currency = posting.currency,
                            narrative = posting.narrative,
                        )
                    } else {
                        PostingEditUiState.NotFound
                    }
                }
                is DataResult.Error -> PostingEditUiState.Error
                is DataResult.Loading -> PostingEditUiState.Loading // Should not happen due to first filter
            }
        }
    }

    fun onAmountChange(newValue: String) = updateEditing { it.copy(amount = newValue, amountTouched = true) }
    fun onTimestampChange(newValue: String) = updateEditing { it.copy(timestamp = newValue, timestampTouched = true) }
    fun onCurrencyChange(newValue: String) = updateEditing { it.copy(currency = newValue, currencyTouched = true) }
    fun onNarrativeChange(newValue: String) = updateEditing { it.copy(narrative = newValue, narrativeTouched = true) }

    fun savePosting() {
        updateEditing {
            it.copy(
                amountTouched = true,
                timestampTouched = true,
                currencyTouched = true,
                narrativeTouched = true,
                saveError = false,
            )
        }
        val editing = _uiState.value as? PostingEditUiState.Editing ?: return
        if (!editing.isValid) return
        val amount = editing.amount.toLongOrNull() ?: return
        val timestamp = runCatching { Instant.parse(editing.timestamp) }.getOrNull() ?: return

        viewModelScope.launch {
            try {
                savePostingUseCase(
                    id = postingId,
                    amount = amount,
                    timestamp = timestamp,
                    currency = editing.currency,
                    narrative = editing.narrative
                )
                _navigationEvent.send(Unit)
            } catch (e: Exception) {
                updateEditing { it.copy(saveError = true) }
            }
        }
    }

    private fun updateEditing(block: (PostingEditUiState.Editing) -> PostingEditUiState.Editing) {
        _uiState.update { if (it is PostingEditUiState.Editing) block(it) else it }
    }
}