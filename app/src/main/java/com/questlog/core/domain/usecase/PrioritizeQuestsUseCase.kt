package com.questlog.core.domain.usecase

import com.questlog.core.domain.model.Task
import javax.inject.Inject

class PrioritizeQuestsUseCase @Inject constructor() {
    operator fun invoke(tasks: List<Task>): List<Task> = tasks.sortedWith(QUEST_ORDER)

    companion object {
        val QUEST_ORDER: Comparator<Task> = compareBy<Task> { it.dueDate ?: Long.MAX_VALUE }
            .thenByDescending { it.challengeRating }
            .thenBy { it.context ?: "\uFFFF" }
            .thenBy { it.createdAt }
    }
}
