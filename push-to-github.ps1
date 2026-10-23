[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
$Host.UI.RawUI.WindowTitle = "iWani - Push to GitHub Automation"

Clear-Host
Write-Host "======================================================================" -ForegroundColor Cyan
Write-Host "            iWANI (Project Nexus) - GitHub Automation Tool           " -ForegroundColor Cyan
Write-Host "======================================================================" -ForegroundColor Cyan
Write-Host ""

Set-Location -Path $PSScriptRoot

# 1. Verify Git Installation
$gitCmd = Get-Command git -ErrorAction SilentlyContinue
if (-not $gitCmd) {
    Write-Host "[ERROR] Git is not installed or not in your system PATH." -ForegroundColor Red
    Write-Host "Please download and install Git from: https://git-scm.com/" -ForegroundColor Yellow
    Write-Host ""
    Read-Host "Press Enter to exit"
    exit 1
}

$gitVer = git --version
Write-Host "[OK] Git is ready: $gitVer" -ForegroundColor Green
Write-Host ""

# Ensure Git Author Identity is set
$authorName = git config user.name
if (-not $authorName) {
    git config --global user.name "Project Nexus"
    git config --global user.email "nexus@icct.edu.ph"
}

# 2. Prompt for Repo URL
$defaultUrl = "https://github.com/S1mplee67/iWani.git"
Write-Host "----------------------------------------------------------------------" -ForegroundColor Gray
Write-Host "Default repository : $defaultUrl" -ForegroundColor Yellow
Write-Host "----------------------------------------------------------------------" -ForegroundColor Gray
Write-Host "Press ENTER to use default, or type a custom repo name / URL:" -ForegroundColor White
$userInput = Read-Host "Repository"

$repoUrl = $defaultUrl
if ($userInput -and $userInput.Trim() -ne "") {
    $clean = $userInput.Trim()
    if ($clean -match "^https?://" -or $clean -match "^git@") {
        $repoUrl = $clean
    } else {
        $clean = $clean.TrimStart("/").TrimEnd(".git")
        $repoUrl = "https://github.com/S1mplee67/$clean.git"
    }
}

Write-Host ""
Write-Host "======================================================================" -ForegroundColor Cyan
Write-Host "Target Repository  : $repoUrl" -ForegroundColor Green
Write-Host "======================================================================" -ForegroundColor Cyan
Write-Host ""

# 3. Initialize Git if needed
if (-not (Test-Path ".git")) {
    Write-Host "[*] Initializing Git repository..." -ForegroundColor White
    git init
}

# 4. Set branch to main
git branch -M main

# 5. Remote Origin
Write-Host "[*] Setting remote origin to $repoUrl..." -ForegroundColor White
git remote remove origin 2>$null
git remote add origin $repoUrl

# 6. Stage files
Write-Host "[*] Staging files (source code, models, and iWani-debug.apk)..." -ForegroundColor White
git add .

# 7. Commit
$status = git status --porcelain
if ($status) {
    Write-Host "[*] Creating commit..." -ForegroundColor White
    git commit -m "feat: iWani Project Nexus Alpha Release - FSL Translation & Emergency SOS"
} else {
    Write-Host "[*] Commit is up to date." -ForegroundColor Green
}

# 8. Push
function Do-Push {
    Write-Host ""
    Write-Host "[*] Pushing to GitHub (main branch)..." -ForegroundColor Cyan
    Write-Host "(If a GitHub sign-in window appears, please authorize your account)" -ForegroundColor Yellow
    Write-Host ""
    
    git push -u origin main
    if ($LASTEXITCODE -ne 0) {
        Write-Host ""
        Write-Host "[*] Standard push rejected. Attempting push with force-sync..." -ForegroundColor Yellow
        git push -u origin main --force
    }

    if ($LASTEXITCODE -eq 0) {
        Write-Host ""
        Write-Host "======================================================================" -ForegroundColor Green
        Write-Host " [SUCCESS] Your repository has been uploaded to GitHub successfully!" -ForegroundColor Green
        Write-Host " View it online at: $repoUrl" -ForegroundColor Cyan
        Write-Host "======================================================================" -ForegroundColor Green
        return $true
    } else {
        Write-Host ""
        Write-Host "======================================================================" -ForegroundColor Red
        Write-Host " [NOTICE] Push could not complete." -ForegroundColor Red
        Write-Host " Common causes:" -ForegroundColor Yellow
        Write-Host " 1. The repository '$repoUrl' has NOT been created on GitHub yet." -ForegroundColor White
        Write-Host "    -> Go to https://github.com/new and create it first!" -ForegroundColor Yellow
        Write-Host " 2. GitHub sign-in was cancelled or credentials failed." -ForegroundColor White
        Write-Host " 3. Check your internet connection." -ForegroundColor White
        Write-Host "======================================================================" -ForegroundColor Red
        return $false
    }
}

$pushed = Do-Push
while (-not $pushed) {
    Write-Host ""
    $retry = Read-Host "Would you like to try pushing again? (Y/N)"
    if ($retry -match "^[Yy]") {
        $pushed = Do-Push
    } else {
        break
    }
}

Write-Host ""
Read-Host "Press Enter to exit"
