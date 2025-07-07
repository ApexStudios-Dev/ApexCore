import dev.apexstudios.gradle.single.ApexSingleExtension

plugins {
    id("apex-conventions.neoforge") version "0.1.69"
    id("apex-conventions.immaculate") version "0.1.69"
    id("apex-conventions.maven-publishing") version "0.1.69"
}

group = "dev.apexstudios"

apex.neoVersion("21.6.16-beta", "1.21.5", "2025.06.15")
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
