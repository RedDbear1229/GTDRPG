package com.questlog.feature.clarify

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.questlog.core.domain.model.InboxItem
import com.questlog.core.domain.model.LifeArea
import com.questlog.core.domain.repository.InboxItemRepository
import com.questlog.core.domain.usecase.ClarifyDraft
import com.questlog.core.domain.usecase.ClarifyItemUseCase
import com.questlog.core.domain.usecase.ClarifyResult
import com.questlog.core.domain.usecase.NonActionable
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber

enum class ClarifyStep { Q1_ACTIONABLE, Q2_NEXT_ACTION, Q3_TWO_MINUTE, Q4_OWNER, Q5_PROJECT, Q6_DETAILS }

data class ClarifyUiState(
    val inboxItem: InboxItem? = null,
    val step: ClarifyStep = ClarifyStep.Q1_ACTIONABLE,
    val isActionable: Boolean? = null,
    val nonActionable: NonActionable? = null,
    val nextAction: String = "",
    val isTwoMinute: Boolean? = null,
    val isMine: Boolean? = null,
    val projectId: String? = null,
    val estimatedMinutes: Int? = null,
    val dueDate: Long? = null,
    val lifeArea: LifeArea = LifeArea.PERSONAL,
    val isSubmitting: Boolean = false,
    val error: String? = null,
)

sealed class ClarifyEvent {
    data object Dismissed : ClarifyEvent()
}

@HiltViewModel(assistedFactory = ClarifyViewModel.Factory::class)
class ClarifyViewModel @AssistedInject constructor(
    @Assisted private val inboxId: String,
    private val inboxRepository: InboxItemRepository,
    private val clarifyItem: ClarifyItemUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ClarifyUiState())
    val uiState = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<ClarifyEvent>(extraBufferCapacity = 1)
    val events = _events.asSharedFlow()

    init {
        viewModelScope.launch {
            val item = inboxRepository.getById(inboxId)
            if (item == null) {
                _uiState.update { it.copy(error = "Inbox 항목을 찾을 수 없습니다") }
            } else {
                _uiState.update { it.copy(inboxItem = item) }
            }
        }
    }

    fun selectActionable(yes: Boolean) {
        _uiState.update { it.copy(isActionable = yes) }
        if (yes) advanceTo(ClarifyStep.Q2_NEXT_ACTION) else { /* user picks destination in Q1 view */ }
    }

    fun selectNonActionable(choice: NonActionable) {
        _uiState.update { it.copy(nonActionable = choice) }
        submit()
    }

    fun setNextAction(value: String) = _uiState.update { it.copy(nextAction = value) }

    fun confirmNextAction() = advanceTo(ClarifyStep.Q3_TWO_MINUTE)

    fun selectTwoMinute(yes: Boolean) {
        _uiState.update { it.copy(isTwoMinute = yes) }
        if (yes) submit() else advanceTo(ClarifyStep.Q4_OWNER)
    }

    fun selectMine(yes: Boolean) {
        _uiState.update { it.copy(isMine = yes) }
        // 위임 경로(false): NPC 선택 UI 는 Phase 2 에서 추가, 일단 Q6 디테일로.
        advanceTo(if (yes) ClarifyStep.Q5_PROJECT else ClarifyStep.Q6_DETAILS)
    }

    fun setProjectId(id: String?) = _uiState.update { it.copy(projectId = id) }

    fun confirmProject() = advanceTo(ClarifyStep.Q6_DETAILS)

    fun setEstimatedMinutes(m: Int?) = _uiState.update { it.copy(estimatedMinutes = m) }
    fun setDueDate(d: Long?) = _uiState.update { it.copy(dueDate = d) }
    fun setLifeArea(area: LifeArea) = _uiState.update { it.copy(lifeArea = area) }

    fun submitDetails() = submit()

    fun back() {
        val current = _uiState.value.step
        val previous = when (current) {
            ClarifyStep.Q1_ACTIONABLE -> {
                _events.tryEmit(ClarifyEvent.Dismissed); return
            }
            ClarifyStep.Q2_NEXT_ACTION -> ClarifyStep.Q1_ACTIONABLE
            ClarifyStep.Q3_TWO_MINUTE -> ClarifyStep.Q2_NEXT_ACTION
            ClarifyStep.Q4_OWNER -> ClarifyStep.Q3_TWO_MINUTE
            ClarifyStep.Q5_PROJECT -> ClarifyStep.Q4_OWNER
            ClarifyStep.Q6_DETAILS -> if (_uiState.value.isMine == false) ClarifyStep.Q4_OWNER else ClarifyStep.Q5_PROJECT
        }
        _uiState.update { it.copy(step = previous) }
    }

    fun clearError() = _uiState.update { it.copy(error = null) }

    private fun advanceTo(step: ClarifyStep) = _uiState.update { it.copy(step = step) }

    private fun submit() {
        val s = _uiState.value
        if (s.isSubmitting) return
        val draft = ClarifyDraft(
            inboxId = inboxId,
            isActionable = s.isActionable == true,
            nonActionable = s.nonActionable,
            nextAction = s.nextAction.takeIf { it.isNotBlank() },
            isTwoMinute = s.isTwoMinute == true,
            delegate = s.isMine == false,
            projectId = s.projectId,
            estimatedMinutes = s.estimatedMinutes,
            dueDate = s.dueDate,
            lifeArea = s.lifeArea,
        )
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true) }
            when (val result = clarifyItem(draft)) {
                is ClarifyResult.Stored, ClarifyResult.Discarded -> {
                    _events.tryEmit(ClarifyEvent.Dismissed)
                }
                is ClarifyResult.Error -> {
                    Timber.w("clarify error: ${result.message}")
                    _uiState.update { it.copy(error = result.message, isSubmitting = false) }
                }
            }
        }
    }

    @AssistedFactory
    interface Factory {
        fun create(inboxId: String): ClarifyViewModel
    }
}
