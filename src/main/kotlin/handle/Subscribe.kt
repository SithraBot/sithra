package top.ninnana.handle

import kotlin.reflect.KClass
import kotlin.reflect.full.*

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
annotation class Subscribe

fun <T : Any> T.registerEventHandle() {
    this::class.functions.forEach { function ->
        val subscribeAnnotation = function.findAnnotation<Subscribe>()
        if (subscribeAnnotation != null) {
            val eventType = function.parameters[2].type.classifier as KClass<*>
            EventHandleManager.registerEventHandle<Any>(eventType) { session, event ->
                function.callSuspend(this, session, event)
            }
        }
    }
}