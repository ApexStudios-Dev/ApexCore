pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://maven.apexmodder.com/releases")
    }

    if(file("../../ApexGradle").exists()) {
        includeBuild("../../ApexGradle")
    } else {
        resolutionStrategy {
            eachPlugin {
                if(requested.id.namespace == "apex-conventions") {
                    useVersion("0.1.94")
                }
            }
        }
    }
}

dependencyResolutionManagement {
    versionCatalogs.create("libs") {
        version("neoforge", "26.1.2.109-beta-pr-3073-feat-26.1-new-mods-list-screen")

        library("registree", "dev.apexstudios", "registree").version("26.1.0")
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

listOf(
    "Registree"
).forEach { lib ->
    if(file("../../${lib}/26.1").exists()) {
        includeBuild("../../${lib}/26.1") {
            name = lib

            dependencySubstitution {
                substitute(module("dev.apexstudios:${lib.lowercase()}"))
                    .using(project(":"))
            }
        }
    }
}

rootProject.name = "ApexCore"
