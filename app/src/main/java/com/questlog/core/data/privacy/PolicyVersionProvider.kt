package com.questlog.core.data.privacy

import com.questlog.core.domain.model.ConsentScope
import javax.inject.Inject
import javax.inject.Singleton

// 정책 텍스트가 변경될 때 해당 scope의 버전을 증분한다.
// 기존 동의 record.policyVersion < 현재 → isGranted = false → 재동의 유도.
@Singleton
class PolicyVersionProvider @Inject constructor() {
    fun forScope(scope: ConsentScope): Int = when (scope) {
        ConsentScope.AI_OUTBOUND -> 2
        ConsentScope.CONTACTS    -> 1
        ConsentScope.MICROPHONE  -> 1
    }
}
