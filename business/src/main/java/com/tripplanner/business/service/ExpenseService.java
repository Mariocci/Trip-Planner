package com.tripplanner.business.service;

import com.tripplanner.domain.dto.CreateExpenseDTO;
import com.tripplanner.domain.dto.ExpenseResponseDTO;
import com.tripplanner.domain.dto.UpdateExpenseDTO;

import java.util.List;


public interface ExpenseService {

    
    ExpenseResponseDTO createExpense(Integer tripId, Integer userId, CreateExpenseDTO createDTO);

    
    ExpenseResponseDTO getExpenseById(Integer expenseId, Integer userId);

    
    List<ExpenseResponseDTO> listTripExpenses(Integer tripId, Integer userId);

    
    ExpenseResponseDTO updateExpense(Integer expenseId, Integer userId, UpdateExpenseDTO updateDTO);

    
    void deleteExpense(Integer expenseId, Integer userId);
}
