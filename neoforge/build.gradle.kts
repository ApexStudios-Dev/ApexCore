import org.gradle.internal.extensions.stdlib.capitalized
import org.slf4j.event.Level

plugins {
    `java-library`
    `maven-publish`

    id("net.neoforged.moddev")
    id("apex-conventions.jspecify")
}

val xplat = evaluationDependsOn(":xplat")

group = "dev.apexstudios.apexcore"
base.archivesName = "neoforge"
version = providers.environmentVariable("VERSION").getOrElse("0.0NONE")

sourceSets {
    create("data") {
        java.setSrcDirs(xplat.files("src/data/java"))
        resources.setSrcDirs(files())

        compileClasspath += sourceSets[SourceSet.MAIN_SOURCE_SET_NAME].output
        runtimeClasspath += sourceSets[SourceSet.MAIN_SOURCE_SET_NAME].output
    }
}

neoForge {
    version = libs.versions.neoforge.get()
    validateAccessTransformers.set(true)
    addModdingDependenciesTo(sourceSets["data"])

    accessTransformers {
        from(
            file("src/${SourceSet.MAIN_SOURCE_SET_NAME}/resources/META-INF/accesstransformer.cfg"),
            file("src/${SourceSet.MAIN_SOURCE_SET_NAME}/resources/META-INF/accesstransformer-dev.cfg")
        )

        publish(file("src/${SourceSet.MAIN_SOURCE_SET_NAME}/resources/META-INF/accesstransformer.cfg"))
        publish(file("src/${SourceSet.MAIN_SOURCE_SET_NAME}/resources/META-INF/accesstransformer-dev.cfg"))
    }

    mods {
        create(SourceSet.MAIN_SOURCE_SET_NAME) {
            sourceSet(sourceSets[SourceSet.MAIN_SOURCE_SET_NAME])
            sourceSet(xplat.sourceSets[SourceSet.MAIN_SOURCE_SET_NAME])
        }

        create("data") {
            sourceSet(sourceSets[SourceSet.MAIN_SOURCE_SET_NAME])
            sourceSet(xplat.sourceSets[SourceSet.MAIN_SOURCE_SET_NAME])
            sourceSet(sourceSets["data"])
        }
    }

    runs {
        listOf(true, false).forEach { isClient ->
            val id = if(isClient) "client" else "server"

            create(id) {
                if(isClient) {
                    client()
                } else {
                    server()
                }

                ideName.set("${id.capitalized()} (:neoforge)")
                logLevel.set(Level.DEBUG)
                gameDirectory.set(layout.projectDirectory.dir("run/$id"))
                sourceSet.set(sourceSets[SourceSet.MAIN_SOURCE_SET_NAME])
                loadedMods.set(listOf(mods[SourceSet.MAIN_SOURCE_SET_NAME]))
                systemProperty("terminal.ansi", "true") // fix terminal not having colors

                jvmArguments.addAll(
                    "-XX:+AllowEnhancedClassRedefinition",
                    "-XX:+IgnoreUnrecognizedVMOptions",
                    "-XX:+AllowRedefinitionToAddDeleteMethods",
                    "-XX:+ClassUnloading"
                )
            }
        }

        create("data") {
            clientData()

            ideName.set("Data")
            logLevel.set(Level.DEBUG)
            gameDirectory.set(layout.projectDirectory.dir("run/data"))
            sourceSet.set(sourceSets["data"])
            loadedMods.set(listOf(mods["data"]))
            systemProperty("terminal.ansi", "true") // fix terminal not having colors

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

dependencies {
    compileOnly(dependencyFactory.create(xplat)) { isTransitive = false }
    accessTransformers(dependencyFactory.create(xplat)) { isTransitive = false }

    compileOnly(libs.registree.xplat) { isTransitive = false }
    implementation(libs.registree.neoforge) { isTransitive = false }
    "dataImplementation"(libs.registree.neoforge) { isTransitive = false }
    jarJar(libs.registree.neoforge) { isTransitive = false }
}

java {
    toolchain.vendor.set(JvmVendorSpec.JETBRAINS)
    withSourcesJar()
}

tasks.named(sourceSets[SourceSet.MAIN_SOURCE_SET_NAME].compileJavaTaskName, JavaCompile::class.java) {
    source(xplat.sourceSets[SourceSet.MAIN_SOURCE_SET_NAME].allJava)
}

tasks.named(sourceSets[SourceSet.MAIN_SOURCE_SET_NAME].javadocTaskName, Javadoc::class.java).configure {
    source(xplat.sourceSets[SourceSet.MAIN_SOURCE_SET_NAME].allJava)
}

tasks.named(sourceSets[SourceSet.MAIN_SOURCE_SET_NAME].sourcesJarTaskName, Jar::class.java) {
    from(xplat.sourceSets[SourceSet.MAIN_SOURCE_SET_NAME].allSource)
}

tasks.named(sourceSets[SourceSet.MAIN_SOURCE_SET_NAME].processResourcesTaskName, ProcessResources::class.java) {
    from(xplat.sourceSets[SourceSet.MAIN_SOURCE_SET_NAME].resources)
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