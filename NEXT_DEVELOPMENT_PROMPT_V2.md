# 🚀 FAIRSPLIT v1.1 — NEXT DEVELOPMENT PROMPT

**Generated:** November 16, 2025  
**Target:** Complete F25, Setup DI, Add Tests, Integrate Charts  
**Timeline:** 7-day Sprint  
**Priority:** P0 Blockers First

See FAIRSPLIT_UPDATED_STATUS.md for complete analysis.

## IMMEDIATE ACTIONS (Copy & Execute)

### Priority 1: Fix Hilt DI (Day 1)
1. Edit app/build.gradle.kts line 7: Remove "apply false" from Hilt plugin
2. Create di/AppModule.kt, di/DatabaseModule.kt, di/RepositoryModule.kt  
3. Annotate FairSplitApplication with @HiltAndroidApp
4. Annotate MainActivity with @AndroidEntryPoint
5. Add @HiltViewModel to all ViewModels
6. Add @Inject constructor to all repositories

### Priority 2: F25 Backend (Days 2-3)
1. Create data/repository/HistoryRepositoryImpl.kt
2. Create ui/history/HistoryViewModel.kt
3. Update HistoryScreen to use hiltViewModel()
4. Implement AutoResetWorker.doWork() logic
5. Configure HiltWorkerFactory

### Priority 3: Charts (Day 4)
1. Add Vico column chart to MonthlyReportScreen
2. Wire chart to real expense data

### Priority 4: Tests (Days 5-7)
1. IncomeViewModelTest.kt
2. ExpenseRepositoryImplTest.kt  
3. HistoryViewModelTest.kt
4. Run: ./gradlew test

## CODE TEMPLATES

Refer to FAIRSPLIT_UPDATED_STATUS.md sections:
- Part 1: Complete Hilt setup code
- Part 2: Complete HistoryRepositoryImpl code
- Part 2.2: Complete HistoryViewModel code
- Part 2.4: Complete AutoResetWorker code
- Part 3: Complete Vico charts code
- Part 4: Complete test templates

## VERIFICATION

```bash
./gradlew clean assembleDebug
./gradlew test
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

**END OF QUICK PROMPT**
