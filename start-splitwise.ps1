# Splitwise Clone Startup Script
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Starting Splitwise Clone Application" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Refresh PATH environment variable
$env:PATH = [System.Environment]::GetEnvironmentVariable("PATH","User") + ";" + [System.Environment]::GetEnvironmentVariable("PATH","Machine")

# Test Maven
Write-Host "Testing Maven..." -ForegroundColor Yellow
try {
    $mavenVersion = & mvn --version 2>&1
    Write-Host "✅ Maven is working!" -ForegroundColor Green
    Write-Host $mavenVersion
} catch {
    Write-Host "❌ Maven not found. Using full path..." -ForegroundColor Red
    $env:PATH += ";C:\Program Files\apache-maven-3.9.6\bin"
}

Write-Host ""
Write-Host "Starting Spring Boot application..." -ForegroundColor Yellow
Write-Host "This may take a few minutes on first run..." -ForegroundColor Yellow
Write-Host ""

# Start the application
try {
    & mvn spring-boot:run
} catch {
    Write-Host "❌ Failed to start application" -ForegroundColor Red
    Write-Host "Error: $_" -ForegroundColor Red
    Write-Host ""
    Write-Host "Troubleshooting:" -ForegroundColor Yellow
    Write-Host "1. Make sure MySQL is running" -ForegroundColor White
    Write-Host "2. Check database password in application.properties" -ForegroundColor White
    Write-Host "3. Ensure port 8080 is not in use" -ForegroundColor White
    Read-Host "Press Enter to exit"
}
