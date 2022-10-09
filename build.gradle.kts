plugins {
    id("org.jetbrains.kotlin.jvm").version("1.5.31")
    id("java-gradle-plugin")
    id("maven-publish")
    signing
}

group = "me.dehasi"

val isPublishing = project.hasProperty("releaseVersion")
if (isPublishing) {
    project.version = project.property("releaseVersion").toString()
} else {
    project.version = "LATEST-SNAPSHOT"
}

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

val pluginName = "Jenkins Pipeline Linter Gradle Plugin"
val pluginDescription = "A plugin to lint jenkins pipelines."

gradlePlugin {
    plugins {
        create("jenkins-pipeline-linter") {
            id = "me.dehasi.jenkins-pipeline-linter"
            implementationClass = "me.dehasi.jenkins.linter.LinterPlugin"
            description = pluginDescription
            displayName = pluginName
        }
    }
}

tasks {
    test {
        useJUnitPlatform()
    }
}

val javadocJar: TaskProvider<Jar> by tasks.registering(Jar::class) {
    archiveClassifier.set("javadoc")
}

val sourcesJar: TaskProvider<Jar> by tasks.registering(Jar::class) {
    archiveClassifier.set("sources")
}

publishing {
    publications.withType<MavenPublication> {
        artifact(javadocJar)
        artifact(sourcesJar)

        repositories {
            maven {
                name = "OSSRH"
                url = uri("https://oss.sonatype.org/service/local/staging/deploy/maven2")
                credentials {
                    username = project.property("ossrhUsername").toString()
                    password = project.property("ossrhPassword").toString()
                }
            }
        }
        groupId = "me.dehasi"
        artifactId = "jenkins-pipeline-linter-gradle-plugin"
        version = project.version.toString()

        pom {
            val projectGitHubUrl = "https://github.com/dehasi/jenkins-pipeline-linter-gradle-plugin"

            name.set(pluginName)
            description.set(pluginDescription)
            url.set(projectGitHubUrl)
            inceptionYear.set("2022")

            scm {
                connection.set("scm:git:$projectGitHubUrl")
                developerConnection.set("scm:git:$projectGitHubUrl")
                url.set(projectGitHubUrl)
            }
            licenses {
                license {
                    name.set("MIT")
                    url.set("https://opensource.org/licenses/MIT")
                }
            }
            developers {
                developer {
                    id.set("dehasi")
                    name.set("Ravil")
                    email.set("dehasi@proton.me")
                    url.set("http://dehasi.me")
                }
            }
            issueManagement {
                system.set("GitHub")
                url.set("$projectGitHubUrl/issues")
            }
        }
        the<SigningExtension>().sign(this)
    }
}

signing {
    isRequired = isPublishing
    sign(configurations.archives.get())
}
