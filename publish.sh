#!/bin/bash

# Publish script for Rive-CMP-Seekrtech
# This script helps publish the library to GitHub Packages

set -e

# Set JAVA_HOME to Java 17
export JAVA_HOME=$(/usr/libexec/java_home -v 17)

echo "🚀 Publishing Rive-CMP-Seekrtech to GitHub Packages"
echo "=================================================="
echo "Using Java: $JAVA_HOME"
echo ""

# Check if GITHUB_TOKEN is set
if [ -z "$GITHUB_TOKEN" ] && [ -z "$(grep 'GITHUB_TOKEN' local.properties 2>/dev/null)" ] && [ -z "$(grep 'gpr.token' gradle.properties 2>/dev/null)" ]; then
    echo "❌ Error: GITHUB_TOKEN not found!"
    echo "Please set one of the following:"
    echo "  1. Environment variable: export GITHUB_TOKEN=your_token"
    echo "  2. Add to local.properties: GITHUB_TOKEN=your_token"
    echo "  3. Add to gradle.properties: gpr.token=your_token"
    exit 1
fi

# Check if GITHUB_USERNAME is set
if [ -z "$GITHUB_USERNAME" ] && [ -z "$(grep 'GITHUB_USERNAME' local.properties 2>/dev/null)" ] && [ -z "$(grep 'gpr.user' gradle.properties 2>/dev/null)" ]; then
    echo "❌ Error: GITHUB_USERNAME not found!"
    echo "Please set one of the following:"
    echo "  1. Environment variable: export GITHUB_USERNAME=your_username"
    echo "  2. Add to local.properties: GITHUB_USERNAME=your_username"
    echo "  3. Add to gradle.properties: gpr.user=your_username"
    exit 1
fi

echo "✅ GitHub credentials configured"
echo ""

# Clean build
echo "🧹 Cleaning previous builds..."
./gradlew clean

# Build the library
echo "🔨 Building library..."
./gradlew :library:assemble

# Run tests (optional, comment out if you want to skip)
# echo "🧪 Running tests..."
# ./gradlew :library:test

# Publish
echo "📦 Publishing to GitHub Packages..."
./gradlew :library:publish

echo ""
echo "✅ Successfully published!"
echo "=================================================="
echo ""
echo "📝 To use this library in another project, add to settings.gradle.kts:"
echo ""
echo "dependencyResolutionManagement {"
echo "    repositories {"
echo "        google()"
echo "        mavenCentral()"
echo "        maven {"
echo "            url = uri(\"https://maven.pkg.github.com/seekrtech/Rive-CMP-Seekrtech\")"
echo "            credentials {"
echo "                username = project.findProperty(\"gpr.user\") as String? ?: System.getenv(\"GITHUB_USERNAME\")"
echo "                password = project.findProperty(\"gpr.token\") as String? ?: System.getenv(\"GITHUB_TOKEN\")"
echo "            }"
echo "        }"
echo "    }"
echo "}"
echo ""
echo "And add to your build.gradle.kts:"
echo ""
echo "implementation(\"com.seekrtech:rive-cmp-seekrtech:$(grep '^version' library/build.gradle.kts | cut -d'"' -f2)\")"
echo ""

