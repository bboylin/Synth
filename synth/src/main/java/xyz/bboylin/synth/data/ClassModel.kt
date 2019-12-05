package xyz.bboylin.synth.data

import xyz.bboylin.synth.asm.SynthClassNode

/**
 * Created by bboylin
 */
class ClassModel(private var mOuterClass: SynthClassNode? = null,
                 private var mInnerClasses: MutableSet<SynthClassNode> = mutableSetOf(),
                 private var mInJar: Boolean = false) {
    fun addInnerClass(innerClass: SynthClassNode) {
        mInnerClasses.add(innerClass)
    }

    fun getOuterClass(): SynthClassNode? = mOuterClass

    fun setOuterClass(outerClass: SynthClassNode) {
        this.mOuterClass = outerClass
    }

    fun getInnerClasses(): MutableSet<SynthClassNode> = mInnerClasses

    fun needInline(): Boolean {
        var hasAccessMethod = mOuterClass!!.hasAccessMethod()
        mInnerClasses.forEach { classNode ->
            hasAccessMethod = hasAccessMethod or classNode.hasAccessMethod()
        }
        return hasAccessMethod
    }

    fun isInJar(): Boolean = mInJar

    fun setInJar(inJar: Boolean) {
        mInJar = inJar
    }
}