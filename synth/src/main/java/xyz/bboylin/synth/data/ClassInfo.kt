package xyz.bboylin.synth.data

import xyz.bboylin.synth.asm.SynthClassNode

/**
 * Created by bboylin
 */
object ClassInfo {
    /** 存放外部类和对应内部类的映射 */
    private val mClassMap: HashMap<String, ClassModel> = HashMap()
    /** 存放class编译后的目录 */
    private var mDirPath = ""

    fun addInnerClass(outerName: String, innerClassNode: SynthClassNode) {
        if (!mClassMap.containsKey(outerName)) {
            mClassMap[outerName] = ClassModel()
        }
        mClassMap[outerName]!!.addInnerClass(innerClassNode)
    }

    fun addOuterClass(outerName: String, outerClassNode: SynthClassNode) {
        if (!mClassMap.containsKey(outerName)) {
            mClassMap[outerName] = ClassModel()
        }
        mClassMap[outerName]!!.setOuterClass(outerClassNode)
    }

    fun getClassMap(): HashMap<String, ClassModel> = mClassMap

    fun setDirPath(dirPath: String) {
        mDirPath = dirPath
    }

    fun getDirPath(): String = mDirPath
}