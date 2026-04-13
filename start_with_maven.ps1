$ErrorActionPreference = "Stop"

# --- Database & Environment Setup ---
$env:DATABASE_URL="jdbc:postgresql://ep-blue-bar-amoa0hnh-pooler.c-5.us-east-1.aws.neon.tech:5432/neondb?sslmode=require"
$env:DB_USERNAME="neondb_owner"
$env:DB_PASSWORD="npg_gxnkATj6C3yW"
$env:PORT="8080"

# --- Local Maven Setup ---
$LocalMvnDir = Join-Path $PSScriptRoot ".mvn_local"
$MavenVersion = "3.9.6"
$MavenBinZip = "apache-maven-$MavenVersion-bin.zip"
$MavenUrl = "https://archive.apache.org/dist/maven/maven-3/$MavenVersion/binaries/$MavenBinZip"
$MavenHome = Join-Path $LocalMvnDir "apache-maven-$MavenVersion"
$MvnCmd = Join-Path $MavenHome "bin\mvn.cmd"

# Create .mvn_local directory if it doesn't exist
if (-not (Test-Path $LocalMvnDir)) {
    New-Item -ItemType Directory -Path $LocalMvnDir | Out-Null
}

# Check if Maven is already set up locally
if (-not (Test-Path $MvnCmd)) {
    Write-Host "Maven not found locally. Downloading Apache Maven $MavenVersion..." -ForegroundColor Cyan
    $ZipPath = Join-Path $LocalMvnDir $MavenBinZip
    
    # Download Maven
    Invoke-WebRequest -Uri $MavenUrl -OutFile $ZipPath
    
    Write-Host "Extracting Maven..." -ForegroundColor Cyan
    Expand-Archive -Path $ZipPath -DestinationPath $LocalMvnDir -Force
    
    # Clean up zip
    Remove-Item $ZipPath
    
    Write-Host "Maven set up successfully." -ForegroundColor Green
}

# Add Maven to PATH for this session
$env:PATH = "$(Join-Path $MavenHome 'bin');$env:PATH"

# Verify installation
Write-Host "Maven Version:" -ForegroundColor Gray
mvn -version

# Run the Spring Boot application
Write-Host "----------------------------------------" -ForegroundColor Cyan
Write-Host "Starting Loan Ledger System (Local)..." -ForegroundColor Green
Write-Host "----------------------------------------" -ForegroundColor Cyan
mvn spring-boot:run
