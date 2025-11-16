<!-- Short, actionable instructions for AI coding agents. Keep concise and project-specific. -->
# GitHub Copilot instructions — FairSplit (concise)

Purpose
- Help an AI agent be immediately productive in this repository (Kotlin + Jetpack Compose + Firebase).

Quick facts (discoverable)
- Stack: Kotlin, Jetpack Compose, Firebase (Auth + Firestore), WorkManager; MVVM + Clean Architecture is the sanctioned pattern.
- Tests: Unit tests with JUnit + MockK; UI tests with Espresso. CI expects Gradle builds and instrumented tests.
- Prompts & automation: `/PROMPT_PLAN_FairSplit_v1.1.md` and `/prompts/` live under `.github/` — agents should use those to drive feature generation.

What I (agent) must do first
- Read `/media/abhishek-atole/Data Folder/Developed Application/Expense Tracker/.github/instructions/GITHUB_COPILOT_INSTRUCTIONS.md.instructions.md` for the master guidance (already present).
- Parse `/PROMPT_PLAN_FairSplit_v1.1.md` (and files under `.github/prompts/`) to generate feature prompt files and corresponding task YAMLs under `/prompts/` and `/tasks/` when requested.

Project-specific conventions (do not guess)
- Architecture: MVVM + Clean Architecture. Place UI in `app/src/main/java/.../ui`, ViewModels in `.../viewmodel`, domain/use-cases in `.../domain` and repositories in `.../data`.
- Naming: Classes PascalCase, functions camelCase, constants UPPER_CASE.
- Data storage: Firestore collections are used for core app data; example collection path used in prompts: `finance/` for monthly-finance related documents. New Firestore collections must follow existing naming patterns.
- Persistence patterns: Reuse Firebase Auth for user identity, prefer per-user collections (e.g., `users/{uid}/...`) where appropriate.

Workflows & commands (explicit)
- Build: use Gradle wrapper (repo is Android/Kotlin):
  - `./gradlew assembleDebug` — build debug APK
  - `./gradlew assembleRelease` — build release
- Unit tests: `./gradlew test` (runs JVM unit tests)
- Instrumented/UI tests: `./gradlew connectedAndroidTest` or via CI matrix configured in `/workflows/`.
- Lint & static checks: `./gradlew lint` (if present in the project)

How to generate features (concrete contract for the agent)
- Input: feature prompt file under `.github/prompts/` or root `PROMPT_PLAN...` describing Fxx.
- Output (per feature):
  - `/prompts/Fxx_[Name].md` (feature prompt)
  - `/tasks/Fxx_TODO.yaml` (task list, follow sample format in existing instructions)
  - Kotlin scaffolding under `app/src/main/java/...` following MVVM + Clean layers
  - Unit test stubs under `app/src/test/...` and UI test stubs under `app/src/androidTest/...`

Examples (take these verbatim where applicable)
- Feature prompt location example: `.github/prompts/PROMPT_PLAN_FairSplit_v1.1.md.prompt.md`
- Existing instruction file to preserve/merge from: `.github/instructions/GITHUB_COPILOT_INSTRUCTIONS.md.instructions.md`
- Example Firestore collection referenced in prompts: `finance/` (insert new finance data there rather than inventing new global collections).

Commit messages
- Follow semantic commit format used in repo docs: e.g. `feat(F20): Add MonthlyIncome data model`, `test(F20): Add balance calculation unit tests`, `ci: Add workflow for instrumented tests`.

Do NOTs (avoid surprises)
- Don’t change project-wide architecture without developer confirmation. Keep MVVM + Clean separation and package layout.
- Don’t add new CI workflows that run device farms without an explicit CI ticket — changes to `/workflows/` should be small and follow existing patterns.

If you need more context
- Ask for: the `app/` source tree (if not present in the workspace snapshot), Gradle `settings.gradle` and `build.gradle` files, or any `README.md` describing the module layout.

Next step
- I created this concise instructions file from the discovered `.github` guidance; tell me any missing conventions or files to include and I'll update the file.
