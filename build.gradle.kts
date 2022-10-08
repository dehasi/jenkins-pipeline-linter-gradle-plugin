plugins {
    id("org.jetbrains.kotlin.jvm").version("1.5.31")
    id("java-gradle-plugin")
    id("maven-publish")
}

group = "me.dehasi"
version = "LATEST-SNAPSHOT"

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_1_8
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(gradleApi())
    implementation("org.jetbrains.kotlin:kotlin-stdlib")

    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.9.0")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.9.0")
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.9.0")
}

gradlePlugin {
    plugins {
        create("jenkins-pipeline-linter-gradle-plugin") {
            id = "me.dehasi.jenkins-pipeline-linter-gradle-plugin"
            implementationClass = "me.dehasi.jenkins.linter.LinterPlugin"
        }
    }
}

tasks {
    test {
        useJUnitPlatform()
    }
}
