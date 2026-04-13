# Set Neon Database Environment Variables
$env:DATABASE_URL="jdbc:postgresql://ep-blue-bar-amoa0hnh-pooler.c-5.us-east-1.aws.neon.tech:5432/neondb?sslmode=require"
$env:DB_USERNAME="neondb_owner"
$env:DB_PASSWORD="npg_gxnkATj6C3yW"
$env:PORT="8080"

Write-Host "Starting Loan Ledger System..." -ForegroundColor Cyan

# Try to run with mvn (Global Maven)
if (Get-Command mvn -ErrorAction SilentlyContinue) {
    mvn spring-boot:run
} 
# Try to run with mvnw (Maven Wrapper)
elseif (Test-Path ".\mvnw.cmd") {
    .\mvnw.cmd spring-boot:run
} 
else {
    Write-Host "Error: Maven (mvn) not found on your system." -ForegroundColor Red
    Write-Host "Please install Apache Maven or open this project in IntelliJ/Eclipse to generate the wrapper." -ForegroundColor Yellow
}
