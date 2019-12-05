package xyz.bboylin.synth.plugin

import com.android.build.gradle.AppExtension
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Created by bboylin
 */
class SynthPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        val android = project.extensions.getByType(AppExtension::class.java)
        val config = project.extensions.create("SynthConfig", SynthConfig::class.java)
        SynthConfig.setDebuggableValue(config.debuggable)
        android.registerTransform(SynthTransform(project))
    }
}