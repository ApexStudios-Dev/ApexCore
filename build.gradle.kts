plugins {
    id("apex-conventions.neoforge")
    id("apex-conventions.neoforge-datagen")
    id("apex-conventions.maven-publishing")
    id("apex-conventions.jspecify")
}

group = "dev.apexstudios"
neoForge.version = libs.versions.neoforge.get()

repositories {
    /*maven("https://prmaven.neoforged.net/NeoForge/pr3492") {
        content {
            includeModule("net.neoforged", "neoforge")
            includeModule("net.neoforged", "testframework")
        }
    }*/

    maven("https://maven.apexmodder.com/prs/Registree/pr37") {
        content {
            includeModule("dev.apexstudios", "registree")
        }
    }
}

dependencies {
    implementation(libs.registree)
    "dataImplementation"(libs.registree)
    jarJar(libs.registree)
}