// API Base URL
const API_BASE_URL = 'http://localhost:8080/api';

// Authentication credentials
const AUTH_USERNAME = 'splitwise';
const AUTH_PASSWORD = 'secure123';

// Create authentication header
function getAuthHeaders() {
    const credentials = btoa(`${AUTH_USERNAME}:${AUTH_PASSWORD}`);
    return {
        'Authorization': `Basic ${credentials}`,
        'Content-Type': 'application/json'
    };
}

// Global variables
let users = [];
let groups = [];
let selectedGroupId = null;
let currentUser = null;

// Initialize the application
document.addEventListener('DOMContentLoaded', function() {
    checkAuthentication();
});

async function checkAuthentication() {
    try {
        // Check if user is logged in
        const response = await fetch(`${API_BASE_URL}/auth/current`, {
            credentials: 'include'
        });

        if (response.ok) {
            const result = await response.json();
            if (result.success) {
                currentUser = result.user;
                localStorage.setItem('currentUser', JSON.stringify(currentUser));
                initializeApp();
            } else {
                redirectToLogin();
            }
        } else {
            redirectToLogin();
        }
    } catch (error) {
        console.error('Auth check failed:', error);
        redirectToLogin();
    }
}

function redirectToLogin() {
    window.location.href = 'login.html';
}

function initializeApp() {
    loadUsers();
    loadGroups();
    setupEventListeners();
    updateUserInfo();
}

function updateUserInfo() {
    // Add user info to header
    const header = document.querySelector('h1');
    if (header && currentUser) {
        header.innerHTML = `🧾 Splitwise Clone - Welcome, ${currentUser.name}! <button onclick="logout()" style="margin-left: 20px; padding: 5px 10px; background: #e74c3c; color: white; border: none; border-radius: 3px; cursor: pointer;">Logout</button>`;
    }
}

// Setup event listeners
function setupEventListeners() {
    document.getElementById('userForm').addEventListener('submit', handleUserSubmit);
    document.getElementById('groupForm').addEventListener('submit', handleGroupSubmit);
    document.getElementById('expenseForm').addEventListener('submit', handleExpenseSubmit);
}

// Tab functionality
function showTab(tabName) {
    // Hide all tab contents
    document.querySelectorAll('.tab-content').forEach(tab => {
        tab.classList.remove('active');
    });
    
    // Remove active class from all tab buttons
    document.querySelectorAll('.tab-button').forEach(button => {
        button.classList.remove('active');
    });
    
    // Show selected tab and mark button as active
    document.getElementById(tabName).classList.add('active');
    event.target.classList.add('active');
    
    // Load data for specific tabs
    if (tabName === 'expenses') {
        loadGroupsForExpenses();
    } else if (tabName === 'balances') {
        loadGroupsForBalances();
    }
}

// Utility functions
function showLoading() {
    document.getElementById('loading').style.display = 'flex';
}

function hideLoading() {
    document.getElementById('loading').style.display = 'none';
}

function showToast(message, type = 'success') {
    const toast = document.getElementById('toast');
    toast.textContent = message;
    toast.className = `toast ${type} show`;
    
    setTimeout(() => {
        toast.classList.remove('show');
    }, 3000);
}

// API helper function
async function apiCall(endpoint, options = {}) {
    showLoading();
    try {
        const response = await fetch(`${API_BASE_URL}${endpoint}`, {
            headers: {
                'Content-Type': 'application/json',
                ...options.headers
            },
            credentials: 'include', // Include session cookies
            ...options
        });

        if (!response.ok) {
            throw new Error('API request failed');
        }

        // Try to parse as JSON, fallback to text if it fails
        let data;
        try {
            data = await response.json();
        } catch (jsonError) {
            data = await response.text();
        }

        return data;
    } catch (error) {
        showToast(error.message, 'error');
        throw error;
    } finally {
        hideLoading();
    }
}

// User management
async function loadUsers() {
    try {
        users = await apiCall('/users');
        displayUsers();
        updateUserSelects();
    } catch (error) {
        console.error('Failed to load users:', error);
    }
}

function displayUsers() {
    const usersList = document.getElementById('usersList');
    usersList.innerHTML = '';
    
    users.forEach(user => {
        const userItem = document.createElement('div');
        userItem.className = 'list-item';
        userItem.innerHTML = `
            <div>
                <h4>${user.name}</h4>
                <p>${user.email}${user.phone ? ' • ' + user.phone : ''}</p>
            </div>
            <div class="actions">
                <button class="btn-danger" onclick="deleteUser(${user.id})">Delete</button>
            </div>
        `;
        usersList.appendChild(userItem);
    });
}

function updateUserSelects() {
    const selects = ['userSelect', 'expensePaidBy'];
    selects.forEach(selectId => {
        const select = document.getElementById(selectId);
        if (select) {
            select.innerHTML = '<option value="">Select a user</option>';
            users.forEach(user => {
                select.innerHTML += `<option value="${user.id}">${user.name}</option>`;
            });
        }
    });
}

async function handleUserSubmit(event) {
    event.preventDefault();
    
    const userData = {
        name: document.getElementById('userName').value,
        email: document.getElementById('userEmail').value,
        phone: document.getElementById('userPhone').value || null
    };
    
    try {
        await apiCall('/users', {
            method: 'POST',
            body: JSON.stringify(userData)
        });
        
        showToast('User added successfully!');
        document.getElementById('userForm').reset();
        loadUsers();
    } catch (error) {
        console.error('Failed to add user:', error);
        showToast('Failed to add user. Please try again.', 'error');
    }
}

async function deleteUser(userId) {
    if (!confirm('Are you sure you want to delete this user?')) return;
    
    try {
        await apiCall(`/users/${userId}`, { method: 'DELETE' });
        showToast('User deleted successfully!');
        loadUsers();
    } catch (error) {
        console.error('Failed to delete user:', error);
        showToast('Failed to delete user. Please try again.', 'error');
    }
}

// Group management
async function loadGroups() {
    try {
        groups = await apiCall('/groups');
        displayGroups();
        updateGroupSelects();
    } catch (error) {
        console.error('Failed to load groups:', error);
    }
}

function displayGroups() {
    const groupsList = document.getElementById('groupsList');
    groupsList.innerHTML = '';
    
    groups.forEach(group => {
        const groupItem = document.createElement('div');
        groupItem.className = 'list-item';
        groupItem.innerHTML = `
            <div>
                <h4>${group.name}</h4>
                <p>${group.description || 'No description'}</p>
            </div>
            <div class="actions">
                <button class="btn-primary" onclick="selectGroup(${group.id})">Manage</button>
                <button class="btn-danger" onclick="deleteGroup(${group.id})">Delete</button>
            </div>
        `;
        groupsList.appendChild(groupItem);
    });
}

function updateGroupSelects() {
    const selects = ['expenseGroup', 'expenseGroupFilter', 'balanceGroupSelect'];
    selects.forEach(selectId => {
        const select = document.getElementById(selectId);
        if (select) {
            const firstOption = select.querySelector('option').textContent;
            select.innerHTML = `<option value="">${firstOption}</option>`;
            groups.forEach(group => {
                select.innerHTML += `<option value="${group.id}">${group.name}</option>`;
            });
        }
    });
}

async function handleGroupSubmit(event) {
    event.preventDefault();
    
    const groupData = {
        name: document.getElementById('groupName').value,
        description: document.getElementById('groupDescription').value || null
    };
    
    try {
        await apiCall('/groups', {
            method: 'POST',
            body: JSON.stringify(groupData)
        });
        
        showToast('Group created successfully!');
        document.getElementById('groupForm').reset();
        loadGroups();
    } catch (error) {
        console.error('Failed to create group:', error);
        showToast('Failed to create group. Please try again.', 'error');
    }
}

async function selectGroup(groupId) {
    selectedGroupId = groupId;
    await loadGroupMembers(groupId);
    document.getElementById('groupMembersSection').style.display = 'block';
}

async function loadGroupMembers(groupId) {
    try {
        const members = await apiCall(`/groups/${groupId}/users`);
        displayGroupMembers(members);
    } catch (error) {
        console.error('Failed to load group members:', error);
    }
}

function displayGroupMembers(members) {
    const membersList = document.getElementById('groupMembersList');
    membersList.innerHTML = '';
    
    if (members.length === 0) {
        membersList.innerHTML = '<p>No members in this group yet.</p>';
        return;
    }
    
    members.forEach(member => {
        const memberItem = document.createElement('div');
        memberItem.className = 'list-item';
        memberItem.innerHTML = `
            <div>
                <h4>${member.name}</h4>
                <p>${member.email}</p>
            </div>
            <div class="actions">
                <button class="btn-danger" onclick="removeUserFromGroup(${selectedGroupId}, ${member.id})">Remove</button>
            </div>
        `;
        membersList.appendChild(memberItem);
    });
}

async function addUserToGroup() {
    const userId = document.getElementById('userSelect').value;
    if (!userId || !selectedGroupId) {
        showToast('Please select a user', 'error');
        return;
    }

    try {
        await apiCall(`/groups/${selectedGroupId}/users/${userId}`, { method: 'POST' });
        showToast('User added to group successfully!', 'success');
        loadGroupMembers(selectedGroupId);
        document.getElementById('userSelect').value = '';
    } catch (error) {
        console.error('Failed to add user to group:', error);
        showToast('Failed to add user to group. User might already be in the group.', 'error');
    }
}

async function removeUserFromGroup(groupId, userId) {
    if (!confirm('Are you sure you want to remove this user from the group?')) return;
    
    try {
        await apiCall(`/groups/${groupId}/users/${userId}`, { method: 'DELETE' });
        showToast('User removed from group successfully!');
        loadGroupMembers(groupId);
    } catch (error) {
        console.error('Failed to remove user from group:', error);
        showToast('Failed to remove user from group. Please try again.', 'error');
    }
}

async function deleteGroup(groupId) {
    if (!confirm('Are you sure you want to delete this group?')) return;

    try {
        await apiCall(`/groups/${groupId}`, { method: 'DELETE' });
        showToast('Group deleted successfully!');
        loadGroups();
        if (selectedGroupId === groupId) {
            document.getElementById('groupMembersSection').style.display = 'none';
            selectedGroupId = null;
        }
    } catch (error) {
        console.error('Failed to delete group:', error);
        showToast('Failed to delete group. Please try again.', 'error');
    }
}

// Expense management
function loadGroupsForExpenses() {
    updateGroupSelects();
    document.getElementById('expenseGroup').addEventListener('change', loadGroupMembersForExpense);
}

async function loadGroupMembersForExpense() {
    const groupId = document.getElementById('expenseGroup').value;
    if (!groupId) {
        document.getElementById('splitDetails').innerHTML = '';
        return;
    }

    try {
        const members = await apiCall(`/groups/${groupId}/users`);
        generateSplitInputs(members);

        // Update paid by select
        const paidBySelect = document.getElementById('expensePaidBy');
        paidBySelect.innerHTML = '<option value="">Who Paid?</option>';
        members.forEach(member => {
            paidBySelect.innerHTML += `<option value="${member.id}">${member.name}</option>`;
        });
    } catch (error) {
        console.error('Failed to load group members for expense:', error);
    }
}

function generateSplitInputs(members) {
    const splitDetails = document.getElementById('splitDetails');
    splitDetails.innerHTML = '';

    members.forEach(member => {
        const splitItem = document.createElement('div');
        splitItem.className = 'split-item';
        splitItem.innerHTML = `
            <label>${member.name}:</label>
            <input type="number" id="split_${member.id}" step="0.01" min="0" placeholder="Amount" data-user-id="${member.id}">
        `;
        splitDetails.appendChild(splitItem);
    });
}

function generateEqualSplit() {
    const amount = parseFloat(document.getElementById('expenseAmount').value);
    const splitInputs = document.querySelectorAll('#splitDetails input[type="number"]');

    if (!amount || splitInputs.length === 0) {
        showToast('Please enter an amount and select a group first', 'error');
        return;
    }

    const equalAmount = (amount / splitInputs.length).toFixed(2);
    splitInputs.forEach(input => {
        input.value = equalAmount;
    });
}

async function handleExpenseSubmit(event) {
    event.preventDefault();

    const groupId = document.getElementById('expenseGroup').value;
    const amount = parseFloat(document.getElementById('expenseAmount').value);
    const splitInputs = document.querySelectorAll('#splitDetails input[type="number"]');

    // Collect splits
    const splits = [];
    let totalSplit = 0;

    splitInputs.forEach(input => {
        const splitAmount = parseFloat(input.value) || 0;
        if (splitAmount > 0) {
            splits.push({
                userId: parseInt(input.dataset.userId),
                amount: splitAmount
            });
            totalSplit += splitAmount;
        }
    });

    // Validate splits
    if (Math.abs(totalSplit - amount) > 0.01) {
        showToast(`Split amounts (${totalSplit.toFixed(2)}) must equal expense amount (${amount.toFixed(2)})`, 'error');
        return;
    }

    if (splits.length === 0) {
        showToast('Please add at least one split', 'error');
        return;
    }

    const expenseData = {
        description: document.getElementById('expenseDescription').value,
        amount: amount,
        paidById: parseInt(document.getElementById('expensePaidBy').value),
        groupId: parseInt(groupId),
        notes: document.getElementById('expenseNotes').value || null,
        splits: splits
    };

    try {
        await apiCall('/expenses', {
            method: 'POST',
            body: JSON.stringify(expenseData)
        });

        showToast('Expense added successfully!');
        document.getElementById('expenseForm').reset();
        document.getElementById('splitDetails').innerHTML = '';

        // Reload expenses if viewing the same group
        const filterGroupId = document.getElementById('expenseGroupFilter').value;
        if (filterGroupId === groupId) {
            loadExpenses();
        }
    } catch (error) {
        console.error('Failed to add expense:', error);
        showToast('Failed to add expense. Please try again.', 'error');
    }
}

async function loadExpenses() {
    const groupId = document.getElementById('expenseGroupFilter').value;
    if (!groupId) {
        document.getElementById('expensesList').innerHTML = '';
        return;
    }

    try {
        const expenses = await apiCall(`/expenses/group/${groupId}`);
        displayExpenses(expenses);
    } catch (error) {
        console.error('Failed to load expenses:', error);
    }
}

function displayExpenses(expenses) {
    const expensesList = document.getElementById('expensesList');
    expensesList.innerHTML = '';

    if (expenses.length === 0) {
        expensesList.innerHTML = '<p>No expenses found for this group.</p>';
        return;
    }

    expenses.forEach(expense => {
        const expenseItem = document.createElement('div');
        expenseItem.className = 'list-item';
        expenseItem.innerHTML = `
            <div>
                <h4>${expense.description}</h4>
                <p>₹${expense.amount} • Paid by ${expense.paidBy.name}</p>
                <p>${new Date(expense.expenseDate).toLocaleDateString()}${expense.notes ? ' • ' + expense.notes : ''}</p>
            </div>
            <div class="actions">
                <button class="btn-primary" onclick="viewExpenseSplits(${expense.id})">View Splits</button>
                <button class="btn-danger" onclick="deleteExpense(${expense.id})">Delete</button>
            </div>
        `;
        expensesList.appendChild(expenseItem);
    });
}

async function viewExpenseSplits(expenseId) {
    try {
        const splits = await apiCall(`/expenses/${expenseId}/splits`);
        let splitsText = 'Expense Splits:\n\n';
        splits.forEach(split => {
            splitsText += `${split.owedBy.name}: ₹${split.amount} ${split.settled ? '(Settled)' : '(Pending)'}\n`;
        });
        alert(splitsText);
    } catch (error) {
        console.error('Failed to load expense splits:', error);
    }
}

async function deleteExpense(expenseId) {
    if (!confirm('Are you sure you want to delete this expense?')) return;

    try {
        await apiCall(`/expenses/${expenseId}`, { method: 'DELETE' });
        showToast('Expense deleted successfully!');
        loadExpenses();
    } catch (error) {
        console.error('Failed to delete expense:', error);
    }
}

// Balance management
function loadGroupsForBalances() {
    updateGroupSelects();
}

async function loadGroupBalances() {
    const groupId = document.getElementById('balanceGroupSelect').value;
    if (!groupId) {
        document.getElementById('balanceResults').innerHTML = '';
        return;
    }

    try {
        const balanceData = await apiCall(`/balances/group/${groupId}`);
        displayGroupBalances(balanceData);
    } catch (error) {
        console.error('Failed to load group balances:', error);
    }
}

function displayGroupBalances(balanceData) {
    const balanceResults = document.getElementById('balanceResults');
    balanceResults.innerHTML = '';

    if (!balanceData.userBalances || balanceData.userBalances.length === 0) {
        balanceResults.innerHTML = '<p>No balance data available for this group.</p>';
        return;
    }

    // Group summary
    const summaryDiv = document.createElement('div');
    summaryDiv.innerHTML = `
        <h3>Group: ${balanceData.groupName}</h3>
        <p><strong>Total Group Expenses: ₹${balanceData.totalGroupExpenses}</strong></p>
    `;
    balanceResults.appendChild(summaryDiv);

    // User balances
    const balancesDiv = document.createElement('div');
    balancesDiv.innerHTML = '<h3>Individual Balances</h3>';

    const balanceSummary = document.createElement('div');
    balanceSummary.className = 'balance-summary';

    balanceData.userBalances.forEach(userBalance => {
        const balanceCard = document.createElement('div');
        balanceCard.className = 'balance-card';

        const netBalanceClass = userBalance.netBalance >= 0 ? 'balance-positive' : 'balance-negative';
        const netBalanceText = userBalance.netBalance >= 0 ?
            `Gets back ₹${userBalance.netBalance}` :
            `Owes ₹${Math.abs(userBalance.netBalance)}`;

        balanceCard.innerHTML = `
            <h4>${userBalance.userName}</h4>
            <p>Total Paid: ₹${userBalance.totalPaid}</p>
            <p>Total Share: ₹${userBalance.totalOwed}</p>
            <p class="${netBalanceClass}"><strong>${netBalanceText}</strong></p>
        `;

        balanceSummary.appendChild(balanceCard);
    });

    balancesDiv.appendChild(balanceSummary);
    balanceResults.appendChild(balancesDiv);

    // Settlements
    if (balanceData.settlements && balanceData.settlements.length > 0) {
        const settlementsDiv = document.createElement('div');
        settlementsDiv.innerHTML = '<h3>Suggested Settlements</h3>';

        balanceData.settlements.forEach(settlement => {
            const settlementItem = document.createElement('div');
            settlementItem.className = 'settlement-item';
            settlementItem.innerHTML = `
                <p><strong>${settlement.fromUserName}</strong> should pay
                <span class="settlement-amount">₹${settlement.amount}</span>
                to <strong>${settlement.toUserName}</strong></p>
            `;
            settlementsDiv.appendChild(settlementItem);
        });

        balanceResults.appendChild(settlementsDiv);
    } else {
        const settledDiv = document.createElement('div');
        settledDiv.innerHTML = '<h3 style="color: #38a169;">✅ All expenses are settled!</h3>';
        balanceResults.appendChild(settledDiv);
    }
}

// Logout function
async function logout() {
    try {
        await fetch(`${API_BASE_URL}/auth/logout`, {
            method: 'POST',
            credentials: 'include'
        });

        localStorage.removeItem('currentUser');
        window.location.href = 'login.html';
    } catch (error) {
        console.error('Logout error:', error);
        // Force redirect even if logout fails
        localStorage.removeItem('currentUser');
        window.location.href = 'login.html';
    }
}
