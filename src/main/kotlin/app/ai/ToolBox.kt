package top.ninnana.app.ai

import top.ninnana.handle.EventListenerManager
import top.ninnana.handle.Subscribe
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.full.*

interface ToolBox {
    fun registerTools() {
        Tools.registerTool(this)
    }
}