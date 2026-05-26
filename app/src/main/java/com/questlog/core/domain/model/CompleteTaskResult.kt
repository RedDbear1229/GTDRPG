package com.questlog.core.domain.model

sealed class CompleteTaskResult {
    data class Success(val combatResult: CombatResult) : CompleteTaskResult()
    object AlreadyCompleted : CompleteTaskResult()
    data class Error(val message: String) : CompleteTaskResult()
}
