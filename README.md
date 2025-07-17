# Splitwise Clone

A full-stack expense sharing application similar to Splitwise, built with Spring Boot backend and HTML/CSS/JavaScript frontend.

## Features

- User registration and management
- Group creation and member management
- Expense tracking with split functionality
- Balance calculation and settlement
- Simple web interface

## Tech Stack

**Backend:**
- Spring Boot 3.2.0
- Spring Data JPA
- MySQL Database
- Maven

**Frontend:**
- HTML5
- CSS3
- Vanilla JavaScript

## Prerequisites

- Java 17 or higher
- MySQL 8.0 or higher
- Maven 3.6 or higher

## Setup Instructions

### Install Maven (if not already installed)
**Option 1: Download and Install Manually**
1. Download Maven from https://maven.apache.org/download.cgi
2. Extract to a folder (e.g., `C:\apache-maven-3.9.5`)
3. Add `C:\apache-maven-3.9.5\bin` to your PATH environment variable
4. Verify installation: `mvn --version`

**Option 2: Using Chocolatey (Windows)**
```bash
choco install maven
```

### Database Setup
1. Install MySQL and start the service
2. Create a database named `splitwise_clone` (or let the app create it automatically)
3. Update database credentials in `src/main/resources/application.properties` if needed

### Backend Setup
1. Clone the repository
2. Navigate to the project directory
3. **Quick Setup**: Run the setup verification script:
   ```bash
   verify-setup.bat
   ```
4. **Start Application**: Run the startup script:
   ```bash
   start-app.bat
   ```
   Or manually with Maven:
   ```bash
   mvn spring-boot:run
   ```
5. The API will be available at `http://localhost:8080`

### Quick Start Scripts
- `verify-setup.bat` - Check all prerequisites (Java, Maven, MySQL)
- `start-app.bat` - Start the application with helpful error messages
- `test-simple.bat` - Run end-to-end tests with sample data

### Test the Setup
- Visit `http://localhost:8080/hello` to verify the backend is running

## API Endpoints

### Test Endpoints
- `GET /` - Welcome message
- `GET /hello` - Test endpoint

### User Management
- `POST /api/users` - Create a new user
- `GET /api/users` - Get all users
- `GET /api/users/{id}` - Get user by ID
- `PUT /api/users/{id}` - Update user
- `DELETE /api/users/{id}` - Delete user
- `GET /api/users/search?name={name}` - Search users by name
- `GET /api/users/email/{email}` - Get user by email

#### User API Examples
```bash
# Create user
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{"name": "John Doe", "email": "john@example.com", "phone": "1234567890"}'

# Get all users
curl http://localhost:8080/api/users

# Get user by ID
curl http://localhost:8080/api/users/1
```

### Group Management
- `POST /api/groups` - Create a new group
- `GET /api/groups` - Get all groups
- `GET /api/groups/{id}` - Get group by ID
- `PUT /api/groups/{id}` - Update group
- `DELETE /api/groups/{id}` - Delete group
- `POST /api/groups/{groupId}/users/{userId}` - Add user to group
- `DELETE /api/groups/{groupId}/users/{userId}` - Remove user from group
- `GET /api/groups/{groupId}/users` - Get all users in a group
- `GET /api/groups/user/{userId}` - Get all groups a user belongs to
- `GET /api/groups/search?name={name}` - Search groups by name
- `GET /api/groups/{groupId}/count` - Get group member count

#### Group API Examples
```bash
# Create group
curl -X POST http://localhost:8080/api/groups \
  -H "Content-Type: application/json" \
  -d '{"name": "Trip to Goa", "description": "Beach vacation expenses"}'

# Add user to group
curl -X POST http://localhost:8080/api/groups/1/users/1

# Get group members
curl http://localhost:8080/api/groups/1/users
```

### Expense Management
- `POST /api/expenses` - Create a new expense with splits
- `GET /api/expenses/{id}` - Get expense by ID
- `GET /api/expenses/group/{groupId}` - Get all expenses in a group
- `GET /api/expenses/user/{userId}` - Get all expenses paid by a user
- `GET /api/expenses/{expenseId}/splits` - Get splits for an expense
- `GET /api/expenses/splits/user/{userId}` - Get all splits owed by a user
- `GET /api/expenses/splits/user/{userId}/unsettled` - Get unsettled splits owed by a user
- `GET /api/expenses/splits/group/{groupId}/user/{userId}/unsettled` - Get unsettled splits for a user in a group
- `PUT /api/expenses/splits/{splitId}/settle` - Mark a split as settled
- `PUT /api/expenses/splits/{splitId}/unsettle` - Mark a split as unsettled
- `DELETE /api/expenses/{id}` - Delete an expense
- `GET /api/expenses/group/{groupId}/user/{userId}/owed` - Get total amount owed by a user in a group
- `GET /api/expenses/group/{groupId}/user/{userId}/paid` - Get total amount paid by a user in a group
- `GET /api/expenses/group/{groupId}/total` - Get total amount spent in a group

#### Expense API Examples
```bash
# Create expense with equal split among 3 users
curl -X POST http://localhost:8080/api/expenses \
  -H "Content-Type: application/json" \
  -d '{
    "description": "Dinner at restaurant",
    "amount": 600.00,
    "paidById": 1,
    "groupId": 1,
    "notes": "Italian restaurant",
    "splits": [
      {"userId": 1, "amount": 200.00},
      {"userId": 2, "amount": 200.00},
      {"userId": 3, "amount": 200.00}
    ]
  }'

# Get group expenses
curl http://localhost:8080/api/expenses/group/1

# Get unsettled splits for a user
curl http://localhost:8080/api/expenses/splits/user/2/unsettled
```

### Balance Calculation
- `GET /api/balances/group/{groupId}` - Get balance calculations for a group (who owes whom)
- `GET /api/balances/user/{userId}/summary` - Get balance summary for a user across all groups
- `GET /api/balances/user/{userId}/total-owed` - Get total amount owed by a user across all groups
- `GET /api/balances/group/{groupId}/is-settled` - Check if all expenses in a group are settled

#### Balance API Examples
```bash
# Get group balances (shows who owes whom)
curl http://localhost:8080/api/balances/group/1

# Get user's balance summary across all groups
curl http://localhost:8080/api/balances/user/2/summary

# Check if group is fully settled
curl http://localhost:8080/api/balances/group/1/is-settled
```

## Frontend Usage

The frontend is a simple HTML/CSS/JavaScript application that provides a user-friendly interface to interact with the backend APIs.

### Features
- **User Management**: Add, view, and delete users
- **Group Management**: Create groups, add/remove members
- **Expense Tracking**: Add expenses with custom or equal splits
- **Balance Calculation**: View who owes whom and settlement suggestions
- **Responsive Design**: Works on desktop and mobile devices

### Setup
1. Start the backend server: `mvn spring-boot:run`
2. Open `frontend/index.html` in a web browser
3. The frontend will connect to the backend at `http://localhost:8080`

### Usage Flow
1. **Add Users**: Create user accounts with name and email
2. **Create Groups**: Set up expense groups (e.g., "Trip to Goa")
3. **Add Members**: Add users to groups
4. **Record Expenses**: Add expenses with split details
5. **View Balances**: See who owes money and settlement suggestions

## Development Progress

- [x] Step 1: Set Up Backend (Spring Boot + MySQL)
- [x] Step 2: Create User Module
- [x] Step 3: Create Group Module
- [x] Step 4: Create Expense Module
- [x] Step 5: Add Balance Calculation Logic
- [x] Step 6: Create Basic Frontend
- [x] Step 7: Test Use Case End-to-End
- [x] Step 8: Polish and Document

## Project Structure

```
splitwise-clone/
├── src/
│   ├── main/
│   │   ├── java/com/splitwise/
│   │   │   ├── SplitwiseCloneApplication.java
│   │   │   ├── controller/
│   │   │   │   ├── TestController.java
│   │   │   │   ├── UserController.java
│   │   │   │   ├── GroupController.java
│   │   │   │   ├── ExpenseController.java
│   │   │   │   └── BalanceController.java
│   │   │   ├── entity/
│   │   │   │   ├── User.java
│   │   │   │   ├── Group.java
│   │   │   │   ├── GroupMember.java
│   │   │   │   ├── Expense.java
│   │   │   │   └── ExpenseSplit.java
│   │   │   ├── repository/
│   │   │   │   ├── UserRepository.java
│   │   │   │   ├── GroupRepository.java
│   │   │   │   ├── GroupMemberRepository.java
│   │   │   │   ├── ExpenseRepository.java
│   │   │   │   └── ExpenseSplitRepository.java
│   │   │   ├── service/
│   │   │   │   ├── UserService.java
│   │   │   │   ├── GroupService.java
│   │   │   │   ├── ExpenseService.java
│   │   │   │   └── BalanceService.java
│   │   │   └── dto/
│   │   │       ├── CreateExpenseRequest.java
│   │   │       └── BalanceResponse.java
│   │   └── resources/
│   │       └── application.properties
│   └── test/
├── frontend/
│   ├── index.html
│   ├── styles.css
│   └── script.js
├── .mvn/wrapper/
├── mvnw.cmd
├── pom.xml
├── test-setup.bat
└── README.md
```
