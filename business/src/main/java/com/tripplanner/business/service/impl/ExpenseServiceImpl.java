package com.tripplanner.business.service.impl;

import com.tripplanner.business.service.ExpenseService;
import com.tripplanner.business.service.TripService;
import com.tripplanner.dataaccess.repository.ExpenseRepository;
import com.tripplanner.dataaccess.repository.TripRepository;
import com.tripplanner.domain.dto.CreateExpenseDTO;
import com.tripplanner.domain.dto.ExpenseResponseDTO;
import com.tripplanner.domain.dto.UpdateExpenseDTO;
import com.tripplanner.domain.entity.Putovanje;
import com.tripplanner.domain.entity.Trosak;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;


@Service
@Transactional
public class ExpenseServiceImpl implements ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final TripRepository tripRepository;
    private final TripService tripService;

    public ExpenseServiceImpl(ExpenseRepository expenseRepository,
                             TripRepository tripRepository,
                             TripService tripService) {
        this.expenseRepository = expenseRepository;
        this.tripRepository = tripRepository;
        this.tripService = tripService;
    }

    @Override
    public ExpenseResponseDTO createExpense(Integer tripId, Integer userId, CreateExpenseDTO createDTO) {
        if (!tripService.isUserParticipant(tripId, userId)) {
            throw new RuntimeException("Access denied: User is not a participant of this trip");
        }

        Putovanje trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new RuntimeException("Trip not found"));

        // Check budget limit if set
        if (trip.getMaxBudget() != null) {
            BigDecimal currentTotal = expenseRepository.sumByPutovanjeId(tripId);
            if (currentTotal == null) currentTotal = BigDecimal.ZERO;
            if (currentTotal.add(createDTO.getIznos()).compareTo(trip.getMaxBudget()) > 0) {
                throw new IllegalArgumentException(
                    "Budget exceeded: adding this expense would surpass the trip budget of " + trip.getMaxBudget());
            }
        }

        Trosak expense = Trosak.builder()
                .iznos(createDTO.getIznos())
                .opis(createDTO.getOpis())
                .datum(createDTO.getDatum())
                .putovanje(trip)
                .build();

        expense = expenseRepository.save(expense);
        
        
        tripService.recalculateTotalExpense(tripId);

        return mapToResponseDTO(expense);
    }

    @Override
    @Transactional(readOnly = true)
    public ExpenseResponseDTO getExpenseById(Integer expenseId, Integer userId) {
        Trosak expense = expenseRepository.findById(expenseId)
                .orElseThrow(() -> new RuntimeException("Expense not found"));

        if (!tripService.isUserParticipant(expense.getPutovanje().getPutovanjeId(), userId)) {
            throw new RuntimeException("Access denied: User is not a participant of this trip");
        }

        return mapToResponseDTO(expense);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExpenseResponseDTO> listTripExpenses(Integer tripId, Integer userId) {
        if (!tripService.isUserParticipant(tripId, userId)) {
            throw new RuntimeException("Access denied: User is not a participant of this trip");
        }

        List<Trosak> expenses = expenseRepository.findByPutovanje_PutovanjeId(tripId);
        return expenses.stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public ExpenseResponseDTO updateExpense(Integer expenseId, Integer userId, UpdateExpenseDTO updateDTO) {
        Trosak expense = expenseRepository.findById(expenseId)
                .orElseThrow(() -> new RuntimeException("Expense not found"));

        if (!tripService.isUserParticipant(expense.getPutovanje().getPutovanjeId(), userId)) {
            throw new RuntimeException("Access denied: User is not a participant of this trip");
        }

        if (updateDTO.getIznos() != null) {
            // Check budget if amount is changing
            Putovanje trip = expense.getPutovanje();
            if (trip.getMaxBudget() != null) {
                BigDecimal currentTotal = expenseRepository.sumByPutovanjeId(trip.getPutovanjeId());
                if (currentTotal == null) currentTotal = BigDecimal.ZERO;
                // Subtract old amount, add new amount
                BigDecimal newTotal = currentTotal.subtract(expense.getIznos()).add(updateDTO.getIznos());
                if (newTotal.compareTo(trip.getMaxBudget()) > 0) {
                    throw new IllegalArgumentException(
                        "Budget exceeded: updating this expense would surpass the trip budget of " + trip.getMaxBudget());
                }
            }
            expense.setIznos(updateDTO.getIznos());
        }
        if (updateDTO.getOpis() != null) {
            expense.setOpis(updateDTO.getOpis());
        }
        if (updateDTO.getDatum() != null) {
            expense.setDatum(updateDTO.getDatum());
        }

        expense = expenseRepository.save(expense);
        
        
        tripService.recalculateTotalExpense(expense.getPutovanje().getPutovanjeId());

        return mapToResponseDTO(expense);
    }

    @Override
    public void deleteExpense(Integer expenseId, Integer userId) {
        Trosak expense = expenseRepository.findById(expenseId)
                .orElseThrow(() -> new RuntimeException("Expense not found"));

        if (!tripService.isUserParticipant(expense.getPutovanje().getPutovanjeId(), userId)) {
            throw new RuntimeException("Access denied: User is not a participant of this trip");
        }

        Integer tripId = expense.getPutovanje().getPutovanjeId();
        expenseRepository.delete(expense);
        
        
        tripService.recalculateTotalExpense(tripId);
    }

    private ExpenseResponseDTO mapToResponseDTO(Trosak expense) {
        return ExpenseResponseDTO.builder()
                .trosakId(expense.getTrosakId())
                .iznos(expense.getIznos())
                .opis(expense.getOpis())
                .datum(expense.getDatum())
                .putovanjeId(expense.getPutovanje().getPutovanjeId())
                .build();
    }
}
