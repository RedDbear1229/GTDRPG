package com.questlog.core.domain.model

sealed class ClaimResult {
    object Success : ClaimResult()
    object AlreadyClaimedOrExpired : ClaimResult()
}
