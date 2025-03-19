import dev.apexstudios.gradle.single.ApexSingleExtension

plugins {
    id("apex-conventions.neoforge")
    id("apex-conventions.immaculate")
    id("apex-conventions.maven-publishing")
}

group = "dev.apexstudios"

apex.neoVersion("21.5.0-alpha.25w09a.20250319.011826", "1.21.4", "2025.02.16")
apex.extendCompilerErrors()

val single = ApexSingleExtension.getOrCreate(project)
single.withDataGen()

neoForge {
    validateAccessTransformers.set(false)

    accessTransformers {
        from(file("src/${SourceSet.MAIN_SOURCE_SET_NAME}/resources/META-INF/accesstransformer-dev.cfg"))
        publish(file("src/${SourceSet.MAIN_SOURCE_SET_NAME}/resources/META-INF/accesstransformer-dev.cfg"))
    }
}

repositories {
    maven("https://maven.apexstudios.dev/private")
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
