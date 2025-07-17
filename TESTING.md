# Splitwise Clone - Testing Guide

This document provides comprehensive testing instructions for the Splitwise Clone application.

## Prerequisites

1. **Java 17+** installed and configured
2. **Maven** installed and in PATH
3. **MySQL** server running
4. **curl** command available (for API testing)

## Setup for Testing

### 1. Start the Backend Server

```bash
# Navigate to project directory
cd splitwise-clone

# Start the Spring Boot application
mvn spring-boot:run
```

The server will start on `http://localhost:8080`

### 2. Verify Server is Running

```bash
# Test basic connectivity
curl http://localhost:8080/hello

# Expected response: "Hello! Splitwise Clone Backend is running successfully!"
```

## Automated Testing

### Option 1: Run Complete Test Script

```bash
# Run the comprehensive test (requires jq for JSON parsing)
./test-workflow.bat

# OR run the simple test (no additional tools required)
./test-simple.bat
```

### Option 2: Manual API Testing

Follow the step-by-step manual testing process below.

## Manual Testing Process

### Step 1: Create Users

```bash
# Create Alice
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{"name": "Alice Johnson", "email": "alice@example.com", "phone": "1234567890"}'

# Create Bob
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{"name": "Bob Smith", "email": "bob@example.com", "phone": "1234567891"}'

# Create Charlie
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{"name": "Charlie Brown", "email": "charlie@example.com", "phone": "1234567892"}'

# Verify users created
curl http://localhost:8080/api/users
```

### Step 2: Create Group

```bash
# Create group
curl -X POST http://localhost:8080/api/groups \
  -H "Content-Type: application/json" \
  -d '{"name": "Trip to Goa", "description": "Beach vacation expenses"}'

# Verify group created
curl http://localhost:8080/api/groups
```

### Step 3: Add Users to Group

```bash
# Add Alice to group (assuming group ID = 1, user ID = 1)
curl -X POST http://localhost:8080/api/groups/1/users/1

# Add Bob to group
curl -X POST http://localhost:8080/api/groups/1/users/2

# Add Charlie to group
curl -X POST http://localhost:8080/api/groups/1/users/3

# Verify group members
curl http://localhost:8080/api/groups/1/users
```

### Step 4: Add Expenses

```bash
# Expense 1: Hotel booking (Alice paid ₹6000, split equally)
curl -X POST http://localhost:8080/api/expenses \
  -H "Content-Type: application/json" \
  -d '{
    "description": "Hotel booking",
    "amount": 6000.00,
    "paidById": 1,
    "groupId": 1,
    "notes": "3 nights accommodation",
    "splits": [
      {"userId": 1, "amount": 2000.00},
      {"userId": 2, "amount": 2000.00},
      {"userId": 3, "amount": 2000.00}
    ]
  }'

# Expense 2: Dinner (Bob paid ₹900, split equally)
curl -X POST http://localhost:8080/api/expenses \
  -H "Content-Type: application/json" \
  -d '{
    "description": "Dinner at restaurant",
    "amount": 900.00,
    "paidById": 2,
    "groupId": 1,
    "notes": "Seafood restaurant",
    "splits": [
      {"userId": 1, "amount": 300.00},
      {"userId": 2, "amount": 300.00},
      {"userId": 3, "amount": 300.00}
    ]
  }'

# Expense 3: Transportation (Charlie paid ₹1200, split equally)
curl -X POST http://localhost:8080/api/expenses \
  -H "Content-Type: application/json" \
  -d '{
    "description": "Transportation",
    "amount": 1200.00,
    "paidById": 3,
    "groupId": 1,
    "notes": "Taxi and bus fares",
    "splits": [
      {"userId": 1, "amount": 400.00},
      {"userId": 2, "amount": 400.00},
      {"userId": 3, "amount": 400.00}
    ]
  }'

# Verify expenses
curl http://localhost:8080/api/expenses/group/1
```

### Step 5: Check Balances

```bash
# Get group balances
curl http://localhost:8080/api/balances/group/1
```

## Expected Results

After running the complete test scenario, you should see:

### Total Group Expenses
- **₹8,100** (₹6,000 + ₹900 + ₹1,200)

### Individual Balances
- **Alice**: Paid ₹6,000, Owes ₹2,700, **Net: +₹3,300** (gets back)
- **Bob**: Paid ₹900, Owes ₹2,700, **Net: -₹1,800** (owes)
- **Charlie**: Paid ₹1,200, Owes ₹2,700, **Net: -₹1,500** (owes)

### Suggested Settlements
- **Bob** should pay **₹1,800** to **Alice**
- **Charlie** should pay **₹1,500** to **Alice**

## Frontend Testing

### 1. Open the Web Interface

1. Ensure the backend server is running
2. Open `frontend/index.html` in a web browser
3. The interface should connect to `http://localhost:8080`

### 2. Test Web Interface Features

1. **Users Tab**: Add, view, and delete users
2. **Groups Tab**: Create groups, add/remove members
3. **Expenses Tab**: Add expenses with splits, view expense history
4. **Balances Tab**: View balance calculations and settlements

### 3. Test Responsive Design

- Test on different screen sizes
- Verify mobile compatibility
- Check all buttons and forms work correctly

## Troubleshooting

### Common Issues

1. **Server not starting**
   - Check if MySQL is running
   - Verify database credentials in `application.properties`
   - Ensure port 8080 is not in use

2. **API calls failing**
   - Verify server is running on `http://localhost:8080`
   - Check for CORS issues in browser console
   - Ensure JSON format is correct in requests

3. **Database errors**
   - Check MySQL connection
   - Verify database `splitwise_clone` exists
   - Check application logs for detailed error messages

4. **Frontend not loading data**
   - Check browser console for JavaScript errors
   - Verify API endpoints are accessible
   - Ensure CORS is properly configured

### Logs and Debugging

- Backend logs: Check console output when running `mvn spring-boot:run`
- Frontend logs: Open browser Developer Tools → Console
- Database logs: Check MySQL error logs if database issues occur

## Performance Testing

For load testing, you can use tools like:
- **Apache Bench (ab)**: `ab -n 100 -c 10 http://localhost:8080/api/users`
- **curl**: Multiple concurrent requests
- **Postman**: Collection runner for API testing

## Security Testing

Basic security checks:
1. Verify input validation (try invalid data)
2. Check for SQL injection protection
3. Test CORS configuration
4. Verify error handling doesn't expose sensitive information

## Next Steps

After successful testing:
1. Deploy to a cloud platform (Heroku, AWS, etc.)
2. Set up CI/CD pipeline
3. Add authentication and authorization
4. Implement real-time notifications
5. Add mobile app support
