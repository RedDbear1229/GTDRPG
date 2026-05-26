package com.questlog.core.domain.repository

import com.questlog.core.domain.model.Character
import kotlinx.coroutines.flow.Flow

// 싱글 유저 가정: 활성 캐릭터는 0 또는 1 개. observeActive() 가 null 을 emit 하면 미온보딩 상태.
interface CharacterRepository {
    fun observeActive(): Flow<Character?>
    suspend fun getActive(): Character?
    suspend fun getById(id: String): Character?
    suspend fun upsert(character: Character)
    suspend fun delete(id: String)
}
