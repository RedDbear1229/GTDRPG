# AGENTS.md

QuestLog is developed primarily in Claude Code. In this repository, Codex's default role is code review, not implementation.

## Codex Role

- Treat Claude Code as the primary code author.
- Default to review-only behavior unless the user explicitly asks Codex to edit code.
- When reviewing, prioritize bugs, behavioral regressions, missing tests, data integrity issues, privacy issues, and architecture boundary violations.
- Put findings first, ordered by severity, with precise file and line references.
- Keep summaries brief and secondary to concrete review findings.

## Project Context

- QuestLog is a local-first Android app combining GTD and D&D-style automatic combat.
- Core product scope, completion criteria, and non-goals live in `PRD.md`.
- Claude-facing implementation rules live in `CLAUDE.md`; treat that file as the main engineering contract.
- Detailed design docs live under `docs/`.

## Review Priorities

Check these first on every meaningful change:

- Quest completion atomicity: completion must go through `CompletionDao.commitCompletion()` as a single transaction.
- Status guards: task completion updates must guard on active status and handle zero affected rows as already completed.
- Combat logs: inserted combat logs are immutable and should use conflict-ignore semantics where required.
- Room schema changes: entity changes require database versioning, migrations, and committed schema JSON under `app/schemas/`.
- Claude API privacy: calls require `canCallApi()` and `PromptSanitizer`; memory enrichment must send only the body field.
- Domain purity: `core/domain` must not import Android framework APIs.
- Stack constraints: use kotlinx.serialization, JUnit 5, MockK, Coroutines/Flow, Compose-only UI, and Timber logging.
- Null safety and concurrency: avoid `!!`, `GlobalScope`, production `runBlocking`, and unstructured concurrent writes.

## Useful Commands

```bash
./gradlew testDebugUnitTest
./gradlew verifyPaparazziDebug
./gradlew lintDebug
./gradlew :app:kspDebugKotlin
```

Use focused tests for narrow reviews when full verification is unnecessary or too slow.

## Review Output

For code reviews, use this shape:

1. Findings with severity and file/line references.
2. Open questions or assumptions, if any.
3. Tests or commands run, including failures or skipped checks.

If no issues are found, say that directly and call out any remaining test coverage or verification gaps.

## Git Safety

- Do not push, force push, reset, or discard user/Claude changes unless explicitly asked.
- Expect a dirty working tree; inspect and work with existing changes.
- Do not rewrite implementation during review unless the user explicitly changes the task from review to fix.
