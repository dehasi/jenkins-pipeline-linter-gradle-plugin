package me.dehasi.jenkins.linter

import org.gradle.api.Plugin
import org.gradle.api.Project

class Linter : Plugin<Project> {
    override fun apply(project: Project) {
        project.task("lint") {
            println("Hello from the jenkinsfile")
        }
    }
}