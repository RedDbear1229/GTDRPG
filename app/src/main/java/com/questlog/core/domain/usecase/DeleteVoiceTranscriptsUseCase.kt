package com.questlog.core.domain.usecase

import com.questlog.core.domain.repository.InboxItemRepository
import javax.inject.Inject

class DeleteVoiceTranscriptsUseCase @Inject constructor(
    private val repository: InboxItemRepository,
) {
    suspend operator fun invoke(): Int = repository.clearVoiceTranscripts()
}
