package top.ninnana.handle

import io.klogging.logger
import io.ktor.client.plugins.websocket.*
import kotlinx.coroutines.runBlocking
import kotlin.reflect.KClass

object EventHandleManager {
    val handles: MutableMap<KClass<*>, MutableList<suspend (ClientWebSocketSession, Any) -> Unit>> = mutableMapOf()
    fun <T : Any> registerEventHandle(event: KClass<*>, handle: suspend (ClientWebSocketSession, T) -> Unit) {
        val list = handles.getOrPut(event) { mutableListOf() }
        try {
            list.add(handle as suspend (ClientWebSocketSession, Any) -> Unit)
        } catch (e: Exception) {
            runBlocking {
                logger(handle::class).error("Failed to register event handle: ${e.message}")
            }
        }
    }

    fun removeEventHandle(event: KClass<*>, handle: (ClientWebSocketSession, Any) -> Unit) {
        handles[event]?.remove(handle)
    }

    suspend fun <T : Any> handleEvent(session: ClientWebSocketSession, event: T) {
        handles[event::class]?.forEach {
            it(session, event)
        }
    }
}