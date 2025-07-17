package com.splitwise.dto;

import java.math.BigDecimal;
import java.util.List;

public class BalanceResponse {
    
    private Long groupId;
    private String groupName;
    private List<UserBalance> userBalances;
    private List<Settlement> settlements;
    private BigDecimal totalGroupExpenses;
    private long timestamp;
    
    // Constructors
    public BalanceResponse() {
        this.timestamp = System.currentTimeMillis();
    }
    
    public BalanceResponse(Long groupId, String groupName, List<UserBalance> userBalances, 
                          List<Settlement> settlements, BigDecimal totalGroupExpenses) {
        this.groupId = groupId;
        this.groupName = groupName;
        this.userBalances = userBalances;
        this.settlements = settlements;
        this.totalGroupExpenses = totalGroupExpenses;
        this.timestamp = System.currentTimeMillis();
    }
    
    // Getters and Setters
    public Long getGroupId() {
        return groupId;
    }
    
    public void setGroupId(Long groupId) {
        this.groupId = groupId;
    }
    
    public String getGroupName() {
        return groupName;
    }
    
    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }
    
    public List<UserBalance> getUserBalances() {
        return userBalances;
    }
    
    public void setUserBalances(List<UserBalance> userBalances) {
        this.userBalances = userBalances;
    }
    
    public List<Settlement> getSettlements() {
        return settlements;
    }
    
    public void setSettlements(List<Settlement> settlements) {
        this.settlements = settlements;
    }
    
    public BigDecimal getTotalGroupExpenses() {
        return totalGroupExpenses;
    }
    
    public void setTotalGroupExpenses(BigDecimal totalGroupExpenses) {
        this.totalGroupExpenses = totalGroupExpenses;
    }
    
    public long getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
    
    // Inner classes
    public static class UserBalance {
        private Long userId;
        private String userName;
        private BigDecimal totalPaid;
        private BigDecimal totalOwed;
        private BigDecimal netBalance; // Positive means they are owed money, negative means they owe money
        
        public UserBalance() {}
        
        public UserBalance(Long userId, String userName, BigDecimal totalPaid, BigDecimal totalOwed, BigDecimal netBalance) {
            this.userId = userId;
            this.userName = userName;
            this.totalPaid = totalPaid;
            this.totalOwed = totalOwed;
            this.netBalance = netBalance;
        }
        
        // Getters and Setters
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
        
        public String getUserName() { return userName; }
        public void setUserName(String userName) { this.userName = userName; }
        
        public BigDecimal getTotalPaid() { return totalPaid; }
        public void setTotalPaid(BigDecimal totalPaid) { this.totalPaid = totalPaid; }
        
        public BigDecimal getTotalOwed() { return totalOwed; }
        public void setTotalOwed(BigDecimal totalOwed) { this.totalOwed = totalOwed; }
        
        public BigDecimal getNetBalance() { return netBalance; }
        public void setNetBalance(BigDecimal netBalance) { this.netBalance = netBalance; }
        
        @Override
        public String toString() {
            return "UserBalance{" +
                    "userId=" + userId +
                    ", userName='" + userName + '\'' +
                    ", totalPaid=" + totalPaid +
                    ", totalOwed=" + totalOwed +
                    ", netBalance=" + netBalance +
                    '}';
        }
    }
    
    public static class Settlement {
        private Long fromUserId;
        private String fromUserName;
        private Long toUserId;
        private String toUserName;
        private BigDecimal amount;
        
        public Settlement() {}
        
        public Settlement(Long fromUserId, String fromUserName, Long toUserId, String toUserName, BigDecimal amount) {
            this.fromUserId = fromUserId;
            this.fromUserName = fromUserName;
            this.toUserId = toUserId;
            this.toUserName = toUserName;
            this.amount = amount;
        }
        
        // Getters and Setters
        public Long getFromUserId() { return fromUserId; }
        public void setFromUserId(Long fromUserId) { this.fromUserId = fromUserId; }
        
        public String getFromUserName() { return fromUserName; }
        public void setFromUserName(String fromUserName) { this.fromUserName = fromUserName; }
        
        public Long getToUserId() { return toUserId; }
        public void setToUserId(Long toUserId) { this.toUserId = toUserId; }
        
        public String getToUserName() { return toUserName; }
        public void setToUserName(String toUserName) { this.toUserName = toUserName; }
        
        public BigDecimal getAmount() { return amount; }
        public void setAmount(BigDecimal amount) { this.amount = amount; }
        
        @Override
        public String toString() {
            return "Settlement{" +
                    "fromUserId=" + fromUserId +
                    ", fromUserName='" + fromUserName + '\'' +
                    ", toUserId=" + toUserId +
                    ", toUserName='" + toUserName + '\'' +
                    ", amount=" + amount +
                    '}';
        }
    }
    
    @Override
    public String toString() {
        return "BalanceResponse{" +
                "groupId=" + groupId +
                ", groupName='" + groupName + '\'' +
                ", userBalances=" + userBalances +
                ", settlements=" + settlements +
                ", totalGroupExpenses=" + totalGroupExpenses +
                ", timestamp=" + timestamp +
                '}';
    }
}
