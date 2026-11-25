@echo off
setlocal enabledelayedexpansion

echo ============================================================
echo   SMS: Packaging Native Windows Executable (SMS.exe)
echo ============================================================

call build.bat
if %errorlevel% neq 0 (
    echo [ERROR] Build step failed. Cannot package.
    exit /b %errorlevel%
)

set "JAVA_HOME=C:\Program Files\Java\jdk-26.0.2.1"
if not exist "%JAVA_HOME%\bin\java.exe" set "JAVA_HOME=C:\Program Files\Java\latest"
set "JPACKAGE=%JAVA_HOME%\bin\jpackage.exe"

if not exist "%JPACKAGE%" (
    echo [ERROR] jpackage.exe not found in %JAVA_HOME%\bin.
    exit /b 1
)

echo [1/3] Preparing staging input for jpackage...
if exist "target\package-input" rmdir /s /q "target\package-input"
mkdir "target\package-input"
mkdir "target\package-input\lib"

copy /y "target\SMS-1.0.0.jar" "target\package-input\" >nul
copy /y "target\lib\*.jar" "target\package-input\lib\" >nul
xcopy /E /I /Y "config" "target\package-input\config" >nul

echo [2/3] Cleaning previous dist output...
if exist "dist\SMS" rmdir /s /q "dist\SMS"
if not exist "dist" mkdir "dist"

echo [3/3] Generating native Windows application package with jpackage...
"%JPACKAGE%" ^
    --type app-image ^
    --name "SMS" ^
    --input "target\package-input" ^
    --main-jar "SMS-1.0.0.jar" ^
    --main-class "sms.App" ^
    --dest "dist" ^
    --module-path "target\lib" ^
    --add-modules "javafx.controls,javafx.fxml,javafx.graphics,javafx.base,java.sql,java.naming" ^
    --java-options "--enable-native-access=ALL-UNNAMED"

if %errorlevel% neq 0 (
    echo [ERROR] jpackage packaging failed!
    exit /b %errorlevel%
)

REM Copy config into dist\SMS for standalone execution
xcopy /E /I /Y "config" "dist\SMS\config" >nul

echo ============================================================
echo   PACKAGING SUCCESSFUL!
echo   Native Desktop Executable generated at:
echo   dist\SMS\SMS.exe
echo ============================================================
exit /b 0
