# Splitwise Clone - Complete Demo Workflow
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Splitwise Clone - Complete Demo" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

$API_BASE = "http://localhost:8080/api"

Write-Host "🧪 Testing complete workflow with realistic scenario..." -ForegroundColor Yellow
Write-Host ""

# Test API connectivity
Write-Host "Step 1: Testing API connectivity..." -ForegroundColor Green
try {
    $response = Invoke-RestMethod -Uri "$API_BASE/../hello" -Method GET
    Write-Host "✅ Backend server is running: $response" -ForegroundColor Green
} catch {
    Write-Host "❌ Backend server is not running!" -ForegroundColor Red
    Write-Host "Please start the server with: mvn spring-boot:run" -ForegroundColor Yellow
    Read-Host "Press Enter to exit"
    exit 1
}

Write-Host ""
Write-Host "Step 2: Creating users..." -ForegroundColor Green

# Create users
$users = @(
    @{ name = "Alice Johnson"; email = "alice@example.com"; phone = "9876543210" },
    @{ name = "Bob Smith"; email = "bob@example.com"; phone = "9876543211" },
    @{ name = "Charlie Brown"; email = "charlie@example.com"; phone = "9876543212" },
    @{ name = "Diana Prince"; email = "diana@example.com"; phone = "9876543213" }
)

$createdUsers = @()
foreach ($user in $users) {
    try {
        $created = Invoke-RestMethod -Uri "$API_BASE/users" -Method POST -ContentType "application/json" -Body ($user | ConvertTo-Json)
        $createdUsers += $created
        Write-Host "✅ Created user: $($created.name) (ID: $($created.id))" -ForegroundColor Green
    } catch {
        Write-Host "⚠️  User $($user.name) might already exist" -ForegroundColor Yellow
    }
}

Write-Host ""
Write-Host "Step 3: Creating groups..." -ForegroundColor Green

# Create groups
$groups = @(
    @{ name = "Trip to Goa"; description = "Beach vacation expenses" },
    @{ name = "Office Lunch"; description = "Team lunch expenses" }
)

$createdGroups = @()
foreach ($group in $groups) {
    try {
        $created = Invoke-RestMethod -Uri "$API_BASE/groups" -Method POST -ContentType "application/json" -Body ($group | ConvertTo-Json)
        $createdGroups += $created
        Write-Host "✅ Created group: $($created.name) (ID: $($created.id))" -ForegroundColor Green
    } catch {
        Write-Host "⚠️  Group $($group.name) might already exist" -ForegroundColor Yellow
    }
}

Write-Host ""
Write-Host "Step 4: Adding users to groups..." -ForegroundColor Green

# Add users to first group (Trip to Goa)
if ($createdGroups.Count -gt 0 -and $createdUsers.Count -gt 0) {
    $groupId = $createdGroups[0].id
    foreach ($user in $createdUsers[0..2]) {  # Add first 3 users
        try {
            Invoke-RestMethod -Uri "$API_BASE/groups/$groupId/users/$($user.id)" -Method POST
            Write-Host "✅ Added $($user.name) to $($createdGroups[0].name)" -ForegroundColor Green
        } catch {
            Write-Host "⚠️  $($user.name) might already be in the group" -ForegroundColor Yellow
        }
    }
}

Write-Host ""
Write-Host "Step 5: Adding realistic expenses..." -ForegroundColor Green

# Add expenses to the group
$expenses = @(
    @{
        description = "Hotel Booking"
        amount = 12000.00
        paidById = $createdUsers[0].id  # Alice pays
        groupId = $createdGroups[0].id
        notes = "3 nights at beach resort"
        splits = @(
            @{ userId = $createdUsers[0].id; amount = 4000.00 },
            @{ userId = $createdUsers[1].id; amount = 4000.00 },
            @{ userId = $createdUsers[2].id; amount = 4000.00 }
        )
    },
    @{
        description = "Flight Tickets"
        amount = 18000.00
        paidById = $createdUsers[1].id  # Bob pays
        groupId = $createdGroups[0].id
        notes = "Round trip flights for all"
        splits = @(
            @{ userId = $createdUsers[0].id; amount = 6000.00 },
            @{ userId = $createdUsers[1].id; amount = 6000.00 },
            @{ userId = $createdUsers[2].id; amount = 6000.00 }
        )
    },
    @{
        description = "Food & Dining"
        amount = 4500.00
        paidById = $createdUsers[2].id  # Charlie pays
        groupId = $createdGroups[0].id
        notes = "Restaurants and local food"
        splits = @(
            @{ userId = $createdUsers[0].id; amount = 1500.00 },
            @{ userId = $createdUsers[1].id; amount = 1500.00 },
            @{ userId = $createdUsers[2].id; amount = 1500.00 }
        )
    },
    @{
        description = "Activities & Sightseeing"
        amount = 3000.00
        paidById = $createdUsers[0].id  # Alice pays again
        groupId = $createdGroups[0].id
        notes = "Water sports and tours"
        splits = @(
            @{ userId = $createdUsers[0].id; amount = 1000.00 },
            @{ userId = $createdUsers[1].id; amount = 1000.00 },
            @{ userId = $createdUsers[2].id; amount = 1000.00 }
        )
    }
)

foreach ($expense in $expenses) {
    try {
        $created = Invoke-RestMethod -Uri "$API_BASE/expenses" -Method POST -ContentType "application/json" -Body ($expense | ConvertTo-Json -Depth 3)
        $payer = $createdUsers | Where-Object { $_.id -eq $expense.paidById }
        Write-Host "✅ Added expense: $($expense.description) - ₹$($expense.amount) (Paid by $($payer.name))" -ForegroundColor Green
    } catch {
        Write-Host "❌ Failed to add expense: $($expense.description)" -ForegroundColor Red
        Write-Host "Error: $($_.Exception.Message)" -ForegroundColor Red
    }
}

Write-Host ""
Write-Host "Step 6: Calculating balances..." -ForegroundColor Green

try {
    $balances = Invoke-RestMethod -Uri "$API_BASE/balances/group/$($createdGroups[0].id)" -Method GET
    
    Write-Host ""
    Write-Host "========================================" -ForegroundColor Cyan
    Write-Host "📊 BALANCE SUMMARY" -ForegroundColor Cyan
    Write-Host "========================================" -ForegroundColor Cyan
    Write-Host "Group: $($balances.groupName)" -ForegroundColor White
    Write-Host "Total Expenses: ₹$($balances.totalGroupExpenses)" -ForegroundColor White
    Write-Host ""
    
    Write-Host "Individual Balances:" -ForegroundColor Yellow
    foreach ($userBalance in $balances.userBalances) {
        $status = if ($userBalance.netBalance -gt 0) { "Gets back" } else { "Owes" }
        $color = if ($userBalance.netBalance -gt 0) { "Green" } else { "Red" }
        $amount = [Math]::Abs($userBalance.netBalance)
        Write-Host "  $($userBalance.userName): Paid ₹$($userBalance.totalPaid), Share ₹$($userBalance.totalOwed), $status ₹$amount" -ForegroundColor $color
    }
    
    Write-Host ""
    if ($balances.settlements.Count -gt 0) {
        Write-Host "💰 Settlement Suggestions:" -ForegroundColor Yellow
        foreach ($settlement in $balances.settlements) {
            Write-Host "  $($settlement.fromUserName) should pay ₹$($settlement.amount) to $($settlement.toUserName)" -ForegroundColor Cyan
        }
    } else {
        Write-Host "✅ All expenses are settled!" -ForegroundColor Green
    }
    
} catch {
    Write-Host "❌ Failed to get balances" -ForegroundColor Red
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "🎉 Demo completed successfully!" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "What was created:" -ForegroundColor Yellow
Write-Host "✅ 4 users (Alice, Bob, Charlie, Diana)" -ForegroundColor Green
Write-Host "✅ 2 groups (Trip to Goa, Office Lunch)" -ForegroundColor Green
Write-Host "✅ 4 realistic expenses with splits" -ForegroundColor Green
Write-Host "✅ Balance calculations and settlements" -ForegroundColor Green
Write-Host ""
Write-Host "Next steps:" -ForegroundColor Yellow
Write-Host "1. Open frontend/index.html in your browser" -ForegroundColor White
Write-Host "2. Select yourself from the 'You are:' dropdown" -ForegroundColor White
Write-Host "3. Explore the created data in all tabs" -ForegroundColor White
Write-Host "4. Try adding more expenses and see live balance updates" -ForegroundColor White
Write-Host ""

Read-Host "Press Enter to continue"
