import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
}

group = "sh.sit.bonfire"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))

    implementation(project(":DevSupJava"))
    implementation("com.atlassian.commonmark:commonmark:0.13.0")
    implementation("com.atlassian.commonmark:commonmark-ext-autolink:0.13.0")
    implementation("com.atlassian.commonmark:commonmark-ext-gfm-strikethrough:0.13.0")
    implementation("com.atlassian.commonmark:commonmark-ext-task-list-items:0.15.0")
}

tasks.test {
    useJUnitPlatform()
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}
tasks.withType(KotlinCompile::class).configureEach {
    kotlinOptions {
        jvmTarget = "1.8"
    }
}
