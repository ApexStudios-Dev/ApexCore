import dev.apexstudios.gradle.single.ApexSingleExtension

plugins {
    id("apex-conventions.neoforge") version "0.1.74"
    id("apex-conventions.maven-publishing") version "0.1.74"
}

group = "dev.apexstudios"

apex.neoVersion("21.9.0-alpha.25w34b.20250827.140505", "1.21.8", "2025.07.20")
apex.extendCompilerErrors()

val single = ApexSingleExtension.getOrCreate(project)
single.withDataGen()

neoForge {
    accessTransformers {
        from(file("src/${SourceSet.MAIN_SOURCE_SET_NAME}/resources/META-INF/accesstransformer-dev.cfg"))
        publish(file("src/${SourceSet.MAIN_SOURCE_SET_NAME}/resources/META-INF/accesstransformer-dev.cfg"))
    }
}

dependencies {
    implementation(libs.mixinextras.expressions)
    "dataImplementation"(libs.mixinextras.expressions)
    annotationProcessor(libs.mixinextras.expressions)
    jarJar(libs.mixinextras.expressions) {
        artifact {
            classifier = "slim"
        }
    }
}
