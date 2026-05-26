package com.questlog.core.data.di

import android.content.Context
import androidx.room.Room
import com.questlog.core.data.db.Converters
import com.questlog.core.data.db.QuestLogDatabase
import com.questlog.core.data.db.dao.CharacterDao
import com.questlog.core.data.db.dao.CompletionDao
import com.questlog.core.data.db.dao.InboxItemDao
import com.questlog.core.data.db.dao.ProjectDao
import com.questlog.core.data.db.dao.TaskDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

// Room bootstrap: v4 = Phase 3 F3.1 활성 스키마 (combat_logs 추가, AutoMigration(3, 4)).
// ⛔ MIGRATION_1_2 / AutoMigration(1, 2) / fallbackToDestructiveMigration() 금지 (도그푸딩 데이터 보호).
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

    @Provides
    fun provideCompletionDao(db: QuestLogDatabase): CompletionDao = db.completionDao()
}
