@echo off
REM Taxila Notification Bot - Windows Setup Script

echo ================================
echo Taxila Notification Bot Setup
echo ================================
echo.

REM Check Java installation
echo [1] Checking Java installation...
java -version >nul 2>&1
if errorlevel 1 (
    echo ERROR: Java not found
    echo Please install Java 11 from: https://www.oracle.com/java/technologies/downloads/
    pause
    exit /b 1
)
for /f tokens^=2 %%j in ('java -version 2^>^&1 ^| find "version"') do set JAVA_VER=%%j
echo √ Java version: %JAVA_VER%
echo.

REM Check Maven installation
echo [2] Checking Maven installation...
mvn -version >nul 2>&1
if errorlevel 1 (
    echo ERROR: Maven not found
    echo Please install Maven from: https://maven.apache.org/download.cgi
    echo And add to PATH
    pause
    exit /b 1
)
for /f tokens^=3 %%m in ('mvn -version 2^>^&1 ^| findstr "Apache Maven"') do set MVN_VER=%%m
echo √ Maven installed: %MVN_VER%
echo.

REM Create .env file
echo [3] Setting up environment variables...
if exist .env (
    echo WARNING: .env file already exists (contains credentials)
    set /p overwrite="Do you want to overwrite it? (y/n): "
    if /i "%overwrite%"=="y" (
        copy .env.example .env
        echo √ .env file updated
    ) else (
        echo Skipping .env setup
    )
) else (
    copy .env.example .env
    echo √ .env file created
)

echo.
echo ⚠️  SECURITY WARNING: Credentials will be stored in .env
echo .env is in .gitignore and will NOT be committed to version control
echo.
echo Enter your credentials (or leave empty to edit .env manually later):
echo.
set /p BITS_ROLL="BITS Email (e.g., abc@wilp.bits-pilani.ac.in): "
set /p TAXILA_PASS="Taxila Password: "
set /p TG_TOKEN="Telegram Bot Token (from @BotFather): "
set /p TG_CHAT="Telegram Chat ID (from @userinfobot): "

if not "%BITS_ROLL%"=="" (
    powershell -Command "(gc .env) -replace 'TAXILA_USERNAME=.*', 'TAXILA_USERNAME=%BITS_ROLL%' | Out-File .env"
)
if not "%TAXILA_PASS%"=="" (
    powershell -Command "(gc .env) -replace 'TAXILA_PASSWORD=.*', 'TAXILA_PASSWORD=%TAXILA_PASS%' | Out-File .env"
)
if not "%TG_TOKEN%"=="" (
    powershell -Command "(gc .env) -replace 'TELEGRAM_BOT_TOKEN=.*', 'TELEGRAM_BOT_TOKEN=%TG_TOKEN%' | Out-File .env"
)
if not "%TG_CHAT%"=="" (
    powershell -Command "(gc .env) -replace 'TELEGRAM_CHAT_ID=.*', 'TELEGRAM_CHAT_ID=%TG_CHAT%' | Out-File .env"
)

echo.
echo [4] Building project...
call mvn clean package

echo.
echo ================================
echo Setup Complete!
echo ================================
echo.
echo Next steps:
echo.
echo 1. Verify .env file:
echo    type .env
echo.
echo 2. Run the bot:
echo    java -jar target\taxila-notification-bot-1.0.0.jar
echo.
echo 3. Or use Maven:
echo    mvn clean compile exec:java -Dexec.mainClass="com.taxilabot.Main"
echo.
echo 4. For cloud deployment, see DEPLOYMENT.md
echo.
echo 5. For troubleshooting, see TROUBLESHOOTING.md
echo.
pause
