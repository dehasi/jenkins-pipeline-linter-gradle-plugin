package me.dehasi.jenkins.linter

import org.gradle.api.Action
import org.gradle.api.provider.Property
import org.gradle.api.provider.SetProperty
import org.gradle.api.tasks.Nested
import java.util.Collections.emptySet

abstract class LinterExtension {
    abstract val pipelinePath: SetProperty<String>
    @Nested abstract fun getJenkinsExtension(): JenkinsExtension

    open fun jenkins(action: Action<in JenkinsExtension>) {
        action.execute(getJenkinsExtension())
    }

    init {
        pipelinePath.convention(emptySet())
    }

    abstract class JenkinsExtension {
        abstract val url: Property<String>

        abstract val username: Property<String>

        abstract val password: Property<String>

        init {
            url.convention("")
            username.convention("")
            password.convention("")
        }
    }
}
