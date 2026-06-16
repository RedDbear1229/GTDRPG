package com.questlog.core.data.db

import androidx.room.testing.MigrationTestHelper
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

private const val TEST_DB = "questlog_migration_test"

@RunWith(AndroidJUnit4::class)
class DatabaseMigrationTest {

    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        QuestLogDatabase::class.java,
    )

    // v9→v10: memory_entries 테이블 추가 + entryDate UNIQUE 인덱스
    @Test
    fun migrate9To10_memoryEntriesCreated() {
        helper.createDatabase(TEST_DB, 9).close()

        val db = helper.runMigrationsAndValidate(TEST_DB, 10, true, MIGRATION_9_10)

        // memory_entries 테이블 존재 + 핵심 컬럼 확인
        db.query("SELECT id, entryDate, characterId, taskId, body, enrichedBody, createdAt, sealedAt FROM memory_entries LIMIT 0").use { cursor ->
            assert(cursor.columnCount == 8) {
                "memory_entries 컬럼 수 불일치: expected 8, got ${cursor.columnCount}"
            }
        }

        // entryDate UNIQUE 인덱스 존재 확인
        db.query("SELECT name FROM sqlite_master WHERE type='index' AND name='index_memory_entries_entryDate'").use { cursor ->
            assert(cursor.moveToFirst()) { "index_memory_entries_entryDate 인덱스 없음" }
        }

        db.close()
    }

    // v8→v9: weekly_reviews 테이블 추가 + weekStart UNIQUE 인덱스
    @Test
    fun migrate8To9_weeklyReviewsCreated() {
        helper.createDatabase(TEST_DB, 8).close()

        val db = helper.runMigrationsAndValidate(TEST_DB, 9, true, MIGRATION_8_9)

        db.query("SELECT id, weekStart, weekLabel, completedCount, xpGained, critCount, missCount, unfinishedCount, aiSummary, xpReward, completedAt FROM weekly_reviews LIMIT 0").use { cursor ->
            assert(cursor.columnCount == 11) {
                "weekly_reviews 컬럼 수 불일치: expected 11, got ${cursor.columnCount}"
            }
        }

        db.query("SELECT name FROM sqlite_master WHERE type='index' AND name='index_weekly_reviews_weekStart'").use { cursor ->
            assert(cursor.moveToFirst()) { "index_weekly_reviews_weekStart 인덱스 없음" }
        }

        db.close()
    }

    // v7→v8: encounter_logs + xp_awards 추가, xp_awards.encounterId UNIQUE
    @Test
    fun migrate7To8_encounterTablesCreated() {
        helper.createDatabase(TEST_DB, 7).close()

        val db = helper.runMigrationsAndValidate(TEST_DB, 8, true, MIGRATION_7_8)

        db.query("SELECT id, templateKey, status, generatedAt, claimedAt, expiresAt, rewardXp, rewardItemId FROM encounter_logs LIMIT 0").use { cursor ->
            assert(cursor.columnCount == 8) { "encounter_logs 컬럼 수 불일치" }
        }

        db.query("SELECT name FROM sqlite_master WHERE type='index' AND name='index_xp_awards_encounterId'").use { cursor ->
            assert(cursor.moveToFirst()) { "index_xp_awards_encounterId UNIQUE 인덱스 없음" }
        }

        db.close()
    }

    // v6→v7: npcs 테이블 추가
    @Test
    fun migrate6To7_npcsCreated() {
        helper.createDatabase(TEST_DB, 6).close()

        val db = helper.runMigrationsAndValidate(TEST_DB, 7, true, MIGRATION_6_7)

        db.query("SELECT id, name, displayName, phoneNumber, classType, source, notes, createdAt, updatedAt FROM npcs LIMIT 0").use { cursor ->
            assert(cursor.columnCount == 9) { "npcs 컬럼 수 불일치" }
        }

        db.close()
    }

    // v5→v6: items + character_items 추가, 슬롯 단일성 부분 인덱스
    @Test
    fun migrate5To6_itemTablesCreated() {
        helper.createDatabase(TEST_DB, 5).close()

        val db = helper.runMigrationsAndValidate(TEST_DB, 6, true, MIGRATION_5_6)

        db.query("SELECT id, itemKey, name, description, flavorText, itemType, rarity, slot, attackBonus, defenseBonus, xpMultiplier, hpBonusFlat, specialEffectCode FROM items LIMIT 0").use { cursor ->
            assert(cursor.columnCount == 13) { "items 컬럼 수 불일치" }
        }

        // 슬롯 단일성 부분 인덱스 (WHERE isEquipped=1)
        db.query("SELECT name FROM sqlite_master WHERE type='index' AND name='index_character_items_equipped_slot_unique'").use { cursor ->
            assert(cursor.moveToFirst()) { "index_character_items_equipped_slot_unique 인덱스 없음" }
        }

        db.close()
    }

    // v4→v5: consent_records 추가
    @Test
    fun migrate4To5_consentRecordsCreated() {
        helper.createDatabase(TEST_DB, 4).close()

        val db = helper.runMigrationsAndValidate(TEST_DB, 5, true, MIGRATION_4_5)

        db.query("SELECT id, scope, policyVersion, acceptedAt, revokedAt FROM consent_records LIMIT 0").use { cursor ->
            assert(cursor.columnCount == 5) { "consent_records 컬럼 수 불일치" }
        }

        db.close()
    }

    // v3→v4: combat_logs 추가, taskId UNIQUE 인덱스
    @Test
    fun migrate3To4_combatLogsCreated() {
        helper.createDatabase(TEST_DB, 3).close()

        val db = helper.runMigrationsAndValidate(TEST_DB, 4, true, MIGRATION_3_4)

        db.query("SELECT id, taskId, characterId, d20Result, totalAttack, monsterAC, xpGained, hpLost, isCriticalHit, isCriticalMiss, rolledAt FROM combat_logs LIMIT 0").use { cursor ->
            assert(cursor.columnCount == 11) { "combat_logs 컬럼 수 불일치" }
        }

        db.query("SELECT name FROM sqlite_master WHERE type='index' AND name='index_combat_logs_taskId'").use { cursor ->
            assert(cursor.moveToFirst()) { "index_combat_logs_taskId UNIQUE 인덱스 없음" }
        }

        db.close()
    }

    // v2→v3: characters 추가
    @Test
    fun migrate2To3_charactersCreated() {
        helper.createDatabase(TEST_DB, 2).close()

        val db = helper.runMigrationsAndValidate(TEST_DB, 3, true, MIGRATION_2_3)

        db.query("SELECT id, name, classType, level, currentXp, maxHp, currentHp FROM characters LIMIT 0").use { cursor ->
            assert(cursor.columnCount == 7) { "characters 핵심 컬럼 불일치" }
        }

        db.close()
    }

    // 전체 체인: v9 → v10 (가장 최근 마이그레이션, 실제 운영 관련성 가장 높음)
    @Test
    fun fullChain_v9ToCurrentVersion_isValid() {
        helper.createDatabase(TEST_DB, 9).close()
        helper.runMigrationsAndValidate(TEST_DB, 10, true, MIGRATION_9_10).close()
    }
}
