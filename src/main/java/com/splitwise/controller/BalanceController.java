package com.splitwise.controller;

import com.splitwise.dto.BalanceResponse;
import com.splitwise.service.BalanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/balances")
@CrossOrigin(origins = "*") // Allow CORS for frontend
public class BalanceController {
    
    private final BalanceService balanceService;
    
    @Autowired
    public BalanceController(BalanceService balanceService) {
        this.balanceService = balanceService;
    }
    
    /**
     * Get balance calculations for a group
     * GET /api/balances/group/{groupId}
     */
    @GetMapping("/group/{groupId}")
    public ResponseEntity<?> getGroupBalances(@PathVariable Long groupId) {
        try {
            BalanceResponse balances = balanceService.calculateGroupBalances(groupId);
            return ResponseEntity.ok(balances);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Failed to calculate group balances: " + e.getMessage()));
        }
    }
    
    /**
     * Get balance summary for a user across all groups
     * GET /api/balances/user/{userId}/summary
     */
    @GetMapping("/user/{userId}/summary")
    public ResponseEntity<?> getUserBalanceSummary(@PathVariable Long userId) {
        try {
            Map<Long, BigDecimal> balanceSummary = balanceService.getUserBalanceSummary(userId);
            return ResponseEntity.ok(new UserBalanceSummaryResponse(userId, balanceSummary));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Failed to get user balance summary: " + e.getMessage()));
        }
    }
    
    /**
     * Get total amount owed by a user across all groups
     * GET /api/balances/user/{userId}/total-owed
     */
    @GetMapping("/user/{userId}/total-owed")
    public ResponseEntity<?> getTotalAmountOwedByUser(@PathVariable Long userId) {
        try {
            BigDecimal totalOwed = balanceService.getTotalAmountOwedByUser(userId);
            return ResponseEntity.ok(new AmountResponse(totalOwed));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Failed to get total amount owed: " + e.getMessage()));
        }
    }
    
    /**
     * Check if all expenses in a group are settled
     * GET /api/balances/group/{groupId}/is-settled
     */
    @GetMapping("/group/{groupId}/is-settled")
    public ResponseEntity<?> isGroupFullySettled(@PathVariable Long groupId) {
        try {
            boolean isSettled = balanceService.isGroupFullySettled(groupId);
            return ResponseEntity.ok(new SettlementStatusResponse(groupId, isSettled));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Failed to check settlement status: " + e.getMessage()));
        }
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
    
    public static class UserBalanceSummaryResponse {
        private Long userId;
        private Map<Long, BigDecimal> groupBalances;
        private BigDecimal totalNetBalance;
        private long timestamp;
        
        public UserBalanceSummaryResponse(Long userId, Map<Long, BigDecimal> groupBalances) {
            this.userId = userId;
            this.groupBalances = groupBalances;
            this.totalNetBalance = groupBalances.values().stream()
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            this.timestamp = System.currentTimeMillis();
        }
        
        public Long getUserId() { return userId; }
        public Map<Long, BigDecimal> getGroupBalances() { return groupBalances; }
        public BigDecimal getTotalNetBalance() { return totalNetBalance; }
        public long getTimestamp() { return timestamp; }
    }
    
    public static class SettlementStatusResponse {
        private Long groupId;
        private boolean isFullySettled;
        private long timestamp;
        
        public SettlementStatusResponse(Long groupId, boolean isFullySettled) {
            this.groupId = groupId;
            this.isFullySettled = isFullySettled;
            this.timestamp = System.currentTimeMillis();
        }
        
        public Long getGroupId() { return groupId; }
        public boolean isFullySettled() { return isFullySettled; }
        public long getTimestamp() { return timestamp; }
    }
}
