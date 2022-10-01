package me.dehasi.jenkins.linter

import org.gradle.api.provider.SetProperty

abstract class LinterExtension {
    abstract val pipelinePath: SetProperty<String>

    init {
        pipelinePath.convention(HashSet())
    }
}
