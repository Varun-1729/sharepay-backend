package com.splitwise.repository;

import com.splitwise.entity.Expense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long> {
    
    /**
     * Find all expenses in a specific group
     * @param groupId the group ID
     * @return list of expenses in the group
     */
    List<Expense> findByGroupIdOrderByExpenseDateDesc(Long groupId);

    /**
     * Find all expenses in a specific group with eager loading
     * @param groupId the group ID
     * @return list of expenses in the group with paidBy and group loaded
     */
    @Query("SELECT e FROM Expense e JOIN FETCH e.paidBy JOIN FETCH e.group WHERE e.group.id = :groupId ORDER BY e.expenseDate DESC")
    List<Expense> findByGroupIdWithDetailsOrderByExpenseDateDesc(@Param("groupId") Long groupId);
    
    /**
     * Find all expenses paid by a specific user
     * @param userId the user ID
     * @return list of expenses paid by the user
     */
    List<Expense> findByPaidByIdOrderByExpenseDateDesc(Long userId);
    
    /**
     * Find expenses in a group paid by a specific user
     * @param groupId the group ID
     * @param userId the user ID
     * @return list of expenses
     */
    List<Expense> findByGroupIdAndPaidByIdOrderByExpenseDateDesc(Long groupId, Long userId);
    
    /**
     * Find expenses by description containing the given string (case-insensitive)
     * @param description the description pattern to search for
     * @return list of expenses matching the pattern
     */
    @Query("SELECT e FROM Expense e WHERE LOWER(e.description) LIKE LOWER(CONCAT('%', :description, '%')) ORDER BY e.expenseDate DESC")
    List<Expense> findByDescriptionContainingIgnoreCase(@Param("description") String description);
    
    /**
     * Find expenses in a group within a date range
     * @param groupId the group ID
     * @param startDate the start date
     * @param endDate the end date
     * @return list of expenses within the date range
     */
    @Query("SELECT e FROM Expense e WHERE e.group.id = :groupId AND e.expenseDate BETWEEN :startDate AND :endDate ORDER BY e.expenseDate DESC")
    List<Expense> findByGroupIdAndExpenseDateBetween(@Param("groupId") Long groupId, 
                                                     @Param("startDate") LocalDateTime startDate, 
                                                     @Param("endDate") LocalDateTime endDate);
    
    /**
     * Calculate total amount spent in a group
     * @param groupId the group ID
     * @return total amount spent
     */
    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM Expense e WHERE e.group.id = :groupId")
    BigDecimal getTotalAmountByGroupId(@Param("groupId") Long groupId);
    
    /**
     * Calculate total amount paid by a user in a group
     * @param groupId the group ID
     * @param userId the user ID
     * @return total amount paid by the user
     */
    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM Expense e WHERE e.group.id = :groupId AND e.paidBy.id = :userId")
    BigDecimal getTotalAmountPaidByUserInGroup(@Param("groupId") Long groupId, @Param("userId") Long userId);
    
    /**
     * Count expenses in a group
     * @param groupId the group ID
     * @return number of expenses
     */
    long countByGroupId(Long groupId);
    
    /**
     * Count expenses paid by a user
     * @param userId the user ID
     * @return number of expenses paid by the user
     */
    long countByPaidById(Long userId);
    
    /**
     * Find recent expenses in a group (last N expenses)
     * @param groupId the group ID
     * @param limit the maximum number of expenses to return
     * @return list of recent expenses
     */
    @Query("SELECT e FROM Expense e WHERE e.group.id = :groupId ORDER BY e.expenseDate DESC LIMIT :limit")
    List<Expense> findRecentExpensesByGroupId(@Param("groupId") Long groupId, @Param("limit") int limit);
    
    /**
     * Delete all expenses in a group
     * @param groupId the group ID
     */
    void deleteByGroupId(Long groupId);
}
