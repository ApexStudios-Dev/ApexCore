pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://maven.apexstudios.dev/releases")
        maven("https://maven.apexstudios.dev/private")
    }
}

dependencyResolutionManagement {
    versionCatalogs.create("libs") {
        library("registree", "dev.apexstudios", "registree").version("21.10.8")
        library("placementvisualizer", "dev.apexstudios", "placementvisualizer").version("21.10.9")
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

rootProject.name = "ApexCore"
