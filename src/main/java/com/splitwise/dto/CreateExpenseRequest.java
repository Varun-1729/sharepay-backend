package com.splitwise.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class CreateExpenseRequest {
    
    @NotBlank(message = "Description is required")
    @Size(min = 2, max = 200, message = "Description must be between 2 and 200 characters")
    private String description;
    
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    private BigDecimal amount;
    
    @NotNull(message = "Paid by user ID is required")
    private Long paidById;
    
    @NotNull(message = "Group ID is required")
    private Long groupId;
    
    @Size(max = 500, message = "Notes cannot exceed 500 characters")
    private String notes;
    
    private LocalDateTime expenseDate;
    
    @NotEmpty(message = "At least one split is required")
    @Valid
    private List<SplitRequest> splits;
    
    // Constructors
    public CreateExpenseRequest() {
    }
    
    public CreateExpenseRequest(String description, BigDecimal amount, Long paidById, Long groupId, List<SplitRequest> splits) {
        this.description = description;
        this.amount = amount;
        this.paidById = paidById;
        this.groupId = groupId;
        this.splits = splits;
    }
    
    // Getters and Setters
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public BigDecimal getAmount() {
        return amount;
    }
    
    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
    
    public Long getPaidById() {
        return paidById;
    }
    
    public void setPaidById(Long paidById) {
        this.paidById = paidById;
    }
    
    public Long getGroupId() {
        return groupId;
    }
    
    public void setGroupId(Long groupId) {
        this.groupId = groupId;
    }
    
    public String getNotes() {
        return notes;
    }
    
    public void setNotes(String notes) {
        this.notes = notes;
    }
    
    public LocalDateTime getExpenseDate() {
        return expenseDate;
    }
    
    public void setExpenseDate(LocalDateTime expenseDate) {
        this.expenseDate = expenseDate;
    }
    
    public List<SplitRequest> getSplits() {
        return splits;
    }
    
    public void setSplits(List<SplitRequest> splits) {
        this.splits = splits;
    }
    
    // Inner class for split requests
    public static class SplitRequest {
        
        @NotNull(message = "User ID is required for split")
        private Long userId;
        
        @NotNull(message = "Amount is required for split")
        @DecimalMin(value = "0.01", message = "Split amount must be greater than 0")
        private BigDecimal amount;
        
        // Constructors
        public SplitRequest() {
        }
        
        public SplitRequest(Long userId, BigDecimal amount) {
            this.userId = userId;
            this.amount = amount;
        }
        
        // Getters and Setters
        public Long getUserId() {
            return userId;
        }
        
        public void setUserId(Long userId) {
            this.userId = userId;
        }
        
        public BigDecimal getAmount() {
            return amount;
        }
        
        public void setAmount(BigDecimal amount) {
            this.amount = amount;
        }
        
        @Override
        public String toString() {
            return "SplitRequest{" +
                    "userId=" + userId +
                    ", amount=" + amount +
                    '}';
        }
    }
    
    @Override
    public String toString() {
        return "CreateExpenseRequest{" +
                "description='" + description + '\'' +
                ", amount=" + amount +
                ", paidById=" + paidById +
                ", groupId=" + groupId +
                ", notes='" + notes + '\'' +
                ", expenseDate=" + expenseDate +
                ", splits=" + splits +
                '}';
    }
}
