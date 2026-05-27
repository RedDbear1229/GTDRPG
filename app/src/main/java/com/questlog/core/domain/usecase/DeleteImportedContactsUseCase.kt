package com.questlog.core.domain.usecase

import com.questlog.core.domain.repository.NpcRepository
import javax.inject.Inject

// PICKER 소스 NPC의 displayName, phoneNumber(PII)를 전부 지운다. NPC 레코드와 위임 태스크는 유지.
class DeleteImportedContactsUseCase @Inject constructor(
    private val npcRepository: NpcRepository,
) {
    suspend operator fun invoke() = npcRepository.clearContactData()
}
