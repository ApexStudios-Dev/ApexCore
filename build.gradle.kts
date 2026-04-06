import java.text.SimpleDateFormat
import java.util.*

plugins {
    `java-library`
    `maven-publish`

    id("net.neoforged.moddev") version "2.0.141"
    id("apex-conventions.jspecify")
}

group = "dev.apexstudios"
base.archivesName = "apexcore"
version = providers.environmentVariable("VERSION").getOrElse("0.0NONE")

sourceSets {
    main {
        resources {
            exclude(".cache")
            srcDir("src/data/generated")
        }
    }

    create("data") {
        resources.setSrcDirs(files())

        compileClasspath += sourceSets[SourceSet.MAIN_SOURCE_SET_NAME].output
        runtimeClasspath += sourceSets[SourceSet.MAIN_SOURCE_SET_NAME].output
    }
}

neoForge {
    version = libs.versions.neoforge.get()
    addModdingDependenciesTo(sourceSets["data"])

    accessTransformers {
        from(
            file("src/${SourceSet.MAIN_SOURCE_SET_NAME}/resources/META-INF/accesstransformer.cfg"),
            file("src/${SourceSet.MAIN_SOURCE_SET_NAME}/resources/META-INF/accesstransformer-dev.cfg")
        )

        publish(file("src/${SourceSet.MAIN_SOURCE_SET_NAME}/resources/META-INF/accesstransformer.cfg"))
        publish(file("src/${SourceSet.MAIN_SOURCE_SET_NAME}/resources/META-INF/accesstransformer-dev.cfg"))
    }

    mods.create("data") {
        sourceSet(sourceSets[SourceSet.MAIN_SOURCE_SET_NAME])
        sourceSet(sourceSets["data"])
    }

    runs {
        create("client") {
            client()
        }

        create("data") {
            clientData()

            sourceSet.set(sourceSets["data"])
            loadedMods.set(listOf(mods["data"]))

            programArguments.addAll(
                "--mod", "apexcore",
                "--all",
                "--output", file("src/data/generated").absolutePath,
                "--existing", file("src/${SourceSet.MAIN_SOURCE_SET_NAME}/resources").absolutePath
            )
        }
    }
}

java {
    toolchain.vendor.set(JvmVendorSpec.JETBRAINS)
    withSourcesJar()
}

repositories {
    maven("https://maven.apexmodder.com/releases")
}

dependencies {
    implementation(libs.registree)
    "dataImplementation"(libs.registree)
    jarJar(libs.registree)
}

tasks.withType(Jar::class.java) {
    manifest {
        attributes["Specification-Title"] = project.name
        attributes["Specification-Vendor"] = "ApexStudios"
        attributes["Specification-Version"] = "1"

        attributes["Implementation-Title"] = project.name
        attributes["Implementation-Vendor"] = "ApexStudios"
        attributes["Implementation-Version"] = project.version
        attributes["Implementation-Timestamp"] = SimpleDateFormat("dd-MM-yyyy'T'HH:mm:ssZ").format(Date())

        attributes["Minecraft-Version"] = neoForge.minecraftVersion
        attributes["NeoForge-Version"] = neoForge.version
    }
}

publishing {
    publications.create("release", MavenPublication::class.java) {
        afterEvaluate {
            groupId = "dev.apexstudios"
            artifactId = "apexcore"
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