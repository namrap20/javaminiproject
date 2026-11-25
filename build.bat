@echo off
setlocal enabledelayedexpansion

echo ============================================================
echo   SMS: Student Management System - Desktop Build Script
echo ============================================================

set "JAVA_HOME=C:\Program Files\Java\jdk-26.0.2.1"
if not exist "%JAVA_HOME%\bin\javac.exe" (
    if exist "C:\Program Files\Java\latest\bin\javac.exe" (
        set "JAVA_HOME=C:\Program Files\Java\latest"
    )
)

set "PATH=%JAVA_HOME%\bin;%PATH%"

set "MVN_CMD=maven\apache-maven-3.9.6\bin\mvn.cmd"
if not exist "%MVN_CMD%" (
    where mvn >nul 2>&1
    if %errorlevel% equ 0 (
        set "MVN_CMD=mvn"
    ) else (
        echo [ERROR] Maven not found.
        exit /b 1
    )
)

echo [1/3] Cleaning previous build targets...
call "%MVN_CMD%" clean -q

echo [2/3] Compiling Java 21 sources with JavaFX & MySQL dependencies...
call "%MVN_CMD%" compile
if %errorlevel% neq 0 (
    echo [ERROR] Compilation failed!
    exit /b %errorlevel%
)

echo [3/3] Packaging JAR and resolving runtime dependencies...
call "%MVN_CMD%" package -DskipTests
if %errorlevel% neq 0 (
    echo [ERROR] Packaging failed!
    exit /b %errorlevel%
)

echo ============================================================
echo   BUILD SUCCESSFUL!
echo   Target JAR: target\SMS-1.0.0.jar
echo   Dependencies: target\lib\
echo ============================================================
exit /b 0
