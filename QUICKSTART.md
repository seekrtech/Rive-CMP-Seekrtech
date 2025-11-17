# Quick Start Guide

## 🚀 发布到 GitHub Packages（首次发布）

### 1️⃣ 确认环境

```bash
# 检查 Java 版本（需要 Java 17）
java -version

# 检查 Xcode 版本（需要 16+）
xcodebuild -version
```

### 2️⃣ 配置 GitHub 认证

在项目根目录的 `local.properties` 文件中添加：

```properties
GITHUB_USERNAME=your-github-username
GITHUB_TOKEN=your-github-personal-access-token
```

**注意：** `local.properties` 已在 `.gitignore` 中，不会被提交到 Git。

### 3️⃣ 发布

```bash
# 方法 1：使用发布脚本（推荐）
./publish.sh

# 方法 2：使用 Gradle
export JAVA_HOME=$(/usr/libexec/java_home -v 17)
./gradlew :library:publish
```

就这么简单！🎉

---

## 📦 在其他项目中使用

### 1️⃣ 配置仓库

在你的项目的 `settings.gradle.kts` 中添加：

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

### 2️⃣ 添加依赖

在你的 `build.gradle.kts` 中：

```kotlin
dependencies {
    implementation("com.seekrtech:rive-cmp-seekrtech:0.0.6.1_alpha")
}
```

### 3️⃣ 配置认证

在使用项目的 `gradle.properties` 或 `local.properties` 中添加：

```properties
gpr.user=your-github-username
gpr.token=your-github-token
```

---

## 🔄 更新版本

### 1️⃣ 修改版本号

编辑 `library/build.gradle.kts`：

```kotlin
version = "0.0.7"  // 改成新版本号
```

### 2️⃣ 更新 README

更新 README.md 中的版本示例。

### 3️⃣ 提交并发布

```bash
# 提交更改
git add .
git commit -m "Bump version to 0.0.7"

# 创建版本标签
git tag v0.0.7
git push origin main
git push origin v0.0.7

# 本地发布
./publish.sh
```

---

## 🐛 常见问题

### "Unable to locate a Java Runtime"

```bash
# 设置 JAVA_HOME
export JAVA_HOME=$(/usr/libexec/java_home -v 17)
```

### "cinterop 失败"

确保你使用的是：
- Kotlin 2.2.0+
- Xcode 16+
- 最新的 Command Line Tools

```bash
xcode-select --install
```

### "Authentication failed"

检查：
1. GitHub Token 是否有 `write:packages` 权限
2. Username 和 Token 是否正确配置
3. Token 是否过期

---

## 📚 更多信息

- 详细发布指南：[PUBLISHING.md](PUBLISHING.md)
- 迁移总结：[MIGRATION_SUMMARY.md](MIGRATION_SUMMARY.md)
- 完整文档：[README.md](README.md)

---

## ✅ 测试清单

发布前确认：

```bash
# 1. 清理构建
export JAVA_HOME=$(/usr/libexec/java_home -v 17)
./gradlew clean

# 2. 构建库
./gradlew :library:assemble

# 3. 测试发布（dry-run）
./gradlew :library:publish --dry-run

# 4. 实际发布
./gradlew :library:publish
```

---

## 🎯 核心命令速查

```bash
# 发布库
./publish.sh

# 清理构建
./gradlew clean

# 构建所有目标
./gradlew build

# 只构建 Android
./gradlew :library:assembleRelease

# 只构建 iOS
./gradlew :library:linkReleaseFrameworkIosArm64

# 检查依赖
./gradlew :library:dependencies

# 发布测试
./gradlew :library:publish --dry-run
```

---

**注意：** 所有 Gradle 命令都需要先设置 JAVA_HOME：
```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 17)
```

或者直接使用 `publish.sh` 脚本，它会自动设置。

