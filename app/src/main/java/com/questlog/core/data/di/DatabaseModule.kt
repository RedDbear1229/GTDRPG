package com.questlog.core.data.di

import android.content.Context
import androidx.room.Room
import com.questlog.core.data.db.Converters
import com.questlog.core.data.db.QuestLogDatabase
import com.questlog.core.data.db.MIGRATION_5_6
import com.questlog.core.data.db.MIGRATION_6_7
import com.questlog.core.data.db.dao.CharacterDao
import com.questlog.core.data.db.dao.CharacterItemDao
import com.questlog.core.data.db.dao.CompletionDao
import com.questlog.core.data.db.dao.ConsentRecordDao
import com.questlog.core.data.db.dao.InboxItemDao
import com.questlog.core.data.db.dao.NpcDao
import com.questlog.core.data.db.dao.ProjectDao
import com.questlog.core.data.db.dao.TaskDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

// Room bootstrap: v5 = Phase 4 F4.0 활성 스키마 (consent_records 추가, AutoMigration(4, 5)).
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
            .addMigrations(MIGRATION_5_6, MIGRATION_6_7)
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

    @Provides
    fun provideConsentRecordDao(db: QuestLogDatabase): ConsentRecordDao = db.consentRecordDao()

    @Provides
    fun provideCharacterItemDao(db: QuestLogDatabase): CharacterItemDao = db.characterItemDao()

    @Provides
    fun provideNpcDao(db: QuestLogDatabase): NpcDao = db.npcDao()
}
