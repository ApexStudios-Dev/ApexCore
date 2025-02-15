pluginManagement {
    if(file("../ApexGradle/build.gradle.kts").exists()) {
        includeBuild("../ApexGradle")
    }

    repositories {
        gradlePluginPortal()
        mavenLocal()

        maven("https://maven.apexstudios.dev/releases")

        val GITHUB_ACTOR = providers.gradleProperty("gpr.user").orElse(providers.environmentVariable("GITHUB_ACTOR"))
        val GITHUB_TOKEN = providers.gradleProperty("gpr.token").orElse(providers.environmentVariable("GITHUB_TOKEN"))

        if(GITHUB_ACTOR.isPresent && GITHUB_TOKEN.isPresent) {
            maven("https://maven.pkg.github.com/ApexStudios-Dev/ApexGradle") {
                credentials {
                    username = GITHUB_ACTOR.get()
                    password = GITHUB_TOKEN.get()
                }
            }
        }
    }

    resolutionStrategy {
        eachPlugin {
            if(requested.id.namespace == "apex-conventions") {
                useVersion("[0.1,)")
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
