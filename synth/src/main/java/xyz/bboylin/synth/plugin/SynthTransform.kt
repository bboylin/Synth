package xyz.bboylin.synth.plugin

import com.android.build.api.transform.QualifiedContent
import com.android.build.api.transform.Transform
import com.android.build.api.transform.TransformInvocation
import com.android.build.gradle.internal.pipeline.TransformManager
import org.gradle.api.Project

/**
 * Created by bboylin
 */
const val TAG = "Synth"

class SynthTransform(private val project: Project) : Transform() {
    override fun getName(): String = TAG

    override fun getInputTypes(): MutableSet<QualifiedContent.ContentType> = TransformManager.CONTENT_CLASS

    override fun isIncremental(): Boolean = false

    override fun getScopes(): MutableSet<in QualifiedContent.Scope> = TransformManager.SCOPE_FULL_PROJECT

    override fun transform(transformInvocation: TransformInvocation) {
        println("----------- $TAG started ------------")
        val startTime = System.currentTimeMillis()
        val synthProcessor = SynthProcessor(transformInvocation)
        synthProcessor.scan()
        synthProcessor.process()
        synthProcessor.finish()
        println("----------- $TAG end ------------")
        println("it takes ${System.currentTimeMillis() - startTime}ms for Synth to work")
    }
}