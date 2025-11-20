import dev.apexstudios.gradle.ApexExtension
import dev.apexstudios.gradle.single.ApexSingleExtension

plugins {
    id("apex-conventions.neoforge") version "0.1.75"
    id("apex-conventions.maven-publishing") version "0.1.75"
}

group = "dev.apexstudios"

apex.neoVersion("21.11.0-alpha.25w45a.20251119.234730", "1.21.10", "2025.10.12")
apex.extendCompilerErrors()

val single = ApexSingleExtension.getOrCreate(project)
single.withDataGen()

neoForge {
    accessTransformers.from(file("src/${ApexExtension.DATA_NAME}/datagen-at.cfg"))
}

repositories {
    maven("https://maven.apexstudios.dev/prs/Registree/pr10")
    maven("https://maven.apexstudios.dev/prs/PlacementVisualizer/pr13")

    apex.neoPrMaven(this, 2815)
}

dependencies {
    implementation(libs.registree)
    "dataImplementation"(libs.registree)
    jarJar(libs.registree)

    implementation(libs.placementvisualizer)
    "dataImplementation"(libs.placementvisualizer)
    jarJar(libs.placementvisualizer)
}