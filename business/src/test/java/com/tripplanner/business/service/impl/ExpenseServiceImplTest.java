package com.tripplanner.business.service.impl;

import com.tripplanner.business.base.ServiceTestBase;
import com.tripplanner.business.service.TripService;
import com.tripplanner.dataaccess.repository.ExpenseRepository;
import com.tripplanner.dataaccess.repository.TripRepository;
import com.tripplanner.domain.dto.CreateExpenseDTO;
import com.tripplanner.domain.dto.ExpenseResponseDTO;
import com.tripplanner.domain.dto.UpdateExpenseDTO;
import com.tripplanner.domain.entity.Putovanje;
import com.tripplanner.domain.entity.Trosak;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link ExpenseServiceImpl}.
 * Tests expense creation, updates, deletion, retrieval, authorization logic,
 * and trip total recalculation.
 * 
 * Validates Requirements: 2.4, 2.9, 2.10, 2.11, 2.12, 2.13, 2.14, 2.15
 */
class ExpenseServiceImplTest extends ServiceTestBase {

    @Mock
    private ExpenseRepository expenseRepository;

    @Mock
    private TripRepository tripRepository;

    @Mock
    private TripService tripService;

    @InjectMocks
    private ExpenseServiceImpl expenseService;

    private Putovanje testTrip;
    private Trosak testExpense;
    private CreateExpenseDTO createExpenseDTO;
    private UpdateExpenseDTO updateExpenseDTO;
    private Integer userId;
    private Integer tripId;

    @BeforeEach
    void setUp() {
        userId = 1;
        tripId = 1;
        
        testTrip = createTestTrip(tripId, "Test Trip", 
                LocalDate.now().plusDays(1), LocalDate.now().plusDays(7));
        
        testExpense = createTestExpense(1, testTrip, new BigDecimal("100.00"), 
                "Test expense", LocalDate.now().plusDays(2));
        
        createExpenseDTO = CreateExpenseDTO.builder()
                .iznos(new BigDecimal("100.00"))
                .opis("Test expense")
                .datum(LocalDate.now().plusDays(2))
                .build();
        
        updateExpenseDTO = UpdateExpenseDTO.builder()
                .iznos(new BigDecimal("150.00"))
                .opis("Updated expense")
                .datum(LocalDate.now().plusDays(3))
                .build();
    }

    // ========== CREATE EXPENSE TESTS ==========

    @Test
    void createExpense_WithValidData_ShouldCreateExpenseAndRecalculateTotal() {
        // Given
        when(tripService.isUserParticipant(tripId, userId)).thenReturn(true);
        when(tripRepository.findById(tripId)).thenReturn(Optional.of(testTrip));
        when(expenseRepository.save(any(Trosak.class))).thenReturn(testExpense);

        // When
        ExpenseResponseDTO result = expenseService.createExpense(tripId, userId, createExpenseDTO);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getTrosakId()).isEqualTo(1);
        assertThat(result.getIznos()).isEqualByComparingTo(new BigDecimal("100.00"));
        assertThat(result.getOpis()).isEqualTo("Test expense");
        assertThat(result.getDatum()).isEqualTo(LocalDate.now().plusDays(2));
        assertThat(result.getPutovanjeId()).isEqualTo(tripId);
        
        verify(tripService).isUserParticipant(tripId, userId);
        verify(tripRepository).findById(tripId);
        verify(expenseRepository).save(any(Trosak.class));
        verify(tripService).recalculateTotalExpense(tripId);
    }

    @Test
    void createExpense_WithNonParticipant_ShouldThrowException() {
        // Given
        when(tripService.isUserParticipant(tripId, userId)).thenReturn(false);

        // When/Then
        assertThatThrownBy(() -> expenseService.createExpense(tripId, userId, createExpenseDTO))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Access denied: User is not a participant of this trip");
        
        verify(tripService).isUserParticipant(tripId, userId);
        verify(tripRepository, never()).findById(anyInt());
        verify(expenseRepository, never()).save(any(Trosak.class));
        verify(tripService, never()).recalculateTotalExpense(anyInt());
    }

    @Test
    void createExpense_WithNonExistentTrip_ShouldThrowException() {
        // Given
        when(tripService.isUserParticipant(tripId, userId)).thenReturn(true);
        when(tripRepository.findById(tripId)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> expenseService.createExpense(tripId, userId, createExpenseDTO))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Trip not found");
        
        verify(tripService).isUserParticipant(tripId, userId);
        verify(tripRepository).findById(tripId);
        verify(expenseRepository, never()).save(any(Trosak.class));
        verify(tripService, never()).recalculateTotalExpense(anyInt());
    }

    @Test
    void createExpense_ShouldTriggerTripTotalRecalculation() {
        // Given
        when(tripService.isUserParticipant(tripId, userId)).thenReturn(true);
        when(tripRepository.findById(tripId)).thenReturn(Optional.of(testTrip));
        when(expenseRepository.save(any(Trosak.class))).thenReturn(testExpense);

        // When
        expenseService.createExpense(tripId, userId, createExpenseDTO);

        // Then - Verify recalculateTotalExpense was called
        verify(tripService).recalculateTotalExpense(tripId);
    }

    // ========== GET EXPENSE BY ID TESTS ==========

    @Test
    void getExpenseById_WithValidIdAndParticipant_ShouldReturnExpense() {
        // Given
        when(expenseRepository.findById(1)).thenReturn(Optional.of(testExpense));
        when(tripService.isUserParticipant(tripId, userId)).thenReturn(true);

        // When
        ExpenseResponseDTO result = expenseService.getExpenseById(1, userId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getTrosakId()).isEqualTo(1);
        assertThat(result.getIznos()).isEqualByComparingTo(new BigDecimal("100.00"));
        assertThat(result.getOpis()).isEqualTo("Test expense");
        assertThat(result.getPutovanjeId()).isEqualTo(tripId);
        
        verify(expenseRepository).findById(1);
        verify(tripService).isUserParticipant(tripId, userId);
    }

    @Test
    void getExpenseById_WithNonExistentExpense_ShouldThrowException() {
        // Given
        when(expenseRepository.findById(999)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> expenseService.getExpenseById(999, userId))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Expense not found");
        
        verify(expenseRepository).findById(999);
        verify(tripService, never()).isUserParticipant(anyInt(), anyInt());
    }

    @Test
    void getExpenseById_WithNonParticipant_ShouldThrowException() {
        // Given
        when(expenseRepository.findById(1)).thenReturn(Optional.of(testExpense));
        when(tripService.isUserParticipant(tripId, userId)).thenReturn(false);

        // When/Then
        assertThatThrownBy(() -> expenseService.getExpenseById(1, userId))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Access denied: User is not a participant of this trip");
        
        verify(expenseRepository).findById(1);
        verify(tripService).isUserParticipant(tripId, userId);
    }

    // ========== LIST TRIP EXPENSES TESTS ==========

    @Test
    void listTripExpenses_WithValidTripAndParticipant_ShouldReturnExpenses() {
        // Given
        Trosak expense1 = createTestExpense(1, testTrip, new BigDecimal("100.00"));
        Trosak expense2 = createTestExpense(2, testTrip, new BigDecimal("50.00"));
        List<Trosak> expenses = Arrays.asList(expense1, expense2);
        
        when(tripService.isUserParticipant(tripId, userId)).thenReturn(true);
        when(expenseRepository.findByPutovanje_PutovanjeId(tripId)).thenReturn(expenses);

        // When
        List<ExpenseResponseDTO> result = expenseService.listTripExpenses(tripId, userId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getTrosakId()).isEqualTo(1);
        assertThat(result.get(0).getIznos()).isEqualByComparingTo(new BigDecimal("100.00"));
        assertThat(result.get(1).getTrosakId()).isEqualTo(2);
        assertThat(result.get(1).getIznos()).isEqualByComparingTo(new BigDecimal("50.00"));
        
        verify(tripService).isUserParticipant(tripId, userId);
        verify(expenseRepository).findByPutovanje_PutovanjeId(tripId);
    }

    @Test
    void listTripExpenses_WithNonParticipant_ShouldThrowException() {
        // Given
        when(tripService.isUserParticipant(tripId, userId)).thenReturn(false);

        // When/Then
        assertThatThrownBy(() -> expenseService.listTripExpenses(tripId, userId))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Access denied: User is not a participant of this trip");
        
        verify(tripService).isUserParticipant(tripId, userId);
        verify(expenseRepository, never()).findByPutovanje_PutovanjeId(anyInt());
    }

    @Test
    void listTripExpenses_WithNoExpenses_ShouldReturnEmptyList() {
        // Given
        when(tripService.isUserParticipant(tripId, userId)).thenReturn(true);
        when(expenseRepository.findByPutovanje_PutovanjeId(tripId)).thenReturn(Arrays.asList());

        // When
        List<ExpenseResponseDTO> result = expenseService.listTripExpenses(tripId, userId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();
        
        verify(tripService).isUserParticipant(tripId, userId);
        verify(expenseRepository).findByPutovanje_PutovanjeId(tripId);
    }

    // ========== UPDATE EXPENSE TESTS ==========

    @Test
    void updateExpense_WithValidData_ShouldUpdateExpenseAndRecalculateTotal() {
        // Given
        when(expenseRepository.findById(1)).thenReturn(Optional.of(testExpense));
        when(tripService.isUserParticipant(tripId, userId)).thenReturn(true);
        when(expenseRepository.save(any(Trosak.class))).thenReturn(testExpense);

        // When
        ExpenseResponseDTO result = expenseService.updateExpense(1, userId, updateExpenseDTO);

        // Then
        assertThat(result).isNotNull();
        assertThat(testExpense.getIznos()).isEqualByComparingTo(new BigDecimal("150.00"));
        assertThat(testExpense.getOpis()).isEqualTo("Updated expense");
        assertThat(testExpense.getDatum()).isEqualTo(LocalDate.now().plusDays(3));
        
        verify(expenseRepository).findById(1);
        verify(tripService).isUserParticipant(tripId, userId);
        verify(expenseRepository).save(testExpense);
        verify(tripService).recalculateTotalExpense(tripId);
    }

    @Test
    void updateExpense_WithPartialData_ShouldUpdateOnlyProvidedFields() {
        // Given
        UpdateExpenseDTO partialUpdate = UpdateExpenseDTO.builder()
                .iznos(new BigDecimal("200.00"))
                .build();
        
        when(expenseRepository.findById(1)).thenReturn(Optional.of(testExpense));
        when(tripService.isUserParticipant(tripId, userId)).thenReturn(true);
        when(expenseRepository.save(any(Trosak.class))).thenReturn(testExpense);

        // When
        expenseService.updateExpense(1, userId, partialUpdate);

        // Then
        assertThat(testExpense.getIznos()).isEqualByComparingTo(new BigDecimal("200.00"));
        assertThat(testExpense.getOpis()).isEqualTo("Test expense"); // Unchanged
        assertThat(testExpense.getDatum()).isEqualTo(LocalDate.now().plusDays(2)); // Unchanged
        
        verify(expenseRepository).save(testExpense);
        verify(tripService).recalculateTotalExpense(tripId);
    }

    @Test
    void updateExpense_WithNonExistentExpense_ShouldThrowException() {
        // Given
        when(expenseRepository.findById(999)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> expenseService.updateExpense(999, userId, updateExpenseDTO))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Expense not found");
        
        verify(expenseRepository).findById(999);
        verify(tripService, never()).isUserParticipant(anyInt(), anyInt());
        verify(expenseRepository, never()).save(any(Trosak.class));
        verify(tripService, never()).recalculateTotalExpense(anyInt());
    }

    @Test
    void updateExpense_WithNonParticipant_ShouldThrowException() {
        // Given
        when(expenseRepository.findById(1)).thenReturn(Optional.of(testExpense));
        when(tripService.isUserParticipant(tripId, userId)).thenReturn(false);

        // When/Then
        assertThatThrownBy(() -> expenseService.updateExpense(1, userId, updateExpenseDTO))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Access denied: User is not a participant of this trip");
        
        verify(expenseRepository).findById(1);
        verify(tripService).isUserParticipant(tripId, userId);
        verify(expenseRepository, never()).save(any(Trosak.class));
        verify(tripService, never()).recalculateTotalExpense(anyInt());
    }

    @Test
    void updateExpense_ShouldTriggerTripTotalRecalculation() {
        // Given
        when(expenseRepository.findById(1)).thenReturn(Optional.of(testExpense));
        when(tripService.isUserParticipant(tripId, userId)).thenReturn(true);
        when(expenseRepository.save(any(Trosak.class))).thenReturn(testExpense);

        // When
        expenseService.updateExpense(1, userId, updateExpenseDTO);

        // Then - Verify recalculateTotalExpense was called
        verify(tripService).recalculateTotalExpense(tripId);
    }

    // ========== DELETE EXPENSE TESTS ==========

    @Test
    void deleteExpense_WithValidIdAndParticipant_ShouldDeleteExpenseAndRecalculateTotal() {
        // Given
        when(expenseRepository.findById(1)).thenReturn(Optional.of(testExpense));
        when(tripService.isUserParticipant(tripId, userId)).thenReturn(true);
        doNothing().when(expenseRepository).delete(testExpense);

        // When
        expenseService.deleteExpense(1, userId);

        // Then
        verify(expenseRepository).findById(1);
        verify(tripService).isUserParticipant(tripId, userId);
        verify(expenseRepository).delete(testExpense);
        verify(tripService).recalculateTotalExpense(tripId);
    }

    @Test
    void deleteExpense_WithNonExistentExpense_ShouldThrowException() {
        // Given
        when(expenseRepository.findById(999)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> expenseService.deleteExpense(999, userId))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Expense not found");
        
        verify(expenseRepository).findById(999);
        verify(tripService, never()).isUserParticipant(anyInt(), anyInt());
        verify(expenseRepository, never()).delete(any(Trosak.class));
        verify(tripService, never()).recalculateTotalExpense(anyInt());
    }

    @Test
    void deleteExpense_WithNonParticipant_ShouldThrowException() {
        // Given
        when(expenseRepository.findById(1)).thenReturn(Optional.of(testExpense));
        when(tripService.isUserParticipant(tripId, userId)).thenReturn(false);

        // When/Then
        assertThatThrownBy(() -> expenseService.deleteExpense(1, userId))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Access denied: User is not a participant of this trip");
        
        verify(expenseRepository).findById(1);
        verify(tripService).isUserParticipant(tripId, userId);
        verify(expenseRepository, never()).delete(any(Trosak.class));
        verify(tripService, never()).recalculateTotalExpense(anyInt());
    }

    @Test
    void deleteExpense_ShouldTriggerTripTotalRecalculation() {
        // Given
        when(expenseRepository.findById(1)).thenReturn(Optional.of(testExpense));
        when(tripService.isUserParticipant(tripId, userId)).thenReturn(true);
        doNothing().when(expenseRepository).delete(testExpense);

        // When
        expenseService.deleteExpense(1, userId);

        // Then - Verify recalculateTotalExpense was called
        verify(tripService).recalculateTotalExpense(tripId);
    }

    // ========== MOCK INTERACTION VERIFICATION TESTS ==========

    @Test
    void createExpense_ShouldVerifyAllMockInteractions() {
        // Given
        when(tripService.isUserParticipant(tripId, userId)).thenReturn(true);
        when(tripRepository.findById(tripId)).thenReturn(Optional.of(testTrip));
        when(expenseRepository.save(any(Trosak.class))).thenReturn(testExpense);

        // When
        expenseService.createExpense(tripId, userId, createExpenseDTO);

        // Then - Verify all expected interactions occurred
        verify(tripService).isUserParticipant(tripId, userId);
        verify(tripRepository).findById(tripId);
        verify(expenseRepository).save(any(Trosak.class));
        verify(tripService).recalculateTotalExpense(tripId);
        verifyNoMoreInteractions(tripService, tripRepository, expenseRepository);
    }

    @Test
    void updateExpense_ShouldVerifyAllMockInteractions() {
        // Given
        when(expenseRepository.findById(1)).thenReturn(Optional.of(testExpense));
        when(tripService.isUserParticipant(tripId, userId)).thenReturn(true);
        when(expenseRepository.save(any(Trosak.class))).thenReturn(testExpense);

        // When
        expenseService.updateExpense(1, userId, updateExpenseDTO);

        // Then - Verify all expected interactions occurred
        verify(expenseRepository).findById(1);
        verify(tripService).isUserParticipant(tripId, userId);
        verify(expenseRepository).save(testExpense);
        verify(tripService).recalculateTotalExpense(tripId);
        verifyNoMoreInteractions(tripService, tripRepository, expenseRepository);
    }

    @Test
    void deleteExpense_ShouldVerifyAllMockInteractions() {
        // Given
        when(expenseRepository.findById(1)).thenReturn(Optional.of(testExpense));
        when(tripService.isUserParticipant(tripId, userId)).thenReturn(true);
        doNothing().when(expenseRepository).delete(testExpense);

        // When
        expenseService.deleteExpense(1, userId);

        // Then - Verify all expected interactions occurred
        verify(expenseRepository).findById(1);
        verify(tripService).isUserParticipant(tripId, userId);
        verify(expenseRepository).delete(testExpense);
        verify(tripService).recalculateTotalExpense(tripId);
        verifyNoMoreInteractions(tripService, tripRepository, expenseRepository);
    }
}
