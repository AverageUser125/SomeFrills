plugins {
    id("java")
}
repositories {
    mavenCentral()
}
dependencies {
    implementation(gradleApi())
}
java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}
tasks.withType<JavaCompile>().configureEach {
    options.release.set(21)
}
