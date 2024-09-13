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
    override val user_id: Long,
    override val group_id: Long?,
) : Event(), Proven {
    override val post_type: String = "message"

    suspend fun ClientWebSocketSession.reply(message: List<MessageNode>) {
        if (group_id != null) {
            send(SendGroupMsg(group_id, message).json())
        } else {
            send(SendPrivateMsg(user_id, message).json())
        }
    }

    suspend fun ClientWebSocketSession.reply(message: String) {
        if (group_id != null) {
            send(SendGroupMsg(group_id, messageBuilder { text(message) }).json())
        } else {
            send(SendPrivateMsg(user_id, messageBuilder { text(message) }).json())
        }
    }
}

interface ChannelId {
    val id: Long
}

class GroupId(override val id: Long) : ChannelId
class PrivateId(override val id: Long) : ChannelId
