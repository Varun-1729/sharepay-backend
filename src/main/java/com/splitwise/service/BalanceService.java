package com.splitwise.service;

import com.splitwise.dto.BalanceResponse;
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
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class BalanceService {
    
    private final ExpenseRepository expenseRepository;
    private final ExpenseSplitRepository expenseSplitRepository;
    private final UserRepository userRepository;
    private final GroupRepository groupRepository;
    private final SecurityUtil securityUtil;

    @Autowired
    public BalanceService(ExpenseRepository expenseRepository,
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
     * Calculate balances for all users in a group
     * @param groupId the group ID
     * @return balance response with user balances and settlements
     * @throws IllegalArgumentException if group not found
     */
    public BalanceResponse calculateGroupBalances(Long groupId) {
        // Validate group exists
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Group not found with id: " + groupId));
        
        // Get all users in the group
        List<User> groupUsers = userRepository.findUsersByGroupId(groupId);
        
        if (groupUsers.isEmpty()) {
            return new BalanceResponse(groupId, group.getName(), new ArrayList<>(), 
                                     new ArrayList<>(), BigDecimal.ZERO);
        }
        
        // Calculate user balances
        List<BalanceResponse.UserBalance> userBalances = new ArrayList<>();
        Map<Long, BigDecimal> netBalances = new HashMap<>();
        
        for (User user : groupUsers) {
            BigDecimal totalPaid = expenseRepository.getTotalAmountPaidByUserInGroup(groupId, user.getId());
            BigDecimal totalOwed = expenseSplitRepository.getTotalAmountOwedByUserInGroup(groupId, user.getId());
            BigDecimal netBalance = totalPaid.subtract(totalOwed);
            
            userBalances.add(new BalanceResponse.UserBalance(
                    user.getId(), user.getName(), totalPaid, totalOwed, netBalance));
            
            netBalances.put(user.getId(), netBalance);
        }
        
        // Calculate settlements (who owes whom)
        List<BalanceResponse.Settlement> settlements = calculateOptimalSettlements(groupUsers, netBalances);
        
        // Get total group expenses
        BigDecimal totalGroupExpenses = expenseRepository.getTotalAmountByGroupId(groupId);
        
        return new BalanceResponse(groupId, group.getName(), userBalances, settlements, totalGroupExpenses);
    }
    
    /**
     * Calculate optimal settlements to minimize the number of transactions
     * This uses a greedy algorithm to settle debts efficiently
     */
    private List<BalanceResponse.Settlement> calculateOptimalSettlements(List<User> users, Map<Long, BigDecimal> netBalances) {
        List<BalanceResponse.Settlement> settlements = new ArrayList<>();
        
        // Create lists of creditors (positive balance) and debtors (negative balance)
        List<UserBalance> creditors = new ArrayList<>();
        List<UserBalance> debtors = new ArrayList<>();
        
        for (User user : users) {
            BigDecimal balance = netBalances.get(user.getId());
            if (balance.compareTo(BigDecimal.ZERO) > 0) {
                creditors.add(new UserBalance(user, balance));
            } else if (balance.compareTo(BigDecimal.ZERO) < 0) {
                debtors.add(new UserBalance(user, balance.abs())); // Store as positive amount
            }
        }
        
        // Sort creditors by amount descending, debtors by amount descending
        creditors.sort((a, b) -> b.amount.compareTo(a.amount));
        debtors.sort((a, b) -> b.amount.compareTo(a.amount));
        
        // Use two pointers to settle debts
        int creditorIndex = 0;
        int debtorIndex = 0;
        
        while (creditorIndex < creditors.size() && debtorIndex < debtors.size()) {
            UserBalance creditor = creditors.get(creditorIndex);
            UserBalance debtor = debtors.get(debtorIndex);
            
            // Calculate settlement amount (minimum of what creditor is owed and what debtor owes)
            BigDecimal settlementAmount = creditor.amount.min(debtor.amount);
            
            // Only create settlement if amount is significant (> 0.01)
            if (settlementAmount.compareTo(new BigDecimal("0.01")) > 0) {
                settlements.add(new BalanceResponse.Settlement(
                        debtor.user.getId(), debtor.user.getName(),
                        creditor.user.getId(), creditor.user.getName(),
                        settlementAmount.setScale(2, RoundingMode.HALF_UP)
                ));
            }
            
            // Update balances
            creditor.amount = creditor.amount.subtract(settlementAmount);
            debtor.amount = debtor.amount.subtract(settlementAmount);
            
            // Move to next creditor or debtor if current one is settled
            if (creditor.amount.compareTo(BigDecimal.ZERO) == 0) {
                creditorIndex++;
            }
            if (debtor.amount.compareTo(BigDecimal.ZERO) == 0) {
                debtorIndex++;
            }
        }
        
        return settlements;
    }
    
    /**
     * Get balance summary for a specific user across all groups
     * @param userId the user ID
     * @return map of group ID to user's net balance in that group
     */
    public Map<Long, BigDecimal> getUserBalanceSummary(Long userId) {
        // Validate user exists
        if (!userRepository.existsById(userId)) {
            throw new IllegalArgumentException("User not found with id: " + userId);
        }
        
        // Get all groups the user is a member of
        List<Group> userGroups = groupRepository.findGroupsByUserId(userId);
        
        Map<Long, BigDecimal> balanceSummary = new HashMap<>();
        
        for (Group group : userGroups) {
            BigDecimal totalPaid = expenseRepository.getTotalAmountPaidByUserInGroup(group.getId(), userId);
            BigDecimal totalOwed = expenseSplitRepository.getTotalAmountOwedByUserInGroup(group.getId(), userId);
            BigDecimal netBalance = totalPaid.subtract(totalOwed);
            
            balanceSummary.put(group.getId(), netBalance);
        }
        
        return balanceSummary;
    }
    
    /**
     * Get total amount a user owes across all groups
     * @param userId the user ID
     * @return total amount owed
     */
    public BigDecimal getTotalAmountOwedByUser(Long userId) {
        return expenseSplitRepository.getTotalAmountOwedByUser(userId);
    }
    
    /**
     * Check if all expenses in a group are settled
     * @param groupId the group ID
     * @return true if all expenses are settled, false otherwise
     */
    public boolean isGroupFullySettled(Long groupId) {
        List<BalanceResponse.Settlement> settlements = calculateGroupBalances(groupId).getSettlements();
        return settlements.isEmpty();
    }
    
    // Helper class for internal calculations
    private static class UserBalance {
        User user;
        BigDecimal amount;
        
        UserBalance(User user, BigDecimal amount) {
            this.user = user;
            this.amount = amount;
        }
    }
}
