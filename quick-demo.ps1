# Quick Demo for Splitwise Clone
Write-Host "Splitwise Clone - Quick Demo" -ForegroundColor Cyan
Write-Host "Creating sample data..." -ForegroundColor Yellow

$API_BASE = "http://localhost:8080/api"

# Create users
Write-Host "Creating users..." -ForegroundColor Green
$alice = Invoke-RestMethod -Uri "$API_BASE/users" -Method POST -ContentType "application/json" -Body '{"name": "Alice Johnson", "email": "alice@example.com", "phone": "9876543210"}'
$bob = Invoke-RestMethod -Uri "$API_BASE/users" -Method POST -ContentType "application/json" -Body '{"name": "Bob Smith", "email": "bob@example.com", "phone": "9876543211"}'
$charlie = Invoke-RestMethod -Uri "$API_BASE/users" -Method POST -ContentType "application/json" -Body '{"name": "Charlie Brown", "email": "charlie@example.com", "phone": "9876543212"}'

Write-Host "Users created: Alice (ID: $($alice.id)), Bob (ID: $($bob.id)), Charlie (ID: $($charlie.id))" -ForegroundColor Green

# Create group
Write-Host "Creating group..." -ForegroundColor Green
$group = Invoke-RestMethod -Uri "$API_BASE/groups" -Method POST -ContentType "application/json" -Body '{"name": "Trip to Goa", "description": "Beach vacation expenses"}'
Write-Host "Group created: $($group.name) (ID: $($group.id))" -ForegroundColor Green

# Add users to group
Write-Host "Adding users to group..." -ForegroundColor Green
Invoke-RestMethod -Uri "$API_BASE/groups/$($group.id)/users/$($alice.id)" -Method POST
Invoke-RestMethod -Uri "$API_BASE/groups/$($group.id)/users/$($bob.id)" -Method POST
Invoke-RestMethod -Uri "$API_BASE/groups/$($group.id)/users/$($charlie.id)" -Method POST
Write-Host "All users added to group" -ForegroundColor Green

# Add expenses
Write-Host "Adding expenses..." -ForegroundColor Green

# Hotel expense (Alice pays)
$hotelExpense = @{
    description = "Hotel Booking"
    amount = 6000.00
    paidById = $alice.id
    groupId = $group.id
    notes = "3 nights accommodation"
    splits = @(
        @{ userId = $alice.id; amount = 2000.00 },
        @{ userId = $bob.id; amount = 2000.00 },
        @{ userId = $charlie.id; amount = 2000.00 }
    )
} | ConvertTo-Json -Depth 3

Invoke-RestMethod -Uri "$API_BASE/expenses" -Method POST -ContentType "application/json" -Body $hotelExpense

# Dinner expense (Bob pays)
$dinnerExpense = @{
    description = "Dinner at Restaurant"
    amount = 900.00
    paidById = $bob.id
    groupId = $group.id
    notes = "Seafood restaurant"
    splits = @(
        @{ userId = $alice.id; amount = 300.00 },
        @{ userId = $bob.id; amount = 300.00 },
        @{ userId = $charlie.id; amount = 300.00 }
    )
} | ConvertTo-Json -Depth 3

Invoke-RestMethod -Uri "$API_BASE/expenses" -Method POST -ContentType "application/json" -Body $dinnerExpense

# Transport expense (Charlie pays)
$transportExpense = @{
    description = "Transportation"
    amount = 1200.00
    paidById = $charlie.id
    groupId = $group.id
    notes = "Taxi and bus fares"
    splits = @(
        @{ userId = $alice.id; amount = 400.00 },
        @{ userId = $bob.id; amount = 400.00 },
        @{ userId = $charlie.id; amount = 400.00 }
    )
} | ConvertTo-Json -Depth 3

Invoke-RestMethod -Uri "$API_BASE/expenses" -Method POST -ContentType "application/json" -Body $transportExpense

Write-Host "Expenses added successfully!" -ForegroundColor Green

# Get balances
Write-Host "Getting balances..." -ForegroundColor Green
$balances = Invoke-RestMethod -Uri "$API_BASE/balances/group/$($group.id)" -Method GET

Write-Host ""
Write-Host "BALANCE SUMMARY" -ForegroundColor Cyan
Write-Host "Group: $($balances.groupName)" -ForegroundColor White
Write-Host "Total Expenses: Rs.$($balances.totalGroupExpenses)" -ForegroundColor White
Write-Host ""

Write-Host "Individual Balances:" -ForegroundColor Yellow
foreach ($userBalance in $balances.userBalances) {
    $status = if ($userBalance.netBalance -gt 0) { "Gets back" } else { "Owes" }
    $amount = [Math]::Abs($userBalance.netBalance)
    Write-Host "  $($userBalance.userName): Paid Rs.$($userBalance.totalPaid), Share Rs.$($userBalance.totalOwed), $status Rs.$amount"
}

Write-Host ""
if ($balances.settlements.Count -gt 0) {
    Write-Host "Settlement Suggestions:" -ForegroundColor Yellow
    foreach ($settlement in $balances.settlements) {
        Write-Host "  $($settlement.fromUserName) should pay Rs.$($settlement.amount) to $($settlement.toUserName)" -ForegroundColor Cyan
    }
} else {
    Write-Host "All expenses are settled!" -ForegroundColor Green
}

Write-Host ""
Write-Host "Demo completed! Open frontend/index.html to see the web interface." -ForegroundColor Green
