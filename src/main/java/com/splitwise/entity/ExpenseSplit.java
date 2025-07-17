package com.splitwise.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "expense_splits")
public class ExpenseSplit {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "expense_id", nullable = false)
    @NotNull(message = "Expense is required")
    private Expense expense;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owed_by", nullable = false)
    @NotNull(message = "Owed by user is required")
    private User owedBy;
    
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;
    
    @Column(name = "is_settled", nullable = false)
    private boolean isSettled = false;
    
    @Column(name = "settled_at")
    private LocalDateTime settledAt;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Constructors
    public ExpenseSplit() {
    }
    
    public ExpenseSplit(Expense expense, User owedBy, BigDecimal amount) {
        this.expense = expense;
        this.owedBy = owedBy;
        this.amount = amount;
        this.isSettled = false;
    }
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
        if (isSettled && settledAt == null) {
            settledAt = LocalDateTime.now();
        }
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Expense getExpense() {
        return expense;
    }
    
    public void setExpense(Expense expense) {
        this.expense = expense;
    }
    
    public User getOwedBy() {
        return owedBy;
    }
    
    public void setOwedBy(User owedBy) {
        this.owedBy = owedBy;
    }
    
    public BigDecimal getAmount() {
        return amount;
    }
    
    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
    
    public boolean isSettled() {
        return isSettled;
    }
    
    public void setSettled(boolean settled) {
        isSettled = settled;
        if (settled && settledAt == null) {
            settledAt = LocalDateTime.now();
        } else if (!settled) {
            settledAt = null;
        }
    }
    
    public LocalDateTime getSettledAt() {
        return settledAt;
    }
    
    public void setSettledAt(LocalDateTime settledAt) {
        this.settledAt = settledAt;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    // Utility methods
    public void markAsSettled() {
        this.isSettled = true;
        this.settledAt = LocalDateTime.now();
    }
    
    public void markAsUnsettled() {
        this.isSettled = false;
        this.settledAt = null;
    }
    
    @Override
    public String toString() {
        return "ExpenseSplit{" +
                "id=" + id +
                ", expenseId=" + (expense != null ? expense.getId() : null) +
                ", owedBy=" + (owedBy != null ? owedBy.getName() : null) +
                ", amount=" + amount +
                ", isSettled=" + isSettled +
                ", settledAt=" + settledAt +
                '}';
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ExpenseSplit)) return false;
        ExpenseSplit that = (ExpenseSplit) o;
        return id != null && id.equals(that.id);
    }
    
    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
