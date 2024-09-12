package top.ninnana.handle

import io.ktor.client.plugins.websocket.*
import top.ninnana.bean.Event
import top.ninnana.bean.MessageEvent

interface EventHandle {
    suspend fun handle(session: ClientWebSocketSession, event: Event)
    var isEnabled: Boolean
    fun enable() {
        isEnabled = true
    }

    fun disable() {
        isEnabled = false
    }
}