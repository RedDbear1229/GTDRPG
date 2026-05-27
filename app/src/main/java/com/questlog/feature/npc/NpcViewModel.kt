package com.questlog.feature.npc

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.questlog.core.domain.model.CharacterClass
import com.questlog.core.domain.model.CompatibilityLevel
import com.questlog.core.domain.model.Npc
import com.questlog.core.domain.model.NpcSource
import com.questlog.core.domain.repository.CharacterRepository
import com.questlog.core.domain.repository.NpcRepository
import com.questlog.core.domain.usecase.CalculateCompatibilityUseCase
import com.questlog.core.domain.usecase.DeleteImportedContactsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class NpcUiState(
    val npcs: List<NpcWithCompatibility> = emptyList(),
    val myClass: CharacterClass? = null,
    val isLoading: Boolean = true,
    val error: String? = null,
)

data class NpcWithCompatibility(
    val npc: Npc,
    val compatibility: CompatibilityLevel,
)

@HiltViewModel
class NpcViewModel @Inject constructor(
    private val npcRepository: NpcRepository,
    private val characterRepository: CharacterRepository,
    private val calculateCompatibility: CalculateCompatibilityUseCase,
    private val deleteImportedContacts: DeleteImportedContactsUseCase,
) : ViewModel() {

    private val _error = MutableStateFlow<String?>(null)

    val uiState: StateFlow<NpcUiState> = combine(
        npcRepository.getAll(),
        characterRepository.observeActive(),
        _error,
    ) { npcs, character, error ->
        val myClass = character?.classType
        NpcUiState(
            npcs = npcs.map { npc ->
                NpcWithCompatibility(
                    npc = npc,
                    compatibility = if (myClass != null) calculateCompatibility(myClass, npc.classType)
                    else CompatibilityLevel.NEUTRAL,
                )
            },
            myClass = myClass,
            isLoading = false,
            error = error,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), NpcUiState())

    fun addManualNpc(name: String, classType: CharacterClass, notes: String = "") {
        viewModelScope.launch {
            npcRepository.upsert(
                Npc(
                    id = UUID.randomUUID().toString(),
                    name = name.trim(),
                    classType = classType,
                    source = NpcSource.MANUAL,
                    notes = notes.trim(),
                )
            )
        }
    }

    fun importContactNpc(displayName: String, phoneNumber: String?, classType: CharacterClass) {
        viewModelScope.launch {
            val nickname = displayName.trim()
            npcRepository.upsert(
                Npc(
                    id = UUID.randomUUID().toString(),
                    name = nickname,
                    displayName = displayName.trim(),
                    phoneNumber = phoneNumber?.trim(),
                    classType = classType,
                    source = NpcSource.PICKER,
                )
            )
        }
    }

    fun deleteNpc(npc: Npc) {
        viewModelScope.launch {
            if (npc.source == NpcSource.PICKER) {
                npcRepository.deleteImportedNpc(npc.id)
            } else {
                npcRepository.deleteById(npc.id)
            }
        }
    }

    fun clearContactData() {
        viewModelScope.launch { deleteImportedContacts() }
    }

    fun dismissError() = _error.update { null }
}
