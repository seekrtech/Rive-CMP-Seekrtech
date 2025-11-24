@file:OptIn(ExperimentalSpmForKmpFeature::class)

import io.github.frankois944.spmForKmp.utils.ExperimentalSpmForKmpFeature
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.net.URI
import java.util.Properties
import java.io.FileInputStream

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.vanniktech.mavenPublish)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.spmForKmp)
    alias(libs.plugins.dokka)
    `maven-publish`
}

// Load local.properties
val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    localProperties.load(FileInputStream(localPropertiesFile))
}

group = "com.seekrtech"
version = "0.0.6.5_alpha"
kotlin {
    androidTarget {
        publishLibraryVariants("release")
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }
    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach {
        it.binaries.framework {
            baseName = "RiveCMP"
            isStatic = true
        }
        it.compilations {
            val main by getting {
                cinterops.create("nativeIosShared")
            }
        }
    }

    sourceSets {
        androidMain.dependencies {
            implementation(compose.preview)
            implementation(libs.rive.android)
            api(libs.androidx.startup)
        }

        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.uiToolingPreview)
        }
        val commonTest by getting {
            dependencies {
                implementation(libs.kotlin.test)
            }
        }
    }
}

android {
    namespace = "com.seekrtech.rivecmp"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

publishing {
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/seekrtech/Rive-CMP-Seekrtech")
            credentials {
                username = localProperties.getProperty("GITHUB_USERNAME")
                    ?: project.findProperty("GITHUB_USERNAME") as String? 
                    ?: project.findProperty("gpr.user") as String? 
                    ?: System.getenv("GITHUB_USERNAME")
                password = localProperties.getProperty("GITHUB_TOKEN")
                    ?: project.findProperty("GITHUB_TOKEN") as String? 
                    ?: project.findProperty("gpr.token") as String? 
                    ?: System.getenv("GITHUB_TOKEN")
            }
        }
    }
}

mavenPublishing {
    coordinates(group.toString(), "rive-cmp-seekrtech", version.toString())

    pom {
        name = "Rive CMP Seekrtech"
        description =
            "A Compose Multiplatform wrapper library for integrating Rive animations, providing a unified API to use rive-android and rive-ios seamlessly across Android and iOS platforms. Forked and maintained by Seekrtech."
        inceptionYear = "2025"
        url = "https://github.com/seekrtech/Rive-CMP-Seekrtech"
        licenses {
            license {
                name = "The Apache License, Version 2.0"
                url = "http://www.apache.org/licenses/LICENSE-2.0.txt"
            }
        }
        developers {
            developer {
                id = "seekrtech"
                name = "Seekrtech"
                url = "https://github.com/seekrtech"
            }
        }
        scm {
            url = "https://github.com/seekrtech/Rive-CMP-Seekrtech"
            connection = "scm:git:git://github.com/seekrtech/Rive-CMP-Seekrtech.git"
            developerConnection = "scm:git:ssh://github.com/seekrtech/Rive-CMP-Seekrtech.git"
        }
    }
}

swiftPackageConfig {
    create("nativeIosShared") {
        minIos = "14.0"
        spmWorkingPath =
            "${projectDir.resolve("SPM")}" // change the Swift Package Manager working Dir
        exportedPackageSettings { includeProduct = listOf("RiveRuntime") }
        dependency {
            remotePackageVersion(
                url = URI("https://github.com/rive-app/rive-ios.git"),
                version = "6.11.1",
                products = {
                    add("RiveRuntime")
                },
            )
        }
    }
}

dependencies {
    debugImplementation(compose.uiTooling)
}
