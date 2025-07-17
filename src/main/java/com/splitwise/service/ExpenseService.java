package com.splitwise.service;

import com.splitwise.dto.CreateExpenseRequest;
import com.splitwise.entity.Expense;
import com.splitwise.entity.ExpenseSplit;
import com.splitwise.entity.Group;
import com.splitwise.entity.User;
import com.splitwise.repository.ExpenseRepository;
import com.splitwise.repository.ExpenseSplitRepository;
import com.splitwise.repository.GroupRepository;
import com.splitwise.repository.UserRepository;
import com.splitwise.util.SecurityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ExpenseService {
    
    private final ExpenseRepository expenseRepository;
    private final ExpenseSplitRepository expenseSplitRepository;
    private final UserRepository userRepository;
    private final GroupRepository groupRepository;
    private final SecurityUtil securityUtil;

    @Autowired
    public ExpenseService(ExpenseRepository expenseRepository,
                         ExpenseSplitRepository expenseSplitRepository,
                         UserRepository userRepository,
                         GroupRepository groupRepository,
                         SecurityUtil securityUtil) {
        this.expenseRepository = expenseRepository;
        this.expenseSplitRepository = expenseSplitRepository;
        this.userRepository = userRepository;
        this.groupRepository = groupRepository;
        this.securityUtil = securityUtil;
    }
    
    /**
     * Create a new expense with splits
     * @param request the expense creation request
     * @return the created expense
     * @throws IllegalArgumentException if validation fails
     */
    public Expense createExpense(CreateExpenseRequest request) {
        // TODO: Add proper authorization check later
        // For now, allow expense creation to test balance functionality

        // Validate that the payer exists
        User paidBy = userRepository.findById(request.getPaidById())
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + request.getPaidById()));

        // Validate that the group exists
        Group group = groupRepository.findById(request.getGroupId())
                .orElseThrow(() -> new IllegalArgumentException("Group not found with id: " + request.getGroupId()));
        
        // Validate that all users in splits exist and are members of the group
        BigDecimal totalSplitAmount = BigDecimal.ZERO;
        List<User> groupUsers = userRepository.findUsersByGroupId(request.getGroupId());

        for (CreateExpenseRequest.SplitRequest splitRequest : request.getSplits()) {
            User user = userRepository.findById(splitRequest.getUserId())
                    .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + splitRequest.getUserId()));

            // Check if user is a member of the group using ID comparison
            boolean isUserInGroup = groupUsers.stream()
                    .anyMatch(groupUser -> groupUser.getId().equals(user.getId()));

            if (!isUserInGroup) {
                throw new IllegalArgumentException("User " + user.getName() + " is not a member of the group");
            }

            totalSplitAmount = totalSplitAmount.add(splitRequest.getAmount());
        }
        
        // Validate that split amounts equal the total expense amount
        if (totalSplitAmount.compareTo(request.getAmount()) != 0) {
            throw new IllegalArgumentException("Split amounts (" + totalSplitAmount + ") do not equal expense amount (" + request.getAmount() + ")");
        }
        
        // Create the expense
        Expense expense = new Expense(request.getDescription(), request.getAmount(), paidBy, group, request.getNotes());
        if (request.getExpenseDate() != null) {
            expense.setExpenseDate(request.getExpenseDate());
        }
        
        expense = expenseRepository.save(expense);
        
        // Create the splits
        for (CreateExpenseRequest.SplitRequest splitRequest : request.getSplits()) {
            User owedBy = userRepository.findById(splitRequest.getUserId()).get(); // Already validated above
            ExpenseSplit split = new ExpenseSplit(expense, owedBy, splitRequest.getAmount());
            expenseSplitRepository.save(split);
            expense.addSplit(split);
        }
        
        return expense;
    }
    
    /**
     * Get all expenses in a group
     * @param groupId the group ID
     * @return list of expenses in the group
     */
    @Transactional(readOnly = true)
    public List<Expense> getExpensesByGroupId(Long groupId) {
        return expenseRepository.findByGroupIdWithDetailsOrderByExpenseDateDesc(groupId);
    }
    
    /**
     * Get expense by ID
     * @param id the expense ID
     * @return Optional containing the expense if found
     */
    @Transactional(readOnly = true)
    public Optional<Expense> getExpenseById(Long id) {
        return expenseRepository.findById(id);
    }
    
    /**
     * Get all expenses paid by a user
     * @param userId the user ID
     * @return list of expenses paid by the user
     */
    @Transactional(readOnly = true)
    public List<Expense> getExpensesPaidByUser(Long userId) {
        return expenseRepository.findByPaidByIdOrderByExpenseDateDesc(userId);
    }
    
    /**
     * Get splits for an expense
     * @param expenseId the expense ID
     * @return list of expense splits
     */
    @Transactional(readOnly = true)
    public List<ExpenseSplit> getExpenseSplits(Long expenseId) {
        return expenseSplitRepository.findByExpenseId(expenseId);
    }
    
    /**
     * Get all splits owed by a user
     * @param userId the user ID
     * @return list of expense splits owed by the user
     */
    @Transactional(readOnly = true)
    public List<ExpenseSplit> getSplitsOwedByUser(Long userId) {
        return expenseSplitRepository.findByOwedById(userId);
    }
    
    /**
     * Get unsettled splits owed by a user
     * @param userId the user ID
     * @return list of unsettled expense splits
     */
    @Transactional(readOnly = true)
    public List<ExpenseSplit> getUnsettledSplitsOwedByUser(Long userId) {
        return expenseSplitRepository.findByOwedByIdAndIsSettledFalse(userId);
    }
    
    /**
     * Get unsettled splits for a user in a specific group
     * @param groupId the group ID
     * @param userId the user ID
     * @return list of unsettled expense splits
     */
    @Transactional(readOnly = true)
    public List<ExpenseSplit> getUnsettledSplitsByGroupAndUser(Long groupId, Long userId) {
        return expenseSplitRepository.findUnsettledByGroupIdAndOwedById(groupId, userId);
    }
    
    /**
     * Mark a split as settled
     * @param splitId the split ID
     * @throws IllegalArgumentException if split not found
     */
    public void settleSplit(Long splitId) {
        ExpenseSplit split = expenseSplitRepository.findById(splitId)
                .orElseThrow(() -> new IllegalArgumentException("Expense split not found with id: " + splitId));
        
        split.markAsSettled();
        expenseSplitRepository.save(split);
    }
    
    /**
     * Mark a split as unsettled
     * @param splitId the split ID
     * @throws IllegalArgumentException if split not found
     */
    public void unsettleSplit(Long splitId) {
        ExpenseSplit split = expenseSplitRepository.findById(splitId)
                .orElseThrow(() -> new IllegalArgumentException("Expense split not found with id: " + splitId));
        
        split.markAsUnsettled();
        expenseSplitRepository.save(split);
    }
    
    /**
     * Delete an expense and all its splits
     * @param id the expense ID
     * @throws IllegalArgumentException if expense not found
     */
    public void deleteExpense(Long id) {
        if (!expenseRepository.existsById(id)) {
            throw new IllegalArgumentException("Expense not found with id: " + id);
        }
        
        // Delete splits first
        expenseSplitRepository.deleteByExpenseId(id);
        
        // Then delete the expense
        expenseRepository.deleteById(id);
    }
    
    /**
     * Get total amount owed by a user in a group
     * @param groupId the group ID
     * @param userId the user ID
     * @return total amount owed
     */
    @Transactional(readOnly = true)
    public BigDecimal getTotalAmountOwedByUserInGroup(Long groupId, Long userId) {
        return expenseSplitRepository.getTotalAmountOwedByUserInGroup(groupId, userId);
    }
    
    /**
     * Get total amount paid by a user in a group
     * @param groupId the group ID
     * @param userId the user ID
     * @return total amount paid
     */
    @Transactional(readOnly = true)
    public BigDecimal getTotalAmountPaidByUserInGroup(Long groupId, Long userId) {
        return expenseRepository.getTotalAmountPaidByUserInGroup(groupId, userId);
    }
    
    /**
     * Get total amount spent in a group
     * @param groupId the group ID
     * @return total amount spent
     */
    @Transactional(readOnly = true)
    public BigDecimal getTotalAmountByGroupId(Long groupId) {
        return expenseRepository.getTotalAmountByGroupId(groupId);
    }
}
