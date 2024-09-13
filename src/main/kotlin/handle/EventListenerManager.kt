package top.ninnana.handle

import io.klogging.logger
import io.ktor.client.plugins.websocket.*
import kotlinx.coroutines.runBlocking
import kotlin.reflect.KClass

typealias EventHookFunction<T> = suspend (ClientWebSocketSession, T) -> Unit

object EventListenerManager {
    val handles: MutableMap<KClass<*>, MutableList<EventHookFunction<Any>>> = mutableMapOf()
    val logger = logger(EventListenerManager::class)
    fun <T : Any> registerEventHook(event: KClass<T>, hook: EventHookFunction<T>) = try {
        val list = handles.getOrPut(event) { mutableListOf() }
        list.add(hook as EventHookFunction<Any>)
    } catch (e: Exception) {
        runBlocking {
            logger.error("Failed to register event hook: ${e.message}")
        }
    }

    fun <T : Any> removeEventHook(event: KClass<*>, hook: EventHookFunction<T>) {
        handles[event]?.remove(hook)
    }

    suspend fun <T : Any> callEvent(session: ClientWebSocketSession, event: T) = try {
        handles[event::class]?.forEach {
            session.run {
                it(session, event)
            }
        }
    } catch (e: Exception) {
        logger.error("Failed to call event: ${e.message}")
    }
}