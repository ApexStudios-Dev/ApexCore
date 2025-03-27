pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenLocal()

        maven("https://maven.apexstudios.dev/releases")
    }

    resolutionStrategy {
        eachPlugin {
            if(requested.id.namespace == "apex-conventions") {
                useVersion("0.1.58")
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
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.9.0"
}

rootProject.name = "ApexCore"
