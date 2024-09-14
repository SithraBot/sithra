package top.ninnana.app

import io.ktor.client.plugins.websocket.*
import top.ninnana.handle.EventListener
import top.ninnana.plugin.Plugin
import top.ninnana.app.ai.*
import top.ninnana.bean.*
import top.ninnana.handle.Subscribe
import top.ninnana.utils.send

object Default : Plugin(), EventListener, ToolBox {
    override fun onLoad() {
        registerListener()
        registerTools()
    }

    @Tool("Ban or unban users known to QQ. High-risk operation.")
    @WithEvent
    suspend fun ban(
        session: ClientWebSocketSession,
        event: Event?,
        @Description("The muting duration (unit: seconds), set to 0 to cancel the muting") duration: Int = 60,
        @Description("QQ account of the banned person") id: Long,
    ): String {
        if (event is Proven && event.group_id != null) {
            if (event.user_id != 3605331714L) return "PERMISSION DENIED" // TODO: 鉴权
            session.send(SetGroupBan(event.group_id!!, id, duration))
        } else return "NOT IN GROUP CHAT"
        return "OPERATION SUCCESSFUL"
    }


    @Subscribe
    suspend fun lifecycleVisitor(session: ClientWebSocketSession, event: LifecycleEvent) {
        when (event.sub_type) {
            "connect" -> {
                logger.info("OneBot 已连接")
            }
        }
    }
}