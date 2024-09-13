package top.ninnana.handle

import kotlinx.coroutines.runBlocking
import kotlin.reflect.KClass
import kotlin.reflect.full.callSuspend
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.functions

interface EventListener {
    companion object {
        inline fun <reified T : Any> registerEventHook(noinline hook: EventHookFunction<T>) {
            val eventType = T::class
            EventListenerManager.registerEventHook(eventType, hook)
        }
    }

    fun registerListener() = try {
        this::class.functions.forEach { function ->
            val subscribeAnnotation = function.findAnnotation<Subscribe>()
            if (subscribeAnnotation != null) {
                val eventType = function.parameters.last().type.classifier as KClass<*>
                EventListenerManager.registerEventHook(eventType) { session, event ->
                    function.callSuspend(this, session, event)
                }
            }
        }
    } catch (e: Exception) {
        runBlocking {
            EventListenerManager.logger.error("Failed to register listener: ${e.message}")
        }
    }
}