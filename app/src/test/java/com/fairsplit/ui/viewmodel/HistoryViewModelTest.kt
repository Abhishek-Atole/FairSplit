package com.fairsplit.ui.viewmodel

import app.cash.turbine.test
import com.fairsplit.domain.model.ArchivedMonth
import com.fairsplit.domain.model.ArchiveStats
import com.fairsplit.domain.repository.HistoryRepository
import com.fairsplit.domain.util.Result
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.time.YearMonth

/**
 * Unit tests for HistoryViewModel
 * Tests archive operations, stats loading, and error handling
 */
@OptIn(ExperimentalCoroutinesApi::class)
class HistoryViewModelTest {

    private lateinit var viewModel: HistoryViewModel
    private lateinit var historyRepository: HistoryRepository
    private lateinit var auth: FirebaseAuth
    private lateinit var mockUser: FirebaseUser
    
    private val testDispatcher = StandardTestDispatcher()
    private val testUserId = "test_user_123"
    
    private val testArchive = ArchivedMonth(
        id = "archive_1",
        userId = testUserId,
        yearMonth = YearMonth.of(2025, 10),
        totalIncome = 50000.0,
        totalExpense = 30000.0,
        balance = 20000.0,
        savingsRate = 40f,
        archivedAt = System.currentTimeMillis(),
        isRestored = false
    )
    
    private val testStats = ArchiveStats(
        totalMonths = 5,
        totalIncome = 250000.0,
        totalExpense = 150000.0,
        averageSavingsRate = 40f,
        bestMonth = YearMonth.of(2025, 9),
        worstMonth = YearMonth.of(2025, 6)
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        
        historyRepository = mockk(relaxed = true)
        auth = mockk(relaxed = true)
        mockUser = mockk(relaxed = true)
        
        every { auth.currentUser } returns mockUser
        every { mockUser.uid } returns testUserId
        every { auth.currentUser?.uid } returns testUserId
        
        // Mock repository defaults
        every { historyRepository.getAllArchives(testUserId) } returns flowOf(emptyList())
        coEvery { historyRepository.getArchiveStats(testUserId) } returns Result.Success(testStats)
    }

    @After
    fun teardown() {
        Dispatchers.resetMain()
        clearAllMocks()
    }

    @Test
    fun `loadArchiveStats should succeed and update state`() = runTest {
        // Given
        coEvery { historyRepository.getArchiveStats(testUserId) } returns Result.Success(testStats)
        
        // When
        viewModel = HistoryViewModel(historyRepository, auth)
        advanceUntilIdle()
        
        // Then
        viewModel.archiveStats.test {
            val stats = awaitItem()
            assertNotNull(stats)
            assertEquals(5, stats!!.totalMonths)
            assertEquals(250000.0, stats.totalIncome, 0.01)
            assertEquals(40f, stats.averageSavingsRate, 0.01f)
        }
        
        viewModel.uiState.test {
            val state = awaitItem()
            assertFalse(state.isLoading)
            assertNull(state.error)
        }
    }

    @Test
    fun `loadArchiveStats when repository fails should show error`() = runTest {
        // Given
        coEvery { historyRepository.getArchiveStats(testUserId) } returns 
            Result.Error("Failed to load stats", Exception("DB error"))
        
        // When
        viewModel = HistoryViewModel(historyRepository, auth)
        advanceUntilIdle()
        
        // Then
        viewModel.uiState.test {
            val state = awaitItem()
            assertFalse(state.isLoading)
            assertEquals("Failed to load stats", state.error)
        }
    }

    @Test
    fun `archives flow should emit repository data`() = runTest {
        // Given
        val archivesList = listOf(testArchive, testArchive.copy(id = "archive_2"))
        every { historyRepository.getAllArchives(testUserId) } returns flowOf(archivesList)
        coEvery { historyRepository.getArchiveStats(testUserId) } returns Result.Success(testStats)
        
        // When
        viewModel = HistoryViewModel(historyRepository, auth)
        
        // Then - Use turbine to collect emissions from the StateFlow
        viewModel.archives.test {
            // Skip initial empty emission
            val firstItem = awaitItem()
            if (firstItem.isEmpty()) {
                // Wait for the actual data emission
                val archives = awaitItem()
                assertEquals(2, archives.size)
                assertEquals("archive_1", archives[0].id)
                assertEquals("archive_2", archives[1].id)
            } else {
                // Data emitted immediately
                assertEquals(2, firstItem.size)
                assertEquals("archive_1", firstItem[0].id)
                assertEquals("archive_2", firstItem[1].id)
            }
        }
    }

    @Test
    fun `archiveCurrentMonth should succeed`() = runTest {
        // Given
        coEvery { historyRepository.performMonthEndReset(testUserId) } returns Result.Success(Unit)
        coEvery { historyRepository.getArchiveStats(testUserId) } returns Result.Success(testStats)
        
        viewModel = HistoryViewModel(historyRepository, auth)
        advanceUntilIdle()
        
        // When
        viewModel.archiveCurrentMonth()
        advanceUntilIdle()
        
        // Then
        viewModel.uiState.test {
            val state = awaitItem()
            assertFalse(state.isLoading)
            assertEquals("Month archived successfully", state.successMessage)
        }
        
        coVerify { historyRepository.performMonthEndReset(testUserId) }
        coVerify(exactly = 2) { historyRepository.getArchiveStats(testUserId) } // init + reload
    }

    @Test
    fun `archiveCurrentMonth when repository fails should show error`() = runTest {
        // Given
        coEvery { historyRepository.performMonthEndReset(testUserId) } returns 
            Result.Error("Archive failed", Exception("Firestore error"))
        
        viewModel = HistoryViewModel(historyRepository, auth)
        advanceUntilIdle()
        
        // When
        viewModel.archiveCurrentMonth()
        advanceUntilIdle()
        
        // Then
        viewModel.uiState.test {
            val state = awaitItem()
            assertFalse(state.isLoading)
            assertEquals("Archive failed", state.error)
        }
    }

    @Test
    fun `deleteArchive should succeed and reload stats`() = runTest {
        // Given
        coEvery { historyRepository.deleteArchive("archive_1") } returns Result.Success(Unit)
        coEvery { historyRepository.getArchiveStats(testUserId) } returns Result.Success(testStats)
        
        viewModel = HistoryViewModel(historyRepository, auth)
        advanceUntilIdle()
        
        // When
        viewModel.deleteArchive("archive_1")
        advanceUntilIdle()
        
        // Then
        viewModel.uiState.test {
            val state = awaitItem()
            assertFalse(state.isLoading)
            assertEquals("Archive deleted", state.successMessage)
        }
        
        coVerify { historyRepository.deleteArchive("archive_1") }
        coVerify(exactly = 2) { historyRepository.getArchiveStats(testUserId) }
    }

    @Test
    fun `deleteArchive when repository fails should show error`() = runTest {
        // Given
        coEvery { historyRepository.deleteArchive("archive_1") } returns 
            Result.Error("Delete failed", Exception("Not found"))
        
        viewModel = HistoryViewModel(historyRepository, auth)
        advanceUntilIdle()
        
        // When
        viewModel.deleteArchive("archive_1")
        advanceUntilIdle()
        
        // Then
        viewModel.uiState.test {
            val state = awaitItem()
            assertFalse(state.isLoading)
            assertEquals("Delete failed", state.error)
        }
    }

    @Test
    fun `restoreArchive should succeed`() = runTest {
        // Given
        coEvery { historyRepository.restoreArchive("archive_1") } returns Result.Success(Unit)
        
        viewModel = HistoryViewModel(historyRepository, auth)
        advanceUntilIdle()
        
        // When
        viewModel.restoreArchive("archive_1")
        advanceUntilIdle()
        
        // Then
        viewModel.uiState.test {
            val state = awaitItem()
            assertFalse(state.isLoading)
            assertEquals("Archive restored", state.successMessage)
        }
        
        coVerify { historyRepository.restoreArchive("archive_1") }
    }

    @Test
    fun `getArchiveByMonth should update selectedArchive`() = runTest {
        // Given
        coEvery { historyRepository.getArchiveByMonth(testUserId, 2025, 10) } returns 
            Result.Success(testArchive)
        
        viewModel = HistoryViewModel(historyRepository, auth)
        advanceUntilIdle()
        
        // When
        viewModel.getArchiveByMonth(2025, 10)
        advanceUntilIdle()
        
        // Then
        viewModel.uiState.test {
            val state = awaitItem()
            assertNotNull(state.selectedArchive)
            assertEquals("archive_1", state.selectedArchive!!.id)
            assertEquals(2025, state.selectedArchive!!.yearMonth.year)
            assertEquals(10, state.selectedArchive!!.yearMonth.monthValue)
        }
        
        coVerify { historyRepository.getArchiveByMonth(testUserId, 2025, 10) }
    }

    @Test
    fun `getArchiveByMonth when not found should set null`() = runTest {
        // Given
        coEvery { historyRepository.getArchiveByMonth(testUserId, 2025, 10) } returns 
            Result.Success(null)
        
        viewModel = HistoryViewModel(historyRepository, auth)
        advanceUntilIdle()
        
        // When
        viewModel.getArchiveByMonth(2025, 10)
        advanceUntilIdle()
        
        // Then
        viewModel.uiState.test {
            val state = awaitItem()
            assertNull(state.selectedArchive)
        }
    }

    @Test
    fun `clearMessages should reset error and successMessage`() = runTest {
        // Given
        coEvery { historyRepository.deleteArchive("archive_1") } returns 
            Result.Error("Delete failed", Exception())
        
        viewModel = HistoryViewModel(historyRepository, auth)
        advanceUntilIdle()
        
        viewModel.deleteArchive("archive_1")
        advanceUntilIdle()
        
        // When
        viewModel.clearMessages()
        
        // Then
        viewModel.uiState.test {
            val state = awaitItem()
            assertNull(state.error)
            assertNull(state.successMessage)
        }
    }

    @Test
    fun `clearSelection should reset selectedArchive`() = runTest {
        // Given
        coEvery { historyRepository.getArchiveByMonth(testUserId, 2025, 10) } returns 
            Result.Success(testArchive)
        
        viewModel = HistoryViewModel(historyRepository, auth)
        advanceUntilIdle()
        
        viewModel.getArchiveByMonth(2025, 10)
        advanceUntilIdle()
        
        // When
        viewModel.clearSelection()
        
        // Then
        viewModel.uiState.test {
            val state = awaitItem()
            assertNull(state.selectedArchive)
        }
    }

    @Test
    fun `initial uiState should not be loading after init`() = runTest {
        // When
        viewModel = HistoryViewModel(historyRepository, auth)
        advanceUntilIdle()
        
        // Then
        viewModel.uiState.test {
            val state = awaitItem()
            assertFalse(state.isLoading)
        }
    }
}
