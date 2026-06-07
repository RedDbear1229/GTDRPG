package com.questlog.core.data.repository

import android.database.sqlite.SQLiteConstraintException
import androidx.paging.PagingSource
import com.questlog.core.data.db.dao.MemoryDao
import com.questlog.core.data.mapper.toDomain
import com.questlog.core.data.mapper.toEntity
import com.questlog.core.domain.model.MemoryEntry
import com.questlog.core.domain.repository.InsertResult
import com.questlog.core.domain.repository.MemoryRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MemoryRepositoryImpl @Inject constructor(
    private val dao: MemoryDao,
) : MemoryRepository {

    override fun pageHistory(): PagingSource<Int, MemoryEntry> =
        dao.pageHistory().map { entity -> entity.toDomain() }

    override suspend fun getTodayEntry(date: String): MemoryEntry? =
        withContext(Dispatchers.IO) { dao.getByDate(date)?.toDomain() }

    override suspend fun insertEntry(entry: MemoryEntry): InsertResult =
        withContext(Dispatchers.IO) {
            runCatching {
                dao.insertEntry(entry.toEntity())
                InsertResult.Success
            }.getOrElse { e ->
                when (e) {
                    is SQLiteConstraintException -> {
                        Timber.d("MemoryRepository: 날짜 중복 삽입 차단 date=${entry.entryDate}")
                        InsertResult.AlreadyExists
                    }
                    else -> throw e
                }
            }
        }

    override suspend fun updateEnrichedBody(id: String, enriched: String) =
        withContext(Dispatchers.IO) { dao.updateEnrichedBody(id, enriched) }

    override suspend fun getThisWeekEntries(weekStart: String): List<MemoryEntry> =
        withContext(Dispatchers.IO) { dao.getThisWeek(weekStart).map { it.toDomain() } }

    override suspend fun countThisWeek(weekStart: String): Int =
        withContext(Dispatchers.IO) { dao.countThisWeek(weekStart) }
}

// PagingSource<Int, MemoryEntryEntity> → PagingSource<Int, MemoryEntry> 변환 헬퍼
private fun <T : Any, R : Any> PagingSource<Int, T>.map(
    transform: (T) -> R,
): PagingSource<Int, R> = object : PagingSource<Int, R>() {
    override fun getRefreshKey(state: androidx.paging.PagingState<Int, R>): Int? =
        state.anchorPosition?.let { anchorPos ->
            state.closestPageToPosition(anchorPos)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPos)?.nextKey?.minus(1)
        }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, R> =
        when (val result = this@map.load(params)) {
            is LoadResult.Page -> LoadResult.Page(
                data = result.data.map(transform),
                prevKey = result.prevKey,
                nextKey = result.nextKey,
            )
            is LoadResult.Error -> LoadResult.Error(result.throwable)
            is LoadResult.Invalid -> LoadResult.Invalid()
        }
}
