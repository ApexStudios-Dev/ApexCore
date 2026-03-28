pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://maven.fabricmc.net")
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
        version("neoforge", "26.1.0.7-beta")
        version("neoform", "26.1-1")

        library("minecraft", "com.mojang", "minecraft").version("26.1")

        library("fabric-loader", "net.fabricmc", "fabric-loader").version("0.18.4")
        library("fabric-api", "net.fabricmc.fabric-api", "fabric-api").version("0.144.0+26.1")
        bundle("fabric", listOf("fabric-loader", "fabric-api"))

        library("devlogin", "net.covers1624", "DevLogin").version("0.1.0.5")

        version("registree", "26.1.8-beta-pr-24")
        library("registree-xplat", "dev.apexstudios.registree", "xplat").versionRef("registree")
        library("registree-neoforge", "dev.apexstudios.registree", "neoforge").versionRef("registree")
        library("registree-fabric", "dev.apexstudios.registree", "fabric").versionRef("registree")

        library("modmenu", "com.terraformersmc", "modmenu").version("18.0.0-alpha.8")
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

gradle.beforeProject {
    repositories {
        maven("https://maven.apexmodder.com/releases")

        maven("https://maven.apexmodder.com/prs/Registree/pr24") {
            content {
                includeModule("dev.apexstudios.registree", "fabric")
                includeModule("dev.apexstudios.registree", "neoforge")
                includeModule("dev.apexstudios.registree", "xplat")
            }
        }
    }
}

include("xplat")
include("neoforge")
include("fabric")

listOf(
    "Registree"
).forEach { lib ->
    if(file("../../${lib}/26.1").exists()) {
        includeBuild("../../${lib}/26.1") {
            name = lib

            dependencySubstitution {
                substitute(module("dev.apexstudios.${lib.lowercase()}:xplat")).using(project(":xplat"))
                substitute(module("dev.apexstudios.${lib.lowercase()}:neoforge")).using(project(":neoforge"))
                substitute(module("dev.apexstudios.${lib.lowercase()}:fabric")).using(project(":fabric"))
            }
        }
    }
}

rootProject.name = "ApexCore"
