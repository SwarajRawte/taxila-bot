#!/bin/bash

# Taxila Notification Bot - Setup Script
# This script helps set up the bot on Linux/macOS

set -e

echo "================================"
echo "Taxila Notification Bot Setup"
echo "================================"
echo ""

# Check Java installation
echo "[1] Checking Java installation..."
if ! command -v java &> /dev/null; then
    echo "ERROR: Java not found. Installing Java 11..."
    if command -v apt-get &> /dev/null; then
        sudo apt-get update
        sudo apt-get install -y openjdk-11-jdk-headless
    elif command -v brew &> /dev/null; then
        brew install openjdk@11
    else
        echo "ERROR: Could not install Java. Please install Java 11 manually."
        exit 1
    fi
fi

JAVA_VERSION=$(java -version 2>&1 | grep -oP 'version "\K[^"]*')
echo "✓ Java version: $JAVA_VERSION"
echo ""

# Check Maven installation
echo "[2] Checking Maven installation..."
if ! command -v mvn &> /dev/null; then
    echo "ERROR: Maven not found. Installing Maven..."
    mkdir -p ~/tools
    cd ~/tools
    
    if command -v wget &> /dev/null; then
        wget https://downloads.apache.org/maven/maven-3/3.9.5/binaries/apache-maven-3.9.5-bin.tar.gz
    else
        curl -O https://downloads.apache.org/maven/maven-3/3.9.5/binaries/apache-maven-3.9.5-bin.tar.gz
    fi
    
    tar -xzf apache-maven-3.9.5-bin.tar.gz
    echo "export PATH=$HOME/tools/apache-maven-3.9.5/bin:\$PATH" >> ~/.bashrc
    export PATH=$HOME/tools/apache-maven-3.9.5/bin:$PATH
fi

MVN_VERSION=$(mvn -v 2>&1 | head -1)
echo "✓ Maven version: $MVN_VERSION"
echo ""

# Check Chrome installation
echo "[3] Checking Chrome/Chromium installation..."
if ! command -v chromium-browser &> /dev/null && ! command -v chromium &> /dev/null && ! command -v google-chrome &> /dev/null; then
    echo "Chrome not found. Installing Chromium..."
    if command -v apt-get &> /dev/null; then
        sudo apt-get install -y chromium-browser
    elif command -v brew &> /dev/null; then
        brew install chromium
    else
        echo "WARNING: Could not install Chrome. Please install manually."
    fi
fi
echo "✓ Chrome/Chromium found"
echo ""

# Create .env file
echo "[4] Setting up environment variables..."
if [ -f ".env" ]; then
    echo "WARNING: .env file already exists (contains credentials)"
    read -p "Do you want to overwrite it? (y/n) " -n 1 -r
    echo
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        echo "Skipping .env setup"
    else
        cp .env.example .env
        echo "✓ .env file created"
    fi
else
    cp .env.example .env
    echo "✓ .env file created"
fi

# Prompt for credentials
echo ""
echo "⚠️  SECURITY WARNING: Credentials will be stored in .env"
echo ".env is in .gitignore and will NOT be committed to version control"
echo ""
echo "Enter your credentials (or leave empty to set later):"
echo ""
read -p "BITS Email (e.g., abc@wilp.bits-pilani.ac.in): " BITS_ROLL
read -p "Taxila Password: " TAXILA_PASS
read -p "Telegram Bot Token (from @BotFather): " TG_TOKEN
read -p "Telegram Chat ID (from @userinfobot): " TG_CHAT

if [ ! -z "$BITS_ROLL" ]; then
    # Use printf to safely escape special characters
    BITS_ROLL_ESCAPED=$(printf '%s\n' "$BITS_ROLL" | sed -e 's/[\&/]/\\&/g')
    sed -i.bak "s/TAXILA_USERNAME=.*/TAXILA_USERNAME=$BITS_ROLL_ESCAPED/" .env
    rm -f .env.bak
fi

if [ ! -z "$TAXILA_PASS" ]; then
    TAXILA_PASS_ESCAPED=$(printf '%s\n' "$TAXILA_PASS" | sed -e 's/[\&/]/\\&/g')
    sed -i.bak "s/TAXILA_PASSWORD=.*/TAXILA_PASSWORD=$TAXILA_PASS_ESCAPED/" .env
    rm -f .env.bak
fi

if [ ! -z "$TG_TOKEN" ]; then
    TG_TOKEN_ESCAPED=$(printf '%s\n' "$TG_TOKEN" | sed -e 's/[\&/]/\\&/g')
    sed -i.bak "s/TELEGRAM_BOT_TOKEN=.*/TELEGRAM_BOT_TOKEN=$TG_TOKEN_ESCAPED/" .env
    rm -f .env.bak
fi

if [ ! -z "$TG_CHAT" ]; then
    sed -i.bak "s/TELEGRAM_CHAT_ID=.*/TELEGRAM_CHAT_ID=$TG_CHAT/" .env
    rm -f .env.bak
fi

echo ""
echo "[5] Building project..."
mvn clean package

echo ""
echo "================================"
echo "Setup Complete! ✓"
echo "================================"
echo ""
echo "Next steps:"
echo ""
echo "1. Verify .env file:"
echo "   cat .env"
echo ""
echo "2. Run the bot:"
echo "   java -jar target/taxila-notification-bot-1.0.0.jar"
echo ""
echo "3. Or use Maven:"
echo "   mvn clean compile exec:java -Dexec.mainClass=\"com.taxilabot.Main\""
echo ""
echo "4. For cloud deployment, see DEPLOYMENT.md"
echo ""
echo "5. For troubleshooting, see TROUBLESHOOTING.md"
echo ""
