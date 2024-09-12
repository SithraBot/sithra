package top.ninnana.app

import io.ktor.client.plugins.websocket.*
import top.ninnana.bean.LifecycleEvent
import top.ninnana.bean.MessageEvent
import top.ninnana.handle.Subscribe
import top.ninnana.plugin.Plugin

class Default : Plugin() {
    @Subscribe
    suspend fun lifecycleVisitor(session: ClientWebSocketSession, event: LifecycleEvent) {
        when (event.sub_type) {
            "connect" -> {
                logger.info("OneBot 已连接")
            }
        }
    }
}