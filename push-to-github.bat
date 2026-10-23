@echo off
cd /d "%~dp0"
title iWani - Push to GitHub Automation Tool

powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0push-to-github.ps1"

if %errorlevel% neq 0 (
    echo.
    echo Script finished with exit code %errorlevel%.
    pause
)
