import dev.apexstudios.gradle.single.ApexSingleExtension

plugins {
    id("apex-conventions.neoforge") version "0.1.87"
    id("apex-conventions.maven-publishing") version "0.1.87"
}

group = "dev.apexstudios"

apex.neoVersion("26.1.0.0-alpha.1+snapshot-1")
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
    maven("https://maven.apexstudios.dev/prs/Registree/pr17") {
        content {
            includeModule("dev.apexstudios", "registree")
        }
    }

    maven("https://maven.apexstudios.dev/prs/Placement-Visualizer/pr19") {
        content {
            includeModule("dev.apexstudios", "placementvisualizer")
        }
    }
}

dependencies {
    implementation(libs.registree)
    "dataImplementation"(libs.registree)
    jarJar(libs.registree)

    implementation(libs.placementvisualizer)
    "dataImplementation"(libs.placementvisualizer)
    jarJar(libs.placementvisualizer)
}