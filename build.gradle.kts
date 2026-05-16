import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("fabric-loom") version "1.15.5"
    id("maven-publish")
    id("org.jetbrains.kotlin.jvm") version "2.3.0"
    id("com.google.devtools.ksp") version "2.3.8"
    java
}

version = project.extra["mod_version"].toString()
group = project.extra["maven_group"].toString()

base {
    archivesName.set(project.extra["archives_base_name"].toString())
}

repositories {
    maven { url = uri("https://maven.terraformersmc.com/releases") }

    maven {
        name = "meteor-maven"
        url = uri("https://maven.meteordev.org/releases")
    }

    maven {
        name = "daqem-maven"
        url = uri("https://maven.daqem.com/releases")
    }
    // Repository for NotEnoughUpdates / MoulConfig
    maven {
        name = "NEU"
        url = uri("https://maven.notenoughupdates.org/releases/")
    }

    maven {
        name = "NEA"
        url = uri("https://repo.nea.moe/releases")
    }
    flatDir {
        dirs("vendor")
    }
}

loom {
    mods {
        create("somefrills") {
            sourceSet(sourceSets["main"])
        }
    }
    accessWidenerPath.set(file("src/main/resources/somefrills.accesswidener"))
}

dependencies {
    // To change the versions see the gradle.properties file
    minecraft("com.mojang:minecraft:${project.extra["minecraft_version"]}")
    mappings("net.fabricmc:yarn:${project.extra["yarn_mappings"]}:v2")
    modImplementation("net.fabricmc:fabric-loader:${project.extra["loader_version"]}")

    // Fabric API. This is technically optional, but you probably want it anyway.
    modImplementation("net.fabricmc.fabric-api:fabric-api:${project.extra["fabric_version"]}")

    modImplementation("meteordevelopment:orbit:${project.extra["orbit_version"]}")
    include("meteordevelopment:orbit:${project.extra["orbit_version"]}")
    // MoulConfig (artifact uses mapping suffix modern-1.21.11)
    modImplementation("org.notenoughupdates.moulconfig:modern-1.21.11:${project.extra["moulconfig_version"]}")
    include("org.notenoughupdates.moulconfig:modern-1.21.11:${project.extra["moulconfig_version"]}")
    modImplementation("net.fabricmc:fabric-language-kotlin:1.13.11+kotlin.2.3.21")

    compileOnly(files("vendor/SkyHanni-7.19.0-mc1.21.11.jar"))
    modImplementation("moe.nea:libautoupdate:1.3.1")
    include("moe.nea:libautoupdate:1.3.1")

    modImplementation("com.terraformersmc:modmenu:${project.extra["modmenu_version"]}")
    modCompileOnly(files("vendor/SkyHanni-7.19.0-mc1.21.11.jar"))

    ksp(project(":processor"))
}

tasks.processResources {
    inputs.property("version", project.version)

    filesMatching("fabric.mod.json") {
        expand(mapOf("version" to inputs.properties["version"]))
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.release.set(21)
    options.compilerArgs.add("-Xlint:all,-processing,-this-escape,-classfile")
}

java {
    // Loom will automatically attach sourcesJar to a RemapSourcesJar task and to the "build" task
    // if it is present.
    // If you remove this line, sources will not be generated.
    withSourcesJar()

    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

kotlin {
    jvmToolchain(21)
}

tasks.jar {
    inputs.property("archivesName", project.base.archivesName)

    from("LICENSE") {
        rename { "${it}_${inputs.properties["archivesName"]}" }
    }
}

tasks.withType<KotlinCompile>().configureEach {
    dependsOn("kspKotlin")
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21)
        freeCompilerArgs.add("-jvm-default=enable")
    }
}

// configure the maven publication
publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            artifactId = project.extra["archives_base_name"].toString()
            from(components["java"])
        }
    }

    // See https://docs.gradle.org/current/userguide/publishing_maven.html for information on how to set up publishing.
    repositories {
        // Add repositories to publish to here.
        // Notice: This block does NOT have the same function as the block in the top level.
        // The repositories here will be used for publishing your artifact, not for
        // retrieving dependencies.
    }
}

tasks.named("compileKotlin") {
    dependsOn("kspKotlin")
}