plugins {
    `java-library`
    `maven-publish`

    id("net.neoforged.moddev")
    id("apex-conventions.jspecify")
}

group = "dev.apexstudios.apexcore"
base.archivesName = "xplat"
version = providers.environmentVariable("VERSION").getOrElse("0.0NONE")

sourceSets.main {
    resources {
        exclude(".cache")
        srcDir("src/data/generated")
    }
}

neoForge {
    neoFormVersion = libs.versions.neoform.get()
    validateAccessTransformers.set(false)

    /*accessTransformers {
        from(file("src/${SourceSet.MAIN_SOURCE_SET_NAME}/resources/META-INF/accesstransformer-xplat.cfg"))
        publish(file("src/${SourceSet.MAIN_SOURCE_SET_NAME}/resources/META-INF/accesstransformer-xplat.cfg"))
    }*/
}

dependencies {
    implementation(libs.registree.xplat) { isTransitive = false }
    accessTransformers(libs.registree.xplat) { isTransitive = false }
}

java {
    toolchain.vendor.set(JvmVendorSpec.JETBRAINS)
    withSourcesJar()
}

publishing {
    publications.create("release", MavenPublication::class.java) {
        afterEvaluate {
            groupId = project.group as String
            artifactId = project.name
            version = project.version as String
        }

        from(components["java"])
    }

    repositories {
        if(System.getenv("MAVEN_USERNAME") != null && System.getenv("MAVEN_PASSWORD") != null) {
            maven("https://maven.apexmodder.com/releases") {
                name = "ApexStudios-Releases"

                credentials {
                    username = System.getenv("MAVEN_USERNAME")
                    password = System.getenv("MAVEN_PASSWORD")
                }

                authentication.create<BasicAuthentication>("basic")
            }
        }
    }
}