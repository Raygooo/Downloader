pluginManagement {
    repositories {
        maven { url = uri("https://maven.aliyun.com/repository/central") } // mavenCentral()
        maven { url = uri("https://maven.aliyun.com/repository/google") } // google()
        maven { url = uri("https://maven.aliyun.com/repository/gradle-plugin") } // gradlePlugin
    }

    val kotlinVersion: String by settings
    val androidGradlePluginVersion: String by settings

    plugins {
        kotlin("android") version kotlinVersion
        id("com.android.application") version androidGradlePluginVersion
        id("com.android.library") version androidGradlePluginVersion
    }

    resolutionStrategy {
        eachPlugin {
            val id = requested.id.id
            if (id == "com.android.application" || id == "com.android.library") {
                useModule("com.android.tools.build:gradle:${androidGradlePluginVersion}")
            }
        }
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        maven { url = uri("https://maven.aliyun.com/repository/central") } // mavenCentral()
        maven { url = uri("https://maven.aliyun.com/repository/google") } // google()
        maven { url = uri("https://maven.aliyun.com/repository/gradle-plugin") } // gradlePlugin
        google()
        mavenCentral()
    }
}
rootProject.name = "Downloader"
include(":app")
