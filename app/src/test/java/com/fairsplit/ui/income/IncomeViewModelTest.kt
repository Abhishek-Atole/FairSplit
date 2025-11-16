package com.fairsplit.ui.income

import app.cash.turbine.test
import com.fairsplit.domain.model.IncomeSource
import com.fairsplit.domain.model.MonthlyIncome
import com.fairsplit.domain.repository.IncomeRepository
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
import java.time.Instant
import java.time.LocalDate

/**
 * Unit tests for IncomeViewModel
 * Tests income operations, state management, and error handling
 */
@OptIn(ExperimentalCoroutinesApi::class)
class IncomeViewModelTest {

    private lateinit var viewModel: IncomeViewModel
    private lateinit var incomeRepository: IncomeRepository
    private lateinit var auth: FirebaseAuth
    private lateinit var mockUser: FirebaseUser
    
    private val testDispatcher = StandardTestDispatcher()
    private val testUserId = "test_user_123"
    
    private val testIncome = MonthlyIncome(
        id = "income_1",
        userId = testUserId,
        amount = 50000.0,
        source = IncomeSource.SALARY,
        startDate = LocalDate.of(2025, 11, 1),
        endDate = LocalDate.of(2025, 11, 30),
        month = 11,
        year = 2025,
        createdAt = Instant.now(),
        updatedAt = Instant.now()
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        
        incomeRepository = mockk(relaxed = true)
        auth = mockk(relaxed = true)
        mockUser = mockk(relaxed = true)
        
        every { auth.currentUser } returns mockUser
        every { mockUser.uid } returns testUserId
        every { auth.currentUser?.uid } returns testUserId
        
        // Mock repository to return empty flow initially
        every { incomeRepository.getAllIncome(testUserId) } returns flowOf(emptyList())
    }

    @After
    fun teardown() {
        Dispatchers.resetMain()
        clearAllMocks()
    }

    @Test
    fun `addIncome with valid data should succeed`() = runTest {
        // Given
        coEvery { incomeRepository.addIncome(any()) } returns Result.Success("income_1")
        every { incomeRepository.getAllIncome(testUserId) } returns flowOf(listOf(testIncome))
        
        viewModel = IncomeViewModel(incomeRepository, auth)
        advanceUntilIdle()
        
        // When
        viewModel.addIncome(
            amount = 50000.0,
            source = IncomeSource.SALARY,
            startDate = LocalDate.of(2025, 11, 1)
        )
        advanceUntilIdle()
        
        // Then - verify repository was called
        coVerify { incomeRepository.addIncome(any()) }
        
        // State will be HistoryLoaded after loadAllIncome() is called
        assertTrue(
            viewModel.uiState.value is IncomeUiState.Success || 
            viewModel.uiState.value is IncomeUiState.HistoryLoaded
        )
    }

    @Test
    fun `addIncome with zero amount should fail`() = runTest {
        // Given
        viewModel = IncomeViewModel(incomeRepository, auth)
        advanceUntilIdle()
        
        // When
        viewModel.addIncome(
            amount = 0.0,
            source = IncomeSource.SALARY,
            startDate = LocalDate.of(2025, 11, 1)
        )
        advanceUntilIdle()
        
        // Then
        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state is IncomeUiState.Error)
            assertEquals("Amount must be greater than 0", (state as IncomeUiState.Error).message)
        }
        
        coVerify(exactly = 0) { incomeRepository.addIncome(any()) }
    }

    @Test
    fun `addIncome with negative amount should fail`() = runTest {
        // Given
        viewModel = IncomeViewModel(incomeRepository, auth)
        advanceUntilIdle()
        
        // When
        viewModel.addIncome(
            amount = -1000.0,
            source = IncomeSource.SALARY,
            startDate = LocalDate.of(2025, 11, 1)
        )
        advanceUntilIdle()
        
        // Then
        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state is IncomeUiState.Error)
        }
    }

    @Test
    fun `addIncome when user not authenticated should fail`() = runTest {
        // Given
        every { auth.currentUser } returns null
        every { auth.currentUser?.uid } returns null
        every { incomeRepository.getAllIncome(any()) } returns flowOf(emptyList())
        
        viewModel = IncomeViewModel(incomeRepository, auth)
        advanceUntilIdle()
        
        // When
        viewModel.addIncome(
            amount = 50000.0,
            source = IncomeSource.SALARY,
            startDate = LocalDate.of(2025, 11, 1)
        )
        advanceUntilIdle()
        
        // Then
        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state is IncomeUiState.Error)
            assertEquals("User not authenticated", (state as IncomeUiState.Error).message)
        }
    }

    @Test
    fun `addIncome when repository fails should show error`() = runTest {
        // Given
        coEvery { incomeRepository.addIncome(any()) } returns 
            Result.Error("Database error", Exception("DB failure"))
        
        viewModel = IncomeViewModel(incomeRepository, auth)
        advanceUntilIdle()
        
        // When
        viewModel.addIncome(
            amount = 50000.0,
            source = IncomeSource.SALARY,
            startDate = LocalDate.of(2025, 11, 1)
        )
        advanceUntilIdle()
        
        // Then
        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state is IncomeUiState.Error)
            assertEquals("Database error", (state as IncomeUiState.Error).message)
        }
    }

    @Test
    fun `loadAllIncome should update incomeList`() = runTest {
        // Given
        val incomeList = listOf(testIncome, testIncome.copy(id = "income_2", amount = 60000.0))
        every { incomeRepository.getAllIncome(testUserId) } returns flowOf(incomeList)
        
        // When
        viewModel = IncomeViewModel(incomeRepository, auth)
        advanceUntilIdle()
        
        // Then
        viewModel.incomeList.test {
            val items = awaitItem()
            assertEquals(2, items.size)
            assertEquals(50000.0, items[0].amount, 0.01)
            assertEquals(60000.0, items[1].amount, 0.01)
        }
    }

    @Test
    fun `loadIncomeForMonth should filter by month and year`() = runTest {
        // Given
        coEvery { incomeRepository.getIncomeByMonth(11, 2025) } returns Result.Success(testIncome)
        every { incomeRepository.getAllIncome(testUserId) } returns flowOf(emptyList())
        
        viewModel = IncomeViewModel(incomeRepository, auth)
        advanceUntilIdle()
        
        // When
        viewModel.loadIncomeForMonth(11, 2025)
        advanceUntilIdle()
        
        // Then
        coVerify { incomeRepository.getIncomeByMonth(11, 2025) }
    }

    @Test
    fun `updateIncome should succeed with valid data`() = runTest {
        // Given
        val updatedIncome = testIncome.copy(amount = 55000.0, source = IncomeSource.BUSINESS)
        coEvery { incomeRepository.updateIncome(any()) } returns Result.Success(Unit)
        every { incomeRepository.getAllIncome(testUserId) } returns flowOf(listOf(updatedIncome))
        
        viewModel = IncomeViewModel(incomeRepository, auth)
        advanceUntilIdle()
        
        // When
        viewModel.updateIncome(updatedIncome)
        advanceUntilIdle()
        
        // Then - verify repository was called
        coVerify { incomeRepository.updateIncome(any()) }
        
        // State will be either Success or HistoryLoaded
        assertTrue(
            viewModel.uiState.value is IncomeUiState.Success || 
            viewModel.uiState.value is IncomeUiState.HistoryLoaded
        )
    }

    @Test
    fun `deleteIncome should succeed`() = runTest {
        // Given
        coEvery { incomeRepository.deleteIncome("income_1") } returns Result.Success(Unit)
        every { incomeRepository.getAllIncome(testUserId) } returns flowOf(emptyList())
        
        viewModel = IncomeViewModel(incomeRepository, auth)
        advanceUntilIdle()
        
        // When
        viewModel.deleteIncome("income_1")
        advanceUntilIdle()
        
        // Then - verify repository was called
        coVerify { incomeRepository.deleteIncome("income_1") }
        
        // State will be either Success or HistoryLoaded
        assertTrue(
            viewModel.uiState.value is IncomeUiState.Success || 
            viewModel.uiState.value is IncomeUiState.HistoryLoaded
        )
    }

    @Test
    fun `deleteIncome when repository fails should show error`() = runTest {
        // Given
        coEvery { incomeRepository.deleteIncome("income_1") } returns 
            Result.Error("Delete failed", Exception("Not found"))
        
        viewModel = IncomeViewModel(incomeRepository, auth)
        advanceUntilIdle()
        
        // When
        viewModel.deleteIncome("income_1")
        advanceUntilIdle()
        
        // Then
        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state is IncomeUiState.Error)
            assertEquals("Delete failed", (state as IncomeUiState.Error).message)
        }
    }

    @Test
    fun `initial state should be Loading`() = runTest {
        // When
        viewModel = IncomeViewModel(incomeRepository, auth)
        
        // Then - initial state before coroutines run
        assertTrue(viewModel.uiState.value is IncomeUiState.Loading)
    }

    @Test
    fun `incomeList flow should emit updates`() = runTest {
        // Given
        val income1 = listOf(testIncome)
        
        every { incomeRepository.getAllIncome(testUserId) } returns flowOf(income1)
        
        viewModel = IncomeViewModel(incomeRepository, auth)
        advanceUntilIdle()
        
        // Then
        viewModel.incomeList.test {
            val firstEmission = awaitItem()
            assertEquals(1, firstEmission.size)
        }
    }
}
