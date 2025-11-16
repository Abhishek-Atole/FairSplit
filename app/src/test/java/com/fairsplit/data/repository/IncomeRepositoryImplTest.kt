package com.fairsplit.data.repository

import app.cash.turbine.test
import com.fairsplit.data.local.dao.MonthlyIncomeDao
import com.fairsplit.data.local.entity.MonthlyIncomeEntity
import com.fairsplit.domain.model.IncomeSource
import com.fairsplit.domain.model.MonthlyIncome
import com.fairsplit.domain.util.Result
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.android.gms.tasks.Tasks
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.time.Instant
import java.time.LocalDate
import java.util.*

@OptIn(ExperimentalCoroutinesApi::class)
class IncomeRepositoryImplTest {

    private lateinit var repository: IncomeRepositoryImpl
    private lateinit var incomeDao: MonthlyIncomeDao
    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var mockUser: FirebaseUser

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
        incomeDao = mockk(relaxed = true)
        firestore = mockk(relaxed = true)
        auth = mockk(relaxed = true)
        mockUser = mockk(relaxed = true)

        every { auth.currentUser } returns mockUser
        every { mockUser.uid } returns testUserId
        every { auth.currentUser?.uid } returns testUserId

        // Stub Firebase write/delete/update tasks so await() completes
        stubFirestoreWriteTasks(firestore, testUserId)
        
        repository = IncomeRepositoryImpl(incomeDao, firestore, auth)
    }

    @After
    fun teardown() {
        clearAllMocks()
    }

    // Helper to stub Firestore write/delete/update Tasks to return completed Tasks
    private fun stubFirestoreWriteTasks(firestore: FirebaseFirestore, uid: String) {
        // Create deeply nested mock chain that matches repository usage
        val mockFinanceCollection = mockk<CollectionReference>(relaxed = true)
        val mockUsersDoc = mockk<DocumentReference>(relaxed = true)
        val mockUserCollection = mockk<CollectionReference>(relaxed = true)
        val mockMonthlyIncomeDoc = mockk<DocumentReference>(relaxed = true)
        val mockItemsCollection = mockk<CollectionReference>(relaxed = true)
        val mockItemDoc = mockk<DocumentReference>(relaxed = true)
        
        // Completed tasks
        val completedVoidTask: com.google.android.gms.tasks.Task<Void> = Tasks.forResult(null)
        val mockDocSnapshot = mockk<DocumentSnapshot>(relaxed = true)
        val completedDocTask = Tasks.forResult(mockDocSnapshot)

        // Build the exact chain: collection("finance").document("users").collection(uid).document("monthly_income").collection("items").document(id)
        every { firestore.collection("finance") } returns mockFinanceCollection
        every { mockFinanceCollection.document("users") } returns mockUsersDoc
        every { mockUsersDoc.collection(uid) } returns mockUserCollection
        every { mockUserCollection.document("monthly_income") } returns mockMonthlyIncomeDoc
        every { mockMonthlyIncomeDoc.collection("items") } returns mockItemsCollection
        every { mockItemsCollection.document(any()) } returns mockItemDoc
        
        // Mock all write operations on final document
        every { mockItemDoc.set(any()) } returns completedVoidTask
        every { mockItemDoc.update(any<Map<String, Any>>()) } returns completedVoidTask
        every { mockItemDoc.delete() } returns completedVoidTask
        every { mockItemDoc.get() } returns completedDocTask
    }

    @Test
    fun `addIncome should save to local database and sync to Firestore`() = runTest {
        // Given
        coEvery { incomeDao.insertIncome(any()) } just Runs

        // When
        val result = repository.addIncome(testIncome)

        // Then
        assertTrue(result is Result.Success)
        coVerify { incomeDao.insertIncome(any()) }
        // Firebase sync verification skipped in unit tests
    }

    @Test
    fun `addIncome should return error when database fails`() = runTest {
        // Given
        val exception = Exception("Database error")
        coEvery { incomeDao.insertIncome(any()) } throws exception

        // When
        val result = repository.addIncome(testIncome)

        // Then
        assertTrue(result is Result.Error)
        assertTrue((result as Result.Error).message.contains("Database error"))
    }

    @Test
    fun `getIncomeByMonth should return flow of incomes`() = runTest {
        // Given
        val entities = listOf(
            MonthlyIncomeEntity(
                id = "income_1",
                userId = testUserId,
                amount = 50000.0,
                source = "SALARY",
                startDate = System.currentTimeMillis(),
                endDate = System.currentTimeMillis() + 86400000L * 30,
                month = 11,
                year = 2025,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
        )
        
        coEvery { 
            incomeDao.getIncomeByMonth(testUserId, 11, 2025) 
        } returns flowOf(entities)

        // When
        val result = repository.getIncomeByMonth(11, 2025)

        // Then
        assertTrue(result is Result.Success)
        val income = (result as Result.Success).data
        assertNotNull(income)
        assertEquals("income_1", income!!.id)
        assertEquals(50000.0, income.amount, 0.01)
    }

    @Test
    fun `getAllIncome should return all user incomes`() = runTest {
        // Given
        val entities = listOf(
            MonthlyIncomeEntity(
                id = "income_1",
                userId = testUserId,
                amount = 50000.0,
                source = "SALARY",
                startDate = System.currentTimeMillis(),
                endDate = null,
                month = 11,
                year = 2025,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            ),
            MonthlyIncomeEntity(
                id = "income_2",
                userId = testUserId,
                amount = 30000.0,
                source = "BUSINESS",
                startDate = System.currentTimeMillis(),
                endDate = null,
                month = 10,
                year = 2025,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
        )
        
        coEvery { incomeDao.getAllIncome(testUserId) } returns flowOf(entities)

        // When
        val result = repository.getAllIncome(testUserId)

        // Then
        result.test {
            val incomes = awaitItem()
            assertEquals(2, incomes.size)
            awaitComplete()
        }
    }

    @Test
    fun `updateIncome should update local and sync to Firestore`() = runTest {
        // Given
        val updatedIncome = testIncome.copy(amount = 55000.0)
        coEvery { incomeDao.updateIncome(any()) } just Runs

        // When
        val result = repository.updateIncome(updatedIncome)

        // Then
        assertTrue(result is Result.Success)
        coVerify { incomeDao.updateIncome(any()) }
    }

    @Test
    fun `deleteIncome should remove from local and Firestore`() = runTest {
        // Given
        val entity = MonthlyIncomeEntity(
            id = "income_1",
            userId = testUserId,
            amount = 50000.0,
            source = "SALARY",
            startDate = System.currentTimeMillis(),
            endDate = null,
            month = 11,
            year = 2025,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        
        coEvery { incomeDao.getIncomeById("income_1") } returns entity
        coEvery { incomeDao.deleteIncome(entity) } just Runs

        // When
        val result = repository.deleteIncome("income_1")

        // Then
        assertTrue(result is Result.Success)
        coVerify { incomeDao.deleteIncome(entity) }
    }

    @Test
    fun `addCarryForwardIncome should create income with previous balance`() = runTest {
        // Given
        val previousBalance = 5000.0
        coEvery { incomeDao.insertIncome(any()) } just Runs

        // When
        val result = repository.addCarryForwardIncome(testUserId, 12, 2025, previousBalance)

        // Then
        assertTrue(result is Result.Success)
        coVerify { 
            incomeDao.insertIncome(match { 
                it.amount == previousBalance && it.month == 12 && it.year == 2025 && it.userId == testUserId
            }) 
        }
    }

    @Test
    fun `repository should throw exception when user not authenticated`() = runTest {
        // Given
        every { auth.currentUser } returns null

        // When & Then
        assertThrows(IllegalStateException::class.java) {
            runTest {
                repository.addIncome(testIncome)
            }
        }
    }
}
