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

    

    @Test
    void createExpense_WithValidData_ShouldCreateExpenseAndRecalculateTotal() {
        
        when(tripService.isUserParticipant(tripId, userId)).thenReturn(true);
        when(tripRepository.findById(tripId)).thenReturn(Optional.of(testTrip));
        when(expenseRepository.save(any(Trosak.class))).thenReturn(testExpense);

        
        ExpenseResponseDTO result = expenseService.createExpense(tripId, userId, createExpenseDTO);

        
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
        
        when(tripService.isUserParticipant(tripId, userId)).thenReturn(false);

        
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
        
        when(tripService.isUserParticipant(tripId, userId)).thenReturn(true);
        when(tripRepository.findById(tripId)).thenReturn(Optional.empty());

        
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
        
        when(tripService.isUserParticipant(tripId, userId)).thenReturn(true);
        when(tripRepository.findById(tripId)).thenReturn(Optional.of(testTrip));
        when(expenseRepository.save(any(Trosak.class))).thenReturn(testExpense);

        
        expenseService.createExpense(tripId, userId, createExpenseDTO);

        
        verify(tripService).recalculateTotalExpense(tripId);
    }

    

    @Test
    void getExpenseById_WithValidIdAndParticipant_ShouldReturnExpense() {
        
        when(expenseRepository.findById(1)).thenReturn(Optional.of(testExpense));
        when(tripService.isUserParticipant(tripId, userId)).thenReturn(true);

        
        ExpenseResponseDTO result = expenseService.getExpenseById(1, userId);

        
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
        
        when(expenseRepository.findById(999)).thenReturn(Optional.empty());

        
        assertThatThrownBy(() -> expenseService.getExpenseById(999, userId))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Expense not found");
        
        verify(expenseRepository).findById(999);
        verify(tripService, never()).isUserParticipant(anyInt(), anyInt());
    }

    @Test
    void getExpenseById_WithNonParticipant_ShouldThrowException() {
        
        when(expenseRepository.findById(1)).thenReturn(Optional.of(testExpense));
        when(tripService.isUserParticipant(tripId, userId)).thenReturn(false);

        
        assertThatThrownBy(() -> expenseService.getExpenseById(1, userId))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Access denied: User is not a participant of this trip");
        
        verify(expenseRepository).findById(1);
        verify(tripService).isUserParticipant(tripId, userId);
    }

    

    @Test
    void listTripExpenses_WithValidTripAndParticipant_ShouldReturnExpenses() {
        
        Trosak expense1 = createTestExpense(1, testTrip, new BigDecimal("100.00"));
        Trosak expense2 = createTestExpense(2, testTrip, new BigDecimal("50.00"));
        List<Trosak> expenses = Arrays.asList(expense1, expense2);
        
        when(tripService.isUserParticipant(tripId, userId)).thenReturn(true);
        when(expenseRepository.findByPutovanje_PutovanjeId(tripId)).thenReturn(expenses);

        
        List<ExpenseResponseDTO> result = expenseService.listTripExpenses(tripId, userId);

        
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
        
        when(tripService.isUserParticipant(tripId, userId)).thenReturn(false);

        
        assertThatThrownBy(() -> expenseService.listTripExpenses(tripId, userId))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Access denied: User is not a participant of this trip");
        
        verify(tripService).isUserParticipant(tripId, userId);
        verify(expenseRepository, never()).findByPutovanje_PutovanjeId(anyInt());
    }

    @Test
    void listTripExpenses_WithNoExpenses_ShouldReturnEmptyList() {
        
        when(tripService.isUserParticipant(tripId, userId)).thenReturn(true);
        when(expenseRepository.findByPutovanje_PutovanjeId(tripId)).thenReturn(Arrays.asList());

        
        List<ExpenseResponseDTO> result = expenseService.listTripExpenses(tripId, userId);

        
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();
        
        verify(tripService).isUserParticipant(tripId, userId);
        verify(expenseRepository).findByPutovanje_PutovanjeId(tripId);
    }

    

    @Test
    void updateExpense_WithValidData_ShouldUpdateExpenseAndRecalculateTotal() {
        
        when(expenseRepository.findById(1)).thenReturn(Optional.of(testExpense));
        when(tripService.isUserParticipant(tripId, userId)).thenReturn(true);
        when(expenseRepository.save(any(Trosak.class))).thenReturn(testExpense);

        
        ExpenseResponseDTO result = expenseService.updateExpense(1, userId, updateExpenseDTO);

        
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
        
        UpdateExpenseDTO partialUpdate = UpdateExpenseDTO.builder()
                .iznos(new BigDecimal("200.00"))
                .build();
        
        when(expenseRepository.findById(1)).thenReturn(Optional.of(testExpense));
        when(tripService.isUserParticipant(tripId, userId)).thenReturn(true);
        when(expenseRepository.save(any(Trosak.class))).thenReturn(testExpense);

        
        expenseService.updateExpense(1, userId, partialUpdate);

        
        assertThat(testExpense.getIznos()).isEqualByComparingTo(new BigDecimal("200.00"));
        assertThat(testExpense.getOpis()).isEqualTo("Test expense"); 
        assertThat(testExpense.getDatum()).isEqualTo(LocalDate.now().plusDays(2)); 
        
        verify(expenseRepository).save(testExpense);
        verify(tripService).recalculateTotalExpense(tripId);
    }

    @Test
    void updateExpense_WithNonExistentExpense_ShouldThrowException() {
        
        when(expenseRepository.findById(999)).thenReturn(Optional.empty());

        
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
        
        when(expenseRepository.findById(1)).thenReturn(Optional.of(testExpense));
        when(tripService.isUserParticipant(tripId, userId)).thenReturn(false);

        
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
        
        when(expenseRepository.findById(1)).thenReturn(Optional.of(testExpense));
        when(tripService.isUserParticipant(tripId, userId)).thenReturn(true);
        when(expenseRepository.save(any(Trosak.class))).thenReturn(testExpense);

        
        expenseService.updateExpense(1, userId, updateExpenseDTO);

        
        verify(tripService).recalculateTotalExpense(tripId);
    }

    

    @Test
    void deleteExpense_WithValidIdAndParticipant_ShouldDeleteExpenseAndRecalculateTotal() {
        
        when(expenseRepository.findById(1)).thenReturn(Optional.of(testExpense));
        when(tripService.isUserParticipant(tripId, userId)).thenReturn(true);
        doNothing().when(expenseRepository).delete(testExpense);

        
        expenseService.deleteExpense(1, userId);

        
        verify(expenseRepository).findById(1);
        verify(tripService).isUserParticipant(tripId, userId);
        verify(expenseRepository).delete(testExpense);
        verify(tripService).recalculateTotalExpense(tripId);
    }

    @Test
    void deleteExpense_WithNonExistentExpense_ShouldThrowException() {
        
        when(expenseRepository.findById(999)).thenReturn(Optional.empty());

        
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
        
        when(expenseRepository.findById(1)).thenReturn(Optional.of(testExpense));
        when(tripService.isUserParticipant(tripId, userId)).thenReturn(false);

        
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
        
        when(expenseRepository.findById(1)).thenReturn(Optional.of(testExpense));
        when(tripService.isUserParticipant(tripId, userId)).thenReturn(true);
        doNothing().when(expenseRepository).delete(testExpense);

        
        expenseService.deleteExpense(1, userId);

        
        verify(tripService).recalculateTotalExpense(tripId);
    }

    

    @Test
    void createExpense_ShouldVerifyAllMockInteractions() {
        
        when(tripService.isUserParticipant(tripId, userId)).thenReturn(true);
        when(tripRepository.findById(tripId)).thenReturn(Optional.of(testTrip));
        when(expenseRepository.save(any(Trosak.class))).thenReturn(testExpense);

        
        expenseService.createExpense(tripId, userId, createExpenseDTO);

        
        verify(tripService).isUserParticipant(tripId, userId);
        verify(tripRepository).findById(tripId);
        verify(expenseRepository).save(any(Trosak.class));
        verify(tripService).recalculateTotalExpense(tripId);
        verifyNoMoreInteractions(tripService, tripRepository, expenseRepository);
    }

    @Test
    void updateExpense_ShouldVerifyAllMockInteractions() {
        
        when(expenseRepository.findById(1)).thenReturn(Optional.of(testExpense));
        when(tripService.isUserParticipant(tripId, userId)).thenReturn(true);
        when(expenseRepository.save(any(Trosak.class))).thenReturn(testExpense);

        
        expenseService.updateExpense(1, userId, updateExpenseDTO);

        
        verify(expenseRepository).findById(1);
        verify(tripService).isUserParticipant(tripId, userId);
        verify(expenseRepository).save(testExpense);
        verify(tripService).recalculateTotalExpense(tripId);
        verifyNoMoreInteractions(tripService, tripRepository, expenseRepository);
    }

    @Test
    void deleteExpense_ShouldVerifyAllMockInteractions() {
        
        when(expenseRepository.findById(1)).thenReturn(Optional.of(testExpense));
        when(tripService.isUserParticipant(tripId, userId)).thenReturn(true);
        doNothing().when(expenseRepository).delete(testExpense);

        
        expenseService.deleteExpense(1, userId);

        
        verify(expenseRepository).findById(1);
        verify(tripService).isUserParticipant(tripId, userId);
        verify(expenseRepository).delete(testExpense);
        verify(tripService).recalculateTotalExpense(tripId);
        verifyNoMoreInteractions(tripService, tripRepository, expenseRepository);
    }
}
