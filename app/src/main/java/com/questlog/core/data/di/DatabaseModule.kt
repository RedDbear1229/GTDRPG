package com.questlog.core.data.di

import android.content.Context
import androidx.room.Room
import com.questlog.core.data.db.Converters
import com.questlog.core.data.db.QuestLogDatabase
import com.questlog.core.data.db.dao.CharacterDao
import com.questlog.core.data.db.dao.InboxItemDao
import com.questlog.core.data.db.dao.ProjectDao
import com.questlog.core.data.db.dao.TaskDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

// Room bootstrap: v3 = Phase 2 F2.1 활성 스키마 (characters 추가, AutoMigration(2, 3)).
// ⛔ MIGRATION_1_2 / AutoMigration(1, 2) / fallbackToDestructiveMigration() 금지 (도그푸딩 데이터 보호).
// 수동 마이그레이션 등록 위치: F3.1 (CombatLog 등) 및 F4.4 (NPC 권한 게이트) 도입 시 .addMigrations(...).
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context,
        converters: Converters,
    ): QuestLogDatabase =
        Room.databaseBuilder(context, QuestLogDatabase::class.java, "questlog.db")
            .addTypeConverter(converters)
            .build()

    @Provides
    fun provideInboxItemDao(db: QuestLogDatabase): InboxItemDao = db.inboxItemDao()

    @Provides
    fun provideTaskDao(db: QuestLogDatabase): TaskDao = db.taskDao()

    @Provides
    fun provideProjectDao(db: QuestLogDatabase): ProjectDao = db.projectDao()

    @Provides
    fun provideCharacterDao(db: QuestLogDatabase): CharacterDao = db.characterDao()
}
