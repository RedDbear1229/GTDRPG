package com.questlog.core.data.repository

import com.questlog.core.data.db.dao.CharacterDao
import com.questlog.core.data.mapper.toDomain
import com.questlog.core.data.mapper.toEntity
import com.questlog.core.domain.model.Character
import com.questlog.core.domain.repository.CharacterRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CharacterRepositoryImpl @Inject constructor(
    private val dao: CharacterDao,
) : CharacterRepository {

    override fun observeActive(): Flow<Character?> =
        dao.observeActive().map { it?.toDomain() }

    override suspend fun getActive(): Character? =
        withContext(Dispatchers.IO) { dao.getActive()?.toDomain() }

    override suspend fun getById(id: String): Character? =
        withContext(Dispatchers.IO) { dao.getById(id)?.toDomain() }

    override suspend fun upsert(character: Character) =
        withContext(Dispatchers.IO) { dao.upsert(character.toEntity()) }

    override suspend fun delete(id: String) =
        withContext(Dispatchers.IO) { dao.delete(id) }
}
