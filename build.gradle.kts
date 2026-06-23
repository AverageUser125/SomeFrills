import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("net.fabricmc.fabric-loom") version "1.15-SNAPSHOT"
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
    implementation("net.fabricmc:fabric-loader:${project.extra["loader_version"]}")

    // Internal dependencies
    implementation("meteordevelopment:orbit:${project.extra["orbit_version"]}")
    include("meteordevelopment:orbit:${project.extra["orbit_version"]}")
    implementation("org.notenoughupdates.moulconfig:modern-26.1:${project.extra["moulconfig_version"]}")
    include("org.notenoughupdates.moulconfig:modern-26.1:${project.extra["moulconfig_version"]}")
    implementation("moe.nea:libautoupdate:${project.extra["libautoupdate_version"]}")
    include("moe.nea:libautoupdate:${project.extra["libautoupdate_version"]}")

    // External dependencies
    implementation("net.fabricmc.fabric-api:fabric-api:${project.extra["fabricapi_version"]}")
    implementation("com.terraformersmc:modmenu:${project.extra["modmenu_version"]}")
    implementation("net.fabricmc:fabric-language-kotlin:${project.extra["fabric_kotlin_version"]}")

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
    options.release.set(25)
    options.compilerArgs.add("-Xlint:all,-processing,-this-escape,-classfile")
}

java {
    // Loom will automatically attach sourcesJar to a RemapSourcesJar task and to the "build" task
    // if it is present.
    // If you remove this line, sources will not be generated.
    withSourcesJar()

    sourceCompatibility = JavaVersion.VERSION_25
    targetCompatibility = JavaVersion.VERSION_25
}

kotlin {
    jvmToolchain(25)
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
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_25)
        freeCompilerArgs.add("-jvm-default=enable")
    }
}

subprojects {
    apply(plugin = "java")

    java {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(25))
        }
    }

    tasks.withType<JavaCompile>().configureEach {
        // Force the Java compiler to use the toolchain version
        options.release.set(25)
    }
}

tasks.named("compileKotlin") {
    dependsOn("kspKotlin")
}