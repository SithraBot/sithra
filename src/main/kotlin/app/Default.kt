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

    @Tool("将已知QQ的用户禁言或取消禁言")
    @WithEvent
    suspend fun ban(
        session: ClientWebSocketSession,
        event: Event?,
        @Description("禁言时长(单位:秒),设置为0则取消禁言") duration: Int = 60,
        @Description("被禁言人QQ号") id: Long,
    ): String {
        if (event is Proven && event.group_id != null) {
            if (event.user_id != 3605331714L) return "没有权限"
            session.send(SetGroupBan(event.group_id!!, id, duration))
        } else return "不在群聊中"
        return "成功禁言"
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

