package me.dehasi.jenkins.linter

import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction

abstract class LinterTask : DefaultTask() {
    @Input abstract fun getMassage(): Property<String>

    @TaskAction fun lint() {
        println("Hello from the jenkinsfile ${getMassage().get()}")
    }
}
