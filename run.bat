@echo off
title SMS — Student Management System
cd /d "%~dp0"

echo ==================================================
echo   SMS — Student Management System (Launcher)
echo ==================================================

if exist "SMS.exe" (
    echo Launching native desktop executable: SMS.exe ...
    start "" "SMS.exe"
    exit /b 0
)

if exist "dist\SMS\SMS.exe" (
    echo Launching native Windows application: dist\SMS\SMS.exe ...
    start "" "dist\SMS\SMS.exe"
    exit /b 0
)

set "JAVA_HOME=C:\Program Files\Java\jdk-26.0.2.1"
if not exist "%JAVA_HOME%\bin\java.exe" set "JAVA_HOME=C:\Program Files\Java\latest"
set "JAVA_BIN=%JAVA_HOME%\bin\java.exe"
if not exist "%JAVA_BIN%" set "JAVA_BIN=java"

if not exist "target\SMS-1.0.0.jar" (
    echo Target JAR not found. Running build.bat first...
    call build.bat
    if %errorlevel% neq 0 (
        echo [ERROR] Build failed. Cannot launch application.
        pause
        exit /b 1
    )
)

echo Starting SMS JavaFX Desktop Application...
"%JAVA_BIN%" --module-path "target\lib" --add-modules javafx.controls,javafx.fxml,javafx.graphics,javafx.base -cp "target\SMS-1.0.0.jar;target\lib\*" sms.App
pause
