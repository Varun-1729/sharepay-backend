package com.splitwise.controller;

import com.splitwise.dto.CreateExpenseRequest;
import com.splitwise.dto.ExpenseDTO;
import com.splitwise.entity.Expense;
import com.splitwise.entity.ExpenseSplit;
import com.splitwise.service.ExpenseService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Optional;

@RestController
@RequestMapping("/api/expenses")
@CrossOrigin(origins = "*") // Allow CORS for frontend
public class ExpenseController {
    
    private final ExpenseService expenseService;
    
    @Autowired
    public ExpenseController(ExpenseService expenseService) {
        this.expenseService = expenseService;
    }
    
    /**
     * Create a new expense with splits
     * POST /api/expenses
     */
    @PostMapping
    public ResponseEntity<?> createExpense(@Valid @RequestBody CreateExpenseRequest request) {
        try {
            Expense expense = expenseService.createExpense(request);
            ExpenseDTO expenseDTO = new ExpenseDTO(expense);
            return ResponseEntity.status(HttpStatus.CREATED).body(expenseDTO);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Failed to create expense: " + e.getMessage()));
        }
    }
    
    /**
     * Get expense by ID
     * GET /api/expenses/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getExpenseById(@PathVariable Long id) {
        Optional<Expense> expense = expenseService.getExpenseById(id);
        if (expense.isPresent()) {
            return ResponseEntity.ok(expense.get());
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse("Expense not found with id: " + id));
        }
    }
    
    /**
     * Get all expenses in a group
     * GET /api/expenses/group/{groupId}
     */
    @GetMapping("/group/{groupId}")
    public ResponseEntity<List<ExpenseDTO>> getExpensesByGroup(@PathVariable Long groupId) {
        List<Expense> expenses = expenseService.getExpensesByGroupId(groupId);
        List<ExpenseDTO> expenseDTOs = expenses.stream()
                .map(ExpenseDTO::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(expenseDTOs);
    }
    
    /**
     * Get all expenses paid by a user
     * GET /api/expenses/user/{userId}
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Expense>> getExpensesPaidByUser(@PathVariable Long userId) {
        List<Expense> expenses = expenseService.getExpensesPaidByUser(userId);
        return ResponseEntity.ok(expenses);
    }
    
    /**
     * Get splits for an expense
     * GET /api/expenses/{expenseId}/splits
     */
    @GetMapping("/{expenseId}/splits")
    public ResponseEntity<List<ExpenseSplit>> getExpenseSplits(@PathVariable Long expenseId) {
        List<ExpenseSplit> splits = expenseService.getExpenseSplits(expenseId);
        return ResponseEntity.ok(splits);
    }
    
    /**
     * Get all splits owed by a user
     * GET /api/expenses/splits/user/{userId}
     */
    @GetMapping("/splits/user/{userId}")
    public ResponseEntity<List<ExpenseSplit>> getSplitsOwedByUser(@PathVariable Long userId) {
        List<ExpenseSplit> splits = expenseService.getSplitsOwedByUser(userId);
        return ResponseEntity.ok(splits);
    }
    
    /**
     * Get unsettled splits owed by a user
     * GET /api/expenses/splits/user/{userId}/unsettled
     */
    @GetMapping("/splits/user/{userId}/unsettled")
    public ResponseEntity<List<ExpenseSplit>> getUnsettledSplitsOwedByUser(@PathVariable Long userId) {
        List<ExpenseSplit> splits = expenseService.getUnsettledSplitsOwedByUser(userId);
        return ResponseEntity.ok(splits);
    }
    
    /**
     * Get unsettled splits for a user in a specific group
     * GET /api/expenses/splits/group/{groupId}/user/{userId}/unsettled
     */
    @GetMapping("/splits/group/{groupId}/user/{userId}/unsettled")
    public ResponseEntity<List<ExpenseSplit>> getUnsettledSplitsByGroupAndUser(
            @PathVariable Long groupId, @PathVariable Long userId) {
        List<ExpenseSplit> splits = expenseService.getUnsettledSplitsByGroupAndUser(groupId, userId);
        return ResponseEntity.ok(splits);
    }
    
    /**
     * Mark a split as settled
     * PUT /api/expenses/splits/{splitId}/settle
     */
    @PutMapping("/splits/{splitId}/settle")
    public ResponseEntity<?> settleSplit(@PathVariable Long splitId) {
        try {
            expenseService.settleSplit(splitId);
            return ResponseEntity.ok(new SuccessResponse("Split marked as settled"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Failed to settle split: " + e.getMessage()));
        }
    }
    
    /**
     * Mark a split as unsettled
     * PUT /api/expenses/splits/{splitId}/unsettle
     */
    @PutMapping("/splits/{splitId}/unsettle")
    public ResponseEntity<?> unsettleSplit(@PathVariable Long splitId) {
        try {
            expenseService.unsettleSplit(splitId);
            return ResponseEntity.ok(new SuccessResponse("Split marked as unsettled"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Failed to unsettle split: " + e.getMessage()));
        }
    }
    
    /**
     * Delete an expense
     * DELETE /api/expenses/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteExpense(@PathVariable Long id) {
        try {
            expenseService.deleteExpense(id);
            return ResponseEntity.ok(new SuccessResponse("Expense deleted successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Failed to delete expense: " + e.getMessage()));
        }
    }
    
    /**
     * Get total amount owed by a user in a group
     * GET /api/expenses/group/{groupId}/user/{userId}/owed
     */
    @GetMapping("/group/{groupId}/user/{userId}/owed")
    public ResponseEntity<AmountResponse> getTotalAmountOwedByUserInGroup(
            @PathVariable Long groupId, @PathVariable Long userId) {
        BigDecimal amount = expenseService.getTotalAmountOwedByUserInGroup(groupId, userId);
        return ResponseEntity.ok(new AmountResponse(amount));
    }
    
    /**
     * Get total amount paid by a user in a group
     * GET /api/expenses/group/{groupId}/user/{userId}/paid
     */
    @GetMapping("/group/{groupId}/user/{userId}/paid")
    public ResponseEntity<AmountResponse> getTotalAmountPaidByUserInGroup(
            @PathVariable Long groupId, @PathVariable Long userId) {
        BigDecimal amount = expenseService.getTotalAmountPaidByUserInGroup(groupId, userId);
        return ResponseEntity.ok(new AmountResponse(amount));
    }
    
    /**
     * Get total amount spent in a group
     * GET /api/expenses/group/{groupId}/total
     */
    @GetMapping("/group/{groupId}/total")
    public ResponseEntity<AmountResponse> getTotalAmountByGroup(@PathVariable Long groupId) {
        BigDecimal amount = expenseService.getTotalAmountByGroupId(groupId);
        return ResponseEntity.ok(new AmountResponse(amount));
    }
    
    // Response classes
    public static class ErrorResponse {
        private String error;
        private long timestamp;
        
        public ErrorResponse(String error) {
            this.error = error;
            this.timestamp = System.currentTimeMillis();
        }
        
        public String getError() { return error; }
        public long getTimestamp() { return timestamp; }
    }
    
    public static class SuccessResponse {
        private String message;
        private long timestamp;
        
        public SuccessResponse(String message) {
            this.message = message;
            this.timestamp = System.currentTimeMillis();
        }
        
        public String getMessage() { return message; }
        public long getTimestamp() { return timestamp; }
    }
    
    public static class AmountResponse {
        private BigDecimal amount;
        private long timestamp;
        
        public AmountResponse(BigDecimal amount) {
            this.amount = amount;
            this.timestamp = System.currentTimeMillis();
        }
        
        public BigDecimal getAmount() { return amount; }
        public long getTimestamp() { return timestamp; }
    }
}
