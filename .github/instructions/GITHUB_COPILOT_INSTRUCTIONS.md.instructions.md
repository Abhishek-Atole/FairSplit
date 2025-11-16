---
applyTo: '**'
---
# 🤖 GITHUB COPILOT INSTRUCTIONS — FAIRSplit (v1.1)

**Project Name:** FairSplit  
**Version:** v1.1  
**Maintainer:** Abhishek Atole  
**Repo Root File:** `/PROMPT_PLAN_FairSplit_v1.1.md`  
**AI Assistants:** GitHub Copilot (Agent Mode) + Claude Sonnet 4.5  
**Development Stack:** Kotlin + Jetpack Compose + Firebase + Vibe Coding  
**Goal:** Automate generation of implementation plans, task files, code modules, and CI/CD workflows based on `PROMPT_PLAN_FairSplit_v1.1.md`.

---

## 🧭 PURPOSE

This instruction file guides **GitHub Copilot Agent Mode** to:
1. Parse the **FairSplit master prompt file** (`PROMPT_PLAN_FairSplit_v1.1.md`).
2. Automatically generate:
   - Feature prompt files (`/prompts/Fxx_FeatureName.md`)
   - Implementation plans
   - Task lists
   - Kotlin code stubs
   - Test cases
   - CI/CD YAML workflows
3. Collaborate with developers to ensure structure, testing, and deployment align with professional Android standards.

---

## 🧩 CORE BEHAVIOR

### 1. File Parsing & Setup
- Locate the master prompt file at: `/PROMPT_PLAN_FairSplit_v1.1.md`.
- Parse all `Feature IDs (F01–F25)` listed in the **Feature List** section.
- For each feature, create a new file in `/prompts/` named:
  ```
  /prompts/Fxx_[FeatureName].md
  ```
- Each feature prompt must follow this format:

  ```markdown
  # [FeatureID] [Feature Name]

  ## 1. Overview
  Short summary of the feature purpose and its relationship to core modules.

  ## 2. UI Flow
  - Define Jetpack Compose screen structure.
  - List key UI components (CardView, Button, Graph, List).
  - Provide state management pattern (ViewModel/StateFlow).

  ## 3. Backend Integration
  - Firestore Collections and Fields.
  - Local caching strategy (Room).
  - API sync (if required).

  ## 4. Data Models
  - Kotlin data class definitions.
  - Relationships with existing tables.

  ## 5. Logic Implementation
  - Key Kotlin functions.
  - Business rules and algorithms.
  - Error handling and validation.

  ## 6. Testing Plan
  - Unit test cases.
  - Espresso UI test scenarios.

  ## 7. Deliverables
  - Kotlin code files.
  - Test coverage report.
  - Documentation snippet.
  ```

---

### 2. Folder Structure Enforcement

Copilot must maintain this structure:
```
/
|-- app/
|   |-- src/
|   |-- main/
|       |-- java/
|       |-- res/
|
|-- prompts/
|   |-- F01_GroupManagement.md
|   |-- F02_AddExpense.md
|   ...
|
|-- tasks/
|   |-- F01_TODO.yaml
|   |-- F02_TODO.yaml
|
|-- workflows/
|   |-- build.yml
|   |-- test.yml
|   |-- deploy.yml
|
|-- docs/
|   |-- API_REFERENCE.md
|   |-- CHANGELOG.md
|   |-- README.md
|
|-- PROMPT_PLAN_FairSplit_v1.1.md
|-- GITHUB_COPILOT_INSTRUCTIONS.md
```

---

### 3. Task Generation Rules

- For each feature (F01–F25), generate a **to-do YAML** file under `/tasks/`.
- Format for each `/tasks/Fxx_TODO.yaml`:

  ```yaml
  feature_id: Fxx
  feature_name: [Feature Name]
  assigned_to: [Team Member or Role]
  priority: [High | Medium | Low]
  status: pending
  tasks:
    - description: "Create Compose UI layout"
      output: "Compose file under /ui/screens/"
    - description: "Implement Firestore integration"
      output: "Repository + ViewModel"
    - description: "Add unit tests and Espresso tests"
      output: "Test file under /test/"
  ```

---

### 4. Code Generation Standards

Copilot must adhere to:
- **Architecture:** MVVM + Clean Architecture  
- **Naming Convention:**  
  - Classes: `PascalCase`  
  - Functions: `camelCase`  
  - Constants: `UPPER_CASE`  
- **Testing:**  
  - Unit tests with `JUnit` + `MockK`
  - UI tests with `Espresso`
  - Code coverage ≥ 80%
- **Error Handling:**  
  - Wrap network/database ops in `Result<T>` or sealed classes
  - Log crashes via Firebase Crashlytics

---

### 5. CI/CD PIPELINE INTEGRATION

Copilot must ensure all new code triggers GitHub Actions workflows:
- `/workflows/build.yml` → Gradle build + lint check  
- `/workflows/test.yml` → Run all JUnit/Espresso tests  
- `/workflows/deploy.yml` → Push to Firebase Test Lab (staging)  

Auto-generate these workflows if missing.

---

### 6. Documentation & Reporting

- Auto-update `/docs/CHANGELOG.md` on every commit with feature tag:
  ```
  [FeatureID] Short description of change
  ```
- Generate `/docs/API_REFERENCE.md` for all Firestore collections and REST endpoints.
- Include UML diagrams (PlantUML or Mermaid) for data relationships where applicable.

---

### 7. Commit Message Format

Copilot must follow **semantic commits**:
```
feat(Fxx): Added [feature name] module
fix(Fxx): Fixed [issue description]
test(Fxx): Added unit/UI tests
docs(Fxx): Updated documentation for [feature]
ci: Updated CI/CD workflows
```

---

### 8. Collaboration Logic (Copilot + Claude)

| Agent | Responsibility |
|--------|----------------|
| **Copilot** | Generate Kotlin, YAML, and CI/CD code. |
| **Claude Sonnet 4.5** | Review logic, provide reasoning, and assist with planning/doc generation. |

When a feature prompt is created, Claude analyzes dependencies and Copilot writes implementation files accordingly.

---

### 9. Error Handling & Escalation

- On build failure → Copilot must generate fix suggestions in `/tasks/error_log.md`.
- On test failure → Auto-create `/tasks/test_fixes.md` with the failing test list.
- On merge conflict → Claude generates human-readable resolution summary.

---

### 10. Output Expectations

After processing this instruction file and `PROMPT_PLAN_FairSplit_v1.1.md`, Copilot should have generated:
1. `/prompts/` folder with 25 structured feature prompt files.  
2. `/tasks/` folder with YAML task lists.  
3. `/workflows/` folder for CI/CD pipelines.  
4. Code scaffolds under `/app/src/main/java/...`  
5. `/docs/` auto-updated with new API + Changelog.  

---

## ✅ END GOAL

By following these instructions:
- FairSplit will have a **self-generating development pipeline**.  
- Every feature in `PROMPT_PLAN_FairSplit_v1.1.md` will have a prompt, code, and test plan.  
- GitHub Copilot will become your full-cycle assistant from planning → coding → testing → deployment.  

---

**End of File — GITHUB_COPILOT_INSTRUCTIONS.md**