import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("fabric-loom") version "1.17.3"
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

    // Internal dependencies
    modImplementation("meteordevelopment:orbit:${project.extra["orbit_version"]}")
    include("meteordevelopment:orbit:${project.extra["orbit_version"]}")
    modImplementation("org.notenoughupdates.moulconfig:modern-1.21.11:${project.extra["moulconfig_version"]}")
    include("org.notenoughupdates.moulconfig:modern-1.21.11:${project.extra["moulconfig_version"]}")
    modImplementation("moe.nea:libautoupdate:${project.extra["libautoupdate_version"]}")
    include("moe.nea:libautoupdate:${project.extra["libautoupdate_version"]}")


    // External mods we optionally modify
    compileOnly(files("vendor/SkyHanni-7.19.0-mc1.21.11.jar"))
    modCompileOnly(files("vendor/SkyHanni-7.19.0-mc1.21.11.jar"))
    compileOnly(files("vendor/skyblock_enhancements-1.0.1+1.21.11.jar"))
    modCompileOnly(files("vendor/skyblock_enhancements-1.0.1+1.21.11.jar"))
    compileOnly(files("vendor/SkyOcean-1.21.11-1.16.1.jar"))
    modCompileOnly(files("vendor/SkyOcean-1.21.11-1.16.1.jar"))

    // External dependencies
    modImplementation("net.fabricmc.fabric-api:fabric-api:${project.extra["fabric_version"]}")
    modImplementation("com.terraformersmc:modmenu:${project.extra["modmenu_version"]}")
    modImplementation("net.fabricmc:fabric-language-kotlin:${project.extra["fabric_kotlin_version"]}")

    ksp(project(":processor"))
}

ksp {
    arg("frills.cache.dir", layout.buildDirectory.dir("ksp-frills-cache").get().asFile.absolutePath)
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


tasks.named("compileKotlin") {
    dependsOn("kspKotlin")
}