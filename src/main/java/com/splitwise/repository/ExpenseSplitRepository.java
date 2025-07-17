package com.splitwise.repository;

import com.splitwise.entity.ExpenseSplit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface ExpenseSplitRepository extends JpaRepository<ExpenseSplit, Long> {
    
    /**
     * Find all splits for a specific expense
     * @param expenseId the expense ID
     * @return list of expense splits
     */
    List<ExpenseSplit> findByExpenseId(Long expenseId);
    
    /**
     * Find all splits owed by a specific user
     * @param userId the user ID
     * @return list of expense splits owed by the user
     */
    List<ExpenseSplit> findByOwedById(Long userId);
    
    /**
     * Find all unsettled splits owed by a specific user
     * @param userId the user ID
     * @return list of unsettled expense splits
     */
    List<ExpenseSplit> findByOwedByIdAndIsSettledFalse(Long userId);
    
    /**
     * Find all settled splits owed by a specific user
     * @param userId the user ID
     * @return list of settled expense splits
     */
    List<ExpenseSplit> findByOwedByIdAndIsSettledTrue(Long userId);
    
    /**
     * Find splits for expenses in a specific group owed by a user
     * @param groupId the group ID
     * @param userId the user ID
     * @return list of expense splits
     */
    @Query("SELECT es FROM ExpenseSplit es WHERE es.expense.group.id = :groupId AND es.owedBy.id = :userId")
    List<ExpenseSplit> findByGroupIdAndOwedById(@Param("groupId") Long groupId, @Param("userId") Long userId);
    
    /**
     * Find unsettled splits for expenses in a specific group owed by a user
     * @param groupId the group ID
     * @param userId the user ID
     * @return list of unsettled expense splits
     */
    @Query("SELECT es FROM ExpenseSplit es WHERE es.expense.group.id = :groupId AND es.owedBy.id = :userId AND es.isSettled = false")
    List<ExpenseSplit> findUnsettledByGroupIdAndOwedById(@Param("groupId") Long groupId, @Param("userId") Long userId);
    
    /**
     * Calculate total amount owed by a user in a group
     * @param groupId the group ID
     * @param userId the user ID
     * @return total amount owed
     */
    @Query("SELECT COALESCE(SUM(es.amount), 0) FROM ExpenseSplit es WHERE es.expense.group.id = :groupId AND es.owedBy.id = :userId AND es.isSettled = false")
    BigDecimal getTotalAmountOwedByUserInGroup(@Param("groupId") Long groupId, @Param("userId") Long userId);
    
    /**
     * Calculate total amount owed by a user across all groups
     * @param userId the user ID
     * @return total amount owed
     */
    @Query("SELECT COALESCE(SUM(es.amount), 0) FROM ExpenseSplit es WHERE es.owedBy.id = :userId AND es.isSettled = false")
    BigDecimal getTotalAmountOwedByUser(@Param("userId") Long userId);
    
    /**
     * Calculate total settled amount by a user in a group
     * @param groupId the group ID
     * @param userId the user ID
     * @return total settled amount
     */
    @Query("SELECT COALESCE(SUM(es.amount), 0) FROM ExpenseSplit es WHERE es.expense.group.id = :groupId AND es.owedBy.id = :userId AND es.isSettled = true")
    BigDecimal getTotalSettledAmountByUserInGroup(@Param("groupId") Long groupId, @Param("userId") Long userId);
    
    /**
     * Count unsettled splits owed by a user
     * @param userId the user ID
     * @return number of unsettled splits
     */
    long countByOwedByIdAndIsSettledFalse(Long userId);
    
    /**
     * Count unsettled splits in a group owed by a user
     * @param groupId the group ID
     * @param userId the user ID
     * @return number of unsettled splits
     */
    @Query("SELECT COUNT(es) FROM ExpenseSplit es WHERE es.expense.group.id = :groupId AND es.owedBy.id = :userId AND es.isSettled = false")
    long countUnsettledByGroupIdAndOwedById(@Param("groupId") Long groupId, @Param("userId") Long userId);
    
    /**
     * Find all splits for expenses in a specific group
     * @param groupId the group ID
     * @return list of expense splits in the group
     */
    @Query("SELECT es FROM ExpenseSplit es WHERE es.expense.group.id = :groupId")
    List<ExpenseSplit> findByGroupId(@Param("groupId") Long groupId);
    
    /**
     * Find all unsettled splits for expenses in a specific group
     * @param groupId the group ID
     * @return list of unsettled expense splits in the group
     */
    @Query("SELECT es FROM ExpenseSplit es WHERE es.expense.group.id = :groupId AND es.isSettled = false")
    List<ExpenseSplit> findUnsettledByGroupId(@Param("groupId") Long groupId);
    
    /**
     * Delete all splits for a specific expense
     * @param expenseId the expense ID
     */
    void deleteByExpenseId(Long expenseId);
    
    /**
     * Delete all splits for expenses in a group
     * @param groupId the group ID
     */
    @Query("DELETE FROM ExpenseSplit es WHERE es.expense.group.id = :groupId")
    void deleteByGroupId(@Param("groupId") Long groupId);

    /**
     * Delete all splits owed by a specific user
     * @param userId the user ID
     */
    void deleteByOwedById(Long userId);
}
