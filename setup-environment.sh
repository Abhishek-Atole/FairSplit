#!/bin/bash
# 🚀 FAIRSplit v1.1 — Quick Environment Setup Script
# Generated: November 13, 2025
# Purpose: Automated environment initialization for Ubuntu/Debian systems

set -e  # Exit on error

echo "=========================================="
echo "🚀 FairSplit v1.1 Environment Setup"
echo "=========================================="
echo ""

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Check if running as root
if [ "$EUID" -eq 0 ]; then 
   echo -e "${RED}❌ Please do not run this script as root${NC}"
   exit 1
fi

echo "Step 1: Checking system requirements..."
echo "----------------------------------------"

# Check Ubuntu/Debian
if ! command -v apt &> /dev/null; then
    echo -e "${RED}❌ This script is for Ubuntu/Debian systems with APT${NC}"
    exit 1
fi

echo -e "${GREEN}✓${NC} APT package manager detected"

# Step 2: Install JDK 17
echo ""
echo "Step 2: Installing OpenJDK 17..."
echo "----------------------------------------"

if command -v java &> /dev/null; then
    JAVA_VERSION=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}' | cut -d'.' -f1)
    if [ "$JAVA_VERSION" -ge 17 ]; then
        echo -e "${GREEN}✓${NC} JDK $JAVA_VERSION already installed"
    else
        echo -e "${YELLOW}⚠${NC} JDK $JAVA_VERSION found, but need JDK 17+"
        sudo apt update
        sudo apt install -y openjdk-17-jdk
    fi
else
    echo "Installing OpenJDK 17..."
    sudo apt update
    sudo apt install -y openjdk-17-jdk
fi

# Verify Java
if java -version 2>&1 | grep -q "openjdk"; then
    echo -e "${GREEN}✓${NC} Java installed successfully"
    java -version
else
    echo -e "${RED}❌ Java installation failed${NC}"
    exit 1
fi

# Step 3: Install Android Studio (via snap)
echo ""
echo "Step 3: Installing Android Studio..."
echo "----------------------------------------"

if command -v android-studio &> /dev/null; then
    echo -e "${GREEN}✓${NC} Android Studio already installed"
else
    echo "Installing Android Studio via snap..."
    sudo snap install android-studio --classic
    
    if [ $? -eq 0 ]; then
        echo -e "${GREEN}✓${NC} Android Studio installed successfully"
    else
        echo -e "${RED}❌ Android Studio installation failed${NC}"
        echo "Try manual installation: https://developer.android.com/studio"
        exit 1
    fi
fi

# Step 4: Setup environment variables
echo ""
echo "Step 4: Configuring environment variables..."
echo "----------------------------------------"

# Detect Android SDK location
if [ -d "$HOME/Android/Sdk" ]; then
    ANDROID_SDK_ROOT="$HOME/Android/Sdk"
elif [ -d "$HOME/android-sdk" ]; then
    ANDROID_SDK_ROOT="$HOME/android-sdk"
else
    ANDROID_SDK_ROOT="$HOME/Android/Sdk"  # Default for future setup
    echo -e "${YELLOW}⚠${NC} Android SDK not found yet. Will set default path."
fi

# Update .bashrc if not already configured
if ! grep -q "ANDROID_HOME" ~/.bashrc; then
    echo "" >> ~/.bashrc
    echo "# Android SDK (added by FairSplit setup)" >> ~/.bashrc
    echo "export ANDROID_HOME=$ANDROID_SDK_ROOT" >> ~/.bashrc
    echo "export PATH=\$PATH:\$ANDROID_HOME/cmdline-tools/latest/bin" >> ~/.bashrc
    echo "export PATH=\$PATH:\$ANDROID_HOME/platform-tools" >> ~/.bashrc
    echo "export PATH=\$PATH:\$ANDROID_HOME/emulator" >> ~/.bashrc
    echo -e "${GREEN}✓${NC} Environment variables added to ~/.bashrc"
else
    echo -e "${GREEN}✓${NC} Environment variables already configured"
fi

# Export for current session
export ANDROID_HOME=$ANDROID_SDK_ROOT
export PATH=$PATH:$ANDROID_HOME/cmdline-tools/latest/bin
export PATH=$PATH:$ANDROID_HOME/platform-tools

# Step 5: Project-specific setup
echo ""
echo "Step 5: Creating local.properties..."
echo "----------------------------------------"

PROJECT_DIR="/media/abhishek-atole/Data Folder/Developed Application/Expense Tracker"

if [ -d "$PROJECT_DIR" ]; then
    cd "$PROJECT_DIR"
    
    # Create local.properties
    if [ -f "local.properties" ]; then
        echo -e "${GREEN}✓${NC} local.properties already exists"
    else
        echo "sdk.dir=$ANDROID_SDK_ROOT" > local.properties
        echo -e "${GREEN}✓${NC} Created local.properties"
    fi
else
    echo -e "${RED}❌ Project directory not found: $PROJECT_DIR${NC}"
    exit 1
fi

# Step 6: Installation summary
echo ""
echo "=========================================="
echo "✅ Environment Setup Complete!"
echo "=========================================="
echo ""
echo "Installed Components:"
echo "  ✓ OpenJDK 17"
echo "  ✓ Android Studio"
echo "  ✓ Environment variables configured"
echo "  ✓ Project local.properties created"
echo ""
echo "=========================================="
echo "🎯 NEXT STEPS (Manual)"
echo "=========================================="
echo ""
echo "1. Launch Android Studio:"
echo "   $ android-studio"
echo ""
echo "2. Complete Android Studio Setup Wizard:"
echo "   - Choose 'Standard' installation"
echo "   - Wait for SDK components download (~3-5 GB)"
echo "   - Accept Android SDK licenses"
echo ""
echo "3. Open FairSplit Project:"
echo "   File → Open → Select: $PROJECT_DIR"
echo ""
echo "4. Wait for Gradle Sync (automatic)"
echo "   - Android Studio will initialize Gradle wrapper"
echo "   - Downloads dependencies (~200 MB)"
echo "   - Syncs build files"
echo "   - Indexes project"
echo ""
echo "5. Build the Project:"
echo "   Build → Make Project (Ctrl+F9)"
echo ""
echo "6. Run Tests:"
echo "   - In terminal: ./gradlew test"
echo "   - Expected: 9 tests pass"
echo ""
echo "=========================================="
echo "📚 Documentation"
echo "=========================================="
echo ""
echo "For detailed instructions, see:"
echo "  • ENVIRONMENT_SETUP_GUIDE.md"
echo "  • BUILD_VALIDATION_REPORT.md"
echo "  • BUILD_VERIFICATION_GUIDE.md"
echo ""
echo "=========================================="
echo "⏱️  Estimated Time Remaining"
echo "=========================================="
echo ""
echo "  Android Studio setup wizard: 5-10 minutes"
echo "  SDK components download:      10-15 minutes"
echo "  First project sync:           3-5 minutes"
echo "  First build:                  1-2 minutes"
echo "  ----------------------------------------"
echo "  Total:                        ~20-30 minutes"
echo ""
echo "🎉 Setup script completed successfully!"
echo "   Please proceed with Android Studio setup."
echo ""
