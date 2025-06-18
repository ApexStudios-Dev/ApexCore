pluginManagement {
    repositories {
        maven("https://maven.apexstudios.dev/private")
        gradlePluginPortal()
        mavenLocal()
    }

    resolutionStrategy {
        eachPlugin {
            if(requested.id.namespace == "apex-conventions") {
                useVersion("0.1.67")
            }
        }
    }
}

dependencyResolutionManagement {
    versionCatalogs.create("libs") {
        library("mixinextras.expressions", "io.github.llamalad7", "mixinextras-neoforge").version {
            strictly("[0.5.0-beta.4,)")
        }
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

rootProject.name = "ApexCore"
