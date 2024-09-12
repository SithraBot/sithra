package top.ninnana.bean

import io.ktor.client.plugins.websocket.*
import io.ktor.websocket.*
import kotlinx.serialization.*
import top.ninnana.utils.json
import top.ninnana.utils.messageBuilder

@Serializable
data class MessageNode(
    val type: String,
    val data: MessageData
)

@Serializable
data class MessageData(
    val text: String? = null,
    val file: String? = null,
    val id: String? = null,
    val qq: Long? = null,
    val name: String? = null,
    val image: String? = null
)

@Serializable
data class MessageEvent(
    override val time: Long,
    override val self_id: Long,
    val message: List<MessageNode>,
    val raw_message: String,
    val user_id: Long,
    val group_id: Long?,
) : Event() {
    override val post_type: String = "message"

    val isPrivate
        get() = group_id == null

    val isGroup
        get() = group_id != null

    val channelId
        get() = if (group_id != null) {
            GroupId(group_id)
        } else {
            PrivateId(user_id)
        }

    suspend fun replay(message: List<MessageNode>, session: ClientWebSocketSession) {
        if (group_id != null) {
            session.send(SendGroupMsg(group_id, message).json())
        } else {
            session.send(SendPrivateMsg(user_id, message).json())
        }
    }

    suspend fun replay(message: String, session: ClientWebSocketSession) {
        if (group_id != null) {
            session.send(SendGroupMsg(group_id, messageBuilder { text(message) }).json())
        } else {
            session.send(SendPrivateMsg(user_id, messageBuilder { text(message) }).json())
        }
    }
}

interface ChannelId {
    val id: Long
}

class GroupId(override val id: Long) : ChannelId
class PrivateId(override val id: Long) : ChannelId
