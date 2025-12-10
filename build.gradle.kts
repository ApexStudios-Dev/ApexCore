import dev.apexstudios.gradle.single.ApexSingleExtension

plugins {
    id("apex-conventions.neoforge") version "0.1.82"
    id("apex-conventions.maven-publishing") version "0.1.82"
}

group = "dev.apexstudios"

apex.neoVersion("21.11.0-beta", "1.21.10", "2025.10.12")
apex.extendCompilerErrors()

val single = ApexSingleExtension.getOrCreate(project)
single.withDataGen()

neoForge {
    accessTransformers {
        from(file("src/${SourceSet.MAIN_SOURCE_SET_NAME}/resources/META-INF/accesstransformer-dev.cfg"))
        publish(file("src/${SourceSet.MAIN_SOURCE_SET_NAME}/resources/META-INF/accesstransformer-dev.cfg"))
    }
}

repositories {
    maven("https://maven.apexstudios.dev/prs/Registree/pr11")
    maven("https://maven.apexstudios.dev/prs/Placement-Visualizer/pr14")
}

dependencies {
    implementation(libs.registree)
    "dataImplementation"(libs.registree)
    jarJar(libs.registree)

    implementation(libs.placementvisualizer)
    "dataImplementation"(libs.placementvisualizer)
    jarJar(libs.placementvisualizer)
}