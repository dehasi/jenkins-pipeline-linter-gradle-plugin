package me.dehasi.jenkins.linter

import org.gradle.api.provider.Property

abstract class LinterExtension {
    abstract val message: Property<String>

    init {
        message.convention("No value set to this property")
    }
}
