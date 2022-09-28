plugins {
    id("org.jetbrains.kotlin.jvm").version("1.5.31")
    id("java-gradle-plugin")
    id("maven-publish")
}

group = "me.dehasi"
version = "LATEST-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib")
    implementation(gradleApi())
}

gradlePlugin {
    plugins {
        create("jenkins-pipeline-linter-gradle-plugin") {
            id = "me.dehasi.jenkins-pipeline-linter-gradle-plugin"
            implementationClass = "me.dehasi.jenkins.linter.Linter"
        }
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}