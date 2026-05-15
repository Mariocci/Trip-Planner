package com.tripplanner.presentation.controller;

import com.tripplanner.business.service.ExpenseService;
import com.tripplanner.domain.dto.CreateExpenseDTO;
import com.tripplanner.domain.dto.ExpenseResponseDTO;
import com.tripplanner.domain.dto.UpdateExpenseDTO;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/trips/{tripId}/expenses")
public class ExpenseController {

    private final ExpenseService expenseService;

    public ExpenseController(ExpenseService expenseService) {
        this.expenseService = expenseService;
    }

    @PostMapping
    public ResponseEntity<ExpenseResponseDTO> createExpense(
            @PathVariable Integer tripId,
            @RequestParam Integer userId,
            @Valid @RequestBody CreateExpenseDTO createDTO) {
        ExpenseResponseDTO expense = expenseService.createExpense(tripId, userId, createDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(expense);
    }

    @GetMapping("/{expenseId}")
    public ResponseEntity<ExpenseResponseDTO> getExpenseById(
            @PathVariable Integer expenseId,
            @RequestParam Integer userId) {
        ExpenseResponseDTO expense = expenseService.getExpenseById(expenseId, userId);
        return ResponseEntity.ok(expense);
    }

    @GetMapping
    public ResponseEntity<List<ExpenseResponseDTO>> listTripExpenses(
            @PathVariable Integer tripId,
            @RequestParam Integer userId) {
        List<ExpenseResponseDTO> expenses = expenseService.listTripExpenses(tripId, userId);
        return ResponseEntity.ok(expenses);
    }

    @PutMapping("/{expenseId}")
    public ResponseEntity<ExpenseResponseDTO> updateExpense(
            @PathVariable Integer expenseId,
            @RequestParam Integer userId,
            @Valid @RequestBody UpdateExpenseDTO updateDTO) {
        ExpenseResponseDTO expense = expenseService.updateExpense(expenseId, userId, updateDTO);
        return ResponseEntity.ok(expense);
    }

    @DeleteMapping("/{expenseId}")
    public ResponseEntity<Void> deleteExpense(
            @PathVariable Integer expenseId,
            @RequestParam Integer userId) {
        expenseService.deleteExpense(expenseId, userId);
        return ResponseEntity.noContent().build();
    }
}
