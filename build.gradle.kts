plugins {
    kotlin("jvm") version "2.1.20"
}

group = "dev.uraxys.idleclient"
version = "1.0"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))

    // Java Image Scaling
    implementation("com.mortennobel:java-image-scaling:0.8.6")

    // ImageIO & WebP plugin
    implementation("com.twelvemonkeys.imageio:imageio-core:3.10.1")
    implementation("io.github.darkxanter:webp-imageio:0.3.3")

    // Jackson
    implementation("com.fasterxml.jackson.core:jackson-core:2.17.2")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.17.2")
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(17)
}