# 11. CI E2E Migration Failure — Postmortem

**Status: RESOLVED** (2026-06-18)

## Problem

`CI / E2E Instrumented Tests` failed on `.github/workflows/ci.yml` → `Run E2E tests on emulator`. Local builds and unit tests passed; only the instrumented migration tests failed.

## Root Cause

`MIGRATION_5_6` created `index_character_items_equipped_slot_unique` as a SQLite partial index:

```sql
CREATE UNIQUE INDEX IF NOT EXISTS `index_character_items_equipped_slot_unique`
ON `character_items` (`characterId`, `equippedSlot`)
WHERE isEquipped = 1
```

Room's `@Entity(indices = [...])` cannot express partial indexes (no `WHERE` clause support). As a result:

- `6.json` had the partial index, but `7.json`–`10.json` did not.
- `MigrationTestHelper.runMigrationsAndValidate()` compares the actual migrated DB against the expected schema JSON. Starting from a v6 DB (which had the partial index), the v7 validation found an unexpected index and failed.

## Fix Applied

Converted the partial index to a Room-expressible full unique composite index.

**Why this is safe**: SQLite UNIQUE indexes allow multiple NULL values per column. Unequipped items always have `equippedSlot = NULL`, so the uniqueness constraint only applies to equipped items — identical semantics to the original partial index.

### Changes

1. **`CharacterItemEntity.kt`** — Added `@Index` annotation:
   ```kotlin
   Index(
       value = ["characterId", "equippedSlot"],
       unique = true,
       name = "index_character_items_equipped_slot_unique",
   )
   ```

2. **`QuestLogDatabase.kt` `MIGRATION_5_6`** — Removed `WHERE isEquipped = 1`:
   ```sql
   CREATE UNIQUE INDEX IF NOT EXISTS `index_character_items_equipped_slot_unique`
   ON `character_items` (`characterId`, `equippedSlot`)
   ```

3. **`app/schemas/.../6.json`–`9.json`** — Updated `character_items.indices` to match the full unique index format (no WHERE clause).

4. **`app/schemas/.../10.json`** — Regenerated via `./gradlew :app:kspDebugKotlin`.

5. **`DatabaseMigrationTest.kt`** — Added `fullChain_v5ToCurrentVersion_isValid()` to catch this class of bug across the full migration chain.

## Key Invariant

- Equipped item: `isEquipped = true`, `equippedSlot != null`
- Unequipped item: `isEquipped = false`, `equippedSlot = null`
- One character can have at most one row per non-null `equippedSlot` value

`CharacterItemDao.unequipSlot()` and `unequipItem()` both set `equippedSlot = NULL` on unequip, maintaining this invariant.

## Prevention

`fullChain_v5ToCurrentVersion_isValid()` in `DatabaseMigrationTest` runs `createDatabase(5)` then `runMigrationsAndValidate(..., 10, ...)` across all five migrations. Any future index drift between the migration SQL and schema JSON will be caught here before CI.
