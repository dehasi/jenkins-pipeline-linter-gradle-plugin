plugins {
    id("org.jetbrains.kotlin.jvm").version("1.5.31")
    id("java-gradle-plugin")
    id("maven-publish")
    id("groovy")
}

group = "me.dehasi"
version = "LATEST-SNAPSHOT"

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib")
    implementation(gradleApi())
    testImplementation("org.spockframework:spock-core:2.0-groovy-3.0")
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
