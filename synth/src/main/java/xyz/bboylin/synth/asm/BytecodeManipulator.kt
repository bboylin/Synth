package xyz.bboylin.synth.asm

import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.*
import xyz.bboylin.synth.util.Utils
import java.lang.RuntimeException

/**
 * Created by bboylin
 * A tool which focus on Bytecode Manipulation
 */
class BytecodeManipulator {
    companion object {
        @JvmStatic
        fun tryInline(outerClass: SynthClassNode, innerClasses: MutableSet<SynthClassNode>) {
            outerClass.methods.forEach { methodNode ->
                if (!Utils.isAccessMethod(methodNode)) {
                    // 遍历 Non-Access method 的指令
                    val iterator = methodNode.instructions.iterator()
                    while (iterator.hasNext()) {
                        val insnNode = iterator.next()
                        // 找到invoke Access方法的指令
                        if (Utils.isInvokeAccessMethodInsn(insnNode)) {
                            val name = (insnNode as MethodInsnNode).name
                            for (classNode in innerClasses) {
                                val accessMethodNode = classNode.getAccessMethodByName(name)
                                if (accessMethodNode != null) {
                                    val insnList = accessMethodNode.instructions
                                    // 找到Access方法后根据其指令内联
                                    inline(classNode, insnList, methodNode, insnNode)
                                    break
                                }
                            }
                        }
                    }
                }
            }
            innerClasses.forEach { classNode ->
                classNode.methods.forEach { methodNode ->
                    if (!Utils.isAccessMethod(methodNode)) {
                        // 遍历 Non-Access method 的指令
                        val iterator = methodNode.instructions.iterator()
                        while (iterator.hasNext()) {
                            val insnNode = iterator.next()
                            // 找到invoke Access方法的指令
                            if (Utils.isInvokeAccessMethodInsn(insnNode)) {
                                val name = (insnNode as MethodInsnNode).name
                                val accessMethodNode = outerClass.getAccessMethodByName(name)
                                if (accessMethodNode != null) {
                                    val insnList = accessMethodNode.instructions
                                    // 找到Access方法后根据其指令内联
                                    inline(outerClass, insnList, methodNode, insnNode)
                                }
                            }
                        }
                    }
                }
                classNode.removeAccessMethods()
            }
            outerClass.removeAccessMethods()
        }

        @JvmStatic
        private fun inline(accessOwnerClassNode: SynthClassNode, accessInsns: InsnList, invokerMethod: MethodNode, invokeInsnNode: MethodInsnNode) {
            val invokerInsns = invokerMethod.instructions
            // 前两个分别是LabelNode和LineNumberNode，真实指令从第三个开始
            val labels = mutableMapOf<LabelNode, LabelNode>()
            var first = accessInsns.first
            for (i in 0..1) {
                if (first is LabelNode) {
                    labels[first] = first
                }
                first = first.next
            }
            when (first.opcode) {
                Opcodes.GETSTATIC -> {
                    // read static field
                    first as FieldInsnNode
                    // 提升字段Access
                    if (upgradeFieldAccess(accessOwnerClassNode, first.name, invokeInsnNode.name)) {
                        // inline
                        invokerInsns.set(invokeInsnNode, first.clone(labels))
                    }
                }
                Opcodes.INVOKESTATIC -> {
                    // invoke static method
                    first as MethodInsnNode
                    if (upgradeMethodAccess(accessOwnerClassNode, first.name, invokeInsnNode.name)) {
                        // inline
                        invokerInsns.set(invokeInsnNode, first.clone(labels))
                    }
                }
                Opcodes.ALOAD -> {
                    val next = first.next
                    when (next.opcode) {
                        Opcodes.GETFIELD -> {
                            // read member field
                            // aload0 -> getfield -> xreturn
                            next as FieldInsnNode
                            if (upgradeFieldAccess(accessOwnerClassNode, next.name, invokeInsnNode.name)) {
                                invokerInsns.set(invokeInsnNode, next.clone(labels))
                            }
                        }
                        Opcodes.INVOKESPECIAL -> {
                            // invoke member method
                            // aload0 -> invokespecial -> return/xreturn
                            if (upgradeMethodAccess(accessOwnerClassNode, (next as MethodInsnNode).name, invokeInsnNode.name)) {
                                // invokespecial -> invokevirtual
                                val invokeVirtual = MethodInsnNode(Opcodes.INVOKEVIRTUAL, next.owner, next.name, next.desc, false)
                                // inline
                                invokerInsns.set(invokeInsnNode, invokeVirtual)
                                // 其他调用这个私有方法的地方因为方法变package Access了，指令要从invokespecial -> invokevirtual
                                accessOwnerClassNode.methods
                                        .filter { methodNode -> !Utils.isAccessMethod(methodNode) }
                                        .forEach { methodNode ->
                                            methodNode.instructions.apply {
                                                val iterator = iterator()
                                                while (iterator.hasNext()) {
                                                    val cursor = iterator.next()
                                                    if (cursor is MethodInsnNode && cursor.opcode == Opcodes.INVOKESPECIAL && cursor.name == invokeVirtual.name) {
                                                        cursor.opcode = Opcodes.INVOKEVIRTUAL
                                                    }
                                                }
                                            }
                                        }
                            }
                        }
                        in arrayOf(Opcodes.DUP, Opcodes.DUP2) -> {
                            // write static field
                            inlineStaticFieldWrite(invokerMethod, first, accessOwnerClassNode, invokeInsnNode, labels)
                        }
                        in Opcodes.ILOAD..Opcodes.ALOAD -> {
                            // write member field
                            // aload -> aload -> dup/dup2 -> putfield -> return/xreturn
                            val dup = next.next.clone(labels)
                            if (dup.opcode in Opcodes.DUP..Opcodes.DUP2_X2) {
                                val putField = next.next.next.clone(labels) as FieldInsnNode
                                if (upgradeFieldAccess(accessOwnerClassNode, putField.name, invokeInsnNode.name)) {
                                    invokerInsns.set(invokeInsnNode, dup)
                                    invokerInsns.insert(dup, putField)
                                    // 考虑double和long，+2而不是+1
                                    invokerMethod.maxStack = invokerMethod.maxStack + 2;
                                }
                            }
                        }
                        Opcodes.INVOKEVIRTUAL -> {
                            // ignore
                            accessOwnerClassNode.addExcludedAccessMethod(invokeInsnNode.name)
                        }
                        else -> {
                            throw RuntimeException("found unprocessed opcode: ${first.next.opcode}")
                        }
                    }
                }
                in Opcodes.ILOAD..Opcodes.DLOAD -> {
                    // write static field
                    inlineStaticFieldWrite(invokerMethod, first, accessOwnerClassNode, invokeInsnNode, labels)
                }
                else -> {
                    throw RuntimeException("found unprocessed opcode: ${first.opcode}")
                }
            }
        }

        @JvmStatic
        private fun inlineStaticFieldWrite(invokerMethod: MethodNode, first: AbstractInsnNode,
                                           accessOwnerClassNode: SynthClassNode, invokeInsnNode: MethodInsnNode,
                                           labels: MutableMap<LabelNode, LabelNode>) {
            // aload0 -> dup/dup2 -> putstatic -> xreturn
            val dup = first.next.clone(labels)
            val putStatic = first.next.next.clone(labels) as FieldInsnNode
            // 提升字段Access
            if (upgradeFieldAccess(accessOwnerClassNode, putStatic.name, invokeInsnNode.name)) {
                val invokerInsns = invokerMethod.instructions
                // inline
                invokerInsns.set(invokeInsnNode, dup)
                invokerInsns.insert(dup, putStatic)
                // 考虑double和long，+2而不是+1
                invokerMethod.maxStack = invokerMethod.maxStack + 2;
            }
        }

        @JvmStatic
        private fun upgradeFieldAccess(classNode: SynthClassNode, fieldName: String, accessMethodName: String): Boolean {
            classNode.fields.forEach { fieldNode ->
                if (fieldNode.name == fieldName && Utils.isPrivate(fieldNode.access)) {
                    // private -> package
                    fieldNode.access = fieldNode.access - 2
                    classNode.markUpgradedField(fieldName)
                    return true
                }
            }
            if (classNode.hasUpgradedField(fieldName)) {
                return true
            }
            // 没找到操作field 则把Access method name加入excluded列表，不执行内联和删除
            classNode.addExcludedAccessMethod(accessMethodName)
            return false
        }

        @JvmStatic
        private fun upgradeMethodAccess(classNode: SynthClassNode, methodName: String, accessMethodName: String): Boolean {
            classNode.methods.forEach { methodNode ->
                if (methodNode.name == methodName && Utils.isPrivate(methodNode.access)) {
                    // private -> package
                    methodNode.access = methodNode.access - 2
                    classNode.markUpgradedMethod(methodName)
                    return true
                }
            }
            if (classNode.hasUpgradedMethod(methodName)) {
                return true
            }
            // 没找到操作method 则把Access method name加入excluded列表，不执行内联和删除
            classNode.addExcludedAccessMethod(accessMethodName)
            return false
        }
    }
}