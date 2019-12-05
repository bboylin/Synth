package xyz.bboylin.synth.asm

import org.objectweb.asm.FieldVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes.ASM6
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.MethodNode
import xyz.bboylin.synth.data.ClassInfo
import xyz.bboylin.synth.util.Utils

/**
 * Created by bboylin
 * A class node used for parsing class info
 */
class SynthClassNode(val classPath: String, val inJar: Boolean) : ClassNode(ASM6) {
    private var outerClassName: String? = null
    private var hasAccessMethod: Boolean = false
    private val excludedAccessMethods = mutableSetOf<String>()
    private val upgradedFields = mutableSetOf<String>()
    private val upgradedMethods = mutableSetOf<String>()

    override fun visitInnerClass(name: String?, outerName: String?, innerName: String?, access: Int) {
        if (outerName != null && innerName != null && Utils.isPrivate(access)) {
            outerClassName = outerName
            if (outerName == this.name) {
                ClassInfo.addOuterClass(outerName, this)
            } else {
                ClassInfo.addInnerClass(outerName, this)
            }
            ClassInfo.getClassMap()[outerName]!!.setInJar(inJar)
        }
        super.visitInnerClass(name, outerName, innerName, access)
    }

    override fun visitMethod(access: Int, name: String, desc: String?, signature: String?, exceptions: Array<out String>?): MethodVisitor? {
        return if (mayNeedInline()) {
            val methodNode = super.visitMethod(access, name, desc, signature, exceptions) as MethodNode
            if (Utils.isAccessMethod(name, access)) {
                hasAccessMethod = true
            }
            return methodNode
        } else {
            null
        }
    }

    override fun visitField(access: Int, name: String?, desc: String?, signature: String?, value: Any?): FieldVisitor? {
        return if (mayNeedInline()) {
            return super.visitField(access, name, desc, signature, value)
        } else {
            null
        }
    }

    fun mayNeedInline(): Boolean = outerClassName != null

    fun hasAccessMethod(): Boolean = hasAccessMethod

    fun getAccessMethodByName(name: String): MethodNode? = methods.find { methodNode -> methodNode.name == name }

    fun removeAccessMethods() {
        methods.removeIf { methodNode ->
            Utils.isAccessMethodName(methodNode.name) && !excludedAccessMethods.contains(methodNode.name)
        }
    }

    fun addExcludedAccessMethod(name: String) {
        excludedAccessMethods.add(name)
    }

    fun markUpgradedField(field: String) {
        upgradedFields.add(field)
    }

    fun markUpgradedMethod(method: String) {
        upgradedMethods.add(method)
    }

    fun hasUpgradedField(field: String): Boolean = upgradedFields.contains(field)

    fun hasUpgradedMethod(method: String): Boolean = upgradedMethods.contains(method)
}