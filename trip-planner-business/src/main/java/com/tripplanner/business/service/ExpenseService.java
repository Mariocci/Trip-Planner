package com.tripplanner.business.service;

import com.tripplanner.domain.dto.CreateExpenseDTO;
import com.tripplanner.domain.dto.ExpenseResponseDTO;
import com.tripplanner.domain.dto.UpdateExpenseDTO;

import java.util.List;

/**
 * Service interface for expense management operations.
 */
public interface ExpenseService {

    /**
     * Creates a new expense for a trip and updates the trip's total expense.
     *
     * @param tripId the ID of the trip
     * @param userId the ID of the user creating the expense
     * @param createDTO the expense creation data
     * @return the created expense
     * @throws RuntimeException if user is not a participant
     */
    ExpenseResponseDTO createExpense(Integer tripId, Integer userId, CreateExpenseDTO createDTO);

    /**
     * Retrieves an expense by ID.
     *
     * @param expenseId the ID of the expense
     * @param userId the ID of the requesting user
     * @return the expense details
     * @throws RuntimeException if expense not found or user is not a participant
     */
    ExpenseResponseDTO getExpenseById(Integer expenseId, Integer userId);

    /**
     * Lists all expenses for a trip.
     *
     * @param tripId the ID of the trip
     * @param userId the ID of the requesting user
     * @return list of expenses
     * @throws RuntimeException if user is not a participant
     */
    List<ExpenseResponseDTO> listTripExpenses(Integer tripId, Integer userId);

    /**
     * Updates an expense and recalculates the trip's total expense.
     *
     * @param expenseId the ID of the expense to update
     * @param userId the ID of the requesting user
     * @param updateDTO the update data
     * @return the updated expense
     * @throws RuntimeException if user is not a participant
     */
    ExpenseResponseDTO updateExpense(Integer expenseId, Integer userId, UpdateExpenseDTO updateDTO);

    /**
     * Deletes an expense and recalculates the trip's total expense.
     *
     * @param expenseId the ID of the expense to delete
     * @param userId the ID of the requesting user
     * @throws RuntimeException if user is not a participant
     */
    void deleteExpense(Integer expenseId, Integer userId);
}
