package com.questlog.core.domain.repository

import androidx.paging.PagingSource
import com.questlog.core.domain.model.MemoryEntry

// android.* import 금지 — KMP 확장 호환성 유지.
// androidx.paging.PagingSource 는 Android-free 라이브러리이므로 domain 사용 허용.

interface MemoryRepository {
    /** 전체 기억 목록을 최신 날짜 순으로 페이징 */
    fun pageHistory(): PagingSource<Int, MemoryEntry>

    /** 특정 날짜("yyyy-MM-dd")의 기억 조회. 없으면 null */
    suspend fun getTodayEntry(date: String): MemoryEntry?

    /** 기억 삽입. 같은 날짜 중복 시 AlreadyExists 반환 */
    suspend fun insertEntry(entry: MemoryEntry): InsertResult

    /** Claude 윤색본 업데이트 */
    suspend fun updateEnrichedBody(id: String, enriched: String)

    /** weekStart("yyyy-MM-dd") 이후 기억 목록 (주간 리뷰용) */
    suspend fun getThisWeekEntries(weekStart: String): List<MemoryEntry>

    /** weekStart 이후 기억 개수 (리마인더 조건 체크용) */
    suspend fun countThisWeek(weekStart: String): Int
}

sealed class InsertResult {
    object Success : InsertResult()
    object AlreadyExists : InsertResult()
}
