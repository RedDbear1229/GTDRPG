package com.questlog.feature.inbox

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.questlog.core.data.privacy.ConsentManager
import com.questlog.core.domain.model.CaptureSource
import com.questlog.core.domain.model.InboxItem
import com.questlog.core.domain.repository.InboxItemRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber

data class InboxUiState(
    val items: List<InboxItem> = emptyList(),
    val isSheetVisible: Boolean = false,
    val isCapturing: Boolean = false,
    val microphoneConsented: Boolean = false,
    val error: String? = null,
)

sealed class InboxEvent {
    data class Captured(val source: CaptureSource) : InboxEvent()
}

@HiltViewModel
class InboxViewModel @Inject constructor(
    private val repository: InboxItemRepository,
    private val consentManager: ConsentManager,
) : ViewModel() {

    private val sheetVisible = MutableStateFlow(false)
    private val capturing = MutableStateFlow(false)
    private val errorState = MutableStateFlow<String?>(null)
    private val micConsented = MutableStateFlow(false)

    init {
        viewModelScope.launch { micConsented.value = consentManager.canUseMicrophone() }
    }

    private val itemsFlow = repository.observeUnclarified()
        .catch { e ->
            Timber.e(e, "observeUnclarified failed")
            errorState.value = e.message
            emit(emptyList())
        }

    val uiState: StateFlow<InboxUiState> = combine(
        itemsFlow,
        sheetVisible,
        capturing,
        micConsented,
        errorState,
    ) { items, sheet, busy, mic, err ->
        InboxUiState(
            items = items,
            isSheetVisible = sheet,
            isCapturing = busy,
            microphoneConsented = mic,
            error = err,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = InboxUiState(),
    )

    private val _events = MutableSharedFlow<InboxEvent>(extraBufferCapacity = 1)
    val events = _events.asSharedFlow()

    fun openSheet() { sheetVisible.value = true }

    fun dismissSheet() { sheetVisible.value = false }

    fun clearError() { errorState.value = null }

    fun captureFromSheet(text: String) = capture(text, CaptureSource.APP, closeSheetAfter = true)

    fun captureFromShare(text: String) = capture(text, CaptureSource.SHARE, closeSheetAfter = false)

    // STT 결과 텍스트를 Inbox에 저장. 권한 재확인은 UI 레이어(MicrophonePermissionGate)가 담당.
    fun captureFromVoice(transcribedText: String) = capture(transcribedText, CaptureSource.VOICE, closeSheetAfter = true)

    fun refreshMicrophoneConsent() {
        viewModelScope.launch { micConsented.value = consentManager.canUseMicrophone() }
    }

    fun delete(id: String) {
        viewModelScope.launch {
            runCatching { repository.delete(id) }
                .onFailure { e ->
                    Timber.e(e, "delete failed")
                    errorState.value = e.message
                }
        }
    }

    private fun capture(rawText: String, source: CaptureSource, closeSheetAfter: Boolean) {
        val trimmed = rawText.trim()
        if (trimmed.isEmpty()) {
            errorState.value = "내용을 입력해주세요"
            return
        }
        viewModelScope.launch {
            capturing.value = true
            runCatching { repository.capture(trimmed, source) }
                .onSuccess {
                    if (closeSheetAfter) sheetVisible.value = false
                    _events.tryEmit(InboxEvent.Captured(source))
                }
                .onFailure { e ->
                    Timber.e(e, "capture failed")
                    errorState.value = e.message
                }
            capturing.value = false
        }
    }
}
