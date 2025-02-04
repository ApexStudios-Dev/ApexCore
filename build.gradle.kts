import dev.apexstudios.gradle.single.ApexSingleExtension
import me.modmuss50.mpp.ReleaseType

plugins {
    id("apex-conventions.neoforge")
    id("apex-conventions.immaculate")
    id("apex-conventions.maven-publishing")
    id("apex-conventions.mod-publishing")
}

group = "dev.apexstudios"

apex.neoVersion("21.4.50-beta", "2025.01.05")
apex.extendCompilerErrors()

val single = ApexSingleExtension.getOrCreate(project)
single.withDataGen()

neoForge.accessTransformers {
    from(file("src/${SourceSet.MAIN_SOURCE_SET_NAME}/resources/META-INF/accesstransformer-dev.cfg"))
    publish(file("src/${SourceSet.MAIN_SOURCE_SET_NAME}/resources/META-INF/accesstransformer-dev.cfg"))
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

publishMods {
    type = ReleaseType.ALPHA

//    modrinth {
//        projectId = "xl3myxch"
//    }

    curseforge {
        projectId = "550778"
        projectSlug = "apexcore"
    }
}
