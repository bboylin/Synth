package xyz.bboylin.synth.plugin

open class SynthConfig(var debuggable: Boolean = false) {
    companion object {
        @JvmStatic
        private val instance = SynthConfig()

        @JvmStatic
        fun isDebuggable(): Boolean = instance.debuggable

        @JvmStatic
        fun setDebuggableValue(debuggable: Boolean) {
            instance.debuggable = debuggable
        }
    }
}