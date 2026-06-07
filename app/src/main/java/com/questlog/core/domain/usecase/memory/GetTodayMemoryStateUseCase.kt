package com.questlog.core.domain.usecase.memory

import com.questlog.core.domain.model.MemoryTodayState
import com.questlog.core.domain.repository.MemoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.time.LocalDate
import javax.inject.Inject

/**
 * 오늘의 기억 작성 상태를 결정한다.
 *
 * 판단 순서:
 * 1. 오늘 이미 저장된 기억 → Saved
 * 2. 오늘 완료 퀘스트 없음 → NoCompletions
 * 3. 오늘 완료 퀘스트 있음 → Selecting(candidates)
 * (Expired 는 ViewModel 에서 어제 날짜를 별도로 체크하거나, 이 UseCase의 확장 포인트로 사용)
 *
 * 날짜는 invoke() 최초 호출 시점에 캐시 → 자정 race 방지.
 */
class GetTodayMemoryStateUseCase @Inject constructor(
    private val memoryRepository: MemoryRepository,
    private val getCandidates: GetCandidateCompletionsUseCase,
) {
    operator fun invoke(): Flow<MemoryTodayState> = flow {
        // 날짜를 Flow 시작 시점에 캐시 (자정 race 방지)
        val today = LocalDate.now().toString()

        val existing = memoryRepository.getTodayEntry(today)
        if (existing != null) {
            emit(MemoryTodayState.Saved(existing))
            return@flow
        }

        val candidates = getCandidates(today)
        if (candidates.isEmpty()) {
            emit(MemoryTodayState.NoCompletions)
        } else {
            emit(MemoryTodayState.Selecting(candidates))
        }
    }
}
