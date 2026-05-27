package com.questlog.core.data.di

import com.questlog.core.data.repository.CharacterRepositoryImpl
import com.questlog.core.data.repository.ClarifyRepositoryImpl
import com.questlog.core.data.repository.CompletionRepositoryImpl
import com.questlog.core.data.repository.InboxItemRepositoryImpl
import com.questlog.core.data.repository.ItemRepositoryImpl
import com.questlog.core.data.repository.ProjectRepositoryImpl
import com.questlog.core.data.repository.TaskRepositoryImpl
import com.questlog.core.domain.repository.CharacterRepository
import com.questlog.core.domain.repository.ClarifyRepository
import com.questlog.core.domain.repository.CompletionRepository
import com.questlog.core.domain.repository.InboxItemRepository
import com.questlog.core.domain.repository.ItemRepository
import com.questlog.core.domain.repository.ProjectRepository
import com.questlog.core.domain.repository.TaskRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindInboxItemRepository(impl: InboxItemRepositoryImpl): InboxItemRepository

    @Binds
    @Singleton
    abstract fun bindProjectRepository(impl: ProjectRepositoryImpl): ProjectRepository

    @Binds
    @Singleton
    abstract fun bindTaskRepository(impl: TaskRepositoryImpl): TaskRepository

    @Binds
    @Singleton
    abstract fun bindClarifyRepository(impl: ClarifyRepositoryImpl): ClarifyRepository

    @Binds
    @Singleton
    abstract fun bindCharacterRepository(impl: CharacterRepositoryImpl): CharacterRepository

    @Binds
    @Singleton
    abstract fun bindCompletionRepository(impl: CompletionRepositoryImpl): CompletionRepository

    @Binds
    @Singleton
    abstract fun bindItemRepository(impl: ItemRepositoryImpl): ItemRepository
}
