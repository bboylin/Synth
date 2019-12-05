package xyz.bboylin.synth.plugin

import com.android.build.api.transform.Format
import com.android.build.api.transform.TransformInvocation
import org.apache.commons.io.FileUtils
import org.objectweb.asm.*
import org.objectweb.asm.util.CheckClassAdapter
import org.objectweb.asm.util.TraceClassVisitor
import xyz.bboylin.synth.asm.SynthClassNode
import xyz.bboylin.synth.data.ClassInfo
import xyz.bboylin.synth.asm.BytecodeManipulator
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.lang.RuntimeException
import java.nio.file.FileSystems
import java.util.zip.ZipFile

/**
 * Created by bboylin
 * a class used for processing the whole stuff
 */
class SynthProcessor(private val transformInvocation: TransformInvocation) {

    fun scan() {
        transformInvocation.inputs.forEach { transformInput ->
            transformInput.directoryInputs.forEach { directoryInput ->
                scanClassInDir(directoryInput.file)
            }
            transformInput.jarInputs.forEach { jarInput ->
                scanClassInJar(jarInput.file.absolutePath)
            }
        }
    }

    fun process() {
        // 先处理Non-Access method方法入手，扫描其中字节码， 如果存在access方法调用就：
        // 遍历找到该access方法，分析其中字节码，内联方法调用
        // 找到字节码操作的字段，修改access为package，如果没找到说明不能内联
        // 如果可以内联 再次遍历 删除access方法 同时写文件
        ClassInfo.getClassMap()
                .filterValues { classModel -> classModel.needInline() }
                .forEach { (_, classModel) ->
                    val outerClass = classModel.getOuterClass()!!
                    val innerClasses = classModel.getInnerClasses()
                    BytecodeManipulator.tryInline(outerClass, innerClasses)
                    writeClassFile(outerClass)
                    innerClasses.forEach { classNode ->
                        writeClassFile(classNode)
                    }
                }
    }

    fun finish() {
        if (SynthConfig.isDebuggable()) {
            println("let's see what's in classInfo:")
            ClassInfo.getClassMap().forEach { (outerName, classModel) ->
                println("outerName:$outerName")
                println("classModel:")
                classModel.apply {
                    println("classModel isInJar: ${isInJar()}  needInline:${needInline()}")
                    getOuterClass().apply {
                        println("outter class: ${this?.name
                                ?: ""} isInJar: ${this?.inJar}  mayNeedInline:${this?.mayNeedInline()} hasAccessMethod:${this?.hasAccessMethod()}")
                        println("classPath: ${this?.classPath}")
                    }
                    getInnerClasses().forEach { inner ->
                        println("inner class: ${inner.name} isInJar: ${inner.inJar}  mayNeedInline:${inner.mayNeedInline()} hasAccessMethod:${inner.hasAccessMethod()}")
                        println("classPath: ${inner.classPath}")
                    }
                }
            }
        }
        val jarClassNodes = mutableMapOf<String, MutableList<SynthClassNode>>()
        ClassInfo.getClassMap()
                .filter { (_, classModel) -> classModel.isInJar() && classModel.needInline() }
                .forEach { (_, classModel) ->
                    classModel.getOuterClass()?.let {
                        if (!it.inJar) {
                            throw RuntimeException("${it.name} not in jar: ${it.classPath}")
                        }
                        if (jarClassNodes[it.classPath] == null) {
                            jarClassNodes[it.classPath] = mutableListOf()
                        }
                        jarClassNodes[it.classPath]!!.add(it)
                    }
                    classModel.getInnerClasses().forEach {
                        if (!it.inJar) {
                            throw RuntimeException("${it.name} not in jar: ${it.classPath}")
                        }
                        if (jarClassNodes[it.classPath] == null) {
                            jarClassNodes[it.classPath] = mutableListOf()
                        }
                        jarClassNodes[it.classPath]!!.add(it)
                    }
                }
        if (SynthConfig.isDebuggable()) {
            println("let's see what's in jarClassNodes:")
            jarClassNodes.forEach { (classPath, classNodes) ->
                println("classPath:$classPath")
                println("classNodes:")
                classNodes.forEach { classNode -> println("${classNode.name}") }
            }
        }
        // TODO use Coroutine
        transformInvocation.inputs.forEach { transformInput ->
            val outputProvider = transformInvocation.outputProvider
            transformInput.directoryInputs.forEach { directoryInput ->
                val dest = outputProvider.getContentLocation(directoryInput.name,
                        directoryInput.contentTypes, directoryInput.scopes, Format.DIRECTORY)
                FileUtils.copyDirectory(directoryInput.file, dest)
            }
            transformInput.jarInputs.forEach { jarInput ->
                val outputTarget = outputProvider.getContentLocation(jarInput.name, jarInput.contentTypes, jarInput.scopes, Format.JAR)
                FileUtils.copyFile(jarInput.file, outputTarget)
                val jarPath = jarInput.file.absolutePath
                if (jarClassNodes.containsKey(jarPath)) {
                    val fileSystems = FileSystems.newFileSystem(outputTarget.toPath(), null)
                    fileSystems.use {
                        jarClassNodes[jarPath]!!.forEach {
                            val entryPath = fileSystems.getPath("${it.name}.class")
                            fileSystems.provider().deleteIfExists(entryPath)
                        }
                    }
                }
            }
        }
    }

    private fun writeClassFile(classNode: SynthClassNode) {
        if (!classNode.mayNeedInline()) {
            return
        }
        val classWriter = ClassWriter(0)
        classNode.accept(CheckClassAdapter(TraceClassVisitor(classWriter, null)))
        val bytes = classWriter.toByteArray()
        val dirPath = if (classNode.inJar) ClassInfo.getDirPath() else classNode.classPath
        val classFilePath = dirPath + File.separator + classNode.name + ".class"
        val file = File(classFilePath)
        if (!file.exists()) {
            val dir = File(classFilePath.substringBeforeLast("/"))
            dir.mkdir()
            file.createNewFile()
        }
        val fos = FileOutputStream(classFilePath)
        fos.write(bytes)
        fos.close()
    }

    private fun scanClassInJar(jarPath: String) {
        val zipFile = ZipFile(jarPath)
        zipFile.use {
            zipFile.entries().asSequence()
                    .filter { zipEntry -> !isSystemClass(zipEntry.name) }
                    .forEach { zipEntry ->
                        val inputStream = zipFile.getInputStream(zipEntry)
                        if (isValidClassPath(zipEntry.name, "/")) {
                            inputStream.use {
                                processClass(inputStream, jarPath, true)
                            }
                        }
                    }
        }
    }

    private fun isSystemClass(className: String): Boolean {
        val systemPackagePrefixSet = mutableSetOf("android/arch/", "android/support/", "androidx/")
        systemPackagePrefixSet.forEach { systemPackagePrefix ->
            if (className.startsWith(systemPackagePrefix)) {
                return true
            }
        }
        return false
    }

    private fun scanClassInDir(dirFile: File) {
        ClassInfo.setDirPath(dirFile.absolutePath)
        dirFile.walk()
                .maxDepth(Int.MAX_VALUE)
                .filter { it.isFile && it.isValidClass() }
                .forEach { file ->
                    processClass(file.inputStream(), dirFile.absolutePath, false)
                }
    }

    private fun processClass(inputStream: InputStream, path: String, inJar: Boolean) {
        val classReader = ClassReader(inputStream)
        classReader.accept(SynthClassNode(path, inJar), ClassReader.SKIP_FRAMES)
    }

    private fun File.isValidClass(): Boolean = isValidClassPath(absolutePath, File.separator)

    private fun isValidClassPath(classPath: String, sep: String): Boolean {
        return classPath.endsWith(".class") &&
                !classPath.endsWith(sep + "R.class") &&
                !classPath.contains(sep + "R$")
    }
}