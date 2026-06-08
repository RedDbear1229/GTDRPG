package com.questlog.di

import android.content.Context
import androidx.room.Room
import com.questlog.core.data.db.Converters
import com.questlog.core.data.db.QuestLogDatabase
import com.questlog.core.data.db.dao.CharacterDao
import com.questlog.core.data.db.dao.CharacterItemDao
import com.questlog.core.data.db.dao.ClaimEncounterRewardDao
import com.questlog.core.data.db.dao.CombatLogDao
import com.questlog.core.data.db.dao.CompletionDao
import com.questlog.core.data.db.dao.ConsentRecordDao
import com.questlog.core.data.db.dao.EncounterLogDao
import com.questlog.core.data.db.dao.InboxItemDao
import com.questlog.core.data.db.dao.MemoryDao
import com.questlog.core.data.db.dao.NpcDao
import com.questlog.core.data.db.dao.ProjectDao
import com.questlog.core.data.db.dao.TaskDao
import com.questlog.core.data.db.dao.WeeklyReviewDao
import com.questlog.core.data.di.DatabaseModule
import dagger.Module
import dagger.Provides
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import javax.inject.Singleton

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [DatabaseModule::class],
)
object TestDatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context,
        converters: Converters,
    ): QuestLogDatabase =
        Room.inMemoryDatabaseBuilder(context, QuestLogDatabase::class.java)
            .addTypeConverter(converters)
            .allowMainThreadQueries()
            .build()

    @Provides fun provideInboxItemDao(db: QuestLogDatabase): InboxItemDao = db.inboxItemDao()
    @Provides fun provideTaskDao(db: QuestLogDatabase): TaskDao = db.taskDao()
    @Provides fun provideProjectDao(db: QuestLogDatabase): ProjectDao = db.projectDao()
    @Provides fun provideCharacterDao(db: QuestLogDatabase): CharacterDao = db.characterDao()
    @Provides fun provideCompletionDao(db: QuestLogDatabase): CompletionDao = db.completionDao()
    @Provides fun provideConsentRecordDao(db: QuestLogDatabase): ConsentRecordDao = db.consentRecordDao()
    @Provides fun provideCharacterItemDao(db: QuestLogDatabase): CharacterItemDao = db.characterItemDao()
    @Provides fun provideNpcDao(db: QuestLogDatabase): NpcDao = db.npcDao()
    @Provides fun provideEncounterLogDao(db: QuestLogDatabase): EncounterLogDao = db.encounterLogDao()
    @Provides fun provideClaimEncounterRewardDao(db: QuestLogDatabase): ClaimEncounterRewardDao = db.claimEncounterRewardDao()
    @Provides fun provideWeeklyReviewDao(db: QuestLogDatabase): WeeklyReviewDao = db.weeklyReviewDao()
    @Provides fun provideCombatLogDao(db: QuestLogDatabase): CombatLogDao = db.combatLogDao()
    @Provides fun provideMemoryDao(db: QuestLogDatabase): MemoryDao = db.memoryDao()
}
