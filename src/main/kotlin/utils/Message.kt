package top.ninnana.utils

import io.ktor.client.plugins.websocket.*
import io.ktor.websocket.*
import top.ninnana.bean.*

fun messageBuilder(builder: MessageBuilder.() -> Unit): List<MessageNode> {
    return MessageBuilder().apply(builder).messages
}

class MessageBuilder {
    val messages: MutableList<MessageNode> = mutableListOf()
    fun text(content: String) {
        messages.add(MessageNode(type = "text", MessageData(text = content)))
    }

    fun at(qq: Long) {
        messages.add(MessageNode(type = "at", MessageData(qq = qq)))
    }
}

suspend inline fun <reified D, reified A : API<D>> ClientWebSocketSession.send(api: A) {
    this.send(api.json())
}

suspend fun ClientWebSocketSession.sendMessage(message: List<MessageNode>, channelId: ChannelId) {
    when (channelId) {
        is GroupId -> send(SendGroupMsg(channelId.id, message))
        is PrivateId -> send(SendPrivateMsg(channelId.id, message))
    }
}