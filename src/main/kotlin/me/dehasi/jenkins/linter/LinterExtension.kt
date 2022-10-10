package me.dehasi.jenkins.linter

import me.dehasi.jenkins.linter.LinterExtension.ActionOnFailure.FAIL_BUILD
import org.gradle.api.Action
import org.gradle.api.provider.Property
import org.gradle.api.provider.SetProperty
import org.gradle.api.tasks.Nested
import java.util.Collections.emptySet

abstract class LinterExtension {
    abstract val pipelinePath: SetProperty<String>
    abstract val actionOnFailure: Property<ActionOnFailure>
    @Nested abstract fun getJenkinsExtension(): JenkinsExtension

    open fun jenkins(action: Action<in JenkinsExtension>) {
        action.execute(getJenkinsExtension())
    }

    init {
        pipelinePath.convention(emptySet())
        actionOnFailure.convention(FAIL_BUILD)
    }

    enum class ActionOnFailure {
        WARNING,
        FAIL_BUILD
    }

    abstract class JenkinsExtension {
        abstract val url: Property<String>

        abstract val username: Property<String>

        abstract val password: Property<String>

        abstract val trustSelfSigned: Property<Boolean>

        abstract val ignoreCertificate: Property<Boolean>

        abstract val useCrumbIssuer: Property<Boolean>

        fun trustSelfSigned() {
            trustSelfSigned.set(true)
        }

        fun ignoreCertificate() {
            ignoreCertificate.set(true)
        }

        fun useCrumbIssuer() {
            useCrumbIssuer.set(true)
        }

        init {
            url.convention("")
            username.convention("")
            password.convention("")
            trustSelfSigned.convention(false)
            ignoreCertificate.convention(false)
            useCrumbIssuer.convention(false)
        }
    }
}
