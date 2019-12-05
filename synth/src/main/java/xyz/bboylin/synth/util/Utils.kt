package xyz.bboylin.synth.util

import jdk.internal.org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.AbstractInsnNode
import org.objectweb.asm.tree.MethodInsnNode
import org.objectweb.asm.tree.MethodNode
import java.util.regex.Pattern

class Utils {
    companion object {

        @JvmStatic
        fun isStatic(accessFlag: Int): Boolean {
            return accessFlag and Opcodes.ACC_STATIC != 0
        }

        @JvmStatic
        fun isSynthetic(accessFlag: Int): Boolean {
            return accessFlag and Opcodes.ACC_SYNTHETIC != 0
        }

        @JvmStatic
        fun isPrivate(accessFlag: Int): Boolean {
            return accessFlag and Opcodes.ACC_PRIVATE != 0
        }

        @JvmStatic
        fun isAccessMethodName(name: String): Boolean {
            return Pattern.matches("access\\$\\d+", name)
        }

        @JvmStatic
        fun isAccessMethod(name: String, access: Int): Boolean {
            return isStatic(access) && isSynthetic(access) && isAccessMethodName(name)
        }

        @JvmStatic
        fun isAccessMethod(mn: MethodNode): Boolean {
            return isAccessMethod(mn.name, mn.access)
        }

        fun isInvokeAccessMethodInsn(insnNode: AbstractInsnNode) = insnNode.opcode == Opcodes.INVOKESTATIC
                && isAccessMethodName((insnNode as MethodInsnNode).name)
    }
}