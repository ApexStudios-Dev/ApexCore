pluginManagement {
    repositories {
        maven("https://maven.apexstudios.dev/proxy")
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    versionCatalogs.create("libs") {
        library("registree", "dev.apexstudios", "registree").version("26.1.0-beta-pr-17")
        library("placementvisualizer", "dev.apexstudios", "placementvisualizer").version("26.1.1-beta-pr-19")
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

rootProject.name = "ApexCore"
