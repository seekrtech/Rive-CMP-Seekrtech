# Publishing Guide for Rive-CMP-Seekrtech

This guide explains how to publish the Rive-CMP-Seekrtech library to GitHub Packages.

## Prerequisites

### 1. Java 17
Ensure you have Java 17 installed:
```bash
java -version
```

If not installed, download from [Adoptium](https://adoptium.net/) or use:
```bash
brew install openjdk@17
```

### 2. Xcode
This project requires Xcode 16+ (currently using Xcode 18.5 SDK) for iOS builds.

### 3. GitHub Personal Access Token
Create a GitHub Personal Access Token with the following permissions:
- `write:packages` (to publish packages)
- `read:packages` (to download packages)

To create a token:
1. Go to GitHub Settings → Developer settings → Personal access tokens → Tokens (classic)
2. Click "Generate new token (classic)"
3. Select the scopes mentioned above
4. Copy the token (you won't be able to see it again!)

### 4. Configure Credentials

Choose one of the following methods:

#### Option 1: local.properties (Recommended for local development)
Create or edit `local.properties` in the project root:
```properties
GITHUB_USERNAME=your-username
GITHUB_TOKEN=your-token
sdk.dir=/path/to/android/sdk
```

#### Option 2: gradle.properties
Add to `~/.gradle/gradle.properties` or project's `gradle.properties`:
```properties
gpr.user=your-username
gpr.token=your-token
```

#### Option 3: Environment Variables (Recommended for CI/CD)
```bash
export GITHUB_USERNAME=your-username
export GITHUB_TOKEN=your-token
```

## Publishing

### Method 1: Using the publish.sh script (Easiest)

```bash
# Make sure you're in the project root
cd /path/to/Rive-CMP-Seekrtech

# Set JAVA_HOME (required)
export JAVA_HOME=$(/usr/libexec/java_home -v 17)

# Run the publish script
./publish.sh
```

### Method 2: Using Gradle directly

```bash
# Set JAVA_HOME
export JAVA_HOME=$(/usr/libexec/java_home -v 17)

# Clean previous builds
./gradlew clean

# Build the library
./gradlew :library:assemble

# Publish to GitHub Packages
./gradlew :library:publish
```

### Method 3: GitHub Actions (Automated)

The repository includes a GitHub Actions workflow that automatically publishes when you push a tag:

```bash
# Create a version tag
git tag v0.0.6.1_alpha

# Push the tag
git push origin v0.0.6.1_alpha
```

Or manually trigger the workflow from the GitHub Actions tab.

## Troubleshooting

### "Unable to locate a Java Runtime"
Make sure to set JAVA_HOME before running Gradle:
```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 17)
```

### "cinteropNativeIosShared failed"
This usually happens with Xcode version mismatches. Make sure you're using:
- Kotlin 2.2.0+ (supports Xcode 18.5)
- Latest Xcode Command Line Tools: `xcode-select --install`

### "Authentication failed"
Verify your GitHub credentials:
- Token has `write:packages` permission
- Username and token are correctly set in one of the configuration methods
- Token hasn't expired

### Configuration Cache Issues
If you encounter configuration cache errors, make sure it's disabled in `gradle.properties`:
```properties
# org.gradle.configuration-cache=true  # Should be commented out
```

## Updating the Version

To publish a new version:

1. Update the version in `library/build.gradle.kts`:
```kotlin
version = "0.0.7"  // Change this
```

2. Update README.md with the new version
3. Commit the changes
4. Publish using one of the methods above

## Verifying Publication

After publishing, you can verify the package on GitHub:
1. Go to https://github.com/seekrtech/Rive-CMP-Seekrtech/packages
2. You should see the published package with the correct version

## Using the Published Library

Other projects can use this library by adding to their `settings.gradle.kts`:

```kotlin
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven {
            url = uri("https://maven.pkg.github.com/seekrtech/Rive-CMP-Seekrtech")
            credentials {
                username = project.findProperty("gpr.user") as String? ?: System.getenv("GITHUB_USERNAME")
                password = project.findProperty("gpr.token") as String? ?: System.getenv("GITHUB_TOKEN")
            }
        }
    }
}
```

And in their `build.gradle.kts`:

```kotlin
dependencies {
    implementation("com.seekrtech:rive-cmp-seekrtech:0.0.6.1_alpha")
}
```

## Notes

- Publishing requires authentication for both writing (publishing) and reading (dependencies)
- GitHub Packages don't support anonymous access, so users will also need GitHub credentials
- The library includes Android (AAR) and iOS (Framework) artifacts
- Documentation (Javadoc) is automatically generated via Dokka

