@echo off
cd /d "%~dp0"
title iWani - Push to GitHub Automation Tool

:: Launch PowerShell automation with Bypass policy
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0push-to-github.ps1"

if %errorlevel% neq 0 (
    echo.
    echo Script ended.
    pause
)
